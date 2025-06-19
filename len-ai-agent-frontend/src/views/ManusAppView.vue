<template>
  <div class="manus-app">
    <chat-interface
      title="AI 超级智能体"
      ai-name="超级智能体"
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
import { connectToManusChat } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';

export default {
  name: 'ManusAppView',
  components: {
    ChatInterface
  },
  setup() {
    const router = useRouter();
    const chatId = ref('');
    const currentEventSource = ref(null);
    const currentSteps = ref([]); // 添加步骤状态变量
    
    // 获取或创建聊天 ID
    chatId.value = getOrCreateChatId('manus_app_chat_id');
    
    // 返回主页
    const goBack = () => {
      router.push('/');
    };
    
    // 解析步骤函数
    const parseSteps = (text) => {
      // 使用正则表达式匹配 "Step X:" 格式
      const stepPattern = /Step \d+: 工具\[\w+\].*?(?=Step \d+:|$)/gs;
      
      // 尝试找到所有步骤
      let matches;
      try {
        matches = Array.from(text.matchAll(stepPattern));
      } catch (e) {
        console.error('正则匹配错误:', e);
        return [text];
      }
      
      // 如果没有匹配到步骤，检查是否包含部分步骤格式
      if (matches.length === 0) {
        if (text.includes('Step') && text.includes('工具[')) {
          // 可能是不完整的步骤，返回原文本
          return [text];
        }
        
        // 检查非步骤格式的其他类型消息
        if (text.trim()) {
          return [text];
        }
        
        return [];
      }
      
      // 创建步骤数组并确保每个步骤都是完整的
      const steps = matches.map(match => match[0].trim());
      return steps;
    };
    
    // 发送消息给后端
    const handleSendMessage = (message, updateResponse) => {
      return new Promise((resolve) => {
        // 如果有现存的连接，先关闭
        if (currentEventSource.value) {
          currentEventSource.value.close();
        }
        
        // 重置步骤数组
        currentSteps.value = [];
        let accumulatedResponse = '';
        
        // 建立新的 SSE 连接
        const eventSource = connectToManusChat(
          message,
          (data) => {
            // 累加新收到的消息片段
            accumulatedResponse += data;
            
            // 调试日志
            console.log('Received data:', data);
            
            // 解析出步骤
            const steps = parseSteps(accumulatedResponse);
            
            // 调试日志
            console.log('Parsed steps:', steps);
            
            // 更新步骤数组
            if (steps.length > currentSteps.value.length) {
              // 有新的步骤
              for (let i = currentSteps.value.length; i < steps.length; i++) {
                console.log('Creating new step:', steps[i]);
                // 为每个新步骤创建一个新的响应
                updateResponse(steps[i], true); // true 表示这是一个新步骤
              }
              currentSteps.value = steps;
            } else if (steps.length > 0) {
              // 最后一个步骤有更新
              console.log('Updating last step:', steps[steps.length - 1]);
              updateResponse(steps[steps.length - 1], false); // false 表示更新现有步骤
              currentSteps.value = steps;
            }
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
.manus-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-1);
}
</style> 