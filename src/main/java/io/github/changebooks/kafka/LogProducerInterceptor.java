package io.github.changebooks.kafka;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 拦截发送消息
 * 从日志上下文获取日志信息，设置待发送消息
 *
 * @param <K> Key
 * @param <V> Value
 * @author changebooks@qq.com
 */
public class LogProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogProducerInterceptor.class);

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> rawRecord) {
        if (rawRecord == null) {
            return null;
        }

        ProducerRecord<K, V> record = null;

        try {
            record = processLog(rawRecord);
        } catch (Throwable tr) {
            LOGGER.error("onSend failed, throwable: ", tr);
        }

        if (record != null) {
            return record;
        } else {
            return rawRecord;
        }
    }

    /**
     * 发送之前
     * 从日志上下文获取日志信息，设置待发送消息
     *
     * @param record 待发送的消息
     * @return 新的待发送消息，包含日志信息
     */
    public ProducerRecord<K, V> processLog(ProducerRecord<K, V> record) {
        Headers headers = new RecordHeaders();

        KafkaTraceId.onSend(headers);
        KafkaLogId.onSend(headers);

        return KafkaHeaders.addProducer(record, headers);
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

}
