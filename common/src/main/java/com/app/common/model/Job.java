package com.app.common.model;

import jakarta.persistence.Entity; // 导入 JPA 的 @Entity
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import jakarta.persistence.Column;

/**
 * 任务实体类 (对应数据库中的 "jobs" 表)
 */
@Entity
@Table(name = "jobs") // 明确指定表名
public class Job {

    @Id // 标记这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 告诉 MySQL 自动生成 ID (自增)
    private Long id;

    // 任务类型, e.g., "DataExtraction", "EmailReport"
    @Column(nullable = false) // 标记为“不能为空”
    private String type;

    // 任务状态 (使用我们刚创建的枚举)
    @Enumerated(EnumType.STRING) // 告诉 JPA 把枚举存成字符串 (e.g., "PENDING")
    @Column(nullable = false)
    private JobStatus status;

    // 任务的“载荷”，比如 JSON 字符串。
    // @Lob 告诉 JPA 这是一个“大对象”(Large Object)，
    // 在 MySQL 里会对应 LONGTEXT 类型，可以存很多内容。
    @Lob
    @Column(nullable = false)
    private String payload;

    // 优先级 (数字越小，优先级越高)
    private int priority;

    // 最大重试次数
    private int maxAttempts = 3; // 默认给 3 次

    // 当前已尝试次数
    private int currentAttempt = 0;

    // 任务创建时间 (我们会在创建时自动设置)
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // 任务最后更新时间
    @Column(nullable = false)
    private Instant updatedAt;

    // (JPA 需要一个无参构造函数)
    public Job() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = JobStatus.PENDING; // 默认状态
        this.priority = 10; // 默认优先级
    }

    // 我们可以创建一个方便的构造函数
    public Job(String type, String payload) {
        this(); // 调用上面的无参构造函数来设置默认值
        this.type = type;
        this.payload = payload;
    }

    // --- 下面是标准的 Getter 和 Setter ---
    // (在 Java 中，这些是必须的，JPA 和其他框架会用到它们)

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

    // (可选) 增加一个在更新时自动修改时间的注解
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}