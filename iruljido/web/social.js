import {T,settings,notify,notice,id,clone,get,tx,serial,keys} from './store';
export const social=()=>T.social;
export const contacts=()=>Object.values(T.social.contacts||{}).filter(c=>c.status==='friend');
export const boards=()=>Object.values(T.social.boards||{});
export const posts=()=>Object.values(T.social.posts||{}).sort((a,b)=>b.at-a.at);
export const label=k=>k===T.social.me?(T.social.nick||'나'):(T.social.contacts?.[k]?.name||k.slice(0,8));
export const consent=()=>!!T.social.enabled;
let library,working=false,running=false,retryTimer=null;
export let netCount=0;
let pieces=Promise.resolve(),heldTimer;
function loadWire(){if(typeof BigInt!=='function')return Promise.reject(Error('원격 친구 연결을 사용하려면 Play 스토어에서 Android System WebView와 Chrome을 업데이트해 주세요.'));if(window.DreamWire)return Promise.resolve(window.DreamWire);if(!library)library=new Promise((ok,no)=>{let script=document.createElement('script');script.src='remote.js';script.onload=()=>ok(window.DreamWire);script.onerror=()=>{library=null;no(Error('원격 친구 기능에는 최신 Android System WebView가 필요해요.'));};document.head.appendChild(script);});return library;}
export async function startRemote(){
 if(!consent()||running)return;let w=await loadWire();if(!w)throw Error('원격 친구 기능에는 최신 Android System WebView가 필요해요.');let key=Device.key();if(!key)throw Error('기기의 친구 ID를 열 수 없어요.');
 if(!consent()||running)return;const who=w.start(key,T.social.relays,{status:()=>{netCount=w.online();notify();if(netCount)flush();},receive:(from,part)=>{let p=pieces.then(()=>receivePiece(from,part));pieces=p.catch(()=>{});return p;}});running=true;
 await settings('social',s=>{s.me=who.id;});retryTimer=setInterval(()=>{if(w.online())flush();},25000);
}
export function stopRemote(){running=false;window.DreamWire?.stop();clearInterval(retryTimer);netCount=0;notify();}
export async function enable(nick,relays){if(!nick.trim())throw Error('닉네임을 입력해 주세요.');if(relays?.some(x=>!/^wss:\/\/[^\s/]+(?:\/[^\s]*)?$/.test(x)))throw Error('wss로 시작하는 중계 주소를 확인해 주세요.');await loadWire();stopRemote();await settings('social',s=>{s.nick=nick.trim().slice(0,25);s.enabled=true;s.relays=relays;});await startRemote();}
export async function disable(){stopRemote();await settings('social',s=>s.enabled=false);}
function setup(s){for(let k of ['contacts','boards','posts','groups','gifts','invites','seen','out'])s[k]||={};}
function envelope(s,type,data){return {type,data,id:id(),at:Date.now(),nick:s.nick||'꿈친구'};}
function queue(s,to,type,data){if(!to||to===s.me)return;const pack=envelope(s,type,data);s.out[pack.id]={to,pack,cursor:0};}
// Older versions may still have unsent friend requests or acknowledgements in this outbox.
const friendHandshakes = new Set(['request','accept','decline']);
const deliverable = () => Object.values(T.social.out || {}).filter(item => item?.pack && !friendHandshakes.has(item.pack.type));
export const queued = () => deliverable().length;
export async function flush(){
 if(working||!running||!window.DreamWire?.online())return;working=true;try{let list=deliverable();for(let item of list){if(!running)break;if(item.nextTry&&Date.now()<item.nextTry)continue;
  try{await window.DreamWire.deliver(item.to,item.pack,item.cursor,(n,total)=>settings('social',s=>{let q=s.out?.[item.pack.id];if(!q)return;if(n===total)delete s.out[item.pack.id];else q.cursor=n;}));}
  catch(err){await settings('social',s=>{let q=s.out?.[item.pack.id];if(q){q.nextTry=Date.now()+10000;q.error=String(err?.message||'연결 대기');}});}}
 }finally{working=false;if(running&&window.DreamWire?.online()&&deliverable().some(q=>!q.nextTry||q.nextTry<=Date.now()))setTimeout(flush,120);}
}
async function mutate(f){await settings('social',s=>{setup(s);f(s)});flush();}
export async function groupSave(name,ids){if(!name.trim())throw Error('그룹 이름을 적어 주세요.');await mutate(s=>{s.groups[name.trim().slice(0,32)]=ids.filter(k=>s.contacts[k]?.status==='friend');});}
export async function postDream(d,targets){
 if(!consent())throw Error('친구 소식 연결을 켜거나 카카오톡으로 공유해 주세요.');if(!targets.length)throw Error('공유할 친구를 선택해 주세요.');await mutate(s=>{let audience=Array.from(new Set(targets.filter(k=>s.contacts[k]?.status==='friend')));if(!audience.length)throw Error('공유할 친구를 선택해 주세요.');let p={id:id(),owner:s.me,at:Date.now(),rev:1,card:d,audience,comments:[],reactions:{}};s.posts[p.id]=p;syncPost(s,p);});notice('선택한 친구에게 공유를 예약했어요.');
}
function syncPost(s,p){for(let k of p.audience)queue(s,k,'post',{...p,audience:[]});}
function engage(s,p,actor,op){
 if(op.kind==='react'&&['응원해','멋지다','같이 가자'].includes(op.text))p.reactions[actor]=op.text;
 else if(op.kind==='comment'&&typeof op.text==='string'&&op.text.trim()){if(p.comments.some(c=>c.id===op.id))return;p.comments.push({id:op.id,author:actor,name:actor===s.me?s.nick:(s.contacts[actor]?.name||'꿈친구'),text:op.text.slice(0,600),reply:p.comments.some(c=>c.id===op.reply)?op.reply:'',at:Date.now()});}
 else return;p.rev++;syncPost(s,p);
}
export async function interact(postId,kind,text,reply=''){await mutate(s=>{let p=s.posts[postId];if(!p)return;let op={id:id(),kind,text,reply};if(p.owner===s.me)engage(s,p,s.me,op);else queue(s,p.owner,'engage',{postId,op});});}
export async function gift(key,card){if(!consent())throw Error('친구 소식 연결을 켜거나 카카오톡으로 공유해 주세요.');await mutate(s=>{if(s.contacts[key]?.status!=='friend')throw Error('친구를 선택해 주세요.');queue(s,key,'gift',{card});});notice('꿈 엽서를 전송 목록에 담았어요.');}
export async function createBoard(title,goal,unit,invited,initial){
 if(!title.trim())throw Error('함께 할 도전 이름을 적어 주세요.');if(!consent())throw Error('먼저 원격 친구 연결을 켜 주세요.');let key=id();await mutate(s=>{let b={id:key,title:title.trim(),owner:s.me,rev:1,goal:Number(goal)||0,unit:unit||'회',members:{[s.me]:s.nick},pending:{},counts:{},items:[],polls:[],photos:[],applied:{}};if(initial)b.items.push({id:id(),text:initial,who:'',ok:false});s.boards[key]=b;
  for(let k of invited){if(s.contacts[k]?.status!=='friend')continue;b.pending[k]=true;queue(s,k,'invite',{board:key,title:b.title,goal:b.goal,unit:b.unit});}});return key;
}
export async function inviteToBoard(bid,k){await mutate(s=>{let b=s.boards[bid];if(!b||b.owner!==s.me||s.contacts[k]?.status!=='friend'||b.members[k])return;b.pending[k]=true;queue(s,k,'invite',{board:bid,title:b.title,goal:b.goal,unit:b.unit});});}
export async function inviteReply(key,yes){await mutate(s=>{let inv=s.invites[key];if(!inv)return;queue(s,inv.from,'join',{board:inv.board,yes});if(yes)inv.wait=true;else delete s.invites[key];});}
function syncBoard(s,b){let {pending,applied,...shared}=b;shared.photos=b.photos.map(({photo,...meta})=>meta);for(let k of Object.keys(b.members))if(k!==s.me)queue(s,k,'boardState',shared);}
function sendBoardPhoto(s,b,photo,to){for(let k of to)if(k!==s.me)queue(s,k,'boardPhoto',{board:b.id,photo:photo.photo,id:photo.id});}
function applyOp(s,b,k,op){
 if(!b.members[k]||!op||typeof op.id!=='string'||b.applied[op.id])return;let text=String(op.text||'').trim().slice(0,150);
 if(op.kind==='target'){let n=Number(op.value);if(!Number.isFinite(n)||n<=0)return;b.targets||={};b.targets[k]=n;}
 else if(op.kind==='count'){let n=Number(op.value);if(!Number.isFinite(n)||n<=0)return;b.counts[k]=(b.counts[k]||0)+n;}
 else if(op.kind==='item'&&text)b.items.push({id:op.id,text,who:b.members[op.who]?op.who:'',ok:false});
 else if(op.kind==='role'){let it=b.items.find(t=>t.id===op.item);if(!it||op.who&&!b.members[op.who])return;it.who=op.who||'';}
 else if(op.kind==='check'){let it=b.items.find(t=>t.id===op.item);if(!it)return;it.ok=!!op.ok;}
 else if(op.kind==='poll'&&text){let options=Array.from(new Set((op.options||[]).map(v=>String(v).trim().slice(0,80)).filter(Boolean))).slice(0,16);if(options.length<2)return;b.polls.push({id:op.id,kind:op.pollType==='날짜'?'날짜':'후보지',title:text,options,votes:{}});}
 else if(op.kind==='vote'){let p=b.polls.find(t=>t.id===op.poll);if(!p)return;p.votes[k]=(op.options||[]).filter(x=>p.options.includes(x));}
 else if(op.kind==='photo'&&validPhoto(op.photo))b.photos.push({id:op.id,photo:op.photo,text,by:k,at:Date.now()});else return;
 b.applied[op.id]=true;b.rev++;syncBoard(s,b);if(op.kind==='photo')sendBoardPhoto(s,b,b.photos[b.photos.length-1],Object.keys(b.members));
}
export async function boardOp(key,op){await mutate(s=>{let b=s.boards[key];if(!b||!b.members[s.me])throw Error('참여 중인 도전을 선택해 주세요.');let a={...op,id:id()};if(s.me===b.owner)applyOp(s,b,s.me,a);else queue(s,b.owner,'boardOp',{board:key,op:a});});}
function validPhoto(v){return typeof v==='string'&&/^data:image\/jpeg;base64,[A-Za-z0-9+/=]+$/.test(v)&&v.length<120000;}
function cleanCard(c){if(!c||typeof c.title!=='string')return null;return {title:c.title.slice(0,150),photo:validPhoto(c.photo)?c.photo:'',note:String(c.note||'').slice(0,1200),rate:c.rate==null?null:Math.max(0,Math.min(100,Number(c.rate)||0)),date:String(c.date||'').slice(0,18),steps:(Array.isArray(c.steps)?c.steps:[]).slice(0,60).map(x=>String(x).slice(0,120))};}
async function applyPacket(from,p){
 if(!/^[a-f0-9]{64}$/.test(from)||!/^[a-f0-9]{32}$/.test(p?.id)||typeof p.type!=='string'||!p.data)return;
 await mutate(s=>{let k=from,d=p.data,c=s.contacts[k];if(s.seen[p.id]||k===s.me||c?.status!=='friend')return;
  if(p.type==='post'){if(d.owner!==k||!cleanCard(d.card)||!/^[a-f0-9]{32}$/.test(d.id))return;let old=s.posts[d.id];if(old&&old.owner!==k)return;if(!old||old.rev<d.rev)s.posts[d.id]={...d,card:cleanCard(d.card),audience:[],comments:Array.isArray(d.comments)?d.comments:[],reactions:d.reactions||{}};}
  else if(p.type==='engage'){let post=s.posts[d.postId];if(post?.owner===s.me&&post.audience.includes(k)&&d.op)engage(s,post,k,d.op);}
  else if(p.type==='gift'){let card=cleanCard(d.card);if(card)s.gifts[p.id]={id:p.id,from:k,at:p.at,card};}
  else if(p.type==='invite'){if(!s.boards[d.board]&&/^[a-f0-9]{32}$/.test(d.board))s.invites[d.board]={from:k,board:d.board,title:String(d.title).slice(0,150),goal:d.goal,unit:d.unit};}
  else if(p.type==='join'){let b=s.boards[d.board];if(b?.owner===s.me&&b.pending[k]){delete b.pending[k];if(d.yes){b.members[k]=c.name;b.rev++;syncBoard(s,b);for(let photo of b.photos)sendBoardPhoto(s,b,photo,[k]);}}}
  else if(p.type==='boardPhoto'){let b=s.boards[d.board];if(b?.owner!==k||!validPhoto(d.photo))return;let photo=b.photos.find(i=>i.id===d.id);if(!photo)return;photo.photo=d.photo;}
  else if(p.type==='boardState'){if(!/^[a-f0-9]{32}$/.test(d.id)||!Array.isArray(d.photos)||!Array.isArray(d.items)||!Array.isArray(d.polls)||!d.counts||typeof d.title!=='string')return;let old=s.boards[d.id],inv=s.invites[d.id];if(d.owner!==k||!d.members?.[s.me]||!(old?.owner===k||inv?.from===k&&inv.wait))return;if(!old||old.rev<d.rev){const prev=new Map((old?.photos||[]).map(i=>[i.id,i.photo]));s.boards[d.id]={...d,photos:d.photos.map(i=>({...i,photo:prev.get(i.id)||''})),pending:{},applied:{}};delete s.invites[d.id];}}
  else if(p.type==='boardOp'){let b=s.boards[d.board];if(b?.owner===s.me)applyOp(s,b,k,d.op);}
  s.seen[p.id]=Date.now();
 });
}
async function receivePiece(from,p){
 if(!p||p.app!=='kkumgalpi2'||!/^([a-f0-9]{32})$/.test(p.id)||!Number.isInteger(p.total)||p.total<1||p.total>240||!Number.isInteger(p.n)||p.n<0||p.n>=p.total||typeof p.part!=='string'||p.part.length>11000||T.social.seen?.[p.id])return;
 if(T.social.contacts?.[from]?.status!=='friend')return;
 let key=from+'.'+p.id;let old=await get('wire',key)||{total:p.total,parts:{},at:Date.now()};if(old.total!==p.total)return;old.parts[p.n]=p.part;await tx(['wire'],t=>t.objectStore('wire').put(old,key));if(Object.keys(old.parts).length!==p.total)return;
 let encoded=Array.from({length:p.total},(_,i)=>old.parts[i]).join(''),packet=JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(encoded),c=>c.charCodeAt(0))));if(packet.id!==p.id)return;await applyPacket(from,packet);if(T.social.seen?.[p.id]){await tx(['wire'],t=>t.objectStore('wire').delete(key));clearTimeout(heldTimer);heldTimer=setTimeout(()=>{pieces=pieces.then(retryHeld).catch(()=>{});},500);}
}
export async function retryHeld(){for(let k of await keys('wire')){let r=await get('wire',k);if(!r)continue;if(Date.now()-r.at>30*86400000){await tx(['wire'],t=>t.objectStore('wire').delete(k));continue;}if(Object.keys(r.parts).length===r.total)await receivePiece(k.split('.')[0],{app:'kkumgalpi2',id:k.split('.')[1],n:0,total:r.total,part:r.parts[0]});}}
