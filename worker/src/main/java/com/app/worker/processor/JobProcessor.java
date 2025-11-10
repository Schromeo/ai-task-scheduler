package com.app.worker.processor; // 确保这个包名和你的文件夹路径一致

import com.app.common.model.Job;
import com.app.common.model.JobStatus;
import com.app.worker.repository.JobRepository; // 导入我们刚创建的 worker 仓库

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling; // ⬅️ **““新””导入！**
import org.springframework.scheduling.annotation.Scheduled;   // ⬅️ **““新””导入！**
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ⬅️ **““新””导入！**

import java.time.Instant;
import java.util.Optional;

@Service
@EnableScheduling // ⬅️ ““激活”” Spring 的““定时任务””功能！
public class JobProcessor {

    // (我们不再需要 @EnableKafka 和 @KafkaListener 了)

    @Autowired
    private JobRepository jobRepository; // ““注入””数据库遥控器

    /**
     * M2 的核心轮询方法！
     * @Scheduled(fixedRate = 5000) 告诉 Spring：
     * ““每隔 5000 毫秒（5秒钟），自动运行一次这个方法！””
     */
    @Scheduled(fixedRate = 5000)
    @Transactional // ⬅️ (非常重要！) 保证““拿任务””和““改状态””在一个数据库事务里完成
    public void processPendingJobs() {
        
        // 1. 去数据库““拿””一个活儿
        Optional<Job> jobToProcess = jobRepository.findNextPendingJob();

        // 2. 检查是不是真的““拿””到了
        if (jobToProcess.isEmpty()) {
            // 没拿到任务，很正常，打印一条安静的日志
            System.out.println("...[Worker] 没发现新任务，休息 5 秒钟...");
            return;
        }

        Job job = jobToProcess.get();

        // 3. （关键！）““抢占””这个任务，防止其他 worker（未来）也拿到它
        job.setStatus(JobStatus.RUNNING);
        job.setUpdatedAt(Instant.now());
        jobRepository.save(job);

        // 4. (模拟) 执行这个““复杂””的任务
        System.out.println(" ");
        System.out.println("🔥🔥🔥 [Worker] M2: ““开始处理”” Job ID: " + job.getId());
        System.out.println("      Payload: " + job.getPayload());

        try {
            // 模拟““干活””，比如花了 2 秒钟
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            // handle interruption
        }

        // 5. (关键！) ““干完活””，更新最终状态
        job.setStatus(JobStatus.COMPLETED);
        jobRepository.save(job);

        System.out.println("✅✅✅ [Worker] M2: ““完成”” Job ID: " + job.getId());
        System.out.println(" ");
    }
}