# Desktop Admin Interface

<cite>
**Referenced Files in This Document**
- [README.md](file://admin-web-soybean/README.md)
- [index.vue](file://admin-web-soybean/src/views/home/index.vue)
- [card-data.vue](file://admin-web-soybean/src/views/home/modules/card-data.vue)
- [line-chart.vue](file://admin-web-soybean/src/views/home/modules/line-chart.vue)
- [project-news.vue](file://admin-web-soybean/src/views/home/modules/project-news.vue)
- [recent-projects.vue](file://admin-web-soybean/src/views/home/modules/recent-projects.vue)
- [index.vue](file://admin-web-soybean/src/views/point/list/index.vue)
- [index.vue](file://admin-web-soybean/src/views/project/list/index.vue)
- [index.vue](file://admin-web-soybean/src/views/system/user/index.vue)
- [point.ts](file://admin-web-soybean/src/service/api/point.ts)
- [project.ts](file://admin-web-soybean/src/service/api/project.ts)
- [index.ts](file://admin-web-soybean/src/service/request/index.ts)
- [index.ts](file://admin-web-soybean/src/store/modules/auth/index.ts)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)

## Introduction
This document describes the desktop admin interface built with the Soybean Admin framework. It focuses on the dashboard analytics widgets, recent activity displays, and system status monitoring; the survey point management interface with filtering, sorting, and bulk operations; the project administration screens including timeline visualization, member management, and progress tracking; and the user management interface with role assignment, permission configuration, and activity monitoring. It also covers form validation, data visualization, responsive layouts, accessibility features, backend API integration, and real-time data updates.

## Project Structure
The admin interface is a Vue 3 + Vite + TypeScript application using Ant Design Vue components and a glass-morphism UI theme. Key areas:
- Home dashboard with analytics cards, charts, recent activity, and recent projects
- Point management with list and map modes, filtering, pagination, and bulk actions
- Project list with status controls, progress bars, and creation/edit modals
- User management with role assignment, permission checks, and CRUD operations
- API service layer abstracting HTTP requests and backend integration
- Authentication store managing tokens, roles, and permissions

```mermaid
graph TB
subgraph "Dashboard"
Home["Home Index<br/>src/views/home/index.vue"]
Cards["Card Data<br/>src/views/home/modules/card-data.vue"]
LineChart["Line Chart<br/>src/views/home/modules/line-chart.vue"]
News["Project News<br/>src/views/home/modules/project-news.vue"]
Recents["Recent Projects<br/>src/views/home/modules/recent-projects.vue"]
end
subgraph "Management Views"
Points["Point List<br/>src/views/point/list/index.vue"]
Projects["Project List<br/>src/views/project/list/index.vue"]
Users["User Management<br/>src/views/system/user/index.vue"]
end
subgraph "Services"
Req["HTTP Request Layer<br/>src/service/request/index.ts"]
APPoint["Point API<br/>src/service/api/point.ts"]
APProj["Project API<br/>src/service/api/project.ts"]
end
subgraph "Auth Store"
Auth["Auth Store<br/>src/store/modules/auth/index.ts"]
end
Home --> Cards
Home --> LineChart
Home --> News
Home --> Recents
Points --> APPoint
Projects --> APProj
Users --> Req
APPoint --> Req
APProj --> Req
Req --> Auth
```

**Diagram sources**
- [index.vue:1-50](file://admin-web-soybean/src/views/home/index.vue#L1-L50)
- [card-data.vue:1-127](file://admin-web-soybean/src/views/home/modules/card-data.vue#L1-L127)
- [line-chart.vue:1-214](file://admin-web-soybean/src/views/home/modules/line-chart.vue#L1-L214)
- [project-news.vue:1-269](file://admin-web-soybean/src/views/home/modules/project-news.vue#L1-L269)
- [recent-projects.vue:1-288](file://admin-web-soybean/src/views/home/modules/recent-projects.vue#L1-L288)
- [index.vue:1-506](file://admin-web-soybean/src/views/point/list/index.vue#L1-L506)
- [index.vue:1-803](file://admin-web-soybean/src/views/project/list/index.vue#L1-L803)
- [index.vue:1-586](file://admin-web-soybean/src/views/system/user/index.vue#L1-L586)
- [point.ts:1-84](file://admin-web-soybean/src/service/api/point.ts#L1-L84)
- [project.ts:1-62](file://admin-web-soybean/src/service/api/project.ts#L1-L62)
- [index.ts:1-199](file://admin-web-soybean/src/service/request/index.ts#L1-L199)
- [index.ts:1-203](file://admin-web-soybean/src/store/modules/auth/index.ts#L1-L203)

**Section sources**
- [README.md:20-80](file://admin-web-soybean/README.md#L20-L80)
- [index.vue:1-50](file://admin-web-soybean/src/views/home/index.vue#L1-L50)

## Core Components
- Dashboard overview with:
  - Analytics cards for key metrics
  - Interactive line chart for task trends
  - Recent activity feed
  - Recent projects list with status badges
- Point management:
  - List and map modes with toggle
  - Filtering by project, keyword, and status
  - Pagination and selection for bulk actions
  - Map overlay controls and legend
- Project administration:
  - Project list with status toggles and progress bars
  - Creation/edit modals with permission-aware actions
  - Metrics cards and status filters
- User management:
  - User list with role tags and status
  - Add/edit/reset-password modals
  - Role and permission-driven UI controls

**Section sources**
- [index.vue:1-50](file://admin-web-soybean/src/views/home/index.vue#L1-L50)
- [card-data.vue:1-127](file://admin-web-soybean/src/views/home/modules/card-data.vue#L1-L127)
- [line-chart.vue:1-214](file://admin-web-soybean/src/views/home/modules/line-chart.vue#L1-L214)
- [project-news.vue:1-269](file://admin-web-soybean/src/views/home/modules/project-news.vue#L1-L269)
- [recent-projects.vue:1-288](file://admin-web-soybean/src/views/home/modules/recent-projects.vue#L1-L288)
- [index.vue:1-506](file://admin-web-soybean/src/views/point/list/index.vue#L1-L506)
- [index.vue:1-803](file://admin-web-soybean/src/views/project/list/index.vue#L1-L803)
- [index.vue:1-586](file://admin-web-soybean/src/views/system/user/index.vue#L1-L586)

## Architecture Overview
The admin interface follows a layered architecture:
- Presentation layer: Vue components and modules
- Service layer: API modules encapsulate HTTP calls
- Request layer: Centralized Axios wrapper with interceptors for auth, error handling, and token refresh
- State management: Pinia stores for auth and routing
- Theme and UI: UnoCSS and Ant Design Vue with custom directives and components

```mermaid
sequenceDiagram
participant U as "User"
participant C as "Component"
participant S as "Service API"
participant R as "Request Wrapper"
participant B as "Backend API"
U->>C : Interact (filter, paginate, click)
C->>S : Call API function (fetchGetPointList, fetchGetProjectList)
S->>R : request(config)
R->>R : Inject Authorization header
R->>B : HTTP request
B-->>R : Response {code, data}
R->>R : Validate success code
R-->>S : Transformed data
S-->>C : Return typed result
C->>C : Update state, render UI
```

**Diagram sources**
- [point.ts:1-84](file://admin-web-soybean/src/service/api/point.ts#L1-L84)
- [project.ts:1-62](file://admin-web-soybean/src/service/api/project.ts#L1-L62)
- [index.ts:20-156](file://admin-web-soybean/src/service/request/index.ts#L20-L156)

**Section sources**
- [index.ts:1-199](file://admin-web-soybean/src/service/request/index.ts#L1-L199)
- [index.ts:1-203](file://admin-web-soybean/src/store/modules/auth/index.ts#L1-L203)

## Detailed Component Analysis

### Dashboard Components
The dashboard composes four primary widgets:
- Analytics cards: Four metric cards with icons, badges, and hover effects
- Line chart: ECharts-based trend visualization with time range tabs and theme-aware tooltips
- Recent activity: Timeline-like activity feed with quick actions
- Recent projects: Paginated list of projects with status indicators and navigation

```mermaid
graph TB
Home["Home Index<br/>src/views/home/index.vue"]
Cards["Card Data<br/>src/views/home/modules/card-data.vue"]
Line["Line Chart<br/>src/views/home/modules/line-chart.vue"]
News["Project News<br/>src/views/home/modules/project-news.vue"]
Recents["Recent Projects<br/>src/views/home/modules/recent-projects.vue"]
Home --> Cards
Home --> Line
Home --> News
Home --> Recents
```

**Diagram sources**
- [index.vue:1-50](file://admin-web-soybean/src/views/home/index.vue#L1-L50)
- [card-data.vue:1-127](file://admin-web-soybean/src/views/home/modules/card-data.vue#L1-L127)
- [line-chart.vue:1-214](file://admin-web-soybean/src/views/home/modules/line-chart.vue#L1-L214)
- [project-news.vue:1-269](file://admin-web-soybean/src/views/home/modules/project-news.vue#L1-L269)
- [recent-projects.vue:1-288](file://admin-web-soybean/src/views/home/modules/recent-projects.vue#L1-L288)

**Section sources**
- [index.vue:12-49](file://admin-web-soybean/src/views/home/index.vue#L12-L49)
- [card-data.vue:18-55](file://admin-web-soybean/src/views/home/modules/card-data.vue#L18-L55)
- [line-chart.vue:15-86](file://admin-web-soybean/src/views/home/modules/line-chart.vue#L15-L86)
- [project-news.vue:20-53](file://admin-web-soybean/src/views/home/modules/project-news.vue#L20-L53)
- [recent-projects.vue:14-39](file://admin-web-soybean/src/views/home/modules/recent-projects.vue#L14-L39)

### Survey Point Management
Key capabilities:
- View modes: List and map with toggle
- Stats summary cards
- Filters: Project dropdown, keyword search, status toggle buttons
- Bulk operations: Selection with batch assign/delete placeholders
- Table columns: Name, project, code, coordinates, status, date, actions
- Map view: Sidebar list with active selection, zoom controls, legend
- Modals: Import dialog placeholder with Excel template download

```mermaid
flowchart TD
Start(["Point List Mounted"]) --> LoadData["Load Point List<br/>fetchGetPointList"]
LoadData --> RenderTable["Render Table/Grid"]
RenderTable --> Filter["Apply Filters<br/>Project/Keyword/Status"]
Filter --> Page["Pagination Change"]
Page --> LoadData
RenderTable --> ViewMode{"View Mode"}
ViewMode --> |List| Table["Table View"]
ViewMode --> |Map| MapView["Map View + Sidebar"]
MapView --> Focus["Focus Active Point"]
Table --> Actions["Single Actions"]
Table --> Bulk["Bulk Actions Placeholder"]
Actions --> Detail["View Detail"]
Bulk --> Assign["Batch Assign Placeholder"]
Bulk --> Delete["Batch Delete Placeholder"]
```

**Diagram sources**
- [index.vue:354-472](file://admin-web-soybean/src/views/point/list/index.vue#L354-L472)
- [point.ts:4-17](file://admin-web-soybean/src/service/api/point.ts#L4-L17)

**Section sources**
- [index.vue:1-506](file://admin-web-soybean/src/views/point/list/index.vue#L1-L506)
- [point.ts:1-84](file://admin-web-soybean/src/service/api/point.ts#L1-L84)

### Project Administration Screens
Key capabilities:
- Metrics cards: Total, active, risk, monthly completion
- Search and status filter bar
- Project table with progress bars and status tags
- Action buttons: View, edit, status transitions, delete
- Progress tracking: Percent calculation and visual indicator
- Status management: Draft → Start, Active → Pause/Complete, Paused → Resume, Completed → Archive

```mermaid
sequenceDiagram
participant U as "User"
participant P as "Project List Component"
participant API as "Project API"
participant REQ as "Request Wrapper"
U->>P : Open Project List
P->>API : fetchGetProjectList(params)
API->>REQ : request({ url : '/project/page', params })
REQ-->>API : { code, data }
API-->>P : Project list records
P->>P : Render table with progress/status
U->>P : Click Status Action
P->>API : fetchUpdateProjectStatus(id, targetStatus)
API->>REQ : request({ url : '/project/ : id/status', params : {targetStatus} })
REQ-->>API : { code, data }
API-->>P : Success
P->>P : Reload list
```

**Diagram sources**
- [index.vue:68-137](file://admin-web-soybean/src/views/project/list/index.vue#L68-L137)
- [project.ts:4-61](file://admin-web-soybean/src/service/api/project.ts#L4-L61)
- [index.ts:31-35](file://admin-web-soybean/src/service/request/index.ts#L31-L35)

**Section sources**
- [index.vue:1-803](file://admin-web-soybean/src/views/project/list/index.vue#L1-L803)
- [project.ts:1-62](file://admin-web-soybean/src/service/api/project.ts#L1-L62)

### User Management Interface
Key capabilities:
- Stats cards: Total users, admin, auditor, collector
- Search and role filter
- User table with avatar initials, role tags, status tag
- Modals: Add user (form validation), Edit user, Reset password (validation)
- Permission-aware UI: Conditional buttons based on role/permission
- Role assignment: Checkbox groups for role selection

```mermaid
flowchart TD
Init(["User Management Mounted"]) --> LoadRoles["Load Roles<br/>fetchGetRoleList"]
Init --> LoadUsers["Load Users<br/>fetchGetUserList"]
LoadUsers --> RenderUsers["Render User Table"]
RenderUsers --> Search["Search/Filter"]
Search --> LoadUsers
RenderUsers --> Actions["Actions: Edit/Reset/Enable/Disable"]
Actions --> Validate["Form Validation"]
Validate --> Submit["Submit via API"]
Submit --> Refresh["Refresh List"]
```

**Diagram sources**
- [index.vue:376-581](file://admin-web-soybean/src/views/system/user/index.vue#L376-L581)
- [index.ts:192-201](file://admin-web-soybean/src/store/modules/auth/index.ts#L192-L201)

**Section sources**
- [index.vue:1-586](file://admin-web-soybean/src/views/system/user/index.vue#L1-L586)
- [index.ts:1-203](file://admin-web-soybean/src/store/modules/auth/index.ts#L1-L203)

### Form Validation Examples
- User creation requires username, real name, and password (minimum length)
- Reset password requires matching new and confirm passwords
- Edit user requires real name
- Validation messages use Ant Design Vue message prompts

**Section sources**
- [index.vue:455-558](file://admin-web-soybean/src/views/system/user/index.vue#L455-L558)

### Data Visualization
- Dashboard line chart uses ECharts with:
  - Smoothed line series and area fill
  - Dynamic tooltip and grid configuration
  - Theme-aware colors and dark mode watcher
- Project progress bars use Ant Design Vue Progress component
- Status badges use colored tags with pulse animation for active state

**Section sources**
- [line-chart.vue:15-86](file://admin-web-soybean/src/views/home/modules/line-chart.vue#L15-L86)
- [index.vue:299-305](file://admin-web-soybean/src/views/project/list/index.vue#L299-L305)
- [index.vue:729-736](file://admin-web-soybean/src/views/project/list/index.vue#L729-L736)

### Responsive Layouts
- CSS Grid and Flexbox for adaptive dashboards
- Ant Design Vue components with responsive props (e.g., input heights, pagination)
- Scroll regions for long lists and tables
- Breakpoints for lg/grid-cols variants

**Section sources**
- [index.vue:28-48](file://admin-web-soybean/src/views/home/index.vue#L28-L48)
- [index.vue:108-127](file://admin-web-soybean/src/views/point/list/index.vue#L108-L127)

### Accessibility Features
- Semantic headings and labels
- Focusable controls and keyboard navigable tables
- Clear contrast and theme-aware colors
- Tooltip and status text for interactive elements
- Disabled states and loading indicators

[No sources needed since this section provides general guidance]

## Dependency Analysis
- Components depend on:
  - Service APIs for data fetching
  - Request wrapper for HTTP communication
  - Auth store for permissions and tokens
- Coupling:
  - Low coupling between views and services via API modules
  - Cohesion within modules (e.g., dashboard widgets)
- External dependencies:
  - Ant Design Vue for UI primitives
  - ECharts for charts
  - UnoCSS for styling

```mermaid
graph LR
Points["Point List<br/>src/views/point/list/index.vue"] --> APIPoint["API: point.ts"]
Projects["Project List<br/>src/views/project/list/index.vue"] --> APIProj["API: project.ts"]
Users["User Management<br/>src/views/system/user/index.vue"] --> Request["Request: index.ts"]
APIPoint --> Request
APIProj --> Request
Request --> Auth["Auth Store<br/>src/store/modules/auth/index.ts"]
```

**Diagram sources**
- [index.vue:284-286](file://admin-web-soybean/src/views/point/list/index.vue#L284-L286)
- [index.vue:24-27](file://admin-web-soybean/src/views/project/list/index.vue#L24-L27)
- [index.vue:238-239](file://admin-web-soybean/src/views/system/user/index.vue#L238-L239)
- [point.ts:1-84](file://admin-web-soybean/src/service/api/point.ts#L1-L84)
- [project.ts:1-62](file://admin-web-soybean/src/service/api/project.ts#L1-L62)
- [index.ts:1-199](file://admin-web-soybean/src/service/request/index.ts#L1-L199)
- [index.ts:1-203](file://admin-web-soybean/src/store/modules/auth/index.ts#L1-L203)

**Section sources**
- [index.ts:20-156](file://admin-web-soybean/src/service/request/index.ts#L20-L156)
- [index.ts:192-201](file://admin-web-soybean/src/store/modules/auth/index.ts#L192-L201)

## Performance Considerations
- Prefer virtualized scrolling for large tables (already using scroll y with calc)
- Debounce search/filter inputs to reduce API calls
- Lazy-load map components when switching to map view
- Cache frequently accessed lists (e.g., projects) and invalidate on mutations
- Use computed properties for derived data (e.g., progress percentages)
- Minimize re-renders by using shallow refs for large arrays and deep equality checks

[No sources needed since this section provides general guidance]

## Troubleshooting Guide
Common issues and resolutions:
- Authentication errors:
  - Token expiration triggers automatic refresh; if refresh fails, reset store and redirect to login
  - Logout codes and modal logout codes handled centrally
- Network and timeout errors:
  - Friendly messages shown; network errors and timeouts detected and surfaced
- Backend error mapping:
  - Backend codes mapped to user-friendly messages; 403 treated as permission denied
- UI feedback:
  - Messages and modals guide users through failures and confirm destructive actions

**Section sources**
- [index.ts:36-96](file://admin-web-soybean/src/service/request/index.ts#L36-L96)
- [index.ts:100-154](file://admin-web-soybean/src/service/request/index.ts#L100-L154)
- [index.ts:50-64](file://admin-web-soybean/src/store/modules/auth/index.ts#L50-L64)

## Conclusion
The desktop admin interface leverages Soybean Admin’s modern stack to deliver a responsive, accessible, and feature-rich management experience. The dashboard provides actionable insights, while the management screens streamline point, project, and user administration. The service layer abstracts backend integration with robust error handling and authentication. By following the patterns outlined here, teams can extend functionality, maintain consistency, and ensure reliable operation across environments.