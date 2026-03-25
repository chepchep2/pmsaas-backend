package com.chep.demo.todo.domain.invitation;

import com.chep.demo.todo.exception.invitation.InvitationStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class InvitationTest {
    private Invitation invitation;
    @BeforeEach
    void setUp() throws Exception {
        invitation = createInvitationWithStatus(Invitation.Status.PENDING);
    }

    private Invitation createInvitationWithStatus(Invitation.Status status) throws Exception {
        // Invitation은 protected 생성자라 Reflection으로 생성
        var constructor = Invitation.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Invitation inv = constructor.newInstance();

        Field statusField = Invitation.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(inv, status);
        return inv;
    }

    @Test
    void marSending_whenPending_changesStatusToSending() {
        // 실행
        invitation.markSending(Instant.now());
        // 검증
        assertThat(invitation.getStatus()).isEqualTo(Invitation.Status.SENDING);
    }

    @Test
    void markSent_whenSending_changesStatusToSent() throws Exception {
        // 준비
        invitation = createInvitationWithStatus(Invitation.Status.SENDING);
        // 실행
        invitation.markSent(Instant.now());
        // 검증
        assertThat(invitation.getStatus()).isEqualTo(Invitation.Status.SENT);
    }

    @Test
    void markSent_whenPending_throwsInvitationStateException() {
        assertThatThrownBy(() -> invitation.markSent(Instant.now()))
                .isInstanceOf(InvitationStateException.class);
    }
}
