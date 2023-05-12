package io.github.changebooks.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批量消费上下文的默认实现
 *
 * @author changebooks@qq.com
 */
public class KafkaBatchContextImpl implements KafkaBatchContext {
    /**
     * 消费上下文列表
     */
    private List<KafkaContext> contexts = Collections.synchronizedList(new ArrayList<>());

    /**
     * 属性列表
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>(4);

    @Override
    public List<KafkaContext> getContexts() {
        return contexts;
    }

    @Override
    public void addContext(KafkaContext context) {
        this.contexts.add(context);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

}
