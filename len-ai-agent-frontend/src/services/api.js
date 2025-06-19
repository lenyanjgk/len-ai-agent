import axios from 'axios';
import { EventSourcePolyfill } from 'event-source-polyfill';
// 如果上面的导入方式不起作用，可以改用原生 EventSource
const EventSource = window.EventSource || EventSourcePolyfill;

const API_BASE_URL = 'http://localhost:8102/api';

// 创建axios实例
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// AI恋爱大师应用的SSE连接
export const connectToLoveAppSse = (message, chatId, onMessage, onError) => {
  const url = `${API_BASE_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`;
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    if (onMessage && event.data) {
      onMessage(event.data);
    }
  };
  
  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };
  
  return eventSource;
};

// AI超级智能体应用的SSE连接
export const connectToManusChat = (message, onMessage, onError) => {
  const url = `${API_BASE_URL}/ai/manus/chat?message=${encodeURIComponent(message)}`;
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    if (onMessage && event.data) {
      onMessage(event.data);
    }
  };
  
  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };
  
  return eventSource;
}; 