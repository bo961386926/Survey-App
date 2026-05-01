# 青泓项目勘察PRD V 2.1

# 青泓项目勘查信息采集与审核系统 - 产品需求文档（PRD）

| **版本** | **日期** | **状态** | **更新说明** |
| --- | --- | --- | --- |
| v2.1 | 2024-05-22 | 已完善 | 确定 uni-app 跨端架构，增加离线填报与水印逻辑 |

---

## 一、 文档概述

### 1.1 项目名称

亳州入河排污口项目勘查信息采集与审核系统

### 1.2 建设目标

*   **标准化采集**：实现入河排口现场勘查信息的标准化、电子化录入。
    
*   **动态适配**：支持后台自定义表单模板，适配不同排口类型及复杂的业务字段需求。
    
*   **高可靠填报**：提供**离线暂存机制**，确保在河道、偏远郊区等弱网环境下数据不丢失。
    
*   **高精度定位**：支持地图人工纠偏，确保排口物理位置坐标的准确性。
    
*   **闭环审核**：建立“提交-审核-通过/驳回”的闭环流程，支持版本留痕。
    
*   **跨端协同**：利用 **uni-app** 架构，实现 Web、小程序、App 三端数据打通，降低第三方协作门槛。
    

### 1.3 核心用户

*   **管理员**：负责项目创建、勘查模板配置、用户权限分配及全局数据汇总。
    
*   **勘查人员**：负责现场打点定位、信息录入、照片拍摄及进度跟踪。
    
*   **审核人员**：负责对勘查结果进行远程审核，提供驳回意见或通过建议。
    
*   **第三方单位**：通过小程序码快速进入系统，按权限进行查看或协助填报。
    

---

## 二、 核心功能需求

### 2.1 后台管理系统（PC 端）

*   **项目管理**：管理项目基本信息（编号、负责人、周期），支持数据全局导出。
    
*   **动态模板引擎**：
    
    *   **可视化配置**：通过拖拽方式定义字段类型（输入、选择、数字、开关、图片）。
        
    *   **逻辑联动**：设置字段间的显隐关系（如：选择“太阳能供电”才显示“日照时长”）。
        
*   **点位地图中心**：全量点位可视化，支持按状态（待勘查、已审核等）分颜色展示。
    
*   **审核与版本管理**：查看每一份勘查报告的历史修改记录，对比版本差异，下达审核指令。
    

### 2.2 移动端勘查系统（uni-app 跨端）

*   **跨端兼容地图**：
    
    *   小程序端使用原生 `<map>` 组件；App/H5 接入高德地图。
        
    *   提供“手动纠偏”功能，允许长按或拖拽 Marker 修正 GPS 定位误差。
        
*   **填报核心（uni-app 特性）**：
    
    *   **离线存储**：统一采用 `uni.setStorageSync` 实时保存草稿，防止进程杀死或断电导致数据丢失。
        
    *   **硬件调用**：强制调用系统相机（`sourceType: ['camera']`），确保照片时效性与真实性。
        
*   **OSS 云端水印**：
    
    *   前端原图直传，后端展示时通过 OSS 处理参数拼接动态水印（包含：勘查人、经纬度、时间）。
        

---

## 三、 数据库表设计 (MySQL 8.0)

SQL

```plaintext
-- 项目基础表
CREATE TABLE project (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  project_name varchar(100) NOT NULL COMMENT '项目名称',
  project_code varchar(50) COMMENT '项目编号',
  manager varchar(30) COMMENT '负责人',
  status tinyint DEFAULT 1 COMMENT '1正常 0禁用',
  create_time datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 动态勘查模板
CREATE TABLE survey_template (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  template_name varchar(100) NOT NULL,
  fields_json json NOT NULL COMMENT '字段配置JSON',
  create_time datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 勘查点位基础信息
CREATE TABLE survey_point (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  project_id bigint NOT NULL,
  point_name varchar(100) NOT NULL,
  longitude decimal(12,8),
  latitude decimal(12,8),
  status tinyint DEFAULT 0 COMMENT '0未勘查 1已提交 2通过 3驳回',
  KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 勘查结果及版本记录
CREATE TABLE survey_result (
  id bigint PRIMARY KEY AUTO_INCREMENT,
  point_id bigint NOT NULL,
  form_data json NOT NULL COMMENT '业务表单数据',
  images text COMMENT '照片列表URL',
  version int DEFAULT 1 COMMENT '版本号，驳回重提递增',
  is_latest tinyint DEFAULT 1 COMMENT '1当前最新 0历史记录',
  audit_status tinyint DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  audit_remark varchar(500) COMMENT '驳回意见',
  survey_user varchar(50),
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  KEY idx_point_version (point_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

```
---

## 四、 核心接口定义

| **接口路径** | **方法** | **说明** |
| --- | --- | --- |
| `/api/project/list` | GET | 获取项目列表 |
| `/api/template/detail` | GET | 获取动态表单 JSON 配置 |
| `/api/point/map` | GET | 获取地图点位分布（含状态聚合） |
| `/api/survey/submit` | POST | 提交勘查结果（含自动版本递增逻辑） |
| `/api/audit/feedback` | POST | 审核反馈（通过/驳回） |
| `/api/export/report/pdf` | GET | 根据 pointId 生成并下载 A4 PDF 报告 |

---

## 五、 动态表单配置示例 (JSON)

JSON

```plaintext
[
  {"label":"排口名称","type":"input","required":true},
  {"label":"排口类型","type":"select","options":["工业","生活","雨水","其他"]},
  {"label":"供电方式","type":"select","options":["市电","太阳能"],"id":"power_type"},
  {
    "label":"日照条件",
    "type":"select",
    "options":["充足","一般","差"],
    "linkage":{"targetId":"power_type","value":"太阳能"} 
  },
  {"label":"现场照片","type":"image","limit":9,"required":true}
]

```
---

## 六、 非功能需求

1.  **性能**：移动端表单渲染需支持懒加载，防止字段过多导致小程序 `setData` 卡顿。
    
2.  **安全**：第三方分享 Token 需加密并设置 72 小时有效期；敏感操作（如删除点位）记录详细操作日志。
    
3.  **兼容性**：支持 iOS 12+、Android 8+、微信小程序最新版本。
    

---

## 七、 AI 开发提示词 (Prompt)

> **指令**：基于 PRD v2.1 开发入河排口勘查系统。**前端要求**：PC 端使用 Vue3 + Element Plus；移动端使用 **uni-app + uView Plus**。移动端需确保在微信小程序与 App 环境下的组件兼容性。**核心逻辑**：

---

## 八、 附录：PDF 报告模板规范

1.  **页眉**：项目名称及点位唯一编号。
    
2.  **正文区**：表格化展示基础属性；分块展示建设条件（供电、通信）。
    
3.  **图片区**：3x3 网格排列，每张图下方附带拍摄时间。
    
4.  **结论区**：展示审核员最终评语及电子签名位。