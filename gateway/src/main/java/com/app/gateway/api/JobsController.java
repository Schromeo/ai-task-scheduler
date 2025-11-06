// 告诉 Java，这个文件属于哪个“包”
package com.app.gateway.api;

// --- 导入 Spring Boot 需要的“工具” ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController 告诉 Spring：“这是一个 API 控制器，请把它交给我管理！”
@RestController
// @RequestMapping("/jobs") 告诉 Spring：“这个类里所有的 API，都在 /jobs 路径下”
@RequestMapping("/jobs")
public class JobsController {

    // (这是““魔法””的开始：依赖注入)
    // 我们告诉 Spring：“请把‘Kafka 发送器’ (KafkaTemplate) 给我，我要用！”
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 这是我们 M1 的核心 Topic (主题)
    private static final String JOB_TOPIC = "topic.jobs";

    /**
     * M1 的核心 API：提交一个新任务
     * * @PostMapping 告诉 Spring：“当有人用 POST 方法访问 /jobs 时，请调用这个函数！”
     */
    @PostMapping
    public ResponseEntity<String> submitJob(@RequestBody String jobPayload) {
        
        // 1. (调试) 先在控制台打印一下，我们收到了什么
        System.out.println("🎉 [Gateway] 收到了一个新 Job 请求: " + jobPayload);

        // 2. (核心) 把这个消息“发送”到 Kafka 的 topic.jobs 主题
        //    (我们 M1 先简单点，直接把收到的“字符串”发出去)
        try {
            kafkaTemplate.send(JOB_TOPIC, jobPayload);
        } catch (Exception e) {
            // 如果 Kafka 挂了 (比如 Docker 没开)，打印错误
            System.err.println("🚨 [Gateway] 发送 Kafka 失败: " + e.getMessage());
            // 告诉“顾客”(客户端)，服务器内部出错了
            return ResponseEntity.internalServerError().body("发送 Kafka 失败: " + e.getMessage());
        }

        // 3. 告诉“顾客”(客户端)：“点餐成功！这是你的回执”
        return ResponseEntity.ok("Job 已收到并发送到 Kafka: " + jobPayload);
    }
}