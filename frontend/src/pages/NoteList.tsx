import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";

type Note = { id:number; title:string; contentMd:string; contentText:string; status:string; }

export default function NoteList(){
  const [items, setItems] = useState<Note[]>([]);
  useEffect(() => {
    api("/api/notes?page=0&size=20").then((p:any) => setItems(p.content || []));
  }, []);
  return (
    <div>
      <h3>노트 목록</h3>
      <ul>
        {items.map(n => (
          <li key={n.id} style={{margin:"8px 0"}}>
            <Link to={`/notes/${n.id}`}>{n.title}</Link>
            <span style={{marginLeft:8, color:"#888"}}>({n.status})</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
