package com.app.worker.repository;

import com.app.common.model.Job; // 导入我们的实体
import com.app.common.model.JobStatus; // 导入我们的状态枚举
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * 这就是 M2 的““魔法””查询！
     * Spring Data JPA 会自动帮我们生成这个 SQL。
     *
     * 它会查找所有状态为 "PENDING" 的任务，
     * 并且按照 "priority" (优先级，数字小的优先) 排序，
     * 然后按照 "createdAt" (创建时间，早的优先) 排序。
     *
     * "findFirst..." 意味着我们““只拿””符合条件的““第一个””任务。
     * "ForUpdate" 是为了““上锁””，防止多个 worker 抢到同一个任务 (虽然我们现在只有一个 worker，但这是专业做法)。
     */
    @Query(value = "SELECT * FROM jobs WHERE status = 'PENDING' ORDER BY priority ASC, created_at ASC LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<Job> findNextPendingJob();

}