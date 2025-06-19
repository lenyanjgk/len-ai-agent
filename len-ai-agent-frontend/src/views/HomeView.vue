<template>
  <div class="home-view">
    <div class="welcome-container">
      <div class="welcome-logo">
        <icon-robot :style="{ fontSize: '64px', color: '#165DFF' }" />
      </div>
      <h1 class="welcome-title">欢迎使用AI聊天应用</h1>
      <div class="welcome-description">
        请从左侧菜单选择一个AI助手开始对话，或直接点击下方按钮
      </div>
      
      <div class="ai-cards">
        <a-card class="ai-card" :style="{ width: '320px' }" hoverable @click="goToLoveApp">
          <template #cover>
            <div class="card-cover love-cover">
              <icon-heart :style="{ fontSize: '40px', color: '#FF7D00' }" />
            </div>
          </template>
          <template #title>AI 恋爱大师</template>
          <template #extra>
            <a-button type="text">
              <template #icon>
                <icon-right />
              </template>
            </a-button>
          </template>
          <a-typography-paragraph>
            你的专属恋爱顾问，提供情感、约会和关系方面的建议与支持。
          </a-typography-paragraph>
        </a-card>
        
        <a-card class="ai-card" :style="{ width: '320px' }" hoverable @click="goToManusApp">
          <template #cover>
            <div class="card-cover manus-cover">
              <icon-bulb :style="{ fontSize: '40px', color: '#0FC6C2' }" />
            </div>
          </template>
          <template #title>AI 超级智能体</template>
          <template #extra>
            <a-button type="text">
              <template #icon>
                <icon-right />
              </template>
            </a-button>
          </template>
          <a-typography-paragraph>
            强大的智能助手，可以回答问题、提供建议、协助创作和分析信息。
          </a-typography-paragraph>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import { 
  IconRobot, 
  IconHeart, 
  IconBulb,
  IconRight
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'HomeView',
  components: {
    IconRobot,
    IconHeart,
    IconBulb,
    IconRight
  },
  setup() {
    const router = useRouter();
    
    const goToLoveApp = () => {
      router.push('/love-app');
    };
    
    const goToManusApp = () => {
      router.push('/manus-app');
    };
    
    return {
      goToLoveApp,
      goToManusApp
    };
  }
}
</script>

<style scoped>
.home-view {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background-color: var(--color-bg-1);
}

.welcome-container {
  max-width: 800px;
  width: 100%;
  text-align: center;
}

.welcome-logo {
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 16px;
}

.welcome-description {
  font-size: 16px;
  color: var(--color-text-2);
  margin-bottom: 40px;
}

.ai-cards {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.ai-card {
  transition: all 0.2s ease-in-out;
}

.ai-card:hover {
  transform: translateY(-5px);
}

.card-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.love-cover {
  background-color: rgba(255, 125, 0, 0.1);
}

.manus-cover {
  background-color: rgba(15, 198, 194, 0.1);
}
</style> 