-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS lenai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE lenai;

-- 创建对话记忆表
CREATE TABLE IF NOT EXISTS chatmemory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_order INT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    message_json TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete BOOLEAN DEFAULT 0,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_conversation_order (conversation_id, message_order),
    INDEX idx_is_delete (is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

