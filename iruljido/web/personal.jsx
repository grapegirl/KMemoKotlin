import {h,Fragment} from 'preact';import {useState,useEffect} from 'preact/hooks';
import {appName,T,findDream,normalize,clone,personal,active,progress,cover,nextStep,featured,today,stamp,num,money,id,notice,putDream,change,setPref,duplicate,log,freshFrom,focused,allPhotos,blobPut,displayDate} from './store';
import {Head,Btn,Field,Text,Select,Group,Check,Pic,Pick,Media,Chips,DreamRow,Empty,Meter,go,back,replace,home,safe} from './ui';
import {savePhotos,saveVideo,bytesFrom64} from './media';
import {pageEnabled} from './features';
import {themes,ideas,questions} from './ideas';
export const statuses=[['active','진행 중'],['someday','언젠가'],['paused','잠시 쉬기'],['archived','보관'],['trash','휴지통']];
function create(seed={}){go('edit',{seed});}
export function Home(){
 const d=featured(),all=personal(),count=all.filter(t=>t.complete).length;return <><header class="homehead"><h1>{appName}</h1><Btn onClick={()=>go('shelf')}>내 목록</Btn><Btn aria-label="설정" onClick={()=>go('settings')}>⚙</Btn></header>
 <div class="tiles">{[
   ['✎','꿈 작성','edit'], ['◴','진행 현황','shelf'], ['♡','친구와 공유','friends']
 ].filter(([, , page])=>pageEnabled(page)).map(([icon,title,page])=>
   <button key={page} onClick={()=>go(page)}><b aria-hidden="true">{icon}</b>{title}</button>
 )}</div>
 <button class="hero" onClick={()=>d?go('detail',{id:d.id}):create()}><Pic refKey={d?cover(d):'intro.jpg'}/><strong>{d?d.title:'나의 첫 번째 꿈 쓰기'}</strong><span>{d?(focused()?'이번 주의 꿈 · ':'다음 할 일 · ')+nextStep(d):'꿈 수첩 열기 →'}</span></button>
 <div class="box"><div class="between"><b>나의 버킷리스트</b><span>{count} / {all.length} 달성</span></div><Meter n={all.length?count*100/all.length:0}/></div>
 {all.filter(x=>x!==d).sort((a,b)=>b.updated-a.updated).slice(0,3).map(x=><DreamRow d={x}/>)}
 <div class="grid home-links">{[['아이디어 찾기','조건에 맞는 꿈', 'explore'],['올해의 빙고','나의 아홉 가지 꿈','bingo'],['함께 도전','친구와 만드는 목록','boards'],['인생 연표','기억하고 싶은 날들','life']].filter(([, , page])=>pageEnabled(page)).map(([t,sub,page])=><button key={page} class="tile" onClick={()=>go(page)}>{t}<span>{sub}</span></button>)}</div></>}
export function Explore(){
 const [theme,st]=useState('전체'),[season,ss]=useState('전체'),[who,sw]=useState('전체'),[budget,sb]=useState(''),[mins,sm]=useState(''),[answer,sa]=useState(''),[family,sf]=useState(''),[name,sn]=useState('');
 let q=questions[Math.floor(new Date(today()+'T12:00').getTime()/86400000)%questions.length];
 let result=ideas.filter(i=>(theme==='전체'||i.theme===theme)&&(season==='전체'||i.season==='사계절'||i.season===season)&&(who==='전체'||who===i.who)&&(budget===''||i.cost<=num(budget))&&(mins===''||i.minutes<=num(mins)));
 return <><Head title="꿈 발견"/><div class="paper"><small>오늘의 질문</small><h3>{q}</h3><Text label="나의 대답" v={answer} on={sa}/><Btn onClick={()=>{if(!answer.trim())throw Error('대답을 적어 주세요.');create({title:answer.slice(0,80),why:answer,status:'someday'});}}>꿈으로 담기</Btn></div>
 <Group title="가족 인터뷰"><Field label="누구의 이야기인가요?" v={name} on={sn}/><Text label="꼭 해보고 싶은 일이 무엇인지 물어보세요." v={family} on={sf}/><Btn onClick={()=>{if(!family.trim())throw Error('대답을 적어 주세요.');create({title:family.slice(0,80),theme:'가족',why:(name||'가족')+'의 이야기 · '+family,logs:[log('가족 인터뷰',family)]});}}>가족의 꿈으로 담기</Btn></Group>
 <h2>꿈 템플릿</h2><Chips items={['전체',...themes.slice(0,-1)]} v={theme} on={st}/><Group title="계절 · 예산 · 시간으로 찾기"><div class="splitcols"><Select label="계절" v={season} on={ss} items={['전체','봄','여름','가을','겨울']}/><Select label="동행" v={who} on={sw} items={['전체','혼자','함께']}/><Field type="number" min="0" label="최대 예산 (원)" v={budget} on={sb}/><Field type="number" min="0" label="가능한 시간 (분)" v={mins} on={sm}/></div></Group>
 {result.map(i=><div class="box"><span class="badge">{i.theme}</span><h3>{i.title}</h3><small>{i.season} · {i.who} · {i.minutes}분 · 약 {money(i.cost)}</small><p>{i.steps.join(' → ')}</p><div class="pair"><Btn onClick={()=>create(freshFrom(i))}>이 계획 쓰기</Btn><Btn onClick={async()=>{await putDream({...freshFrom(i),status:'someday'});notice('언젠가 보관함에 담았어요.')}}>언젠가 담기</Btn></div></div>)}
 {!result.length&&<Empty>조건에 맞는 꿈이 없어요. 시간이나 예산을 바꿔 보세요.</Empty>}</>;
}
export function Shelf({initial='전체'}){
 let [q,sq]=useState(''),[status,ss]=useState(initial),[sort,so]=useState('최근 기록'),[folder,sf]=useState('전체'),[max,smax]=useState('');
 let folders=Array.from(new Set(T.dreams.map(d=>d.folder).filter(Boolean)));
 let ds=T.dreams.filter(d=>{if(status==='휴지통')return d.status==='trash';if(status==='보관')return d.status==='archived';if(!active(d))return false;
 return status==='전체'||(status==='완료'?d.complete:status==='진행 중'?d.status==='active'&&!d.complete:status==='언젠가'?d.status==='someday':d.status==='paused');}).filter(d=>(d.title+' '+d.tags+' '+d.theme+' '+d.folder).toLowerCase().includes(q.toLowerCase())&&(folder==='전체'||d.folder===folder)&&(max===''||d.budget.reduce((a,b)=>a+num(b.cost),0)<=num(max)));
 ds.sort((a,b)=>sort==='마감일'?(a.due||'9999').localeCompare(b.due||'9999'):sort==='진행률'?progress(b)-progress(a):sort==='낮은 예산'?a.budget.reduce((v,x)=>v+num(x.cost),0)-b.budget.reduce((v,x)=>v+num(x.cost),0):sort==='우선순위'?b.priority-a.priority:b.updated-a.updated);
 return <><Head title="내 꿈 목록" right={<Btn onClick={()=>create()}>＋ 작성</Btn>}/><Field label="꿈 · 태그 검색" v={q} on={sq} placeholder="제주, 가족, 책…"/><Chips items={['전체','진행 중','완료','언젠가','쉬는 중','보관','휴지통']} v={status} on={ss}/>
 <Group title="컬렉션 · 정렬 · 예산"><Select label="컬렉션" v={folder} on={sf} items={['전체',...folders]}/><Select label="정렬" v={sort} on={so} items={['최근 기록','우선순위','마감일','진행률','낮은 예산']}/><Field label="최대 예산 (원)" type="number" min="0" v={max} on={smax}/></Group>
 {ds.map(d=><DreamRow d={d}/>)}{!ds.length&&<Empty>이 조건에 해당하는 꿈이 없어요.</Empty>}</>;
}
function Rows({rows,on,make,render,label}){return <>{rows.map((item,i)=><div class="box" key={item.id||i}>{render(item,patch=>on(rows.map((r,k)=>k===i?{...r,...patch}:r)))}<Btn kind="compact" onClick={()=>on(rows.filter((_,k)=>i!==k))}>이 항목 빼기</Btn></div>)}<Btn onClick={()=>on([...rows,make()])}>＋ {label}</Btn></>}
export function Editor({dreamId,seed}){
 let [d,sd]=useState(()=>normalize(clone(dreamId?findDream(dreamId):seed||{})));let set=(k,v)=>sd(old=>({...old,[k]:v}));const text=(k,label,more={})=><Field label={label} v={d[k]} on={v=>set(k,v)} {...more}/>;
 async function save(){d.title=d.title.trim();if(!d.title)throw Error('꿈 이름을 적어 주세요.');if(d.repeat&&!d.repeatStart)throw Error('반복을 시작할 날짜와 시각을 골라 주세요.');if(d.places.some(p=>p.lat!==''&&(!Number.isFinite(+p.lat)||Math.abs(+p.lat)>85)||p.lng!==''&&(!Number.isFinite(+p.lng)||Math.abs(+p.lng)>180)||(p.lat==='')!==(p.lng==='')))throw Error('위도와 경도를 함께 확인해 주세요.');
  let x={...d,steps:d.steps.filter(s=>s.text.trim()),budget:d.budget.filter(b=>b.text.trim()),kit:d.kit.filter(k=>k.text.trim()),places:d.places.filter(p=>p.name.trim()||p.lat!==''),target:num(d.target),saving:num(d.saving),fund:num(d.fund)};if(x.complete)x.steps.forEach(s=>s.ok=true);if(x.steps.some(s=>s.remind)||x.repeat||x.pausedUntil)window.Device?.notifications();await putDream(x);replace('detail',{id:d.id});notice('꿈을 저장했어요.');}
 return <><Head title={dreamId?'꿈 수정':'꿈 작성'}/><div class="post"><Pic refKey={d.cover||'sun.jpg'} kind="preview"/><div class="toolbar"><Pick label="표지 사진" on={async f=>set('cover',(await savePhotos(f))[0])}/><Pick label="직접 촬영" capture on={async f=>set('cover',(await savePhotos(f))[0])}/></div></div>
 {text('title','꿈 이름',{maxLength:80,placeholder:'한라산에서 일출 보기'})}<Text label="이 꿈을 이루고 싶은 이유" v={d.why} on={v=>set('why',v)} maxLength={1600}/>
 <Group title={'작은 단계 · '+d.steps.length} open><Rows rows={d.steps} on={v=>set('steps',v)} label="단계 추가" make={()=>({id:id(),text:'',minutes:0,date:'',remind:'',ok:false})} render={(s,patch)=><><Field label="할 일" v={s.text} on={v=>patch({text:v})} maxLength={120}/><div class="splitcols"><Field type="date" label="예정일" v={s.date} on={v=>patch({date:v})}/><Field type="number" min="0" label="소요 시간 (분)" v={s.minutes||''} on={v=>patch({minutes:num(v)})}/></div><Field type="datetime-local" label="단계 알림" v={s.remind} on={v=>patch({remind:v})}/></>}/></Group>
 <Group title="계획과 분류"><div class="splitcols"><Select label="분야" v={d.theme} on={v=>set('theme',v)} items={themes}/><Select label="우선순위" v={String(d.priority)} on={v=>set('priority',+v)} items={[['0','보통'],['1','중요'],['2','가장 중요']]}/></div>
 {text('due','목표 날짜',{type:'date'})}{text('tags','태그',{placeholder:'가족, 제주'})}{text('folder','컬렉션',{placeholder:'서른 전에'})}{text('sourceLink','참고 링크 또는 메모')}
 <Select label="목록" v={d.status} on={v=>set('status',v)} items={statuses}/>{d.status==='paused'&&text('pausedUntil','다시 시작할 날',{type:'date'})}
 <Select label="반복 일정" v={d.repeat} on={v=>set('repeat',v)} items={[['','반복 없음'],['daily','매일'],['weekly','매주']]}/>{d.repeat&&text('repeatStart','첫 반복 날짜와 시각',{type:'datetime-local'})}</Group>
 <Group title="수량 목표와 비용"><div class="splitcols">{text('target','목표 수량',{type:'number',min:0})}{text('unit','단위',{placeholder:'권, km, 회'})}</div><h3>예상 비용</h3><Rows rows={d.budget} on={v=>set('budget',v)} label="비용 항목" make={()=>({id:id(),text:'',cost:0})} render={(b,p)=><div class="splitcols"><Field label="항목" v={b.text} on={v=>p({text:v})}/><Field label="금액 (원)" type="number" min="0" v={b.cost} on={v=>p({cost:num(v)})}/></div>}/>
 <p>예상 총액 <b>{money(d.budget.reduce((a,b)=>a+num(b.cost),0))}</b></p>{text('fund','모을 목표 금액 (원)',{type:'number',min:0})}{text('saving','현재 모은 금액 (원)',{type:'number',min:0})}</Group>
 <Group title="준비물과 장소"><Rows rows={d.kit} on={v=>set('kit',v)} label="준비물" make={()=>({id:id(),text:'',ok:false})} render={(s,p)=><Field label="챙길 물건" v={s.text} on={v=>p({text:v})}/>}/>
 <Rows rows={d.places} on={v=>set('places',v)} label="장소" make={()=>({id:id(),name:'',lat:'',lng:''})} render={(s,p)=><><Field label="장소 이름 또는 주소" v={s.name} on={v=>p({name:v})}/><div class="splitcols"><Field type="number" step="any" label="지도 핀 위도" v={s.lat} on={v=>p({lat:v})}/><Field type="number" step="any" label="경도" v={s.lng} on={v=>p({lng:v})}/></div></>}/></Group>
 <Group title="미래의 나에게 편지">{d.letter&&!d.complete&&dreamId?<p>완료하는 날 열어 볼 편지가 있어요.</p>:<Text label="이 꿈을 이루고 나에게 건네고 싶은 말" v={d.letter} on={v=>set('letter',v)}/>}</Group>
 <Btn kind="primary" onClick={save}>꿈 저장</Btn></>;
}
export function Detail({d}){
 let [amount,sa]=useState('1'),[deposit,sd]=useState(''),[pause,sp]=useState('');if(!d)return <Empty>꿈을 찾지 못했어요.</Empty>;
 const update=f=>change(d.id,f),setStatus=async status=>{await update(x=>{x.previousStatus=x.status;x.status=status;x.logs.push(log('목록',statuses.find(a=>a[0]===status)?.[1]||status));});notice('목록을 변경했어요.');};
 if(!active(d))return <><Head title={d.status==='trash'?'휴지통':'보관한 꿈'}/><h2>{d.title}</h2><Btn kind="primary" onClick={()=>setStatus(['active','paused','someday'].includes(d.previousStatus)?d.previousStatus:'active')}>내 목록으로 복원</Btn></>;
 let total=d.logs.reduce((a,l)=>a+num(l.minutes),0),n=progress(d);
 return <><Head title="꿈 펼치기" right={<Btn onClick={()=>go('edit',{dreamId:d.id})}>수정</Btn>}/><div class="post"><Pic refKey={cover(d)} kind="preview"/><span class="badge">{d.complete?'✓ 완료 인증':d.theme}</span><h2>{d.title}</h2>{d.due&&<p>목표일 {d.due}</p>}{d.why&&<p class="bodytext"><small>{!d.complete&&Date.now()-d.updated>14*86400000?'다시 펼쳐 보는, 처음 시작한 이유':'이 꿈을 시작한 이유'}</small><br/>{d.why}</p>}{d.note&&<p>{d.note}</p>}</div>
 <div class="box"><div class="between"><b>진행 현황</b><strong>{n}%</strong></div><Meter n={n}/><p>다음 할 일 · {nextStep(d)}</p>
 {d.steps.map((s,i)=><Check v={s.ok} on={safe(async ok=>{if(d.complete)throw Error('완료한 꿈이에요.');await update(x=>{let st=x.steps[i];st.ok=ok;st.doneAt=ok?Date.now():0;x.logs.push(log('단계',st.text+(ok?' 완료':' 다시 열기')));});})}>{s.text}{s.date&&<small>{s.date}</small>}</Check>)}
 {d.target>0&&<><h3>{d.count} / {d.target}{d.unit}</h3><div class="pair"><Field label="추가 수량" type="number" step="any" min="0" v={amount} on={sa}/><Btn disabled={d.complete} onClick={()=>{let a=num(amount);if(!a)throw Error('수량을 입력해 주세요.');return update(x=>{x.count=num(x.count)+a;x.logs.push(log('수량','+'+a+x.unit));});}}>＋ 기록</Btn></div></>}</div>
 <div class="pair"><Btn onClick={()=>go('journal',{id:d.id})}>진행 일기{total>0?' · '+total+'분':''}</Btn><Btn onClick={()=>go('timeline',{id:d.id})}>타임라인</Btn></div>
 <Btn kind="primary" onClick={()=>go('proof',{id:d.id})}>{d.complete?'인증 사진·소감 수정':'사진·영상으로 완료 인증'}</Btn><Btn kind="wide" onClick={()=>go('share',{id:d.id})}>꿈 공유</Btn>
 {d.complete&&<div class="paper"><h3>{d.completionDateUnknown?'완료일 미기록':'완료한 날 · '+displayDate(d)}</h3>{d.letter&&<Group title="미래의 나에게 쓴 편지 열기"><p class="bodytext">{d.letter}</p></Group>}{d.recap&&<p>{d.recap}</p>}{d.again&&<p>{d.again}</p>}<Check v={d.revisit} on={v=>update(x=>{x.revisit=v})}>다시 하고 싶어요</Check><Gallery refs={d.photos}/>{d.video&&<Media refKey={d.video} type="video"/>}<div class="pair"><Btn onClick={()=>create({...duplicate(d,'next'),title:'',steps:[]})}>다음 꿈 잇기</Btn><Btn onClick={()=>create(duplicate(d,'again'))}>다시 도전하기</Btn></div></div>}
 {d.parent&&findDream(d.parent)&&<Btn onClick={()=>go('detail',{id:d.parent})}>이전 꿈 · {findDream(d.parent).title}</Btn>}
 <Group title="예산 · 준비물 · 장소"><p>예상 비용 {money(d.budget.reduce((a,b)=>a+num(b.cost),0))}</p>{d.budget.map(b=><p>{b.text} · {money(b.cost)}</p>)}{d.fund>0&&<><p>모은 금액 {money(d.saving)} / {money(d.fund)}</p><Meter n={d.saving*100/d.fund}/><Field label="이번에 모은 금액 (원)" type="number" min="0" v={deposit} on={sd}/><Btn onClick={async()=>{if(!num(deposit))throw Error('금액을 입력해 주세요.');await update(x=>{x.saving=num(x.saving)+num(deposit);x.logs.push(log('저축',money(deposit)+' 모으기'));});sd('');}}>저축 기록</Btn></>}
 {d.kit.map((s,i)=><Check v={s.ok} on={ok=>update(x=>{x.kit[i].ok=ok})}>{s.text}</Check>)}{d.places.map(p=><Btn onClick={()=>window.Device?.map(p.lat!==''&&p.lng!==''?p.lat+','+p.lng+' ('+p.name+')':p.name)}>{p.name||'저장한 장소'} ↗</Btn>)}{d.sourceLink&&<p class="wrap">{d.sourceLink} <Btn kind="compact" onClick={()=>window.Device?.copy(d.sourceLink)}>복사</Btn></p>}</Group>
 <Group title="꿈 관리"><div class="toolbar"><Btn disabled={d.complete||d.status!=='active'} onClick={async()=>{await setPref('focusId',d.id);await setPref('focusUntil',Date.now()+7*86400000);notice('이번 주의 꿈으로 지정했어요.');}}>이번 주 집중</Btn><Btn onClick={()=>create(duplicate(d,'duplicate'))}>계획 복제</Btn>{pageEnabled('boards')&&<Btn onClick={()=>go('boards',{dreamId:d.id})}>함께 도전</Btn>}</div>
 {d.status==='paused'||d.status==='someday'?<Btn onClick={()=>setStatus('active')}>지금 시작하기</Btn>:!d.complete&&<><Field label="다시 시작할 날짜" type="date" v={pause} on={sp}/><Btn onClick={()=>{if(!pause||pause<=today())throw Error('오늘 이후의 날짜를 골라 주세요.');window.Device?.notifications();return update(x=>{x.status='paused';x.pausedUntil=pause;x.logs.push(log('잠시 쉬기',pause+'까지'));});}}>그날까지 잠시 쉬기</Btn></>}
 <div class="toolbar"><Btn onClick={()=>setStatus('archived')}>보관하기</Btn><Btn kind="danger" onClick={async()=>{await setStatus('trash');notice('휴지통으로 옮겼어요. 복원할 수 있어요.');}}>휴지통으로</Btn></div></Group></>;
}
export function Journal({d}){
 let [type,st]=useState('진행 일기'),[text,sText]=useState(''),[solution,sSol]=useState(''),[mins,sm]=useState(''),[photos,sPhotos]=useState([]),[audio,sa]=useState(''),[rec,srec]=useState(false),[elapsed,se]=useState(Date.now());
 let timer=T.prefs.timer;if(timer&&timer.dream!==d.id)timer=null;
 useEffect(()=>{window.recordState=srec;window.voiceReady=async b=>{sa(await blobPut(new Blob([bytesFrom64(b)],{type:'audio/mp4'})));};let it=setInterval(()=>se(Date.now()),1000);return()=>{clearInterval(it);if(rec)window.Device?.record(false);};},[rec]);
 async function save(){if(rec)throw Error('먼저 녹음을 마쳐 주세요.');let duration=num(mins)+(timer?Math.max(1,Math.round((Date.now()-timer.start)/60000)):0);if(!text.trim()&&!photos.length&&!audio&&!duration&&!solution.trim())throw Error('기록할 내용을 입력해 주세요.');await change(d.id,x=>x.logs.push(log(type,[text,solution?'다시 시도할 방법 · '+solution:''].filter(Boolean).join('\n'),{minutes:duration,photos,audio})));if(timer)await setPref('timer',null);back();notice('진행 기록을 남겼어요.');}
 return <><Head title="진행 일기"/><h2>{d.title}</h2><Chips items={['진행 일기','활동 시간','막힌 이유']} v={type} on={st}/><Text label={type==='막힌 이유'?'어디에서 멈췄나요?':'오늘의 기록'} v={text} on={sText}/>{type==='막힌 이유'&&<Text label="다시 시도할 방법" v={solution} on={sSol}/>}
 <Field label="활동 시간 (분)" type="number" min="0" v={mins} on={sm}/><Btn onClick={async()=>{if(timer){let total=Math.max(1,Math.round((Date.now()-timer.start)/60000));sm(String(num(mins)+total));await setPref('timer',null);}else{if(T.prefs.timer)throw Error('다른 꿈에서 시간을 기록하고 있어요.');await setPref('timer',{dream:d.id,start:Date.now()});}}}>{timer?'측정 마치기 · '+Math.max(0,Math.floor((elapsed-timer.start)/1000))+'초':'활동 시간 측정'}</Btn>
 <div class="toolbar"><Pick label="중간 사진 추가" multiple on={async f=>{let a=await savePhotos(f);sPhotos(old=>old.concat(a));}}/><Btn onClick={()=>window.Device?.record(!rec)}>{rec?'녹음 마치기':'음성 메모 녹음'}</Btn></div><Gallery refs={photos} remove={k=>sPhotos(photos.filter(x=>x!==k))}/>{audio&&<Media refKey={audio} type="audio"/>}<Btn kind="primary" onClick={save}>기록 저장</Btn></>;
}
export function Gallery({refs=[],remove}){return <div class="gallery">{refs.map(k=><button aria-label={remove?'사진 빼기':'사진 크게 보기'} onClick={()=>{if(remove){if(confirm('이 사진을 목록에서 뺄까요?'))remove(k);}else go('photo',{photo:k});}}><Pic refKey={k}/></button>)}</div>}
export function Proof({d}){
 let [doneDate,sdate]=useState(d.completedAt?today(d.completedAt):today()),[photos,sp]=useState(d.photos.slice()),[video,sv]=useState(d.video),[note,sn]=useState(d.note),[recap,sr]=useState(d.recap),[again,sa]=useState(d.again);
 async function add(f){const img=await savePhotos(f);sp(v=>v.concat(img));}
 return <><Head title="완료 인증"/><h2>{d.title}</h2><Gallery refs={photos} remove={k=>sp(photos.filter(x=>x!==k))}/><div class="pair"><Pick label="사진 촬영" capture on={add}/><Pick label="여러 사진 선택" multiple on={add}/></div><div class="toolbar"><Pick label="짧은 영상 촬영" accept="video/*" capture on={async f=>sv(await saveVideo(f[0]))}/><Pick label="영상 선택" accept="video/*" on={async f=>sv(await saveVideo(f[0]))}/>{video&&<Btn onClick={()=>sv('')}>영상 빼기</Btn>}</div>{video&&<Media refKey={video} type="video"/>}
 <Field type="date" label="꿈을 이룬 날" max={today()} v={doneDate} on={sdate}/><Text label="한 줄 소감" v={note} on={sn} maxLength={800}/><Group title="완료 인터뷰"><Text label="가장 기억에 남은 순간은?" v={recap} on={sr}/><Text label="다시 한다면?" v={again} on={sa}/></Group>
 <Btn kind="primary" onClick={async()=>{if(!photos.length&&!video)throw Error('인증 사진이나 영상을 한 개 이상 담아 주세요.');if(!doneDate||doneDate>today())throw Error('오늘 또는 이전 날짜를 골라 주세요.');await change(d.id,x=>{x.complete=true;x.completedAt=x.completedAt&&today(x.completedAt)===doneDate?x.completedAt:new Date(doneDate+'T12:00').getTime();x.completionDateUnknown=false;x.status='active';x.photos=photos;x.proof=photos[0]||'';x.video=video;x.note=note;x.recap=recap;x.again=again;x.steps.forEach(s=>s.ok=true);if(x.target)x.count=Math.max(x.count,x.target);x.logs.push(log('완료 인증',note||'꿈을 이뤘어요.'));});back();notice('완료 앨범에 담았어요.');}}>인증하고 저장</Btn></>;
}
export function Timeline({d}){
 let photos=allPhotos(d);let [before,sb]=useState(photos[0]),[after,sa]=useState(photos[photos.length-1]);
 return <><Head title="꿈 타임라인"/><h2>{d.title}</h2>{photos.length>=2&&<Group title="시작 전 · 후 비교" open><div class="splitcols">{[[before,sb,'시작 전'],[after,sa,'지금']].map(([key,set,label])=><div><Pic refKey={key} kind="preview"/><Select label={label} v={key} on={set} items={photos.map((r,i)=>[r,'사진 '+(i+1)])}/></div>)}</div></Group>}
 {[log('꿈 작성',d.title,{at:d.created}),...d.logs].sort((a,b)=>b.at-a.at).map(l=><article class="entry"><small>{stamp(l.at)} · {l.type}</small><p>{l.text}</p>{l.minutes>0&&<span class="badge">{l.minutes}분</span>}<Gallery refs={l.photos}/>{l.audio&&<Media refKey={l.audio} type="audio"/>}</article>)}</>;
}
