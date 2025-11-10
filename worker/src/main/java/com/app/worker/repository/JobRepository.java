package com.app.worker.repository;

import com.app.common.model.Job;
import com.app.common.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ⬅️ 确保导入了这个
import org.springframework.stereotype.Repository;

import java.time.Instant; // ⬅️ 确保导入了这个
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * M2.5 健壮性升级！
     *
     * 我们现在查找两种任务：
     * 1. 状态为 PENDING 的 (新任务)
     * 2. 状态为 RUNNING 并且 租约已过期 (lease_expires_at < :now) 的 (僵尸任务)
     *
     * 依然按照 优先级 和 创建时间 排序。
     * "LIMIT 1 FOR UPDATE SKIP LOCKED" 是““数据库行锁””，
     * 它能确保在““集群””环境中（多个 worker 实例），
     * 只有一个 worker 能““抢””到这个任务，别的 worker 会跳过它。
     */
    @Query(value = "SELECT * FROM jobs " +
                   "WHERE (status = 'PENDING' OR (status = 'RUNNING' AND lease_expires_at < :now)) " +
                   "ORDER BY priority ASC, created_at ASC " +
                   "LIMIT 1 FOR UPDATE SKIP LOCKED", 
           nativeQuery = true)
    Optional<Job> findNextAvailableJob(@Param("now") Instant now); // ⬅️ 我们把 "now" 作为参数传进去

}