package io.github.changebooks.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.Serializable;

/**
 * 消费上下文
 *
 * @author changebooks@qq.com
 */
public final class KafkaContext implements Serializable {
    /**
     * OK
     */
    public static final int SUCCESS_OK = 0;

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 消息内容
     */
    private ConsumerRecord<String, String> record;

    public KafkaContext() {
        this.code = SUCCESS_OK;
    }

    public boolean isSuccess() {
        return code == SUCCESS_OK;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ConsumerRecord<String, String> getRecord() {
        return record;
    }

    public void setRecord(ConsumerRecord<String, String> record) {
        this.record = record;
    }

}
