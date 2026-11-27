import {h, Fragment} from 'preact';
import {useEffect, useRef, useState} from 'preact/hooks';
import {T, id, saveMemo, deleteMemo, notice} from './store';
import {Head, Btn, Empty, routes, go, replace, back, where} from './ui';

const findMemo = key => T.memos.find(memo => memo.id === key);

export function openMemoTarget(target) {
 routes.length = 0;
 if (target === 'new') {
  replace('memo');
 } else if (target !== 'list' && findMemo(target)) {
  replace('memo', {memoId:target});
 } else {
  replace('memos');
 }
}

const displayTime = time => new Date(time).toLocaleString('ko-KR', {
 year:'numeric', month:'short', day:'numeric', hour:'numeric', minute:'2-digit'
});

export function MemoBook() {
 return <>
  <Head title="메모장" right={<Btn kind="memo-action" onClick={() => go('memo')}>새 메모</Btn>}/>
  {T.memos.length === 0 && <Empty>아직 저장한 메모가 없어요.</Empty>}
  <div class="memo-list">{T.memos.map(memo =>
   <button class="memo-row" key={memo.id} onClick={() => go('memo', {memoId:memo.id})}>
    <span class="memo-lines">{memo.body.trim()}</span>
    <small>{displayTime(memo.updated)}</small>
   </button>
  )}</div>
 </>;
}

function readDraft(key, memo) {
 try {
  const value = JSON.parse(localStorage.getItem(key));
  if (value && typeof value.body === 'string' && Number.isSafeInteger(value.baseUpdated)
      && /^[a-f0-9]{32}$/.test(value.id) && (!memo || memo.id === value.id)) {
   // A process can stop between a successful IndexedDB commit and clearing its draft.
   if (!memo && findMemo(value.id)?.body === value.body) localStorage.removeItem(key);
   else return value;
  }
 } catch {}
 return {id:memo?.id || id(), body:memo?.body || '', baseUpdated:memo?.updated || 0};
}

export function MemoEditor({memoId}) {
 const memo = memoId ? findMemo(memoId) : null;
 const key = 'memo-draft:' + (memoId || 'new');
 const [draft, setDraft] = useState(() => readDraft(key, memo));
 const [draftSaved, setDraftSaved] = useState(() => draft.body !== (memo?.body || ''));
 const input = useRef();
 const editorKey = where().key;
 useEffect(() => {
  input.current?.focus();
  window.Device?.memoKeyboard?.();
 }, []);

 function edit(body) {
  const next = {...draft, body};
  setDraft(next);
  try {
   if (body === (memo?.body || '')) localStorage.removeItem(key);
   else localStorage.setItem(key, JSON.stringify(next));
   setDraftSaved(body !== (memo?.body || ''));
  } catch { setDraftSaved(false); }
 }

 function finish(message) {
  try { localStorage.removeItem(key); } catch {}
  notice(message);
  if (where().key === editorKey) back();
 }

 async function save() {
  const copied = await saveMemo(draft.id, draft.body, draft.baseUpdated);
  finish(copied ? '원본이 변경되어 새 메모로 저장했어요.' : '메모를 저장했어요.');
 }

 async function remove() {
  if (!confirm('이 메모를 삭제할까요?')) return;
  await deleteMemo(draft.id, draft.baseUpdated);
  finish('메모를 삭제했어요.');
 }

 return <>
  <Head title={memoId ? '메모 수정' : '새 메모'} right={
   <Btn kind="memo-action" disabled={!draft.body.trim()} onClick={save}>저장</Btn>
  }/>
  <div class="memo-paper">
   <textarea ref={input} aria-label="메모 내용" class="memo-input" value={draft.body}
    placeholder="기억해 둘 내용을 적어 보세요." onInput={event => edit(event.currentTarget.value)}/>
  </div>
  <div class="memo-meta">
   <small aria-live="polite">{draftSaved ? '임시 저장됨' : memo ? displayTime(memo.updated) : ''}</small>
   {memoId && <Btn kind="compact" onClick={remove}>메모 삭제</Btn>}
  </div>
 </>;
}
