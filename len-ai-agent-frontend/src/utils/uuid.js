import { v4 as uuidv4 } from 'uuid';

export const generateUUID = () => {
  return uuidv4();
};

export const getOrCreateChatId = (key) => {
  let chatId = localStorage.getItem(key);
  if (!chatId) {
    chatId = generateUUID();
    localStorage.setItem(key, chatId);
  }
  return chatId;
}; 