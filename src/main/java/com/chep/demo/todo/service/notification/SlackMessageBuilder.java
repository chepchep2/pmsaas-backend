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
            // TODO: 다국어 지원 시 MessageSource로 전환 필요
            case TASK_CREATED -> "[%s] New task has been created.\nTask: %s\nProject: %s\nCreator: %s\nAssignee: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "None" : assigneeNames
            );
            case TASK_ASSIGNEES_CHANGED -> "[%s] Task assignees have been updated.\nTask: %s\nProject: %s\nModifier: %s\nAssignee: %s".formatted(
                    notification.getWorkspace().getName(),
                    notification.getTask().getTitle(),
                    notification.getProject().getName(),
                    notification.getActor().getName(),
                    assigneeNames.isEmpty() ? "None" : assigneeNames
            );
        };
    }
}
