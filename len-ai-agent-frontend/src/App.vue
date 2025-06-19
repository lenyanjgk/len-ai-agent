<template>
  <div class="app-container">
    <a-layout class="layout-container">
      <!-- 侧边栏 -->
      <a-layout-sider
        class="layout-sider"
        width="200"
        hide-trigger
        collapsible
        :collapsed="siderCollapsed"
      >
        <div class="sider-header">
          <div class="logo">
            <img src="@/assets/logo.svg" alt="AI应用" />
            <span v-if="!siderCollapsed">智慧客</span>
          </div>
        </div>
        
        <div class="sider-content">
          <div class="menu-container">
            <div class="menu-header">
              <span>会话列表</span>
              <a-button type="text" class="add-btn" @click="onAddChat">
                <template #icon><icon-plus /></template>
                新建会话
              </a-button>
            </div>
            
            <a-menu
              :default-selected-keys="selectedKeys"
              @menu-item-click="onMenuItemClick"
            >
              <a-menu-item key="home" class="menu-item">
                <template #icon><icon-message /></template>
                新会话 6/8 21:00
              </a-menu-item>
              <a-menu-item key="love-app" class="menu-item">
                <template #icon><icon-heart /></template>
                AI 恋爱大师
              </a-menu-item>
              <a-menu-item key="manus-app" class="menu-item">
                <template #icon><icon-robot /></template>
                AI 超级智能体
              </a-menu-item>
            </a-menu>
            
            <div class="settings-section">
              <a-menu @menu-item-click="onMenuItemClick">
                <a-menu-item key="settings" class="menu-item">
                  <template #icon><icon-settings /></template>
                  设置
                </a-menu-item>
              </a-menu>
            </div>
          </div>
        </div>
      </a-layout-sider>

      <!-- 内容区域 -->
      <a-layout>
        <a-layout-content class="layout-content">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  IconHeart, 
  IconRobot, 
  IconSettings,
  IconPlus,
  IconMessage
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'App',
  components: {
    IconHeart,
    IconRobot,
    IconSettings,
    IconPlus,
    IconMessage
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const siderCollapsed = ref(false);
    
    const selectedKeys = computed(() => {
      const path = route.path;
      if (path === '/') return ['home'];
      if (path === '/love-app') return ['love-app'];
      if (path === '/manus-app') return ['manus-app'];
      if (path === '/settings') return ['settings'];
      return ['home'];
    });
    
    const onMenuItemClick = (key) => {
      switch(key) {
        case 'home':
          router.push('/');
          break;
        case 'love-app':
          router.push('/love-app');
          break;
        case 'manus-app':
          router.push('/manus-app');
          break;
        case 'settings':
          // 设置功能还未实现
          break;
      }
    };
    
    const onAddChat = () => {
      router.push('/');
    };
    
    return {
      siderCollapsed,
      selectedKeys,
      onMenuItemClick,
      onAddChat
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--color-bg-1);
  color: var(--color-text-1);
}

.app-container {
  height: 100vh;
  width: 100%;
}

.layout-container {
  height: 100%;
  width: 100%;
}

.layout-sider {
  background-color: var(--color-bg-2);
  border-right: 1px solid var(--color-border);
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: none;
}

.sider-header {
  padding: 16px;
  height: 64px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--color-border);
}

.logo {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
}

.logo img {
  width: 24px;
  height: 24px;
  margin-right: 10px;
}

.sider-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.menu-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.menu-header {
  padding: 16px 16px 8px 16px;
  font-size: 12px;
  color: var(--color-text-3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-item {
  margin: 4px 0;
}

.settings-section {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
  margin-bottom: 16px;
}

.add-btn {
  font-size: 12px;
  padding: 0 8px;
  height: 24px;
}

.layout-content {
  padding: 0;
  height: 100%;
  background-color: var(--color-bg-1);
}
</style>
