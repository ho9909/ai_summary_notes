export const API = import.meta.env.VITE_API_BASE || "http://localhost:8080";

function currentUserId(): string {
  const v = localStorage.getItem("USER_ID");
  return v || (import.meta.env.VITE_DEFAULT_USER_ID || "1");
}

export async function api(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers || {});
  headers.set("X-USER-ID", currentUserId());
  if (!headers.has("Content-Type") && init.body) headers.set("Content-Type", "application/json");
  const res = await fetch(API + path, { ...init, headers });
  if (!res.ok) throw new Error(await res.text());
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
}
export function setUserId(id: number){ localStorage.setItem("USER_ID", String(id)); }
