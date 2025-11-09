package com.app.common.model;

/**
 * 任务的执行状态
 */
public enum JobStatus {
    PENDING,  // 待处理 (刚创建)
    ASSIGNED, // 已分配 (调度器已抓取，但 worker 未开始)
    RUNNING,  // 执行中 (worker 正在执行)
    COMPLETED, // 已完成
    FAILED,    // 已失败 (所有重试均告终)
    CANCELLED  // 已取消
}