package com.lenyan.lenaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lenyan.lenaiagent.domain.ChatMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lenyan
 * @since 2025-04-29
 */
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemory> {

    /**
     * 获取最大消息序号
     */
    @Select("SELECT MAX(message_order) FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0")
    Integer getMaxOrder(@Param("conversationId") String conversationId);

    /**
     * 获取会话消息数量
     */
    @Select("SELECT COUNT(*) FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0")
    int getMessageCount(@Param("conversationId") String conversationId);

    /**
     * 逻辑删除会话消息
     */
    @Update("UPDATE chatmemory SET is_delete = 1, update_time = NOW() WHERE conversation_id = #{conversationId} AND is_delete = 0")
    int logicalDeleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 获取最近消息，按消息顺序降序
     */
    @Select("SELECT * FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{limit}")
    List<ChatMemory> getLatestMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);

    /**
     * 分页获取消息
     */
    @Select("SELECT * FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{pageSize} OFFSET #{offset}")
    List<ChatMemory> getMessagesPaginated(@Param("conversationId") String conversationId,
            @Param("pageSize") int pageSize, @Param("offset") int offset);

    /**
     * 获取指定偏移和数量的消息
     */
    @Select("SELECT * FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMemory> getMessagesWithOffset(@Param("conversationId") String conversationId, @Param("limit") int limit,
            @Param("offset") int offset);
}
