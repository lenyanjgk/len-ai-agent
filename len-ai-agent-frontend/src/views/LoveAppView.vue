<template>
  <div class="love-app">
    <chat-interface
      title="AI 恋爱大师"
      ai-name="恋爱大师"
      :chat-id="chatId"
      :on-send-message="handleSendMessage"
      :on-go-back="goBack"
    />
  </div>
</template>

<script>
import { onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import ChatInterface from '@/components/ChatInterface.vue';
import { connectToLoveAppSse } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';

export default {
  name: 'LoveAppView',
  components: {
    ChatInterface
  },
  setup() {
    const router = useRouter();
    const chatId = ref('');
    const currentEventSource = ref(null);
    
    // 获取或创建聊天 ID
    chatId.value = getOrCreateChatId('love_app_chat_id');
    
    // 返回主页
    const goBack = () => {
      router.push('/');
    };
    
    // 发送消息给后端
    const handleSendMessage = (message, updateResponse) => {
      return new Promise((resolve) => {
        // 如果有现存的连接，先关闭
        if (currentEventSource.value) {
          currentEventSource.value.close();
        }
        
        // 用于累积SSE消息的变量
        let accumulatedResponse = '';
        
        // 建立新的 SSE 连接
        const eventSource = connectToLoveAppSse(
          message,
          chatId.value,
          (data) => {
            // 累加新收到的消息片段
            accumulatedResponse += data;
            // 更新聊天信息，使用累加后的完整消息
            updateResponse(accumulatedResponse);
          },
          (error) => {
            console.error('SSE连接错误:', error);
            resolve();
          }
        );
        
        // 当连接关闭时，解析promise
        eventSource.addEventListener('done', () => {
          resolve();
          eventSource.close();
        });
        
        // 保存当前连接，以便后续可以关闭
        currentEventSource.value = eventSource;
      });
    };
    
    // 组件卸载前关闭SSE连接
    onBeforeUnmount(() => {
      if (currentEventSource.value) {
        currentEventSource.value.close();
        currentEventSource.value = null;
      }
    });
    
    return {
      chatId,
      handleSendMessage,
      goBack
    };
  }
};
</script>

<style scoped>
.love-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-1);
}
</style> 