package io.github.changebooks.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 批量消费
 *
 * @author changebooks@qq.com
 */
@FunctionalInterface
public interface KafkaBatchConsumer {
    /**
     * 执行消费
     *
     * @param records 消息列表
     * @param context 消费上下文
     * @return 消费成功提交消息？否则，消费失败等待重试，或抛出异常等待重试
     */
    boolean consume(final List<ConsumerRecord<String, String>> records, @Nullable final KafkaBatchContext context);

}
