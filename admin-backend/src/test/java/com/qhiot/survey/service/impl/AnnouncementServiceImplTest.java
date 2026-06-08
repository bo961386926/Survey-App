package com.qhiot.survey.service.impl;

import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.entity.Announcement;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 公告服务单元测试
 *
 * <p>覆盖：创建、编辑、发布、撤回、删除的完整状态机。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AnnouncementService 单元测试")
class AnnouncementServiceImplTest {

    private AnnouncementServiceImpl announcementService;

    @BeforeEach
    void setUp() {
        announcementService = Mockito.spy(new AnnouncementServiceImpl());
        // 模拟 MyBatis-Plus save 回填 ID
        doAnswer(invocation -> {
            Announcement a = invocation.getArgument(0);
            if (a.getId() == null) {
                a.setId(1L);
            }
            return true;
        }).when(announcementService).save(any(Announcement.class));
        doReturn(true).when(announcementService).updateById(any(Announcement.class));
        doReturn(true).when(announcementService).removeById(any(Announcement.class));

        // 模拟登录用户
        LoginUser loginUser = new LoginUser(10L, "admin", "", "管理员",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createAnnouncement - 创建成功，状态为草稿(0)")
    void testCreateAnnouncement() {
        Announcement input = new Announcement();
        input.setTitle("测试公告");
        input.setType("system_notification");
        input.setContent("公告内容");

        Announcement result = announcementService.createAnnouncement(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isZero();
        assertThat(result.getPublisherId()).isEqualTo(10L);
        assertThat(result.getCreateTime()).isNotNull();
        verify(announcementService, times(1)).save(any(Announcement.class));
    }

    @Test
    @DisplayName("updateAnnouncement - 草稿可正常编辑")
    void testUpdateDraft() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(0);
        doReturn(existing).when(announcementService).getById(1L);

        Announcement update = new Announcement();
        update.setTitle("新标题");

        Announcement result = announcementService.updateAnnouncement(1L, update);
        assertThat(result).isNotNull();
        verify(announcementService, times(1)).updateById(any(Announcement.class));
    }

    @Test
    @DisplayName("updateAnnouncement - 已发布不可编辑")
    void testUpdatePublished_ShouldThrow() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(2); // 已发布
        doReturn(existing).when(announcementService).getById(1L);

        assertThatThrownBy(() -> announcementService.updateAnnouncement(1L, new Announcement()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已发布");
        verify(announcementService, never()).updateById(any());
    }

    @Test
    @DisplayName("updateAnnouncement - 公告不存在时抛出异常")
    void testUpdateNotFound_ShouldThrow() {
        doReturn(null).when(announcementService).getById(999L);

        assertThatThrownBy(() -> announcementService.updateAnnouncement(999L, new Announcement()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("publishAnnouncement - 发布成功，状态变为已发布(2)")
    void testPublish() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(0);
        doReturn(existing).when(announcementService).getById(1L);

        announcementService.publishAnnouncement(1L);

        assertThat(existing.getStatus()).isEqualTo(2);
        assertThat(existing.getPublishTime()).isNotNull();
        verify(announcementService, times(1)).updateById(existing);
    }

    @Test
    @DisplayName("recallAnnouncement - 仅已发布可撤回")
    void testRecall_Success() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(2);
        doReturn(existing).when(announcementService).getById(1L);

        announcementService.recallAnnouncement(1L);

        assertThat(existing.getStatus()).isEqualTo(3);
        verify(announcementService, times(1)).updateById(existing);
    }

    @Test
    @DisplayName("recallAnnouncement - 草稿不可撤回")
    void testRecallDraft_ShouldThrow() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(0);
        doReturn(existing).when(announcementService).getById(1L);

        assertThatThrownBy(() -> announcementService.recallAnnouncement(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅已发布");
        verify(announcementService, never()).updateById(any());
    }

    @Test
    @DisplayName("deleteAnnouncement - 草稿可删除")
    void testDeleteDraft() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(1);
        doReturn(existing).when(announcementService).getById(1L);

        announcementService.deleteAnnouncement(1L);

        verify(announcementService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("deleteAnnouncement - 已发布不可删除")
    void testDeletePublished_ShouldThrow() {
        Announcement existing = new Announcement();
        existing.setId(1L);
        existing.setStatus(2);
        doReturn(existing).when(announcementService).getById(1L);

        assertThatThrownBy(() -> announcementService.deleteAnnouncement(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不可删除");
        verify(announcementService, never()).removeById(any());
    }

    @Test
    @DisplayName("deleteAnnouncement - 不存在的公告抛异常")
    void testDeleteNotFound_ShouldThrow() {
        doReturn(null).when(announcementService).getById(999L);

        assertThatThrownBy(() -> announcementService.deleteAnnouncement(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
    }
}
