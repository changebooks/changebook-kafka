package io.github.changebooks.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Utils;

/**
 * 标头
 *
 * @author changebooks@qq.com
 */
public final class KafkaHeaders {

    private KafkaHeaders() {
    }

    /**
     * 获取响应标头
     *
     * @param records 接收消息集
     * @param <K>     Key
     * @param <V>     Value
     * @return 响应标头
     */
    public static <K, V> Headers getConsumer(ConsumerRecords<K, V> records) {
        if (records == null) {
            return null;
        }

        for (ConsumerRecord<K, V> r : records) {
            if (r == null) {
                continue;
            }

            Headers headers = r.headers();
            if (headers != null) {
                return headers;
            }
        }

        return null;
    }

    /**
     * 新增请求标头
     *
     * @param record  发送消息
     * @param headers 请求标头
     * @param <K>     Key
     * @param <V>     Value
     * @return 新的发送消息，包含请求标头
     */
    public static <K, V> ProducerRecord<K, V> addProducer(ProducerRecord<K, V> record, Headers headers) {
        if (record == null) {
            return null;
        }

        if (headers == null) {
            return record;
        }

        headers = addAll(record.headers(), headers);
        return new ProducerRecord<>(
                record.topic(),
                record.partition(),
                record.timestamp(),
                record.key(),
                record.value(),
                headers);
    }

    /**
     * 获取一个记录
     *
     * @param headers 标头
     * @param key     键名
     * @return 最近新增的一个记录
     */
    public static String get(Headers headers, String key) {
        if (headers == null) {
            return null;
        }

        Header h = headers.lastHeader(key);
        if (h == null) {
            return null;
        }

        byte[] value = h.value();
        if (value == null) {
            return null;
        } else {
            return Utils.utf8(value);
        }
    }

    /**
     * 设置一个记录
     *
     * @param headers 标头
     * @param key     键名
     * @param value   记录，空？忽略
     * @return 标头
     */
    public static Headers set(Headers headers, String key, String value) {
        if (headers == null) {
            return null;
        }

        if (value == null) {
            return headers;
        } else {
            return headers.add(key, Utils.utf8(value));
        }
    }

    /**
     * 合并标头
     *
     * @param headers 当前标头
     * @param others  待合并的标头
     * @return 合并后的标头
     */
    public static Headers addAll(Headers headers, Headers others) {
        if (headers == null) {
            return others;
        }

        if (others == null) {
            return headers;
        }

        for (Header o : others) {
            if (o != null) {
                headers.add(o);
            }
        }

        return headers;
    }

}
