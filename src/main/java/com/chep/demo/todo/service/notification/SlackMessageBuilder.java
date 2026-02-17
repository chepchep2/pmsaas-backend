package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.Notification;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class SlackMessageBuilder {

    public String build(Notification notification) {
        String assigneeNames = notification.getTask().getAssignees().stream()
                .map(a -> a.getUser().getName())
                .collect(Collectors.joining(", "));

        return switch (notification.getType()) {
            case TASK_CREATED -> "[%s] 새 Task가 생성되었습니다.\nTask: %s\nProject: %s\n생성자: %s\n담당자: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "없음" : assigneeNames
            );
            case TASK_ASSIGNEES_CHANGED -> "[%s] Task Assignee가 변경되었습니다.\nTask: %s\nProject: %s\n변경자: %s\n담당자: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "없음" : assigneeNames
            );
        };
    }
}
