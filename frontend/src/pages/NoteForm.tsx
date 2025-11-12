import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

export default function NoteForm(){
  const nav = useNavigate();
  const [title, setTitle] = useState("");
  const [contentMd, setContent] = useState("");

  async function submit(){
    const id = await api("/api/notes", { method:"POST", body: JSON.stringify({ title, contentMd })});
    nav(`/notes/${id}`);
  }
  return (
    <div>
      <h3>새 노트</h3>
      <div style={{display:"grid", gap:8}}>
        <input placeholder="제목" value={title} onChange={e=>setTitle(e.target.value)} />
        <textarea rows={10} placeholder="# 본문 (Markdown)"
          value={contentMd} onChange={e=>setContent(e.target.value)} />
        <button onClick={submit} disabled={!title || !contentMd}>저장</button>
      </div>
    </div>
  );
}
