import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";

type Note = { id:number; title:string; contentMd:string; contentText:string; status:string };
type SummaryResp = { oneLine:string; paragraph:string; model:string; style:string; tokensPrompt:number; tokensOutput:number; createdAt?:string };

export default function NoteDetail(){
  const { id } = useParams();
  const [note, setNote] = useState<Note|null>(null);
  const [sum, setSum] = useState<SummaryResp|null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api(`/api/notes/${id}`).then(setNote);
    api(`/api/notes/${id}/summary`).then(setSum).catch(()=>setSum(null));
  }, [id]);

  async function summarize(style:"brief"|"detailed"){
    setLoading(true);
    await api(`/api/notes/${id}/summary`, { method:"POST", body: JSON.stringify({ style }) });
    const latest = await api(`/api/notes/${id}/summary`);
    setSum(latest); setLoading(false);
  }

  if(!note) return <div>로딩...</div>;
  return (
    <div>
      <h3>{note.title}</h3>
      <p><b>본문(텍스트):</b> {note.contentText}</p>
      <div style={{display:"flex", gap:8, margin:"8px 0"}}>
        <button onClick={()=>summarize("brief")} disabled={loading}>{loading?"요약중...":"요약(brief)"}</button>
        <button onClick={()=>summarize("detailed")} disabled={loading}>{loading?"요약중...":"요약(detailed)"}</button>
      </div>
      {sum && (
        <div style={{border:"1px solid #ddd", padding:12, borderRadius:8}}>
          <div style={{color:"#666"}}>{sum.model} · {sum.style} · {sum.tokensPrompt}/{sum.tokensOutput} tokens</div>
          <h4>{sum.oneLine}</h4>
          <pre style={{whiteSpace:"pre-wrap"}}>{sum.paragraph}</pre>
          {sum.createdAt && <div style={{color:"#888"}}>at {sum.createdAt}</div>}
        </div>
      )}
    </div>
  );
}
