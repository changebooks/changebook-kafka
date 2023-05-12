package io.github.changebooks.kafka;

import java.util.List;
import java.util.Map;

/**
 * 批量消费上下文
 *
 * @author changebooks@qq.com
 */
public interface KafkaBatchContext {
    /**
     * 获取全部消费上下文
     *
     * @return 消费上下文列表
     */
    List<KafkaContext> getContexts();

    /**
     * 新增消费上下文
     *
     * @param context 消费上下文
     */
    void addContext(KafkaContext context);

    /**
     * 获取全部属性
     *
     * @return 属性列表
     */
    Map<String, Object> getAttributes();

    /**
     * 获取一个属性
     *
     * @param key 属性名
     * @return 属性值
     */
    Object getAttribute(String key);

    /**
     * 新增一个属性
     *
     * @param key   属性名
     * @param value 属性值
     */
    void setAttribute(String key, Object value);

}
