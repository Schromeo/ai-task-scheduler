package com.app.worker.processor;

import com.app.common.model.Job;
import com.app.common.model.JobStatus;
import com.app.worker.repository.JobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit; // ⬅️ 确保导入这个
import java.util.Optional;

@Service
@EnableScheduling 
public class JobProcessor {

    @Autowired
    private JobRepository jobRepository;
    
    // 定义租约时长 (例如：5分钟)
    private static final int LEASE_DURATION_MINUTES = 5;

    /**
     * M2.5 健壮性升级！
     */
    @Scheduled(fixedRate = 5000) // 还是每 5 秒轮询一次
    @Transactional // 保证“拿”和“锁”是一个原子操作
    public void processPendingJobs() {
        
        // 1. 去数据库““拿””一个““可用””的活儿 (新任务 或 僵尸任务)
        //    我们传入““现在””的时刻
        Optional<Job> jobToProcess = jobRepository.findNextAvailableJob(Instant.now());

        if (jobToProcess.isEmpty()) {
            // 没拿到任务，很正常
            System.out.println("...[Worker] M2.5: 没发现新任务，休息 5 秒钟...");
            return;
        }

        Job job = jobToProcess.get();

        // 2. (核心！) ““抢占””这个任务
        try {
            // (1) 设置状态为 RUNNING
            job.setStatus(JobStatus.RUNNING);
            // (2) 增加重试次数
            job.setCurrentAttempt(job.getCurrentAttempt() + 1);
            // (3) 设置““租约””：5分钟后过期！
            job.setLeaseExpiresAt(Instant.now().plus(LEASE_DURATION_MINUTES, ChronoUnit.MINUTES));
            
            // (4) ““锁住””这个任务！(更新数据库)
            jobRepository.save(job);
            
            // 3. (模拟) 执行这个““复杂””的任务
            System.out.println(" ");
            System.out.println("🔥🔥🔥 [Worker] M2.5: “开始处理” Job ID: " + job.getId() + " (第 " + job.getCurrentAttempt() + " 次尝试)");
            System.out.println("      Payload: " + job.getPayload());

            // 模拟““干活””，比如花了 2 秒钟
            Thread.sleep(2000); 

            // 4. (关键！) ““干完活””，更新最终状态
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);

            System.out.println("✅✅✅ [Worker] M2.5: “完成” Job ID: " + job.getId());
            System.out.println(" ");

        } catch (Exception e) {
            // 如果““干活””时（比如 Thread.sleep）出错了...
            System.err.println("🚨 [Worker] M2.5: ““处理失败”” Job ID: " + job.getId() + " - " + e.getMessage());
            
            // 简单地标记为 FAILED 
            // 注意：因为我们设置了租约，如果这里失败了，
            // 5分钟后它会被 findNextAvailableJob() 重新捞出来
            // 这就是我们的““重试””机制！
            
            // 我们检查是否超过了最大重试次数
            if (job.getCurrentAttempt() >= job.getMaxAttempts()) {
                 job.setStatus(JobStatus.FAILED); // 彻底失败
                 System.err.println("🚨 [Worker] M2.5: Job ID: " + job.getId() + " 已达最大重试次数，标记为 FAILED。");
            } else {
                // 状态仍然是 RUNNING，但租约会过期，下次轮询会再次捡起它
                 System.out.println("...[Worker] M2.5: Job ID: " + job.getId() + " 将在租约到期后重试...");
            }
            jobRepository.save(job); // 保存 FAILED 状态或““等待重试””的 RUNNING 状态
        }
    }
}