package com.app.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;

/**
 * 任务实体类 (对应数据库中的 "jobs" 表)
 */
@Entity
@Table(name = "jobs") // 明确指定表名
public class Job {

    @Id // 标记这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 告诉 MySQL 自动生成 ID (自增)
    private Long id;

    @Column(nullable = false) 
    private String type;

    @Enumerated(EnumType.STRING) // 告诉 JPA 把枚举存成字符串 (e.g., "PENDING")
    @Column(nullable = false)
    private JobStatus status;

    @Lob
    @Column(nullable = false)
    private String payload;

    private int priority;

    private int maxAttempts = 3; // 默认给 3 次

    private int currentAttempt = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // ⬇️⬇️ 【M2.5 新增字段】 ⬇️⬇️
    // 租约到期时间。
    // 当 worker 拿起任务时，会把这个时间设为 "now + 5 minutes"
    // 如果 worker 崩溃，这个时间戳会过期
    @Column(nullable = true) // PENDING 状态时可以为 null
    private Instant leaseExpiresAt;
    // ⬆️⬆️ 【M2.5 新增字段】 ⬆️⬆️


    // (JPA 需要一个无参构造函数)
    public Job() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = JobStatus.PENDING; // 默认状态
        this.priority = 10; // 默认优先级
        this.leaseExpiresAt = null; // ⬅️ 【M2.5 新增】
    }

    // 我们可以创建一个方便的构造函数
    public Job(String type, String payload) {
        this(); // 调用上面的无参构造函数来设置默认值
        this.type = type;
        this.payload = payload;
    }

    // --- 下面是标准的 Getter 和 Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getCurrentAttempt() {
        return currentAttempt;
    }

    public void setCurrentAttempt(int currentAttempt) {
        this.currentAttempt = currentAttempt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ⬇️⬇️ 【M2.5 新增 Get/Set】 ⬇️⬇️
    public Instant getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    public void setLeaseExpiresAt(Instant leaseExpiresAt) {
        this.leaseExpiresAt = leaseExpiresAt;
    }
    // ⬆️⬆️ 【M2.5 新增 Get/Set】 ⬆️⬆️
    
    // (可选) 增加一个在更新时自动修改时间的注解
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}