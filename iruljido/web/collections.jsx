import {version as appVersion} from '../package.json';
import {inAppFriendsEnabled} from './features';
import {h,Fragment} from 'preact';import {useState,useEffect,useRef} from 'preact/hooks';
import {feature} from 'topojson-client';import atlas from 'world-atlas/countries-110m.json';
import {appName,T,findDream,personal,active,cover,today,stamp,progress,setPref,notice,change,allPhotos,num,log,id,displayDate} from './store';
import {Head,Btn,Field,Select,Check,DreamRow,Empty,Group,Chips,Pic,Pick,go,safe,back} from './ui';
import {backup,restore,book,drawCard,canvasBlob,output} from './media';
const fmtMonth=d=>d.getFullYear()+'년 '+(d.getMonth()+1)+'월';
function dayEvents(day){
 let out=[];for(let d of personal()){if(d.status==='paused'&&d.pausedUntil===day)out.push([d,'다시 시작']);if(d.complete){if(!d.completedAt)continue;if(today(d.completedAt)===day)out.push([d,'완료 기념']);else if(T.prefs.memories&&day>today(d.completedAt)&&day.slice(5)===today(d.completedAt).slice(5))out.push([d,'추억 기념일']);continue;}
  if(d.status!=='active')continue;if(d.due===day)out.push([d,'목표일']);for(let s of d.steps)if(!s.ok&&(s.date===day||s.remind?.slice(0,10)===day))out.push([d,s.text+(s.remind?.startsWith(day)?' · '+s.remind.slice(11,16):'')]);
  if(d.repeat&&d.repeatStart&&day>=d.repeatStart.slice(0,10)&&(d.repeat==='daily'||new Date(day+'T12:00').getDay()===new Date(d.repeatStart).getDay()))out.push([d,'반복 일정 '+d.repeatStart.slice(11,16)]);
 }return out;
}
export function Plans(){
 let [sel,ss]=useState(today()),[month,sm]=useState(new Date(today()+'T12:00')),[minutes,smin]=useState('15');let base=new Date(month.getFullYear(),month.getMonth(),1,12),days=new Date(month.getFullYear(),month.getMonth()+1,0).getDate();
 let quick=[];for(let d of personal().filter(d=>!d.complete&&d.status==='active'))for(let s of d.steps)if(!s.ok&&s.minutes>0&&s.minutes<=num(minutes))quick.push([d,s]);let events=dayEvents(sel);
 return <><Head title="꿈 달력"/><div class="box"><div class="between"><Btn onClick={()=>sm(new Date(month.getFullYear(),month.getMonth()-1,1,12))}>‹</Btn><b>{fmtMonth(month)}</b><Btn onClick={()=>sm(new Date(month.getFullYear(),month.getMonth()+1,1,12))}>›</Btn></div><div class="calendar">{'일월화수목금토'.split('').map(v=><span>{v}</span>)}{Array.from({length:base.getDay()},()=> <span/>)}{Array.from({length:days},(_,i)=>{let date=today(new Date(month.getFullYear(),month.getMonth(),i+1,12));return <button class={sel===date?'selected':''} onClick={()=>ss(date)}>{i+1}{dayEvents(date).length>0&&<i/>}</button>})}</div></div>
 <h2>{sel} 일정</h2>{events.map(([d,t])=><button class="dreamrow" onClick={()=>go('detail',{id:d.id})}><span><b>{d.title}</b><small>{t}</small></span></button>)}{!events.length&&<Empty>이날 등록된 일정이 없어요.</Empty>}
 <h2>지금 할 수 있는 일</h2><Chips items={['5','10','15','30','60'].map(k=>[k,k+'분'])} v={minutes} on={smin}/>{quick.sort((a,b)=>a[1].minutes-b[1].minutes).map(([d,s])=><button class="dreamrow" onClick={()=>go('detail',{id:d.id})}><div><b>{s.text}</b><small>{d.title} · {s.minutes}분</small></div></button>)}{!quick.length&&<Empty>작은 단계에 소요 시간을 적으면 여기에서 찾을 수 있어요.</Empty>}</>;
}
export function Album(){
 let [show,ss]=useState('모두');let ds=personal().filter(d=>d.complete&&(show==='모두'||d.revisit)).sort((a,b)=>b.completedAt-a.completedAt);return <><Head title="꿈 앨범"/><div class="grid">{[['발자취 지도','map'],['올해의 결산','year'],['인생 연표','life'],['PDF 책 만들기','book']].map(([t,p])=><Btn onClick={()=>go(p)}>{t}</Btn>)}</div><Chips items={['모두','다시 하고 싶은 꿈']} v={show} on={ss}/><h2>이룬 꿈 {ds.length}</h2>{!ds.length&&<Empty>사진이나 영상으로 완료 인증한 꿈이 모여요.</Empty>}<div class="albumgrid">{ds.map(d=><button onClick={()=>go('detail',{id:d.id})}><Pic refKey={cover(d)}/><strong>{d.title}</strong><small>{displayDate(d)}</small></button>)}</div></>;
}
export function Life(){
 let all=personal(),years=new Map();all.forEach(d=>{for(let [t,a] of [[d.created,'시작'],...(d.complete&&d.completedAt?[[d.completedAt,'완료']]:[])]){let yr=new Date(t).getFullYear();if(!years.has(yr))years.set(yr,[]);years.get(yr).push({t,a,d})}});
 let [birth,sb]=useState(T.prefs.birth||'');return <><Head title="인생 연표"/><Group title="연도에 나이 표시"><Field label="태어난 해" type="number" min="1900" v={birth} on={sb}/><Btn onClick={()=>{if(birth&&(birth<1900||birth>new Date().getFullYear()))throw Error('출생 연도를 확인해 주세요.');return setPref('birth',+birth);}}>저장</Btn></Group>
 {Array.from(years.keys()).sort((a,b)=>b-a).map(y=><section><h2>{y}년 {T.prefs.birth&&y>=T.prefs.birth?'· 연나이 '+(y-T.prefs.birth)+'세':''}</h2>{years.get(y).sort((a,b)=>b.t-a.t).map(({t,a,d})=><button class="dreamrow" onClick={()=>go('detail',{id:d.id})}><span class="badge">{a}</span><span class="grow">{d.title}</span><small>{today(t).slice(5)}</small></button>)}</section>)}{!all.length&&<Empty>첫 꿈을 적으면 연표가 시작돼요.</Empty>}</>;
}
export function Year(){
 let all=personal(),now=new Date().getFullYear(),years=Array.from(new Set([now,...all.map(d=>new Date(d.completedAt||d.created).getFullYear())])).sort((a,b)=>b-a),[year,sy]=useState(now);
 let ds=all.filter(d=>d.complete&&new Date(d.completedAt).getFullYear()===+year),starts=all.filter(d=>new Date(d.created).getFullYear()===+year),longest=ds.slice().sort((a,b)=>(b.completedAt-b.created)-(a.completedAt-a.created))[0],days=longest?Math.max(1,Math.floor((longest.completedAt-longest.created)/86400000)+1):0;
 return <><Head title="올해의 결산"/><Select label="연도" v={year} on={v=>sy(+v)} items={years.map(y=>[y,y+'년'])}/><div class="paper"><h2>{year}년의 {appName}</h2><p>시작한 꿈 <b>{starts.length}</b> · 이룬 꿈 <b>{ds.length}</b></p>{longest&&<p>가장 오래 준비한 꿈 · {longest.title} ({days}일)</p>}</div>{ds.map(d=><DreamRow d={d}/>)}{ds.length>0&&<Btn kind="primary" onClick={()=>go('share',{id:ds[0].id,batch:ds.map(x=>x.id),heading:year+'년, 이룬 꿈 '+ds.length+'가지',caption:longest.title+' · '+days+'일의 기록'})}>결산 카드 만들기</Btn>}</>;
}
export function Bingo(){
 const ds=personal(),saved=T.prefs.bingo||[],slots=Array.from({length:9},(_,i)=>saved[i]||'');let [editing,se]=useState(!saved.some(Boolean)),[draft,sd]=useState(slots.slice());
 const lines=[[0,1,2],[3,4,5],[6,7,8],[0,3,6],[1,4,7],[2,5,8],[0,4,8],[2,4,6]].filter(l=>l.every(i=>findDream(slots[i])?.complete)).length;
 return <><Head title="버킷리스트 빙고" right={<Btn onClick={()=>{sd(slots.slice());se(!editing);}}>{editing?'닫기':'칸 편집'}</Btn>}/>{editing?<><p>아홉 칸에 서로 다른 꿈을 골라요.</p>{draft.map((v,i)=><Select label={(i+1)+'번째 칸'} v={v} on={key=>sd(draft.map((k,n)=>n===i?key:k))} items={[['','빈칸'],...ds.filter(d=>d.id===v||!draft.includes(d.id)).map(d=>[d.id,d.title])]}/>)}<Btn kind="primary" onClick={async()=>{await setPref('bingo',draft);se(false);}}>빙고판 저장</Btn></>:<><p>{lines}줄 완성</p><div class="bingo">{slots.map(k=>{let d=findDream(k);return <button class={d?.complete?'on':''} onClick={()=>d?go('detail',{id:k}):se(true)}>{d?.complete?'✓ ':''}{d?.title||'＋'}</button>})}</div></>}</>;
}
export function Book(){
 let ds=personal(),[p,sp]=useState(ds.filter(d=>d.complete).map(d=>d.id));return <><Head title="버킷리스트 책"/><p>꿈 이름, 사진, 일기와 소감을 한 권의 PDF로 만들어요.</p><Btn onClick={()=>sp(p.length===ds.length?[]:ds.map(d=>d.id))}>{p.length===ds.length?'선택 해제':'모두 선택'}</Btn>{ds.map(d=><Check v={p.includes(d.id)} on={v=>sp(v?[...p,d.id]:p.filter(k=>k!==d.id))}>{d.title}</Check>)}<Btn kind="primary" onClick={()=>book(ds.filter(x=>p.includes(x.id)))}>PDF 저장 · {p.length}개 꿈</Btn></>;
}
const geo=feature(atlas,atlas.objects.countries).features;
export function MapView(){
 let pins=personal().filter(d=>d.complete).reduce((a,d)=>a.concat(d.places.filter(p=>p.lat!==''&&p.lng!==''&&isFinite(+p.lat)&&isFinite(+p.lng)).map(p=>({...p,d}))),[]);let all=personal();
 let [view,sv]=useState(()=>({lon:pins[0]?+pins[0].lng:127,lat:pins[0]?+pins[0].lat:36,z:pins.length?8:3})),[selected,sp]=useState(null),[dream,sd]=useState(all[0]?.id||''),[place,sn]=useState('');let r=useRef(),pointer=useRef();let scale=2*view.z;
 useEffect(()=>{let c=r.current,g=c.getContext('2d');g.clearRect(0,0,800,540);let project=([x,y])=>[400+(x-view.lon)*scale,270-(y-view.lat)*scale];g.fillStyle='#dadebf';g.strokeStyle='#a5b5a9';g.lineWidth=1;
  for(let f of geo){const groups=f.geometry.type==='Polygon'?[f.geometry.coordinates]:f.geometry.coordinates;g.beginPath();for(let poly of groups)for(let ring of poly){ring.forEach((v,k)=>{let [x,y]=project(v);if(k===0)g.moveTo(x,y);else g.lineTo(x,y);});g.closePath();}g.fill('evenodd');g.stroke();}
  for(let p of pins){let [x,y]=project([+p.lng,+p.lat]);g.fillStyle='#2a583e';g.beginPath();g.arc(x,y,8,0,Math.PI*2);g.fill();g.font='bold 17px sans-serif';g.fillText(p.name||p.d.title,x+10,y+3);}
  if(selected){let [x,y]=project([selected.lng,selected.lat]);g.strokeStyle='#bc5b36';g.beginPath();g.arc(x,y,12,0,Math.PI*2);g.stroke();}
 },[view,pins.length,selected,dream,T.dreams]);
 const coord=e=>{let b=r.current.getBoundingClientRect();return [(e.clientX-b.left)*800/b.width,(e.clientY-b.top)*540/b.height];};
 return <><Head title="발자취 지도"/><p class="muted">완료한 꿈의 장소 {pins.length}곳 · 지도를 끌어 이동하고 눌러 핀을 찍으세요.</p><canvas width="800" height="540" class="map" ref={r} onPointerDown={e=>{r.current.setPointerCapture(e.pointerId);pointer.current={xy:coord(e),view};}} onPointerUp={e=>{if(!pointer.current)return;let [x,y]=coord(e),[a,b]=pointer.current.xy,v=pointer.current.view;pointer.current=null;
 if(Math.abs(x-a)+Math.abs(y-b)>10)sv({...v,lon:Math.max(-180,Math.min(180,v.lon-(x-a)/scale)),lat:Math.max(-85,Math.min(85,v.lat+(y-b)/scale))});else sp({lng:Math.max(-180,Math.min(180,view.lon+(x-400)/scale)).toFixed(5),lat:Math.max(-85,Math.min(85,view.lat-(y-270)/scale)).toFixed(5)});}}/><div class="toolbar"><Btn onClick={()=>sv({...view,z:Math.min(800,view.z*2)})}>＋ 확대</Btn><Btn onClick={()=>sv({...view,z:Math.max(.9,view.z/2)})}>− 축소</Btn><small>지도 · Natural Earth</small></div>
 {selected&&all.length>0&&<div class="box"><Select label="핀을 추가할 꿈" v={dream} on={sd} items={all.map(d=>[d.id,d.title])}/><Field label="장소 이름" v={place} on={sn}/><small>{selected.lat}, {selected.lng}</small><Btn kind="wide" onClick={async()=>{if(!place.trim())throw Error('장소 이름을 적어 주세요.');await change(dream,d=>d.places.push({...selected,name:place.trim(),id:id()}));sp(null);sn('');notice('꿈에 장소를 저장했어요.');}}>이 위치 저장</Btn></div>}
 {pins.map(p=><div class="box"><strong>{p.name||p.d.title}</strong><p>{p.d.title}</p><div class="pair"><Btn onClick={()=>sv({lon:+p.lng,lat:+p.lat,z:60})}>핀 보기</Btn><Btn onClick={()=>window.Device?.map(p.lat+','+p.lng+' ('+p.name+')')}>지도 앱 열기</Btn></div></div>)}</>;
}
export function Settings(){
 const [lock,sl]=useState(!!window.Device?.lockEnabled());useEffect(()=>{let f=()=>sl(!!window.Device?.lockEnabled());window.addEventListener('resume-ui',f);return()=>window.removeEventListener('resume-ui',f)},[]);
 return <><Head title="설정"/><Group title="기록 보관" open><p class="muted">꿈·메모·일기·사진·음성·영상과 개인 설정을 ZIP으로 옮겨요.{inAppFriendsEnabled&&' 친구 연결용 기기 키와 원격 대화는 백업 범위에서 제외돼요.'}</p><div class="toolbar"><Btn onClick={backup}>사진 포함 백업</Btn><Pick label="백업 복원" accept="application/zip" on={f=>restore(f[0])}/></div></Group><Group title="알림과 홈 화면" open><Check v={T.prefs.memories} on={async v=>{if(v)window.Device?.notifications();await setPref('memories',v);}}>매년 완료 기념일 알림</Check><div class="widget-actions"><Btn onClick={()=>window.Device?.pinWidget()}>꿈 진행률 위젯 추가</Btn><Btn onClick={()=>window.Device?.pinMemoWidget?.()}>빠른 메모 위젯 추가</Btn></div><Btn kind="wide" onClick={()=>window.Device?.openMemoBook?window.Device.openMemoBook():go('memos')}>메모장 열기</Btn></Group>
 <div class="box"><div class="between"><strong>앱 잠금</strong><Btn onClick={()=>{window.Device?.setLock(!lock);setTimeout(()=>sl(!!window.Device?.lockEnabled()),150);}}>{lock?'잠금 끄기':'잠금 켜기'}</Btn></div><small>휴대폰에 등록된 생체 인증 또는 화면 잠금을 사용해요.</small></div><Btn kind="wide" onClick={()=>go('shelf',{initial:'휴지통'})}>휴지통 · 복원</Btn><Btn kind="wide" onClick={()=>go('book')}>PDF 책 만들기</Btn><p class="muted center">{appName} {appVersion.replace(/\.0$/, '')}</p></>;
}
