import {h,Fragment,render,Component} from 'preact';
import {useState,useEffect} from 'preact/hooks';
import {T,listen,notify,init,findDream,change,resumeDue,notice,refreshMemos} from './store';
import {Head,Pic,Empty,Footer,Btn,go,where,navigation,home,back} from './ui';
import {Home,Explore,Shelf,Editor,Detail,Journal,Timeline,Proof} from './personal';
import {Plans,Album,Life,Year,Bingo,Settings,MapView,Book} from './collections';
import {Friends,Boards,Board,NearCard} from './community';
import {Share} from './sharing';
import {MemoBook,MemoEditor,openMemoTarget} from './memos';
import {inAppFriendsEnabled} from './features';
import {startRemote,consent,retryHeld,flush} from './social';
class Boundary extends Component{
 constructor(){super();this.state={err:''};}
 componentDidCatch(e){console.error(e);this.setState({err:String(e.message)});}
 render(p,s){return s.err?<div class="box"><h2>화면을 열 수 없어요</h2><p>{s.err}</p><Btn onClick={home}>수첩으로</Btn></div>:p.children;}
}
function Screen({r}){
 const d=r.id?findDream(r.id):null;switch(r.name){
 case 'memos':return <MemoBook/>;case 'memo':return <MemoEditor {...r}/>;case 'home':return <Home/>;case 'shelf':return <Shelf {...r}/>;case 'edit':return <Editor {...r}/>;case 'explore':return <Explore/>;case 'detail':return <Detail d={d}/>;case 'journal':return d&&<Journal d={d}/>;case 'timeline':return d&&<Timeline d={d}/>;case 'proof':return d&&<Proof d={d}/>;case 'plans':return <Plans/>;case 'album':return <Album/>;case 'life':return <Life/>;case 'year':return <Year/>;case 'bingo':return <Bingo/>;case 'map':return <MapView/>;case 'book':return <Book/>;case 'settings':return <Settings/>;case 'friends':return <Friends {...r}/>;case 'nearCard':return <NearCard {...r}/>;case 'boards':return <Boards {...r}/>;case 'board':return <Board {...r}/>;case 'share':return d&&<Share {...r} d={d}/>;case 'photo':return <><Head title="사진"/><Pic refKey={r.photo} kind="preview" style={{maxHeight:'80vh',objectFit:'contain'}}/></>;default:return <Home/>;}
}
function App(){
 const [r,sr]=useState(where()),[,refresh]=useState(0),[toast,st]=useState('');navigation(sr);
 useEffect(()=>{let clear,fn=e=>{st(e.detail);clearTimeout(clear);clear=setTimeout(()=>st(''),4000)};window.addEventListener('message-ui',fn);let unsub=listen(()=>refresh(x=>x+1));return()=>{clearTimeout(clear);unsub();window.removeEventListener('message-ui',fn)}},[]);
 let tab=['home','explore','album','plans','friends'].includes(r.name)?r.name:['life','map','year','book'].includes(r.name)?'album':['board','boards','nearCard'].includes(r.name)?'friends':'';
 return <><main><Boundary key={r.key}><Screen r={r}/></Boundary></main>{tab&&<Footer active={tab}/>}<div aria-live="polite">{toast&&<div class="toast">{toast}</div>}</div></>;
}
let memoRequest=0;
function external(){if(!T.ready||!window.Device)return;
 if(Device.isMemoWindow?.()){const target=Device.takeMemoTarget();if(target){const turn=++memoRequest;refreshMemos().then(()=>{if(turn===memoRequest)openMemoTarget(target);}).catch(e=>notice(e.message));}return;}
 const value=Device.takeInput();if(!value)return;
 if(value.startsWith('dream:')){let d=findDream(value.slice(6));if(d)go('detail',{id:d.id});return;}
 if(/kkumgalpi:\/\/friend(?:[\/?#\s]|$)|^\s*npub1[a-z0-9]+\s*$/i.test(value)){notice('이 버전에서는 친구 초대를 열 수 없어요.');return;}
 let url=value.match(/https?:\/\/[^\s]+/i)?.[0]||'';let title=value.replace(url,'').trim().slice(0,80);go('edit',{seed:{title:title||'',sourceLink:url||value,why:title?'':value,status:'someday'}});
}
window.externalInput=external;
window.appResume=()=>{window.dispatchEvent(new Event('resume-ui'));if(T.ready){if(window.Device?.isMemoWindow?.()){refreshMemos().then(external).catch(e=>notice(e.message));return;}resumeDue().catch(()=>{});external();if(inAppFriendsEnabled && consent())startRemote().then(flush).catch(()=>{});}};
async function boot(){const since=Date.now();try{const memoWindow=!!window.Device?.isMemoWindow?.();await init(memoWindow);if(memoWindow)openMemoTarget(Device.takeMemoTarget()||'list');render(<App/>,document.querySelector('#root'));external();if(inAppFriendsEnabled && consent())startRemote().then(retryHeld).catch(e=>notice(e.message));}catch(e){let root=document.querySelector('#root');root.textContent='기록을 열지 못했어요. '+e.message;console.error(e);}
 const finishSplash=()=>{
  if(T.ready && !window.Device?.isMemoWindow?.()) window.Device?.journalReady?.();
 };
 setTimeout(()=>{
  const el=document.querySelector('#splash');
  if(!el){finishSplash();return;}
  el.style.opacity='0';
  setTimeout(()=>{el.remove();finishSplash();},250);
 },Math.max(0,850-(Date.now()-since)));}
boot();
