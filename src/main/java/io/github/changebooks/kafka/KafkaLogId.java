package io.github.changebooks.kafka;

import io.github.changebooks.log.LogId;
import io.github.changebooks.log.LogParentId;
import org.apache.kafka.common.header.Headers;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 日志id
 *
 * @author changebooks@qq.com
 */
public final class KafkaLogId {
    /**
     * 键名
     */
    public static final String KEY_NAME = "log_id";

    private KafkaLogId() {
    }

    /**
     * 消费之前
     * 从已接收消息获取日志id，设置日志上下文
     *
     * @param headers 已接收消息的标头
     */
    public static void onConsume(Headers headers) {
        Objects.requireNonNull(headers, "headers can't be null");

        String logId = KafkaHeaders.get(headers, KEY_NAME);
        if (StringUtils.hasText(logId)) {
            LogParentId.set(logId);
        }

        LogId.init();
    }

    /**
     * 发送之前
     * 从日志上下文获取日志id，设置待发送消息
     *
     * @param headers 待发送的消息的标头
     */
    public static void onSend(Headers headers) {
        Objects.requireNonNull(headers, "headers can't be null");

        String logId = LogId.get();
        if (StringUtils.hasText(logId)) {
            KafkaHeaders.set(headers, KEY_NAME, logId);
            return;
        }

        logId = KafkaHeaders.get(headers, KEY_NAME);
        if (StringUtils.hasText(logId)) {
            return;
        }

        LogId.init();
        logId = LogId.get();
        KafkaHeaders.set(headers, KEY_NAME, logId);
    }

}
