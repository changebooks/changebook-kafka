# changebook-kafka
### 多线程消费、日志上下文

### pom.xml
```
<dependency>
  <groupId>io.github.changebooks</groupId>
  <artifactId>changebook-kafka</artifactId>
  <version>1.0.2</version>
</dependency>
```

### 拦截接收消息：从已接收消息获取日志信息，设置日志上下文
```
spring:
  kafka:
    consumer:
      properties:
        interceptor:
          classes: io.github.changebooks.kafka.LogConsumerInterceptor
```

### 拦截发送消息：从日志上下文获取日志信息，设置待发送消息
```
spring:
  kafka:
    producer:
      properties:
        interceptor:
          classes: io.github.changebooks.kafka.LogProducerInterceptor
```

### 配置
```
spring:
  kafka:
    bootstrap-servers: 127.0.0.1:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      batch-size: 6400
      properties:
        interceptor:
          classes: io.github.changebooks.kafka.LogProducerInterceptor
        linger:
          ms: 1000
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: demo
      client-id: demo
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 500
      properties:
        interceptor:
          classes: io.github.changebooks.kafka.LogConsumerInterceptor
    listener:
      ack-mode: manual
      type: batch
```

### 配置
```
@SpringBootApplication
public class Application {

    public static final String TOPIC = "topic001";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

### 发送消息
```
@Service
public class MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息
     *
     * @param value 消息内容
     */
    public void send(String value) throws ExecutionException, InterruptedException {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(Application.TOPIC, value);
        SendResult<String, String> sendResult = future.get();
        LOGGER.info("send trace, value: {}, thread: {}, sendResult: {}", value, Thread.currentThread().getId(), sendResult);
    }

    /**
     * 批量发送消息
     *
     * @param data 消息内容
     */
    public void send(List<String> data) throws ExecutionException, InterruptedException {
        List<ListenableFuture<SendResult<String, String>>> futures = new ArrayList<>();
        for (String value : data) {
            ListenableFuture<SendResult<String, String>> f = kafkaTemplate.send(Application.TOPIC, value);
            futures.add(f);
        }

        List<String> result = new ArrayList<>();
        for (ListenableFuture<SendResult<String, String>> f : futures) {
            result.add(f.get().getProducerRecord().value());
        }

        LOGGER.info("send trace, thread: {}, result: {}", Thread.currentThread().getId(), result);
    }

}
```

### 接收消息
```
@Service
public class MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    @KafkaListener(topics = Application.TOPIC)
    public void onListen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        LOGGER.info("onListen, record: {}", record);
        ack.acknowledge();
    }

}
```

### 批量接收消息，并行消费
```
@Service
public class MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    /**
     * 多余的空闲线程生存时间
     */
    public static final long KEEP_ALIVE_TIME = 1L;

    /**
     * 阻塞队列
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new SynchronousQueue<>();

    /**
     * 线程数
     */
    private static final int THREAD_NUM = 4;

    /**
     * 执行线程
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            THREAD_NUM + 1,        // 线程池核心线程数
            THREAD_NUM * 2 + 1,    // 线程池最大线程数
            KEEP_ALIVE_TIME,       // 多余的空闲线程生存时间
            TimeUnit.MILLISECONDS, // 时间单位
            WORK_QUEUE,            // 阻塞队列
            线程工厂,
            崩溃处理
    );

    /**
     * 消费接口
     */
    private final KafkaBatchConsumer consumer = this::onConsume;

    /**
     * 消息分页，并行消费
     */
    private final KafkaBatchListener batchListener = new KafkaBatchListener(EXECUTOR, consumer).setThreadNum(THREAD_NUM);

    @KafkaListener(topics = Application.TOPIC)
    public void onListen(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        List<String> values = records.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        LOGGER.info("onListen, thread: {}, records.size: {}, values: {}", Thread.currentThread().getId(), records.size(), values);

        KafkaBatchContext context = new KafkaBatchContextImpl();

        if (batchListener.listen(records, context)) {
            ack.acknowledge();
        } else {
            // retry use context
        }
    }

    /**
     * 执行消费
     *
     * @param records 消息列表
     * @param context 消费上下文
     * @return 消费成功提交消息？否则，消费失败等待重试，或抛出异常等待重试
     */
    public boolean onConsume(List<ConsumerRecord<String, String>> records, @Nullable final KafkaBatchContext context) {
        List<String> values = records.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        LOGGER.info("onConsume, thread: {}, records.size: {}, values: {}", Thread.currentThread().getId(), records.size(), values);

        return true;
    }

}
```
