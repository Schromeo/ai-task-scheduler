// 告诉 Java，这个文件属于哪个“包”
package com.app.worker.consumer;

// --- 导入 Spring Boot 需要的“工具” ---
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

// @Service 告诉 Spring：“这是一个‘“服务’”’组件，请把它交给我管理！”
@Service
public class JobListener {

    // 这是我们 M1 的核心 Topic (主题)
    private static final String JOB_TOPIC = "topic.jobs";

    /**
     * M1 的““核心””！
     * @KafkaListener 告诉 Spring：“请““死死地””盯住 'topic.jobs'，
     * 只要一有消息，立刻““抢””过来，
     * 然后““塞””进这个函数的 message 参数里！”
     */
    @KafkaListener(topics = JOB_TOPIC, groupId = "scheduler-cg")
    public void handleJob(String message) {
        
        // ！！““M1 胜利的欢呼””！！
        // 我们在““Worker 的控制台””打印我们收到的消息！
        System.out.println(" "); // (打印个空行，为了““显眼””)
        System.out.println("✅✅✅ [Worker] ““球””接到了！Job 内容是: " + message);
        System.out.println(" "); // (再打印个空行)

        // (在 M2 中，我们才会在这里““真正””地““执行””任务)
    }
}