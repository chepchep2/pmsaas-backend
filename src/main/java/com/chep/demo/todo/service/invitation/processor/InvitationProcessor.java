package com.chep.demo.todo.service.invitation.processor;

import com.chep.demo.todo.service.invitation.InvitationEmailService;
import com.chep.demo.todo.service.invitation.InvitationStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InvitationProcessor {
    private static final Logger log = LoggerFactory.getLogger(InvitationProcessor.class);
    private final InvitationEmailService invitationEmailService;
    private final InvitationStateService invitationStateService;

    public InvitationProcessor(InvitationEmailService invitationEmailService, InvitationStateService invitationStateService) {
        this.invitationEmailService = invitationEmailService;
        this.invitationStateService = invitationStateService;
    }

    public void process(Long invitationId) {
        boolean locked = invitationStateService.tryMarkSending(invitationId);

        if (!locked) {
            log.warn("Already processed or not found: {}", invitationId);
            return;
        }

        log.info("Marked as SENDING: invitation={}", invitationId);

        invitationEmailService.sendInvitationEmail(invitationId);
    }
}
