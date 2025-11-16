import React, { useState } from "react";
import { api, setUserId } from "../api/client";

export default function Login(){
  const [userId, setId] = useState("");
  const [nickname, setNick] = useState("");

  async function submit(){
    const body:any = {};
    if(userId) body.userId = Number(userId); else body.nickname = nickname || "guest";
    const resp = await api("/api/auth/login", { method:"POST", body: JSON.stringify(body) });
    setUserId(resp.userId);
    alert(`로그인 완료: USER_ID=${resp.userId}\n이제 X-USER-ID 헤더 자동 전송`);
  }

  return (
    <div style={{display:"grid", gap:8, maxWidth:400}}>
      <h3>로그인(Header-Auth)</h3>
      <input placeholder="기존 userId (선택)" value={userId} onChange={e=>setId(e.target.value)} />
      <input placeholder="닉네임 (신규 생성 시)" value={nickname} onChange={e=>setNick(e.target.value)} />
      <button onClick={submit}>로그인</button>
    </div>
  );
}
