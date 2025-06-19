<template>
  <div class="chat-container">
    <div class="chat-header">
      <div class="header-content">
        <div class="title-area">
          <a-button v-if="onGoBack" type="text" shape="circle" @click="onGoBack" class="back-button">
            <template #icon><icon-left /></template>
          </a-button>
          <h2>{{ title }}</h2>
          <a-tag size="small" color="arcoblue">{{ aiName }}</a-tag>
        </div>
        <div class="chat-id">会话ID: {{ chatId }}</div>
      </div>
      <a-button type="outline" status="danger" class="clear-btn" @click="clearMessages" size="small">
        <template #icon><icon-delete /></template>
        清除对话
      </a-button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-chat">
        <a-empty description="开始新的对话吧！">
          <template #image>
            <div class="empty-icon">
              <icon-robot :style="{ fontSize: '64px', color: '#3370FF' }" />
            </div>
          </template>
          <template #extra>
            <p class="empty-text">我是智慧客 AI，很高兴见到你！</p>
            <p class="empty-subtext">我可以帮你写代码、读文件、写作与创意内容，请把你的任务交给我吧～</p>
          </template>
        </a-empty>
      </div>
      <chat-message
        v-for="(message, index) in messages"
        :key="index"
        :text="shouldApplyTypingEffect(message, index) ? displayText : message.content"
        :is-user="message.isUser"
        :sender-name="message.isUser ? '我' : aiName"
        :timestamp="message.timestamp"
      />
      <div v-if="isTyping" class="typing-indicator">
        <a-spin dot />
        <span class="typing-text">{{ aiName }}正在回复...</span>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-container">
        <a-input-search
          v-model="userInput"
          placeholder="发送消息给智慧客 AI..."
          search-button
          :loading="loading"
          :disabled="loading"
          ref="messageInput"
          @search="sendMessage"
          allow-clear
        >
          <template #button-icon>
            <icon-send />
          </template>
          <template #button-default>
            发送
          </template>
        </a-input-search>
        <div class="input-hint">
          <a-typography-text type="secondary">Shift + Enter 换行，Enter 发送</a-typography-text>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue';
import ChatMessage from './ChatMessage.vue';
import { 
  IconRobot, 
  IconDelete,
  IconSend,
  IconLeft
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'ChatInterface',
  components: {
    ChatMessage,
    IconRobot,
    IconDelete,
    IconSend,
    IconLeft
  },
  props: {
    title: {
      type: String,
      required: true
    },
    aiName: {
      type: String,
      default: 'AI'
    },
    chatId: {
      type: String,
      required: true
    },
    onSendMessage: {
      type: Function,
      required: true
    },
    onGoBack: {
      type: Function,
      default: null
    }
  },
  setup(props) {
    const messages = ref([]);
    const userInput = ref('');
    const loading = ref(false);
    const messagesContainer = ref(null);
    const messageInput = ref(null);
    const isTyping = ref(false);
    const displayText = ref('');
    const fullText = ref('');
    const typingSpeed = ref(30); // 打字速度，毫秒/字符
    let typingTimer = null;
    const currentTypingMessageIndex = ref(-1); // 跟踪正在打字效果中的消息索引

    // 从localStorage加载历史消息
    const loadMessages = () => {
      const savedMessages = localStorage.getItem(`chat_messages_${props.chatId}`);
      if (savedMessages) {
        messages.value = JSON.parse(savedMessages);
      }
    };

    // 保存消息到localStorage
    const saveMessages = () => {
      localStorage.setItem(`chat_messages_${props.chatId}`, JSON.stringify(messages.value));
    };

    // 清除所有消息
    const clearMessages = () => {
      if (confirm('确定要清除所有对话记录吗？')) {
        // 清空消息数组
        messages.value = [];
        // 从localStorage中删除保存的消息
        localStorage.removeItem(`chat_messages_${props.chatId}`);
        // 重置打字状态
        isTyping.value = false;
        displayText.value = '';
        fullText.value = '';
        clearTimeout(typingTimer);
      }
    };

    // 打字机效果
    const typeNextChar = () => {
      if (currentTypingMessageIndex.value >= 0 && currentTypingMessageIndex.value < messages.value.length) {
        const currentMessage = messages.value[currentTypingMessageIndex.value];
        if (!currentMessage.isUser && displayText.value.length < fullText.value.length) {
        // 显示下一个字符
        displayText.value = fullText.value.substring(0, displayText.value.length + 1);
        // 继续打字
        typingTimer = setTimeout(typeNextChar, typingSpeed.value);
      } else {
        // 打字结束
        isTyping.value = false;
          currentTypingMessageIndex.value = -1;
        clearTimeout(typingTimer);
        }
      }
    };

    // 开始打字效果
    const startTyping = (text, messageIndex) => {
      // 保存完整文本
      fullText.value = text;
      // 重置显示文本
      displayText.value = '';
      // 设置为打字中状态
      isTyping.value = true;
      // 记录正在打字的消息索引
      currentTypingMessageIndex.value = messageIndex !== undefined ? messageIndex : messages.value.length - 1;
      // 开始打字
      clearTimeout(typingTimer);
      typingTimer = setTimeout(typeNextChar, typingSpeed.value);
    };

    // 滚动到底部
    const scrollToBottom = async () => {
      await nextTick();
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
      }
    };

    // 发送消息
    const sendMessage = async () => {
      const text = userInput.value.trim();
      if (!text || loading.value) return;

      // 添加用户消息
      const userMessage = {
        content: text,
        isUser: true,
        timestamp: Date.now()
      };
      messages.value.push(userMessage);
      saveMessages();
      scrollToBottom();

      // 清空输入框
      userInput.value = '';
      loading.value = true;

      try {
        // 创建AI响应占位消息
        const aiMessage = {
          content: '',
          isUser: false,
          timestamp: Date.now()
        };
        messages.value.push(aiMessage);

        // 调用父组件提供的发送消息函数
        const updateResponse = (text, isNewStep = false) => {
          // 检查是否为PDF下载消息
          const isPdfMessage = text.includes('[点击下载PDF](') || 
                             text.includes('- 下载链接:') || 
                             text.includes('PDF生成成功');
          
          if (isNewStep && text.trim()) {
            // 如果是新步骤，创建新消息
            const newAiMessage = {
              content: text,
              isUser: false,
              timestamp: Date.now()
            };
            messages.value.push(newAiMessage);
            // 不对新步骤应用打字机效果，直接显示内容
          } else {
            // 更新最后一条消息内容
            aiMessage.content = text;
            
            // 如果是PDF下载消息，不应用打字机效果
            if (isPdfMessage) {
              // 停止当前打字效果
              if (isTyping.value) {
                clearTimeout(typingTimer);
                isTyping.value = false;
                currentTypingMessageIndex.value = -1;
              }
            } else {
              // 如果当前不在打字状态，启动打字效果
              if (!isTyping.value) {
                startTyping(text, messages.value.length - 1);
              } else if (currentTypingMessageIndex.value === messages.value.length - 1) {
                // 如果正在对最后一条消息应用打字效果，更新完整文本
                fullText.value = text;
              }
            }
          }
          saveMessages();
          scrollToBottom();
        };

        // 调用父组件提供的消息处理函数
        await props.onSendMessage(text, updateResponse);
      } catch (error) {
        console.error('消息发送失败', error);
        // 添加错误消息
        messages.value.push({
          content: '消息发送失败，请稍后重试。',
          isUser: false,
          timestamp: Date.now()
        });
      } finally {
        loading.value = false;
        saveMessages();
        scrollToBottom();
        // 聚焦输入框
        messageInput.value?.$el?.querySelector('input')?.focus();
      }
    };

    // 监听消息变化，自动滚动到底部
    watch(messages, () => {
      scrollToBottom();
    }, { deep: true });

    // 组件挂载时加载消息
    onMounted(() => {
      loadMessages();
      scrollToBottom();
      messageInput.value?.$el?.querySelector('input')?.focus();
    });

    // 判断是否应该应用打字机效果
    const shouldApplyTypingEffect = (message, index) => {
      // 如果不是AI消息或者不在打字中，不应用效果
      if (message.isUser || !isTyping.value) {
        return false;
      }
      
      // 如果是最后一条消息且正在打字中
      if (index === messages.value.length - 1 && currentTypingMessageIndex.value === index) {
        // 如果消息包含PDF下载链接，不应用打字机效果
        const isPdfMessage = message.content.includes('[点击下载PDF](') || 
                            message.content.includes('- 下载链接:') ||
                            message.content.includes('PDF生成成功');
        
        return !isPdfMessage;
      }
      
      return false;
    };

    return {
      messages,
      userInput,
      loading,
      messagesContainer,
      messageInput,
      sendMessage,
      isTyping,
      displayText,
      clearMessages,
      shouldApplyTypingEffect
    };
  }
};
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 0;
  overflow: hidden;
  background-color: var(--color-bg-1);
}

.chat-header {
  padding: 12px 24px;
  background-color: rgba(22, 202, 157, 0.1);
  border-bottom: 1px solid rgba(22, 202, 157, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  flex: 1;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button {
  margin-right: 4px;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--color-text-1);
  font-weight: 500;
}

.chat-id {
  font-size: 0.8rem;
  color: var(--color-text-3);
  margin-top: 4px;
}

.clear-btn {
  margin-left: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: var(--color-bg-1);
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 24px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 24px;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 12px;
  color: var(--color-text-1);
}

.empty-subtext {
  font-size: 14px;
  color: var(--color-text-3);
  max-width: 400px;
  line-height: 1.6;
}

.chat-input {
  padding: 16px 24px;
  background-color: var(--color-bg-2);
  border-top: 1px solid var(--color-border);
}

.input-container {
  max-width: 768px;
  margin: 0 auto;
}

.input-hint {
  margin-top: 8px;
  text-align: right;
  font-size: 12px;
}

/* 打字指示器样式 */
.typing-indicator {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-top: 8px;
}

.typing-text {
  margin-left: 8px;
  color: var(--color-text-3);
  font-size: 14px;
}
</style> 