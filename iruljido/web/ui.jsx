import {h,Fragment} from 'preact';import {useState,useEffect,useRef} from 'preact/hooks';
import {source,cover,progress,nextStep,notice,id,normalize,T} from './store';
import {pageEnabled} from './features';
export const routes=[];let route={name:'home',key:0};let navListener=()=>{};
export function navigation(listener){navListener=listener;}
export function where(){return route;}
export function go(name,params={}){if(!pageEnabled(name))return;routes.push({...route,scroll:window.scrollY});route={...params,name,key:id()};navListener({...route});window.scrollTo(0,0);}
export function replace(name,params={}){if(!pageEnabled(name))return;let prev=routes[routes.length-1];if(prev&&prev.name===name&&prev.id===params.id)routes.pop();route={...params,name,key:id()};navListener({...route});window.scrollTo(0,0);}
export function home(){routes.length=0;replace('home');}
export function back(){if(document.querySelector('dialog[open]')){document.querySelector('dialog[open]').close();return;}if(!routes.length){if(route.name==='home'||window.Device?.isMemoWindow?.()){window.Device?.exit();return;}home();return;}route=routes.pop();navListener({...route});setTimeout(()=>scrollTo(0,route.scroll||0),0);}
window.back=back;
export function safe(fn){return async e=>{try{await fn(e)}catch(err){notice(err?.message||'처리하지 못했어요. 다시 시도해 주세요.');console.error(err);}}}
export function Btn({children,onClick,kind='',...props}){const [busy,setBusy]=useState(false);return <button {...props} disabled={busy||props.disabled} class={'btn '+kind+' '+(props.class||'')} onClick={safe(async e=>{if(!onClick)return;const p=onClick(e);if(p&&typeof p.then==='function'){setBusy(true);try{await p}finally{setBusy(false)}}})}>{children}</button>}
export function Head({title,right}){return <header class="head"><button class="back" onClick={back} aria-label="뒤로">‹</button><h1>{title}</h1>{right}</header>}
export function Field({label,v,on,type='text',...p}){return <label class="field"><span>{label}</span><input aria-label={label} {...p} type={type} value={v??''} onInput={e=>on(e.currentTarget.value)}/></label>}
export function Text({label,v,on,...p}){return <label class="field"><span>{label}</span><textarea aria-label={label} {...p} value={v||''} onInput={e=>on(e.currentTarget.value)}/></label>}
export function Select({label,v,on,items}){return <label class="field"><span>{label}</span><select aria-label={label} value={v} onChange={e=>on(e.currentTarget.value)}>{items.map(x=>{let [k,n]=Array.isArray(x)?x:[x,x];return <option value={k}>{n}</option>})}</select></label>}
export function Check({v,on,children}){return <label class="check"><input type="checkbox" checked={!!v} onChange={e=>on(e.currentTarget.checked)}/>{children}</label>}
export function Group({title,children,open=false}){return <details open={open||undefined}><summary>{title}</summary><div class="group-body">{children}</div></details>}
export function Empty({children}){return <p class="empty">{children}</p>}
export function useSrc(ref){let [url,set]=useState('');useEffect(()=>{let ok=true;set('');source(ref||'sun.jpg').then(s=>{if(ok)set(s)}).catch(()=>{});return()=>{ok=false}},[ref]);return url;}
export function Pic({refKey,kind='',...props}){const src=useSrc(refKey);return src?<img src={src} class={kind} alt={props.alt||''} {...props}/>:<span class={'placeholder '+kind}/>;}
export function Media({refKey,type}){let src=useSrc(refKey);if(!src)return null;return type==='audio'?<audio controls preload="none" src={src}/>:<video controls playsInline preload="metadata" src={src}/>;}
export function Meter({n}){return <div class="meter"><i style={{width:Math.max(0,Math.min(100,n))+'%'}}/></div>}
export function DreamRow({d,on}){return <button class="dreamrow" onClick={on||(()=>go('detail',{id:d.id}))}><Pic refKey={cover(d)}/><span class="grow"><strong>{d.title}</strong><small>{d.complete?'완료 인증':nextStep(d)}</small></span><span class="badge">{progress(d)}%</span></button>}
export function Pick({label='사진 선택',accept='image/*',multiple=false,capture=false,on}){let r=useRef();const [busy,set]=useState(false);return <><button disabled={busy} onClick={()=>r.current.click()}>{busy?'준비 중…':label}</button><input class="file" type="file" ref={r} accept={accept} multiple={multiple} capture={capture?'environment':undefined} onChange={safe(async e=>{let files=Array.from(e.currentTarget.files||[]);e.currentTarget.value='';if(!files.length)return;set(true);try{await on(files)}finally{set(false)}})}/></>}
export function Chips({items,v,on}){return <div class="chips">{items.map(i=>{let [k,n]=Array.isArray(i)?i:[i,i];return <button class={k===v?'selected':''} onClick={()=>on(k)}>{n}</button>})}</div>}
export function FileToggle({label,active,on}){return <Btn kind={active?'on':''} onClick={on}>{label}</Btn>}
export function Footer({active}){let links=[['home','⌂','수첩'],['explore','✧','발견'],['plans','▦','일정'],['album','▧','앨범'],['friends','♡','공유']];return <nav>{links.filter(([page])=>pageEnabled(page)).map(([k,icon,t])=><button class={active===k?'active':''} onClick={()=>{if(active===k)return;routes.length=0;replace(k)}}><b aria-hidden="true">{icon}</b>{t}</button>)}</nav>}
