package io.github.changebooks.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息分页，并行消费
 * 总行数 / 线程数 = 每线程处理行数
 * 可以整除时，线程数 == 实际线程数，如：总行数 = 9 和 线程数 = 3，实际结果：实际线程数 = 3，每线程处理行数 = 3
 * 不能整除时，线程数 != 实际线程数，如：总行数 = 9 和 线程数 = 4，实际结果：实际线程数 = 3，每线程处理行数 = 3
 *
 * @author changebooks@qq.com
 */
public class KafkaBatchListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaBatchListener.class);

    /**
     * 执行线程
     */
    private final Executor executor;

    /**
     * 消费接口
     */
    private final KafkaBatchConsumer consumer;

    /**
     * 线程数
     * 实际线程数 = CEIL(总行数 / CEIL(总行数 / 线程数))
     */
    private int threadNum = 1;

    public KafkaBatchListener(Executor executor, KafkaBatchConsumer consumer) {
        Assert.notNull(executor, "executor can't be null");
        Assert.notNull(consumer, "consumer can't be null");

        this.executor = executor;
        this.consumer = consumer;
    }

    /**
     * 计算分页，多线程消费
     *
     * @param records 消息列表
     * @param context 消费上下文
     * @return 消费成功提交消息？否则，消费失败等待重试
     */
    public boolean listen(final List<ConsumerRecord<String, String>> records, @Nullable final KafkaBatchContext context) {
        if (records == null) {
            LOGGER.warn("listen warning, records can't be null");
            return consume(null, context);
        }

        if (records.isEmpty()) {
            LOGGER.warn("listen warning, records can't be empty");
            return consume(records, context);
        }

        if (threadNum <= 1) {
            return consume(records, context);
        }

        // IF records.size() = 9 AND threadNum = 3, RESULT pageList.size() = 3 AND pageList[0].size() = 3
        // IF records.size() = 9 AND threadNum = 4, RESULT pageList.size() = 3 AND pageList[0].size() = 3
        List<List<ConsumerRecord<String, String>>> pageList = PageUtils.compute(records, threadNum);
        return asyncConsume(pageList, context);
    }

    /**
     * 多线程消费
     *
     * @param pageList 分页列表
     * @param context  消费上下文
     * @return 全部线程消费成功提交消息？否则，任一线程消费失败等待重试
     */
    public boolean asyncConsume(@NonNull final List<List<ConsumerRecord<String, String>>> pageList, @Nullable final KafkaBatchContext context) {
        int size = pageList.size();
        if (size <= 0) {
            return consume(null, context);
        }

        if (size == 1) {
            return consume(pageList.get(0), context);
        }

        final AtomicBoolean result = new AtomicBoolean(true);

        final CountDownLatch lock = new CountDownLatch(size);
        final Map<String, String> logContext = MDC.getCopyOfContextMap();

        for (List<ConsumerRecord<String, String>> records : pageList) {
            executor.execute(() -> {
                try {
                    if (logContext != null) {
                        MDC.setContextMap(logContext);
                    }

                    if (!consume(records, context)) {
                        result.set(false);
                    }
                } finally {
                    lock.countDown();

                    if (logContext != null) {
                        MDC.clear();
                    }
                }
            });
        }

        try {
            lock.await();
        } catch (InterruptedException tr) {
            LOGGER.error("asyncConsume failed, throwable: ", tr);
        }

        return result.get();
    }

    /**
     * 执行消费
     *
     * @param records 消息列表
     * @param context 消费上下文
     * @return 消费成功提交消息？否则，消费失败等待重试
     */
    public boolean consume(final List<ConsumerRecord<String, String>> records, @Nullable final KafkaBatchContext context) {
        try {
            return consumer.consume(records, context);
        } catch (Throwable tr) {
            LOGGER.error("consume failed, throwable: ", tr);
            return false;
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    public KafkaBatchConsumer getConsumer() {
        return consumer;
    }

    public int getThreadNum() {
        return threadNum;
    }

    /**
     * 设置线程数
     * 实际线程数 = CEIL(总行数 / CEIL(总行数 / 线程数))
     *
     * @param threadNum 线程数，线程数 小于 (线程池最大线程数 - 1)
     * @return the {@link KafkaBatchListener} instance
     */
    public KafkaBatchListener setThreadNum(int threadNum) {
        Assert.isTrue(threadNum > 0, "threadNum must be greater than 0");

        this.threadNum = threadNum;
        return this;
    }

}
