import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const systemPrefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;

export const useThemeStore = create(
  persist(
    (set) => ({
      theme: systemPrefersDark ? 'dark' : 'light',
      toggle: () => set((s) => ({ theme: s.theme === 'dark' ? 'light' : 'dark' })),
    }),
    { name: 'theme' },
  ),
);
