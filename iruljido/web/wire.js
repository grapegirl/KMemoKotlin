// Loaded only when the user enables remote friends. Relay operators see addresses and traffic, not dream text/photos.
import {getPublicKey,verifyEvent,finalizeEvent,getEventHash} from 'nostr-tools/pure';
import {wrapEvent} from 'nostr-tools/nip59';
import {v2} from 'nostr-tools/nip44';
const DEFAULT=['wss://relay.damus.io','wss://auth.nostr1.com','wss://nos.lol'];
const encoder=new TextEncoder(),decoder=new TextDecoder();let sk,me,cb,urls=[],connections=new Map(),active=false,queue=Promise.resolve(),seq=0,known=new Set();
function b64(bytes){let s='';for(let k=0;k<bytes.length;k+=8192)s+=String.fromCharCode(...bytes.subarray(k,k+8192));return btoa(s);}
function init(base64){sk=Uint8Array.from(atob(base64),c=>c.charCodeAt(0));me=getPublicKey(sk);return {id:me};}
function verifiedUnwrap(e){
 if(e.kind!==1059||!verifyEvent(e)||!e.tags.some(t=>t[0]==='p'&&t[1]===me))throw Error('envelope');
 const decrypt=x=>JSON.parse(v2.decrypt(x.content,v2.utils.getConversationKey(sk,x.pubkey)));
 let seal=decrypt(e);if(seal.kind!==13||!verifyEvent(seal))throw Error('seal');
 let rumor=decrypt(seal);if(rumor.kind!==14||rumor.pubkey!==seal.pubkey||getEventHash(rumor)!==rumor.id||!rumor.tags.some(t=>t[0]==='p'&&t[1]===me))throw Error('sender');
 return {from:seal.pubkey,piece:JSON.parse(rumor.content)};
}
function sub(c){if(c.ws.readyState===1)c.ws.send(JSON.stringify(['REQ','dreams',{kinds:[1059],'#p':[me],limit:500}]));}
function send(c,data){if(c.ws.readyState===1)c.ws.send(JSON.stringify(data));}
function connect(url){
 if(!active||connections.has(url))return;let ws;try{ws=new WebSocket(url)}catch{return;}
 let c={ws,waiting:new Map(),recent:[],auth:'',authenticated:false,pageCount:0,oldest:Infinity};connections.set(url,c);
 ws.onopen=()=>{sub(c);cb.status();};
 ws.onmessage=ev=>{if(typeof ev.data!=='string'||ev.data.length>400000)return;let msg;try{msg=JSON.parse(ev.data);}catch{return;}
  let [cmd,a,b,data]=msg;
  if(cmd==='AUTH'&&typeof a==='string'&&a.length<1024){let resp=finalizeEvent({kind:22242,created_at:Date.now()/1000|0,content:'',tags:[['relay',url],['challenge',a]]},sk);c.auth=resp.id;send(c,['AUTH',resp]);return;}
  if(cmd==='OK'){
   if(a===c.auth&&b){c.authenticated=true;sub(c);return;}
   let p=c.waiting.get(a);if(p){c.waiting.delete(a);b?p.ok():p.no(new Error(String(data||'중계 거절')));}return;
  }
  if(cmd==='EOSE'&&(a==='dreams'||a==='older')){if(c.pageCount>=500&&isFinite(c.oldest)){send(c,['CLOSE','older']);send(c,['REQ','older',{kinds:[1059],'#p':[me],until:c.oldest-1,limit:500}]);}else if(a==='older')send(c,['CLOSE','older']);c.pageCount=0;c.oldest=Infinity;return;}
  if(cmd!=='EVENT'||!b)return;
  if(a!=='dreams'&&a!=='older')return;c.pageCount++;c.oldest=Math.min(c.oldest,b.created_at);
  if(known.has(b.id))return;known.add(b.id);if(known.size>8000)known.delete(known.values().next().value);
  queue=queue.then(async()=>{if(!active)return;try{let v=verifiedUnwrap(b);await cb.receive(v.from,v.piece);}catch{known.delete(b.id);}}).catch(()=>{known.delete(b.id);});
 };
 ws.onerror=()=>{};ws.onclose=()=>{for(let p of c.waiting.values())p.no(new Error('중계 연결 종료'));c.waiting.clear();if(connections.get(url)===c)connections.delete(url);if(active){cb.status();let epoch=seq;setTimeout(()=>{if(active&&seq===epoch)connect(url)},12000);}};
}
function stop(){active=false;seq++;for(let c of connections.values()){c.ws.onclose=null;c.ws.close();for(let p of c.waiting.values())p.no(Error('연결 해제'));}connections.clear();known.clear();}
function start(base64,custom,callbacks){stop();let who=init(base64);cb=callbacks;active=true;urls=(custom?.length?custom:DEFAULT).filter(x=>/^wss:\/\//.test(x));for(let url of urls)connect(url);return who;}
function online(){return Array.from(connections.values()).filter(c=>c.ws.readyState===1);}
async function publish(e){
 let con=online();if(!con.length)throw Error('중계 연결 대기');
 return new Promise((ok,no)=>{let bad=0;for(let c of con){let timeout=setTimeout(()=>{c.waiting.delete(e.id);if(++bad===con.length)no(Error('중계 응답 대기'));},9000);
  c.waiting.set(e.id,{ok:()=>{clearTimeout(timeout);ok();},no:()=>{clearTimeout(timeout);if(++bad===con.length)no(Error('중계 전송 실패'));}});send(c,['EVENT',e]);}});
}
async function deliver(to,payload,cursor,onAck){
 let bytes=encoder.encode(JSON.stringify(payload)),encoded=b64(bytes),size=11000,count=Math.ceil(encoded.length/size);if(count>240)throw Error('공유 내용이 너무 커요.');
 for(let n=cursor||0;n<count;n++){let content=JSON.stringify({app:'kkumgalpi2',id:payload.id,n,total:count,part:encoded.slice(n*size,(n+1)*size)});
  await publish(wrapEvent({kind:14,created_at:Math.floor(Date.now()/1000),tags:[['p',to]],content},sk,to));await onAck(n+1,count);}
}
window.DreamWire={start,stop,online:()=>online().length,deliver};
