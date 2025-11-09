package com.app.gateway.api;

import com.app.common.model.Job; // 导入我们的实体
import com.app.common.model.JobStatus;
import com.app.gateway.api.dto.JobSubmitRequest; // 导入我们刚创建的 DTO
import com.app.gateway.repository.JobRepository; // 导入我们刚创建的 Repository

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobsController {

    // ““旧的”” KafkaTemplate 已经删掉了
    // 换成““新””的 JobRepository！
    @Autowired
    private JobRepository jobRepository;

    /**
     * M2 版本的 API：提交一个新任务并存入数据库
     */
    @PostMapping
    public ResponseEntity<Job> submitJob(@RequestBody JobSubmitRequest request) {
        
        System.out.println("🎉 [Gateway] M2: 收到新 Job 请求: " + request.getPayload());

        // 1. 把 DTO 转换成 数据库实体(Entity)
        Job newJob = new Job(request.getType(), request.getPayload());
        newJob.setStatus(JobStatus.PENDING); // 明确设置状态为“待处理”

        // 2. (核心) 保存到 MySQL 数据库！
        try {
            Job savedJob = jobRepository.save(newJob);

            // 3. 把““保存成功””并带有““新ID””的 Job 对象返回给前端
            return ResponseEntity.ok(savedJob);

        } catch (Exception e) {
            System.err.println("🚨 [Gateway] M2: 数据库保存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}