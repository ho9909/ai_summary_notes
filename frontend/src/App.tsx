import React from "react";
import { Link, Outlet } from "react-router-dom";

export default function App() {
  return (
    <div style={{maxWidth: 880, margin: "24px auto", fontFamily: "system-ui, sans-serif"}}>
      <header style={{display:"flex", justifyContent:"space-between", alignItems:"center"}}>
        <h2>AI Summary Notes</h2>
        <nav style={{display:"flex", gap:12}}>
          <Link to="/">목록</Link>
          <Link to="/notes/new">새 노트</Link>
        </nav>
      </header>
      <hr/>
      <Outlet/>
    </div>
  );
}
