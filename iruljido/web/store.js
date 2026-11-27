export const appName = document.title;
export const today=(t=Date.now())=>{const d=new Date(t);return [d.getFullYear(),('0'+(d.getMonth()+1)).slice(-2),('0'+d.getDate()).slice(-2)].join('-')};
export const stamp=(t)=>new Date(t).toLocaleDateString('ko-KR');
export const id=()=>Array.from(crypto.getRandomValues(new Uint32Array(4)),x=>x.toString(16).padStart(8,'0')).join('');
export const clone=o=>JSON.parse(JSON.stringify(o));
export const num=x=>Math.max(0,Number(x)||0);
export const money=x=>num(x).toLocaleString('ko-KR')+'원';
const listeners=new Set();export const listen=f=>(listeners.add(f),()=>listeners.delete(f));
export const notify=()=>listeners.forEach(f=>f());
export const T={dreams:[],memos:[],prefs:{},social:{},ready:false};let db,tail=Promise.resolve();
export function serial(job){const p=tail.then(job);tail=p.catch(()=>{});return p;}
export function notice(text){window.dispatchEvent(new CustomEvent('message-ui',{detail:String(text)}));}window.notice=notice;
function req(q){return new Promise((ok,no)=>{q.onsuccess=()=>ok(q.result);q.onerror=()=>no(q.error);});}
export function tx(stores,work){return new Promise((ok,no)=>{let t=db.transaction(stores,'readwrite');t.oncomplete=ok;t.onerror=t.onabort=()=>no(t.error||new Error('저장 공간을 확인해 주세요.'));try{work(t)}catch(e){t.abort();no(e)}});}
export const all=s=>req(db.transaction(s).objectStore(s).getAll());
export const get=(s,k)=>req(db.transaction(s).objectStore(s).get(k));
export const keys=s=>req(db.transaction(s).objectStore(s).getAllKeys());
export const blobPut=async b=>{let key='m:'+id();await tx(['media'],t=>t.objectStore('media').add(b,key));return key;};
const urls=new Map();
export async function source(key){if(!key)return 'sun.jpg';if(key==='sun.jpg'||key==='intro.jpg'||key.startsWith('data:image/jpeg;base64,')||key.startsWith('data:image/png;base64,'))return key;
 if(!key.startsWith('m:'))return 'sun.jpg';if(urls.has(key))return urls.get(key);
 let b=await get('media',key);if(!b)return '';let u=URL.createObjectURL(b);urls.set(key,u);return u;}
export const mediaKeys=d=>Array.from(new Set([d.cover,d.proof,...d.photos,d.video,...d.logs.reduce((a,l)=>a.concat(l.photos||[],l.audio||[]),[])].filter(k=>typeof k==='string'&&k.startsWith('m:'))));
export function normalize(x={}){
 let d={id:id(),created:Date.now(),updated:Date.now(),title:'',cover:'',proof:'',note:'',complete:false,completedAt:0,status:'active',theme:'기타',tags:'',folder:'',due:'',priority:0,unit:'회',target:0,count:0,saving:0,fund:0,budget:[],kit:[],places:[],steps:[],logs:[],photos:[],video:'',why:'',letter:'',recap:'',again:'',shareWith:[],pausedUntil:'',repeat:'',repeatStart:'',sourceLink:'',...x};
 for(let k of ['budget','kit','places','steps','logs','photos','shareWith'])if(!Array.isArray(d[k]))d[k]=[];
 d.steps=d.steps.map(s=>({id:s.id||id(),text:s.text||'',ok:!!s.ok,minutes:s.minutes||0,date:s.date||'',remind:s.remind||'',doneAt:s.doneAt||0}));
 if(d.complete&&!d.completedAt&&!d.completionDateUnknown){if(x.updated)d.completedAt=x.updated;else d.completionDateUnknown=true;}if(d.proof&&!d.photos.includes(d.proof))d.photos.unshift(d.proof);
 return d;
}
export const active=d=>!['archived','trash'].includes(d.status);
export const personal=()=>T.dreams.filter(active);
export const findDream=k=>T.dreams.find(d=>d.id===k);
export const progress=d=>d.complete?100:d.target?Math.min(100,Math.round(100*d.count/d.target)):d.steps.length?Math.round(100*d.steps.filter(s=>s.ok).length/d.steps.length):0;
export const cover=d=>d.proof||d.photos[0]||d.cover||'sun.jpg';
export const nextStep=d=>d.steps.find(s=>!s.ok)?.text||(d.target?d.count+' / '+d.target+d.unit:d.complete?'꿈을 이뤘어요':'첫 단계를 정해 보세요');
export const log=(type,text,more={})=>({id:id(),at:Date.now(),type,text,photos:[],...more});
export const focused=()=>{let d=findDream(T.prefs.focusId);return d&&d.status==='active'&&!d.complete&&T.prefs.focusUntil>Date.now()?d:null;};
export const featured=()=>focused()||personal().filter(d=>d.status==='active'&&!d.complete).sort((a,b)=>b.priority-a.priority||b.updated-a.updated)[0]||personal()[0];
export async function init(memoOnly=false){
 let q=indexedDB.open('dream-journal',3);q.onupgradeneeded=()=>{let d=q.result;if(!d.objectStoreNames.contains('memos'))d.createObjectStore('memos',{keyPath:'id'});if(!d.objectStoreNames.contains('dreams'))d.createObjectStore('dreams',{keyPath:'id'});for(let k of ['settings','media','wire'])if(!d.objectStoreNames.contains(k))d.createObjectStore(k);};db=await req(q);
 if(memoOnly){await refreshMemos();T.ready=true;return;}
 let ds=await all('dreams');T.dreams=ds.map(normalize);T.prefs=await get('settings','prefs')||{};T.social=await get('settings','social')||{};
 // Persist stable step identifiers once for records created by the first APK.
 if(ds.some(d=>!d.status||d.steps?.some(s=>!s.id)))await tx(['dreams'],t=>T.dreams.forEach(d=>t.objectStore('dreams').put(d)));
 await refreshMemos();T.ready=true;await resumeDue();syncDevice();notify();
}
export async function resumeDue(){let now=today();let due=T.dreams.filter(d=>d.status==='paused'&&d.pausedUntil&&d.pausedUntil<=now);for(let d of due)await change(d.id,x=>{x.status='active';x.pausedUntil='';x.logs.push(log('재시작','다시 시작하는 날'))});}
export async function putDream(d){return serial(async()=>{let x=normalize(d);if(!x.title.trim())throw new Error('꿈 이름을 입력해 주세요.');x.updated=Date.now();await tx(['dreams'],t=>t.objectStore('dreams').put(x));T.dreams=[x,...T.dreams.filter(a=>a.id!==x.id)];syncDevice();notify();return x});}
export async function change(k,f){return serial(async()=>{let old=findDream(k);if(!old)throw new Error('꿈을 찾을 수 없어요.');let d=clone(old);f(d);d.updated=Date.now();await tx(['dreams'],t=>t.objectStore('dreams').put(d));T.dreams=T.dreams.map(x=>x.id===k?d:x);syncDevice();notify();return d});}
export async function settings(key,f){return serial(async()=>{let v=clone(T[key]);f(v);await tx(['settings'],t=>t.objectStore('settings').put(v,key));T[key]=v;if(key==='prefs')syncDevice();notify();return v;});}
export const setPref=(k,v)=>settings('prefs',p=>{p[k]=v});
export function duplicate(d,mode){return normalize({title:d.title,theme:d.theme,cover:d.cover,folder:d.folder,tags:d.tags,why:d.why,target:d.target,unit:d.unit,budget:clone(d.budget),kit:d.kit.map(k=>({text:k.text,ok:false})),places:clone(d.places),steps:d.steps.map(s=>({text:s.text,minutes:s.minutes})),parent:mode==='duplicate'?'':d.id,origin:mode});}
export function schedules(){
 let result=[];function add(d,key,date,text,repeat=''){let when=new Date(date).getTime();if(Number.isFinite(when))result.push({key:d.id+':'+key,dream:d.id,title:d.title,text,when,repeat});}
 for(let d of personal()){
  if(d.complete){if(T.prefs.memories&&d.completedAt){let dt=new Date(d.completedAt);dt.setFullYear(dt.getFullYear()+1);dt.setHours(9,0,0,0);add(d,'memory',dt,'오늘은 이 꿈을 이룬 기념일이에요.','yearly');}continue;}
  if(d.status==='paused'){if(d.pausedUntil)add(d,'resume',d.pausedUntil+'T09:00','다시 시작하기로 한 날이에요.');continue;}
  if(d.status!=='active')continue;
  d.steps.filter(s=>!s.ok&&s.remind).forEach(s=>add(d,s.id,s.remind,s.text));
  if(d.repeat&&d.repeatStart)add(d,'repeat',d.repeatStart,nextStep(d),d.repeat);
 }return result.sort((a,b)=>a.key.localeCompare(b.key));
}
export function syncDevice(){if(!window.Device)return;const d=featured();Device.widget(d?.id||'',d?.title||appName,d?progress(d)+'% · '+nextStep(d):'첫 꿈을 적어 보세요.');Device.alarms(JSON.stringify(schedules()));}
export function freshFrom(d){return normalize({title:d.title||'',theme:d.theme||'기타',steps:(d.steps||[]).map(x=>({text:typeof x==='string'?x:x.text})),why:d.why||''});}
export const allPhotos=d=>Array.from(new Set([d.cover,...d.logs.reduce((a,l)=>a.concat(l.photos||[]),[]),...d.photos,d.proof].filter(Boolean)));
export async function restoreData(dreams,prefs,media,memos=[]){
 await serial(async()=>{let list=dreams.map(normalize);const mergedPrefs={...prefs,...T.prefs};
 await tx(['dreams','media','settings','memos'],t=>{mergeBackupMemos(t,memos);for(let [key,blob] of media)t.objectStore('media').put(blob,key);for(let d of list){let existing=findDream(d.id);if(!existing||d.updated>existing.updated)t.objectStore('dreams').put(d);}t.objectStore('settings').put(mergedPrefs,'prefs');});
 T.dreams=(await all('dreams')).map(normalize);T.prefs=mergedPrefs;await refreshMemos();syncDevice();notify();});
}

export const displayDate=d=>d.complete&&d.completionDateUnknown?'완료일 미기록':stamp(d.completedAt||d.updated||d.created);

export function validMemo(memo) {
 return !!memo && typeof memo.id === 'string' && /^[a-f0-9]{32}$/.test(memo.id) && typeof memo.body === 'string' && !!memo.body.trim()
   && Number.isSafeInteger(memo.created) && memo.created > 0
   && Number.isSafeInteger(memo.updated) && memo.updated >= memo.created && memo.updated <= 8640000000000000;
}

let lastMemoRevision = -1;
export async function refreshMemos() {
 const transaction = db.transaction(['memos', 'settings']);
 const [memos, storedRevision] = await Promise.all([
  req(transaction.objectStore('memos').getAll()),
  req(transaction.objectStore('settings').get('memoRevision'))
 ]);
 const revision = storedRevision || 0;
 if (revision < lastMemoRevision) return;
 lastMemoRevision = revision;
 T.memos = memos.sort((a, b) => b.updated - a.updated || b.id.localeCompare(a.id));
 const latest = T.memos[0];
 const preview = latest ? latest.body.trim().slice(0, 480).replace(/[\uD800-\uDBFF]$/, '') : '';
 window.Device?.memoPreview?.(revision, latest?.id || '', preview);
 notify();
}

function advanceMemoRevision(transaction) {
 const settings = transaction.objectStore('settings');
 const request = settings.get('memoRevision');
 request.onsuccess = () => settings.put((request.result || 0) + 1, 'memoRevision');
}

export function saveMemo(memoId, body, baseUpdated) {
 if (!body.trim()) throw Error('메모 내용을 입력해 주세요.');
 return serial(async () => {
  let savedAsCopy = false;
  await tx(['memos', 'settings'], transaction => {
   const memos = transaction.objectStore('memos');
   const read = memos.get(memoId);
   read.onsuccess = () => {
    const previous = read.result;
    if (previous?.body === body) return;
    savedAsCopy = (previous?.updated || 0) !== baseUpdated;
    const now = Date.now();
    const created = previous && !savedAsCopy ? previous.created : now;
    const updated = previous ? Math.max(now, previous.updated + 1) : now;
    memos.put({id: savedAsCopy ? id() : memoId, body, created, updated});
    advanceMemoRevision(transaction);
   };
  });
  await refreshMemos();
  return savedAsCopy;
 });
}

export function deleteMemo(memoId, baseUpdated) {
 return serial(async () => {
  let changed = false;
  await tx(['memos', 'settings'], transaction => {
   const memos = transaction.objectStore('memos');
   const read = memos.get(memoId);
   read.onsuccess = () => {
    if (!read.result) return;
    changed = read.result.updated !== baseUpdated;
    if (changed) return;
    memos.delete(memoId);
    advanceMemoRevision(transaction);
   };
  });
  await refreshMemos();
  if (changed) throw Error('다른 곳에서 수정한 메모예요. 목록에서 다시 열어 주세요.');
 });
}

function mergeBackupMemos(transaction, incoming) {
 if (!incoming.length) return;
 const memos = transaction.objectStore('memos');
 for (const {id, body, created, updated} of incoming) {
  const read = memos.get(id);
  read.onsuccess = () => {
   if (!read.result || read.result.updated < updated) memos.put({id, body, created, updated});
  };
 }
 advanceMemoRevision(transaction);
}
