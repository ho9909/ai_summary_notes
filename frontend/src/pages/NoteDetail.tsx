import React, { useEffect, useMemo, useRef, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../api/client";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

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

const STATUS_OPTIONS = ["DRAFT", "PUBLISHED"];
const AUTOSAVE_MS = 1000; // 디바운스 지연(ms)

export default function NoteDetail() {
  const { id } = useParams();
  const [note, setNote] = useState<Note | null>(null);
  const [sum, setSum] = useState<SummaryResp | null>(null);
  const [loading, setLoading] = useState(false);

  // 편집 모드 & 폼 상태
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState("");
  const [contentMd, setContentMd] = useState("");
  const [tags, setTags] = useState("");
  const [status, setStatus] = useState(STATUS_OPTIONS[0]);

  // 미리보기 표시
  const [showPreview, setShowPreview] = useState(true);

  // 상태 메시지
  const [flash, setFlash] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  // 자동 저장 상태
  const [autoSaving, setAutoSaving] = useState(false);
  const [lastSavedAt, setLastSavedAt] = useState<string | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastSavedSnapshotRef = useRef<string>(""); // 마지막으로 저장한 스냅샷

  // 스냅샷 생성(공백 정리)
  const currentSnapshot = useMemo(
    () =>
      JSON.stringify({
        title: title.trim(),
        contentMd: contentMd.trim(),
        tags: (tags || "").trim(),
        status,
      }),
    [title, contentMd, tags, status]
  );

  function nowTime() {
    const d = new Date();
    const HH = String(d.getHours()).padStart(2, "0");
    const MM = String(d.getMinutes()).padStart(2, "0");
    const SS = String(d.getSeconds()).padStart(2, "0");
    return `${HH}:${MM}:${SS}`;
  }

  async function loadAll() {
    setErr(null);
    const n = await api(`/api/notes/${id}`);
    setNote(n);
    // 폼 초기화
    setTitle(n.title || "");
    setContentMd(n.contentMd || "");
    setTags(n.tags || "");
    setStatus(n.status || STATUS_OPTIONS[0]);

    // 마지막 저장 스냅샷도 현재 값으로 동기화
    lastSavedSnapshotRef.current = JSON.stringify({
      title: (n.title || "").trim(),
      contentMd: (n.contentMd || "").trim(),
      tags: (n.tags || "").trim(),
      status: n.status || STATUS_OPTIONS[0],
    });

    try {
      const s = await api(`/api/notes/${id}/summary`);
      setSum(s);
    } catch {
      setSum(null); // 요약이 없을 수 있음
    }
  }

  useEffect(() => {
    if (!id) return;
    loadAll().catch((e) => setErr(e?.message || "로드 실패"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // Ctrl/Cmd + S 단축키 → 전체 저장
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      const isSave = (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "s";
      if (isSave) {
        e.preventDefault();
        if (editing) saveAll();
      }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [editing, title, contentMd, tags, status]);

  async function summarize(style: "brief" | "detailed") {
    if (!id) return;
    setLoading(true);
    setErr(null);
    try {
      await api(`/api/notes/${id}/summary`, {
        method: "POST",
        body: JSON.stringify({ style }),
      });
      // 간단 폴링
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

  function startEdit() {
    if (!note) return;
    setTitle(note.title || "");
    setContentMd(note.contentMd || "");
    setTags(note.tags || "");
    setStatus(note.status || STATUS_OPTIONS[0]);
    setEditing(true);
  }

  function cancelEdit() {
    if (note) {
      setTitle(note.title || "");
      setContentMd(note.contentMd || "");
      setTags(note.tags || "");
      setStatus(note.status || STATUS_OPTIONS[0]);
      // 마지막 스냅샷도 원복
      lastSavedSnapshotRef.current = JSON.stringify({
        title: (note.title || "").trim(),
        contentMd: (note.contentMd || "").trim(),
        tags: (note.tags || "").trim(),
        status: note.status || STATUS_OPTIONS[0],
      });
    }
    setEditing(false);
  }

  async function saveAll() {
    if (!id) return;
    if (!title.trim() || !contentMd.trim()) {
      setErr("제목과 본문(Markdown)은 비워둘 수 없습니다.");
      return;
    }
    setErr(null);
    try {
      await api(`/api/notes/${id}`, {
        method: "PATCH",
        body: JSON.stringify({
          title: title.trim(),
          contentMd: contentMd.trim(),
          status,
          tags: (tags || "").trim(),
        }),
      });
      await loadAll();
      setEditing(false);
      setFlash("저장되었습니다.");
      setLastSavedAt(nowTime());
      lastSavedSnapshotRef.current = currentSnapshot;
      setTimeout(() => setFlash(null), 1500);
    } catch (e: any) {
      setErr(e?.message || "저장 실패");
    }
  }

  // 변경된 필드만 부분 저장(자동 저장에서 사용)
  async function savePartial() {
    if (!id) return;
    const prev = JSON.parse(lastSavedSnapshotRef.current || "{}");
    const curr = JSON.parse(currentSnapshot);

    const patch: any = {};
    (["title", "contentMd", "tags", "status"] as const).forEach((k) => {
      if (prev[k] !== curr[k]) patch[k] = curr[k];
    });

    if (Object.keys(patch).length === 0) return; // 변경 없음
    setAutoSaving(true);
    try {
      await api(`/api/notes/${id}`, {
        method: "PATCH",
        body: JSON.stringify(patch),
      });
      // 저장 성공 → 스냅샷 갱신
      lastSavedSnapshotRef.current = currentSnapshot;
      setLastSavedAt(nowTime());
    } catch (e: any) {
      // 자동 저장 실패는 플래시 대신 에러만 표시(과도한 팝업 방지)
      setErr(e?.message || "자동 저장 실패");
    } finally {
      setAutoSaving(false);
    }
  }

  // 자동 저장(디바운스): 편집 중 + 유효한 값 + 스냅샷 변경 시 AUTOSAVE_MS 뒤에 savePartial 호출
  useEffect(() => {
    if (!editing) return;
    if (!title.trim() || !contentMd.trim()) return; // 필수값 없으면 저장 안 함
    if (currentSnapshot === lastSavedSnapshotRef.current) return; // 변경 없음

    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      savePartial();
    }, AUTOSAVE_MS);

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentSnapshot, editing]);

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
      {/* 헤더 */}
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        {!editing ? (
          <>
            <h3 style={{ margin: 0 }}>{note.title}</h3>
            <span style={{ color: "#888" }}>{note.status}</span>
            <div style={{ marginLeft: "auto", display: "flex", gap: 8 }}>
              <button onClick={startEdit}>수정</button>
              <Link to="/">목록</Link>
            </div>
          </>
        ) : (
          <>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목"
              style={{ flex: 1, fontSize: 18, padding: "6px 8px" }}
            />
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              style={{ height: 32 }}
            >
              {STATUS_OPTIONS.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input
                type="checkbox"
                checked={showPreview}
                onChange={(e) => setShowPreview(e.target.checked)}
              />
              미리보기
            </label>
            <div style={{ marginLeft: "auto", display: "flex", gap: 8 }}>
              <button onClick={saveAll} disabled={!title.trim() || !contentMd.trim()}>
                저장(⌘/Ctrl+S)
              </button>
              <button onClick={cancelEdit}>취소</button>
              <Link to="/">목록</Link>
            </div>
          </>
        )}
      </div>

      {/* 편집/보기 */}
      {!editing ? (
        <>
          {/* 보기 모드 */}
          <section style={{ marginTop: 12 }}>
            <b>본문(텍스트):</b>
            <p style={{ marginTop: 6 }}>{note.contentText}</p>
          </section>

          <section style={{ marginTop: 12 }}>
            <b>태그:</b>{" "}
            {note.tags && note.tags.length > 0 ? (
              <span>
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
                        marginRight: 6,
                      }}
                    >
                      #{t}
                    </span>
                  ))}
              </span>
            ) : (
              <span style={{ color: "#888" }}>없음</span>
            )}
          </section>
        </>
      ) : (
        <>
          {/* 편집 모드: 좌측 에디터 / 우측 미리보기 */}
          <section
            style={{
              marginTop: 12,
              display: "grid",
              gridTemplateColumns: showPreview ? "1fr 1fr" : "1fr",
              gap: 12,
              alignItems: "start",
            }}
          >
            <div>
              <label style={{ display: "block", fontWeight: 600, marginBottom: 6 }}>
                본문(Markdown)
              </label>
              <textarea
                rows={18}
                value={contentMd}
                onChange={(e) => setContentMd(e.target.value)}
                placeholder="# 본문 입력"
                style={{ width: "100%", fontFamily: "ui-monospace, Menlo, monospace" }}
              />
            </div>

            {showPreview && (
              <div>
                <div style={{ fontWeight: 600, marginBottom: 6, display: "flex", justifyContent: "space-between" }}>
                  <span>미리보기</span>
                  <span style={{ color: "#888", fontWeight: 400 }}>
                    {autoSaving ? "자동 저장 중..." : lastSavedAt ? `자동 저장됨 ${lastSavedAt}` : ""}
                  </span>
                </div>
                <div
                  style={{
                    border: "1px solid #e5e7eb",
                    borderRadius: 8,
                    padding: 12,
                    minHeight: 300,
                    background: "#fafafa",
                  }}
                >
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {contentMd || "*미리보기 할 내용이 없습니다.*"}
                  </ReactMarkdown>
                </div>
              </div>
            )}
          </section>

          <section style={{ marginTop: 12 }}>
            <label style={{ display: "block", fontWeight: 600, marginBottom: 6 }}>
              태그(쉼표 구분: ai,spring)
            </label>
            <input
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="ai,spring"
              style={{ width: "100%" }}
            />
          </section>
        </>
      )}

      {flash && <div style={{ color: "#0a0", marginTop: 8 }}>{flash}</div>}

      {/* 요약 영역 */}
      <section style={{ marginTop: 20, display: "flex", gap: 8 }}>
        <button onClick={() => summarize("brief")} disabled={loading || editing}>
          {loading ? "요약중..." : "요약(brief)"}
        </button>
        <button onClick={() => summarize("detailed")} disabled={loading || editing}>
          {loading ? "요약중..." : "요약(detailed)"}
        </button>
      </section>

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
            {sum.model} · {sum.style} · {sum.tokensPrompt}/{sum.tokensOutput} tokens
            {typeof sum.cost === "number" ? ` · ₩${sum.cost}` : ""}
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
