import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../api/client";

type Note = {
  id: number;
  title: string;
  contentMd: string;
  contentText: string;
  status: string;
  tags?: string;
};

type SummaryResp = {
  oneLine: string;
  paragraph: string;
  model: string;
  style: "brief" | "detailed";
  tokensPrompt: number;
  tokensOutput: number;
  createdAt?: string;
  cost?: number;
};

export default function NoteDetail() {
  const { id } = useParams();
  const [note, setNote] = useState<Note | null>(null);
  const [sum, setSum] = useState<SummaryResp | null>(null);
  const [loading, setLoading] = useState(false);

  // 태그 편집 상태
  const [editingTags, setEditingTags] = useState(false);
  const [tags, setTags] = useState("");
  const [flash, setFlash] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const n = await api(`/api/notes/${id}`);
        setNote(n);
        setTags(n.tags || "");
      } catch (e: any) {
        setErr(e?.message || "노트 로딩 실패");
      }
      try {
        const s = await api(`/api/notes/${id}/summary`);
        setSum(s);
      } catch {
        // 요약이 아직 없을 수 있음 → 무시
      }
    })();
  }, [id]);

  async function summarize(style: "brief" | "detailed") {
    if (!id) return;
    setLoading(true);
    setErr(null);
    try {
      await api(`/api/notes/${id}/summary`, {
        method: "POST",
        body: JSON.stringify({ style }),
      });
      // 간단 폴링(요약이 저장될 시간을 조금 기다렸다가 가져오기)
      for (let i = 0; i < 3; i++) {
        try {
          const latest = await api(`/api/notes/${id}/summary`);
          if (latest) {
            setSum(latest);
            break;
          }
        } catch {}
        await new Promise((r) => setTimeout(r, 700));
      }
    } catch (e: any) {
      setErr(e?.message || "요약 생성 실패");
    } finally {
      setLoading(false);
    }
  }

  async function saveTags() {
    if (!id) return;
    setErr(null);
    try {
      await api(`/api/notes/${id}`, {
        method: "PATCH",
        body: JSON.stringify({ tags }),
      });
      const n = await api(`/api/notes/${id}`);
      setNote(n);
      setEditingTags(false);
      setFlash("태그가 저장되었습니다.");
      setTimeout(() => setFlash(null), 1500);
    } catch (e: any) {
      setErr(e?.message || "태그 저장 실패");
    }
  }

  if (err) {
    return (
      <div>
        <p style={{ color: "#c00" }}>{err}</p>
        <Link to="/">← 목록으로</Link>
      </div>
    );
  }

  if (!note) return <div>로딩...</div>;

  return (
    <div>
      <div style={{ display: "flex", alignItems: "baseline", gap: 12 }}>
        <h3 style={{ margin: 0 }}>{note.title}</h3>
        <span style={{ color: "#888" }}>{note.status}</span>
        <Link to="/" style={{ marginLeft: "auto" }}>
          목록
        </Link>
      </div>

      {/* 본문 요약용 텍스트 */}
      <section style={{ marginTop: 12 }}>
        <b>본문(텍스트):</b>
        <p style={{ marginTop: 6 }}>{note.contentText}</p>
      </section>

      {/* 태그 표시/편집 */}
      <section style={{ marginTop: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <b>태그:</b>
          {!editingTags ? (
            <>
              {note.tags && note.tags.length > 0 ? (
                <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                  {note.tags
                    .split(",")
                    .filter(Boolean)
                    .map((t) => (
                      <span
                        key={t}
                        style={{
                          fontSize: 12,
                          padding: "2px 6px",
                          border: "1px solid #ddd",
                          borderRadius: 12,
                        }}
                        title={t}
                      >
                        #{t}
                      </span>
                    ))}
                </div>
              ) : (
                <span style={{ color: "#888" }}>없음</span>
              )}
              <button
                onClick={() => {
                  setTags(note.tags || "");
                  setEditingTags(true);
                }}
                style={{ marginLeft: 8 }}
              >
                태그 수정
              </button>
            </>
          ) : (
            <div style={{ display: "flex", gap: 8, width: "100%" }}>
              <input
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                placeholder="쉼표구분: ai,spring"
                style={{ flex: 1 }}
              />
              <button onClick={saveTags}>저장</button>
              <button
                onClick={() => {
                  setEditingTags(false);
                  setTags(note.tags || "");
                }}
                type="button"
              >
                취소
              </button>
            </div>
          )}
        </div>
        {flash && <div style={{ color: "#0a0", marginTop: 6 }}>{flash}</div>}
      </section>

      {/* 요약 버튼 */}
      <section style={{ marginTop: 20, display: "flex", gap: 8 }}>
        <button onClick={() => summarize("brief")} disabled={loading}>
          {loading ? "요약중..." : "요약(brief)"}
        </button>
        <button onClick={() => summarize("detailed")} disabled={loading}>
          {loading ? "요약중..." : "요약(detailed)"}
        </button>
      </section>

      {/* 요약 결과 패널 */}
      {sum && (
        <section
          style={{
            marginTop: 12,
            border: "1px solid #ddd",
            padding: 12,
            borderRadius: 8,
          }}
        >
          <div style={{ color: "#666", marginBottom: 4 }}>
            {sum.model} · {sum.style} · {sum.tokensPrompt}/{sum.tokensOutput}{" "}
            tokens {typeof sum.cost === "number" ? `· ₩${sum.cost}` : ""}
          </div>
          <h4 style={{ margin: "6px 0" }}>{sum.oneLine}</h4>
          <pre style={{ whiteSpace: "pre-wrap", margin: 0 }}>{sum.paragraph}</pre>
          {sum.createdAt && (
            <div style={{ color: "#888", marginTop: 6 }}>at {sum.createdAt}</div>
          )}
        </section>
      )}
    </div>
  );
}
