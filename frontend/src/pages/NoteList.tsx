import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api/client";

type Note = { id:number; title:string; contentMd:string; contentText:string; status:string; tags?:string };

export default function NoteList(){
  const [items, setItems] = useState<Note[]>([]);
  const [sp, setSp] = useSearchParams();
  const [query, setQuery] = useState(sp.get("query") || "");
  const [tag, setTag] = useState(sp.get("tag") || "");

  async function load(q?:string, t?:string){
    const qs = new URLSearchParams();
    if (q && q.trim()) qs.set("query", q.trim());
    if (t && t.trim()) qs.set("tag", t.trim());
    qs.set("page","0"); qs.set("size","20");
    const page = await api(`/api/notes?${qs.toString()}`);
    setItems(page.content || []);
    setSp(qs); // URL 반영
  }

  useEffect(() => { load(query, tag); /* eslint-disable-next-line */ }, []);

  function onSubmit(e:React.FormEvent){
    e.preventDefault();
    load(query, tag);
  }

  return (
    <div>
      <h3>노트 목록</h3>

      <form onSubmit={onSubmit} style={{display:"flex", gap:8, margin:"8px 0"}}>
        <input
          placeholder="제목 검색 (query)"
          value={query}
          onChange={e=>setQuery(e.target.value)}
          style={{flex:1}}
        />
        <input
          placeholder="태그 (예: ai)"
          value={tag}
          onChange={e=>setTag(e.target.value)}
          style={{width:160}}
        />
        <button type="submit">검색</button>
        <Link to="/notes/new" style={{marginLeft:"auto"}}>새 노트</Link>
      </form>

      <ul style={{padding:0, listStyle:"none"}}>
        {items.map(n => (
          <li key={n.id} style={{padding:"8px 0", borderBottom:"1px solid #eee"}}>
            <div style={{display:"flex", justifyContent:"space-between", alignItems:"baseline"}}>
              <Link to={`/notes/${n.id}`} style={{fontWeight:600}}>{n.title}</Link>
              <span style={{color:"#888"}}>{n.status}</span>
            </div>
            {n.tags && n.tags.length>0 && (
              <div style={{marginTop:6, display:"flex", gap:6, flexWrap:"wrap"}}>
                {n.tags.split(",").filter(Boolean).map(t => (
                  <span
                    key={t}
                    onClick={()=>{ setTag(t); setQuery(""); load("", t); }}
                    style={{fontSize:12, padding:"2px 6px", border:"1px solid #ddd", borderRadius:12, cursor:"pointer"}}
                    title="이 태그로 필터"
                  >#{t}</span>
                ))}
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
