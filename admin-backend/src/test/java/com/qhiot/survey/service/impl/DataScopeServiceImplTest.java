package com.qhiot.survey.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.entity.CollabEntry;
import com.qhiot.survey.entity.ProjectMember;
import com.qhiot.survey.entity.SurveyPoint;
import com.qhiot.survey.entity.SysTask;
import com.qhiot.survey.mapper.CollabAccessLogMapper;
import com.qhiot.survey.mapper.CollabEntryMapper;
import com.qhiot.survey.mapper.ProjectMemberMapper;
import com.qhiot.survey.mapper.SurveyPointMapper;
import com.qhiot.survey.mapper.SysTaskMapper;
import com.qhiot.survey.security.CollabSecurityService;
import com.qhiot.survey.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeServiceImplTest {

    private ProjectMemberMapper projectMemberMapper;
    private SurveyPointMapper surveyPointMapper;
    private SysTaskMapper sysTaskMapper;
    private CollabEntryMapper collabEntryMapper;
    private CollabAccessLogMapper collabAccessLogMapper;
    private DataScopeServiceImpl dataScopeService;
    private List<ProjectMember> projectMembers;
    private long projectMemberCount;
    private SurveyPoint point;
    private SysTask task;
    private CollabEntry collabEntry;

    @BeforeEach
    void setUp() {
        projectMembers = List.of();
        projectMemberCount = 0L;
        point = null;
        task = null;
        collabEntry = null;
        projectMemberMapper = proxy(ProjectMemberMapper.class);
        surveyPointMapper = proxy(SurveyPointMapper.class);
        sysTaskMapper = proxy(SysTaskMapper.class);
        collabEntryMapper = proxy(CollabEntryMapper.class);
        collabAccessLogMapper = proxy(CollabAccessLogMapper.class);
        CollabSecurityService collabSecurityService = new CollabSecurityService(
                collabEntryMapper,
                collabAccessLogMapper,
                new ObjectMapper()
        );
        dataScopeService = new DataScopeServiceImpl(projectMemberMapper, surveyPointMapper, sysTaskMapper, collabSecurityService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminHasUnrestrictedProjectScope() {
        loginAs(1L, "admin", "ROLE_ADMIN");

        assertThat(dataScopeService.isSystemAdmin()).isTrue();
        assertThat(dataScopeService.getAccessibleProjectIds()).isNull();
        assertThat(dataScopeService.canAccessProject(100L)).isTrue();
    }

    @Test
    void projectMemberCanAccessOnlyMemberProjects() {
        loginAs(2L, "manager", "project:view");
        ProjectMember member = new ProjectMember();
        member.setProjectId(100L);
        projectMembers = List.of(member);
        projectMemberCount = 1L;

        assertThat(dataScopeService.getAccessibleProjectIds()).containsExactly(100L);
        assertThat(dataScopeService.canAccessProject(100L)).isTrue();
    }

    @Test
    void assigneeCanAccessPointEvenWithoutProjectMembership() {
        loginAs(3L, "surveyor", "point:view");
        this.point = new SurveyPoint();
        this.point.setId(200L);
        this.point.setProjectId(100L);
        this.point.setAssigneeId(3L);

        assertThat(dataScopeService.canAccessPoint(200L)).isTrue();
    }

    @Test
    void taskOwnerCanAccessTaskEvenWithoutProjectMembership() {
        loginAs(4L, "owner", "task:view");
        this.task = new SysTask();
        this.task.setId(300L);
        this.task.setProjectId(100L);
        this.task.setOwnerUserId(4L);

        assertThat(dataScopeService.canAccessTask(300L)).isTrue();
    }

    @Test
    void collabEntryUsesAuthorizedProjectsAndPoints() {
        loginAsCollab(9001L);
        collabEntry = new CollabEntry();
        collabEntry.setId(9001L);
        collabEntry.setStatus(1);
        collabEntry.setProjectIds("[100]");
        collabEntry.setPointIds("[200]");

        this.point = new SurveyPoint();
        this.point.setId(200L);
        this.point.setProjectId(999L);
        this.task = new SysTask();
        this.task.setId(300L);
        this.task.setProjectId(100L);

        assertThat(dataScopeService.getAccessibleProjectIds()).containsExactly(100L);
        assertThat(dataScopeService.getAccessiblePointIds()).containsExactly(200L);
        assertThat(dataScopeService.canAccessProject(100L)).isTrue();
        assertThat(dataScopeService.canAccessProject(999L)).isFalse();
        assertThat(dataScopeService.canAccessPoint(200L)).isTrue();
        assertThat(dataScopeService.canAccessTask(300L)).isTrue();
    }

    private void loginAs(Long userId, String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = java.util.Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();
        LoginUser loginUser = new LoginUser(userId, username, "password", username, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, grantedAuthorities)
        );
    }

    private void loginAsCollab(Long entryId) {
        List<SimpleGrantedAuthority> grantedAuthorities = List.of(
                new SimpleGrantedAuthority(CollabSecurityService.COLLAB_ROLE)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("collab:" + entryId, null, grantedAuthorities)
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> mapperType) {
        return (T) Proxy.newProxyInstance(
                mapperType.getClassLoader(),
                new Class[]{mapperType},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectList" -> projectMembers;
                    case "selectCount" -> projectMemberCount;
                    case "selectById" -> {
                        if (mapperType == SurveyPointMapper.class) {
                            yield point;
                        }
                        if (mapperType == SysTaskMapper.class) {
                            yield task;
                        }
                        if (mapperType == CollabEntryMapper.class) {
                            yield collabEntry;
                        }
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException("Unexpected mapper call: " + method.getName());
                }
        );
    }
}
