/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE: string
  readonly VITE_DEFAULT_USER_ID: string
  // 필요한 값이 더 있으면 여기에 추가
  // readonly VITE_SOMETHING: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
