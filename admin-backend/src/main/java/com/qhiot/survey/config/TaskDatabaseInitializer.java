package com.qhiot.survey.config;

import com.qhiot.survey.entity.Project;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.service.ProjectService;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import com.qhiot.survey.service.SysTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class TaskDatabaseInitializer implements CommandLineRunner {

    private final SysTaskService sysTaskService;
    private final ProjectService projectService;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            long count = sysTaskService.count();
            if (count > 0) {
                log.info("====== [任务数据初始化] 任务表 sys_task 中已有 {} 条数据，跳过初始化 ======", count);
                return;
            }

            log.info("====== [任务数据初始化] 任务表 sys_task 为空，开始初始化默认项目、采集员和任务数据 ======");

            // 1. 初始化需要的采集员用户
            Map<String, SysUser> surveyors = initSurveyors();

            // 2. 初始化需要的项目
            Map<String, Project> projects = initProjects();

            // 3. 初始化 17 个勘察指派任务
            initTasks(surveyors, projects);

            log.info("====== [任务数据初始化] 默认任务及关联数据初始化成功 ======");
        } catch (Exception e) {
            log.error("====== [任务数据初始化] 初始化默认任务失败: {} ======", e.getMessage(), e);
        }
    }

    private Map<String, SysUser> initSurveyors() {
        Map<String, SysUser> surveyorMap = new HashMap<>();
        String[][] userSpecs = {
            {"lijianguo", "李建国"},
            {"chenxiaodong", "陈晓东"},
            {"wangyali", "王雅莉"},
            {"zhangwei", "张伟"},
            {"lina", "李娜"},
            {"wangqiang", "王强"},
            {"liuyang", "刘洋"}
        };

        SysRole surveyorRole = sysRoleService.lambdaQuery()
                .eq(SysRole::getRoleCode, "surveyor")
                .or()
                .eq(SysRole::getRoleCode, "SURVEYOR")
                .one();
        if (surveyorRole == null) {
            surveyorRole = new SysRole();
            surveyorRole.setRoleCode("surveyor");
            surveyorRole.setRoleName("采集员");
            surveyorRole.setPermissions("[\"point:view\", \"survey:create\", \"survey:edit\", \"survey:submit\"]");
            surveyorRole.setSort(4);
            surveyorRole.setStatus(1);
            sysRoleService.save(surveyorRole);
            log.info("====== [任务数据初始化] sys_role 表中缺失 surveyor 角色，已自动创建，ID: {} ======", surveyorRole.getId());
        }
        Long roleId = surveyorRole.getId();

        for (String[] spec : userSpecs) {
            String username = spec[0];
            String realName = spec[1];

            SysUser user = sysUserService.getUserByUsername(username);
            if (user == null) {
                user = new SysUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("Admin@123"));
                user.setRealName(realName);
                user.setStatus(1); // 启用
                user.setIsFirstLogin(0);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                sysUserService.save(user);

                // 关联角色
                sysRoleService.assignRoleToUser(user.getId(), List.of(roleId));
                log.info("====== [任务数据初始化] 创建采集员用户: {} ({}) ======", realName, username);
            }
            surveyorMap.put(realName, user);
        }
        return surveyorMap;
    }

    private Map<String, Project> initProjects() {
        Map<String, Project> projectMap = new HashMap<>();
        String[] projectNames = {
            "锦江区电力扩容项目",
            "5G基站年度巡检项目",
            "旧址安全隐患排查",
            "市政管线改造项目",
            "雄安新区地下设施规划",
            "2026年度城市更新基础测绘",
            "控制点连测项目",
            "老城区管网改造排查",
            "系统日常校准与维护",
            "高新区数字化馆网改造项目",
            "春风隧道及周边加固工程",
            "望京商圈千兆光网升级项目",
            "市政道路管网安全排查",
            "张江高科三期建设勘察项目",
            "白云电力大动脉通道整理项目",
            "5G绿色基站试点建设项目",
            "老旧社区排水网清淤检测项目"
        };

        for (String name : projectNames) {
            Project project = projectService.lambdaQuery().eq(Project::getProjectName, name).one();
            if (project == null) {
                project = new Project();
                project.setProjectName(name);
                project.setProjectCode("PRJ-" + String.format("%04d", Math.abs(name.hashCode() % 10000)));
                project.setManager("系统初始化");
                project.setRegion("未设定");
                project.setStartDate(LocalDate.now());
                project.setEndDate(LocalDate.now().plusMonths(6));
                project.setStatus(1); // 进行中
                project.setTemplateCount(1);
                project.setPointCount(5);
                project.setCompletedCount(0);
                project.setPendingAuditCount(0);
                project.setCreateTime(LocalDateTime.now());
                project.setUpdateTime(LocalDateTime.now());
                projectService.save(project);
                log.info("====== [任务数据初始化] 创建初始化项目: {} ======", name);
            }
            projectMap.put(name, project);
        }
        return projectMap;
    }

    private void initTasks(Map<String, SysUser> surveyors, Map<String, Project> projects) {
        LocalDateTime now = LocalDateTime.now();
        // SPEC: {任务编号, 任务名称, 项目名称, 任务类型, 优先级(0普通,1重要,2紧急), 状态(0待分配,1进行中,2已完成,3已逾期,4已终止), 负责人名字(可为空), 截止日期偏移天数, 描述}
        Object[][] taskSpecs = {
            {"P-7721", "成都锦江基站电力环境勘察", "锦江区电力扩容项目", "电力测绘", 2, 1, "", -2, "对锦江区几个核心电力扩容基站的配电箱及变压器现场进行外部尺寸测量和线缆引接走向测绘。"},
            {"P-3342", "上海陆家嘴5G微基站信号测试", "5G基站年度巡检项目", "无线勘察", 1, 1, "李建国", 5, "在陆家嘴商圈内部使用专用无线网路分析仪测试5G毫米波基站的信号盲区和上下行速率。"},
            {"P-2029", "北京龙山路基站旧址加固方案", "旧址安全隐患排查", "结构安全", 0, 2, "陈晓东", -5, "测绘龙山路旧址的承重结构形变和水泥风化程度，输出地质与构件承重加固前置基础测绘数据。"},
            {"P-8109", "天府大道高架管线路由摸排", "市政管线改造项目", "线缆规划", 0, 0, "", 10, "摸排天府大道高架下敷设的弱电与信号光缆走线，记录预留孔位与管道剩余容量。"},
            {"P-1102", "雄安新区管廊断面数字化测量", "雄安新区地下设施规划", "土建勘察", 0, 1, "王雅莉", 4, "利用三维激光扫描仪对规划中的雄安数字管廊第3标段完成管廊内部断面的三维坐标精准捕捉。"},
            {"P-9901", "南山区地形图测绘补漏", "2026年度城市更新基础测绘", "地形测绘", 2, 1, "张伟", -2, "针对南山区城中村改造边界的几处阴影与高楼遮挡区域，使用RTK手持设备执行室外地形补测。"},
            {"P-9902", "外业控制点布设及连测", "控制点连测项目", "外业控制", 1, 1, "李娜", 7, "在目标作业范围内根据测绘规范布设一级GPS控制点，并与市级已知水准网连测。"},
            {"P-9903", "地下管线探测数据录入", "老城区管网改造排查", "管线探测", 0, 1, "王强", 6, "通过管线探测仪定位雨污排水管网埋深与流向，并在地图工作台录入管道内径与连接属性。"},
            {"P-9904", "设备仪器校准报告提交", "系统日常校准与维护", "系统维护", 0, 2, "刘洋", -3, "完成RTK测量仪和激光测距仪的年度标定，向审核中心上传由计量检测院签发的仪器合格证。"},
            {"P-9905", "高新区科学馆多功能厅无线覆盖测试", "高新区数字化馆网改造项目", "无线勘察", 0, 0, "", 15, "馆内多功能厅各角落AP节点的5GHz频段衰减测试，出具覆盖热力草图。"},
            {"P-9906", "罗湖区春风隧道边坡稳定性检测", "春风隧道及周边加固工程", "结构安全", 2, 1, "陈晓东", -1, "监测春风隧道出口边坡的位移计数据，连测地表隆起和倾斜率并及时同步至数据库。"},
            {"P-9907", "朝阳区望京SOHO地下光缆敷设勘察", "望京商圈千兆光网升级项目", "线缆规划", 1, 2, "李娜", -1, "实勘望京SOHO塔2地下管井连通管道，拟定三家运营商共享千兆敷设方案的走线设计图。"},
            {"P-9908", "海淀区中关村南大街井盖沉降监测", "市政道路管网安全排查", "管线探测", 0, 0, "", 8, "沿南大街双向车道对有异常震动或凹陷的市政井盖进行标高测绘与外观残损拍照。"},
            {"P-9909", "浦东新区张江高科超高层倾斜度测量", "张江高科三期建设勘察项目", "结构安全", 1, 1, "张伟", 12, "使用精密全站仪对在建主体塔楼的垂直度、中心偏移与累计沉降情况进行阶段性抽检测定。"},
            {"P-9910", "白云区三元里直埋电缆外开挖核实", "白云电力大动脉通道整理项目", "电力测绘", 0, 2, "王强", -2, "针对管线交汇点进行人工探坑，核实直埋10kV高压线缆的保护管材质、埋深以及交叉间距。"},
            {"P-9911", "天河区珠江新城微型基站电磁辐射勘测", "5G绿色基站试点建设项目", "无线勘察", 0, 0, "", 20, "测量居民区周边新装微基站周围10米、30米、50米范围内的综合电场强度是否符合国家环保标准。"},
            {"P-9912", "西湖区文三路古力井下沉及淤积测绘", "老旧社区排水网清淤检测项目", "管线探测", 1, 1, "王雅莉", 9, "普查文三路沿线古力井深度、淤积层厚度与杂物堵塞情况，编制管网排水能力评估数据。"}
        };

        // 默认负责人账号: admin (userID = 1)
        Long defaultOwnerId = 1L;

        for (Object[] spec : taskSpecs) {
            String code = (String) spec[0];
            String name = (String) spec[1];
            String projName = (String) spec[2];
            String category = (String) spec[3];
            Integer priority = (Integer) spec[4];
            Integer status = (Integer) spec[5];
            String surveyorName = (String) spec[6];
            Integer deadlineOffset = (Integer) spec[7];
            String desc = (String) spec[8];

            Project project = projects.get(projName);
            Long projectId = project != null ? project.getId() : 1L;

            Long assigneeId = null;
            if (surveyorName != null && !surveyorName.isEmpty()) {
                SysUser surveyor = surveyors.get(surveyorName);
                if (surveyor != null) {
                    assigneeId = surveyor.getId();
                }
            }

            SysTask task = new SysTask();
            task.setTaskName(name);
            task.setProjectId(projectId);
            task.setPlotCode("PLOT-" + code.replace("P-", ""));
            task.setPrecisionRequirement("厘米级 (1-5cm)");
            task.setSensorType("激光雷达/RTK");
            task.setPriority(priority);
            task.setDescription(desc);
            task.setStatus(status);
            task.setDeadline(now.plusDays(deadlineOffset));
            task.setAssigneeId(assigneeId);
            task.setOwnerUserId(defaultOwnerId);
            task.setCategory(category);

            // 构造默认子任务清单 JSON
            String subtasksJson = String.format("[" +
                    "{\"id\":1,\"title\":\"现场控制点校准\",\"status\":%s}," +
                    "{\"id\":2,\"title\":\"主体结构/走线实勘测绘\",\"status\":%s}," +
                    "{\"id\":3,\"title\":\"测绘成果及现场照片上传\",\"status\":%s}" +
                    "]", 
                    status == 2 ? "\"completed\"" : "\"pending\"",
                    status == 2 ? "\"completed\"" : (status == 1 ? "\"ongoing\"" : "\"pending\""),
                    status == 2 ? "\"completed\"" : "\"pending\""
            );
            task.setSubtasks(subtasksJson);

            task.setCreateTime(now.minusDays(10));
            task.setUpdateTime(now.minusDays(1));

            sysTaskService.save(task);
            log.info("====== [任务数据初始化] 创建任务: {} ({}) ======", name, code);
        }
    }
}
