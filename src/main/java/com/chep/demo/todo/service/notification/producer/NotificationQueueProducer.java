package com.chep.demo.todo.service.notification.producer;

import com.chep.demo.todo.service.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueueProducer {
    private final RedisTemplate<String, String> redisTemplate;

    public NotificationQueueProducer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void push(Long notificationId) {
        redisTemplate.opsForList().rightPush(RedisKeys.NOTIFICATION_QUEUE, notificationId.toString());
    }
}
