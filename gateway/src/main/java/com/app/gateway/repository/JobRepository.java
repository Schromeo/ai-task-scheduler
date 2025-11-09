package com.app.gateway.repository;

import com.app.common.model.Job; // 导入我们 common 模块里的 Job
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository 告诉 Spring 这是一个数据仓库
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // JpaRepository<Job, Long> 的意思是：
    // "这是一个管理 Job 实体的仓库，Job 的主键(Id)类型是 Long"
    
    // Spring Data JPA 会自动为我们提供 save(), findById(), findAll() 等方法
    // 我们什么都不用写！
}