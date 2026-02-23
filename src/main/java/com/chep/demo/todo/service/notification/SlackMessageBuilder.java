package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.domain.notification.Notification;
import com.chep.demo.todo.domain.task.TaskAssignee;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlackMessageBuilder {

    public String build(Notification notification, List<TaskAssignee> assignees) {
        String assigneeNames = assignees.stream()
                .map(a -> a.getUser().getName())
                .collect(Collectors.joining(", "));

        return switch (notification.getType()) {
            case TASK_CREATED -> "[%s] New task has been created.\nTask: %s\nProject: %s\n생성자: %s\n담당자: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "없음" : assigneeNames
            );
            case TASK_ASSIGNEES_CHANGED -> "[%s] Task assignees have been updated.\nTask: %s\nProject: %s\n변경자: %s\n담당자: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "없음" : assigneeNames
            );
        };
    }
}
