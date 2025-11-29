create table if not exists SPRING_AI_CHAT_MEMORY(
    conversation_id VARCHAR(36) not null,
    content TEXT not null,
    type VARCHAR(10) not null,
    timestamp TIMESTAMP not null,
    index SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX (conversation_id , timestamp) using BTREE
);
