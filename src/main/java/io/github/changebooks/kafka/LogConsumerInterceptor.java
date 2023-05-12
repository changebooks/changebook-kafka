package io.github.changebooks.kafka;

import io.github.changebooks.log.LogClear;
import io.github.changebooks.log.LogId;
import io.github.changebooks.log.LogTraceId;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 拦截接收消息
 * 从已接收消息获取日志信息，设置日志上下文
 *
 * @param <K> Key
 * @param <V> Value
 * @author changebooks@qq.com
 */
public class LogConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogConsumerInterceptor.class);

    @Override
    public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
        if (records == null) {
            return null;
        }

        try {
            processLog(records);
        } catch (Throwable tr) {
            LOGGER.error("onConsume failed, throwable: ", tr);
        }

        return records;
    }

    /**
     * 消费之前
     * 从已接收消息获取日志信息，设置日志上下文
     *
     * @param records 已接收的消息
     */
    public void processLog(ConsumerRecords<K, V> records) {
        Headers headers = KafkaHeaders.getConsumer(records);

        if (headers == null) {
            LogTraceId.init();
            LogId.init();
        } else {
            KafkaTraceId.onConsume(headers);
            KafkaLogId.onConsume(headers);
        }
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        LogClear.clear();
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

}
