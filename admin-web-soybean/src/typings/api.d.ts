/**
 * Namespace Api
 *
 * All backend api type
 */
declare namespace Api {
  namespace Common {
    /** common params of paginating */
    interface PaginatingCommonParams {
      /** current page number */
      current: number;
      /** page size */
      size: number;
      /** total count */
      total: number;
    }

    /** common params of paginating query list data */
    interface PaginatingQueryRecord<T = any> extends PaginatingCommonParams {
      records: T[];
    }

    /** common search params of table */
    type CommonSearchParams = Pick<Common.PaginatingCommonParams, 'current' | 'size'>;

    /**
     * enable status
     *
     * - "1": enabled
     * - "2": disabled
     */
    type EnableStatus = '1' | '2';

    /** common record */
    type CommonRecord<T = any> = {
      /** record id（后端Snowflake ID已序列化为字符串，兼容number类型） */
      id: string | number;
      /** record creator */
      createBy: string;
      /** record create time */
      createTime: string;
      /** record updater */
      updateBy: string;
      /** record update time */
      updateTime: string;
      /** record status */
      status: EnableStatus;
    } & T;
  }

  /**
   * Namespace Auth
   *
   * Backend api module: "auth"
   */
  namespace Auth {
    interface LoginToken {
      accessToken?: string;
      token?: string;
      refreshToken: string;
      /** 用户ID（后端 Snowflake ID 已序列化为字符串，避免前端精度丢失） */
      userId: string;
      username: string;
      realName: string;
      /** 用户角色编码列表（从 sys_role 表获取，如 ["admin", "auditor"]） */
      roleCodes?: string[];
      /** 用户权限列表（从角色的 permissions 字段聚合，如 ["user:list", "user:create"]） */
      permissions?: string[];
      isFirstLogin: boolean;
      loginWarning?: string;
    }

    interface UserInfo {
      userId: string;
      userName: string;
      realName: string;
      roles: string[];
      /** 用户权限列表（RBAC 权限） */
      permissions: string[];
      buttons: string[];
    }
  }

  /**
   * Namespace Route
   *
   * Backend api module: "route"
   */
  namespace Route {
    type ElegantConstRoute = import('@elegant-router/types').ElegantConstRoute;

    interface MenuRoute extends ElegantConstRoute {
      id: string;
    }

    interface UserRoute {
      routes: MenuRoute[];
      home: import('@elegant-router/types').LastLevelRouteKey;
    }
  }

  /**
   * namespace SystemManage
   *
   * backend api module: "systemManage"
   */
  namespace SystemManage {
    /** role */
    type Role = Common.CommonRecord<{
      /** role name */
      roleName: string;
      /** role code */
      roleCode: string;
      /** role description */
      roleDesc: string;
    }>;

    /** role search params */
    type RoleSearchParams = Partial<
      Pick<Api.SystemManage.Role, 'roleName' | 'roleCode' | 'status'> & Common.CommonSearchParams
    >;

    /** role list */
    type RoleList = Common.PaginatingQueryRecord<Role>;

    /** all role */
    type AllRole = Pick<Role, 'id' | 'roleName' | 'roleCode'>;

    /**
     * user gender
     *
     * - "1": "male"
     * - "2": "female"
     */
    type UserGender = '1' | '2';

    /** user */
    type User = Common.CommonRecord<{
      /** user name */
      username: string;
      /** user gender */
      userGender: UserGender;
      /** user nick name */
      nickName: string;
      /** real name */
      realName: string;
      /** user phone */
      userPhone: string;
      /** user email */
      userEmail: string;
      /** user role code collection */
      userRoles: string[];
    }>;

    /** user search params */
    type UserSearchParams = Partial<
      Pick<Api.SystemManage.User, 'userName' | 'userGender' | 'nickName' | 'userPhone' | 'userEmail' | 'status'> &
        Common.CommonSearchParams
    >;

    /** user list */
    type UserList = Common.PaginatingQueryRecord<User>;

    /**
     * menu type
     *
     * - "1": directory
     * - "2": menu
     */
    type MenuType = '1' | '2';

    type MenuButton = {
      /**
       * button code
       *
       * it can be used to control the button permission
       */
      code: string;
      /** button description */
      desc: string;
    };

    /**
     * icon type
     *
     * - "1": iconify icon
     * - "2": local icon
     */
    type IconType = '1' | '2';

    type MenuPropsOfRoute = Pick<
      import('vue-router').RouteMeta,
      | 'i18nKey'
      | 'keepAlive'
      | 'constant'
      | 'order'
      | 'href'
      | 'hideInMenu'
      | 'activeMenu'
      | 'multiTab'
      | 'fixedIndexInTab'
      | 'query'
    >;

    type Menu = Common.CommonRecord<{
      /** parent menu id */
      parentId: number;
      /** menu type */
      menuType: MenuType;
      /** menu name */
      menuName: string;
      /** route name */
      routeName: string;
      /** route path */
      routePath: string;
      /** component */
      component?: string;
      /** iconify icon name or local icon name */
      icon: string;
      /** icon type */
      iconType: IconType;
      /** buttons */
      buttons?: MenuButton[] | null;
      /** children menu */
      children?: Menu[];
    }> &
      MenuPropsOfRoute;

    /** menu list */
    type MenuList = Common.PaginatingQueryRecord<Menu>;

    type MenuTree = {
      id: number;
      label: string;
      pId: number;
      children?: MenuTree[];
    };
  }

  /**
   * namespace Project
   *
   * backend api module: "project"
   */
  namespace Project {
    type ProjectStatus = 'pending' | 'in_progress' | 'completed' | 'archived';

    interface ProjectInfo {
      id: number | string;
      projectName: string;
      projectCode: string;
      manager: string;
      region: string;
      status: ProjectStatus | string | number;
      startDate: string;
      endDate: string;
      templateCount: number;
      pointCount: number;
      completedCount: number;
      pendingAuditCount: number;
      pointTotal?: number; // 兼容旧字段
      pointDone?: number; // 兼容旧字段
      auditPending?: number; // 兼容旧字段
      description?: string;
      clientName?: string;
    }

    type ProjectSearchParams = Partial<
      Pick<ProjectInfo, 'projectName' | 'status' | 'manager'> &
      { keyword?: string } &
      Common.CommonSearchParams
    >;

    type ProjectList = Common.PaginatingQueryRecord<ProjectInfo>;

    interface ProjectSection {
      name: string;
      manager: string;
      progress: number;
      points: number;
      completed: number;
    }

    interface ProjectMember {
      name: string;
      role: string;
      avatar: string;
    }

    interface ProjectDetail extends ProjectInfo {
      sections: ProjectSection[];
      members: ProjectMember[];
      templateBindings: any[];
    }

    interface ProjectEditParams {
      projectName: string;
      projectCode: string;
      clientName?: string;
      startDate?: string;
      endDate?: string;
      manager?: string;
      description?: string;
    }
  }

  /**
   * namespace Point
   *
   * backend api module: "point"
   */
  namespace Point {
    type PointStatus = 'pending_audit' | 'approved' | 'rejected' | 'pending';

    interface SurveyPoint {
      pointId: number | string;
      pointCode: string;
      pointName: string;
      projectId: number | string;
      sectionId: number | string;
      longitude: number;
      latitude: number;
      status: PointStatus | string;
      assigneeUserId: number | string;
      assigneeName: string;
      latestVersion: number;
      abnormalTags: string[];
      // Client-side fields for UI (optional mapping)
      type?: string;
    }

    interface PointMapData {
      points: SurveyPoint[];
      statusStats: {
        pendingAudit: number;
        approved: number;
        rejected: number;
      };
    }

    type PointSearchParams = Partial<
      Pick<SurveyPoint, 'projectId' | 'sectionId' | 'status' | 'assigneeUserId'> &
      { keyword?: string; abnormalTag?: string; projectName?: string } &
      Common.CommonSearchParams
    >;

    type PointList = Common.PaginatingQueryRecord<SurveyPoint>;

    interface PointDetail extends SurveyPoint {
      projectName: string;
      sectionName: string;
      collectorName: string;
      createTime: string;
      updateTime: string;
      formData?: any;
      images?: string[];
    }

    interface PointEdit {
      pointName: string;
      projectId: number;
      sectionId?: number;
      longitude: number;
      latitude: number;
      status?: string;
      assigneeUserId?: number;
      abnormalTags?: string[];
    }
  }

  /**
   * namespace Audit
   *
   * backend api module: "audit"
   */
  namespace Audit {
    type AuditStatus = 'pending' | 'approved' | 'rejected';

    interface AuditRecord {
      id: number;
      pointId: number;
      pointName: string;
      projectName: string;
      auditorName: string;
      status: AuditStatus;
      version: number;
      submitTime: string;
      auditTime?: string;
      auditComment?: string;
      rejectReason?: string;
    }

    type AuditSearchParams = Partial<
      Pick<AuditRecord, 'status'> &
      { keyword?: string; projectId?: number } &
      Common.CommonSearchParams
    >;

    type AuditList = Common.PaginatingQueryRecord<AuditRecord>;

    interface AuditDetail extends AuditRecord {
      formData: any;
      images: string[];
      location: {
        longitude: number;
        latitude: number;
      };
      versionHistory: VersionHistory[];
    }

    interface VersionHistory {
      version: number;
      submitTime: string;
      auditorName?: string;
      status: AuditStatus;
      auditComment?: string;
    }

    interface VersionDiff {
      version1: number;
      version2: number;
      differences: Array<{
        field: string;
        oldValue: any;
        newValue: any;
      }>;
    }
  }

  /**
   * namespace Template
   *
   * backend api module: "template"
   */
  namespace Template {
    interface TemplateInfo {
      id: number;
      templateName: string;
      templateCode: string;
      fieldsCount: number;
      status: 'draft' | 'published' | 'archived';
      version: number;
      createTime: string;
      updateTime: string;
    }

    type TemplateSearchParams = Partial<
      Pick<TemplateInfo, 'templateName' | 'status'> &
      Common.CommonSearchParams
    >;

    type TemplateList = Common.PaginatingQueryRecord<TemplateInfo>;

    interface TemplateDetail extends TemplateInfo {
      fields: TemplateField[];
      rules?: any;
      bindings?: TemplateBinding[];
    }

    interface TemplateField {
      id: string;
      label: string;
      type: 'input' | 'select' | 'number' | 'switch' | 'image' | 'textarea' | 'date';
      required: boolean;
      options?: Array<{ label: string; value: any }>;
      linkage?: {
        targetField: string;
        value: any;
      };
      order: number;
    }

    interface TemplateEdit {
      templateName: string;
      templateCode?: string;
      fields: TemplateField[];
      rules?: any;
    }

    interface TemplateVersion {
      version: number;
      templateId: number;
      fields: TemplateField[];
      rules?: any;
      status: 'draft' | 'published';
      publishTime?: string;
      createTime: string;
    }

    interface TemplatePreview {
      templateName: string;
      fields: TemplateField[];
      renderConfig: any;
    }

    interface TemplateBinding {
      id: number;
      projectId: number;
      sectionId?: number;
      outfallType: string;
      templateId: number;
      templateVersionId: number;
    }
  }

  /**
   * namespace Export
   *
   * backend api module: "export"
   */
  namespace Export {
    type ExportTaskType = 'point_list' | 'audit_result' | 'pdf_report' | 'batch_pdf';
    type ExportTaskStatus = 'pending' | 'processing' | 'completed' | 'failed';

    interface ExportTask {
      id: number;
      taskName: string;
      taskType: ExportTaskType;
      status: ExportTaskStatus;
      fileUrl?: string;
      fileSize?: number;
      expireTime?: string;
      createTime: string;
      completeTime?: string;
      errorMessage?: string;
    }

    type ExportSearchParams = Partial<
      Pick<ExportTask, 'taskType' | 'status'> &
      Common.CommonSearchParams
    >;

    type ExportList = Common.PaginatingQueryRecord<ExportTask>;

    interface ExportCreate {
      taskName: string;
      taskType: ExportTaskType;
      params?: {
        projectId?: number;
        pointIds?: number[];
        status?: string;
      };
    }
  }
}
