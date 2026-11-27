import {h,Fragment} from 'preact';import {useEffect,useRef,useState} from 'preact/hooks';
import {appName,T,findDream,personal,cover,progress,stamp,allPhotos,id,notice,change,displayDate} from './store';
import {Btn,Head,Select,Field,Check,Group,Chips,Empty,Pic,go,safe} from './ui';
import {drawCard,cardDefaults,canvasBlob,output,slideshow,imageBlob,readURL} from './media';
import {inAppFriendsEnabled} from './features';
import {contacts,postDream,gift,social,consent} from './social';
export function Share({d,batch,heading,caption}){
 let [opt,so]=useState({...cardDefaults}),[mode,sm]=useState(batch?.length?'여러 꿈':'한 가지 꿈'),[selected,ss]=useState(batch?.slice(0,9)||[d.id]),[photoIds,sp]=useState([]),[mask,sk]=useState(false),[radius,sr]=useState(.055),[sending,send]=useState(''),[targets,st]=useState(d.shareWith||[]),[includeSteps,setSteps]=useState(false),[rendered,se]=useState(null),[error,serr]=useState('');
 const friends=inAppFriendsEnabled ? contacts() : [];
 let cfg=(k,v)=>so(x=>({...x,[k]:v}));let photos=allPhotos(d),available=personal(),group=selected.map(findDream).filter(Boolean);
 let multi=mode==='여러 꿈',card=multi?{...d,title:heading||'나의 꿈 '+group.length+'가지',note:caption||group.map(x=>x.title).join(' · '),complete:group.length>0&&group.every(t=>t.complete),completedAt:0}:d;
 let refs=multi?group.map(cover):mode==='과정 카드'?(photoIds.length?photoIds:photos.slice(0,3)):[cover(d)];
 useEffect(()=>{let alive=true;se(null);serr('');drawCard(card,opt,refs).then(c=>{if(alive)se(c)}).catch(e=>{if(alive)serr(e.message)});return()=>{alive=false}},[d.updated,JSON.stringify(opt),mode,selected.join(','),photoIds.join(',')]);
 let preview=useRef();useEffect(()=>{if(!rendered||!preview.current)return;const c=preview.current;c.width=rendered.width;c.height=rendered.height;c.getContext('2d').drawImage(rendered,0,0);},[rendered]);
 function clickPicture(e){if(!mask)return;let r=e.currentTarget.getBoundingClientRect();cfg('masks',[...opt.masks,{x:(e.clientX-r.left)/r.width,y:(e.clientY-r.top)/r.height,r:radius}]);}
 const getBlob=async()=>{if(!rendered)throw Error('카드가 준비될 때까지 기다려 주세요.');if(multi&&!group.length)throw Error('카드에 담을 꿈을 선택해 주세요.');return canvasBlob(rendered);};
 async function payload(){let photo=await readURL(await imageBlob(await getBlob(),420,.72));return {title:card.title,photo,note:opt.note?card.note:'',date:opt.date?displayDate(card):'',rate:opt.rate?progress(card):null,steps:includeSteps&&!multi?d.steps.map(x=>x.text):[]};}
 async function shareFile(action){await output(await getBlob(),appName+'-꿈카드.jpg',action);}
 const addTarget=k=>st(vals=>vals.includes(k)?vals.filter(x=>x!==k):[...vals,k]);
 return <><Head title="꿈 공유"/><Chips items={['한 가지 꿈','과정 카드','여러 꿈']} v={mode} on={v=>{sm(v);cfg('masks',[]);}}/>
 {multi&&<Group title={'카드에 담을 꿈 · '+selected.length+' / 9'}>{available.map(x=><Check v={selected.includes(x.id)} on={v=>{if(v&&selected.length>=9){notice('카드 한 장에는 아홉 개까지 담아요.');return;}ss(v?[...selected,x.id]:selected.filter(k=>k!==x.id));}}>{x.title}</Check>)}</Group>}
 {mode==='과정 카드'&&<Group title="과정 사진 고르기">{photos.length?photos.map((p,i)=><Check v={photoIds.includes(p)} on={v=>sp(v?[...photoIds,p].slice(0,9):photoIds.filter(x=>x!==p))}>사진 {i+1}</Check>):<Empty>진행 일기에 중간 사진을 먼저 담아 주세요.</Empty>}</Group>}
 <canvas ref={preview} class="editcanvas cardpreview" onClick={clickPicture} aria-label="공유 카드 미리보기" style={{display:rendered?'block':'none'}}/>{error&&<Empty>{error}</Empty>}
 <Group title="카드 디자인 · 비율 · 공유할 내용"><div class="splitcols"><Select label="디자인" v={opt.design} on={v=>cfg('design',v)} items={['엽서','필름','기록지']}/><Select label="이미지 비율" v={opt.ratio} on={v=>so({...opt,ratio:v,masks:[]})} items={['정사각형','스토리','가로']}/></div>{[['rate','진행률'],['date','날짜'],['note','한 줄 소감']].map(([k,t])=><Check v={opt[k]} on={v=>cfg(k,v)}>{t} 표시</Check>)}{friends.length>0&&<Check v={includeSteps} on={setSteps}>앱 친구에게 작은 단계도 공유</Check>}</Group>
 <Group title="사진 자르기 · 얼굴 가리기"><Field type="range" label="확대해 자르기" min="1" max="3" step=".05" v={opt.zoom} on={v=>cfg('zoom',+v)}/><Field label="좌우 위치" type="range" min="0" max="1" step=".02" v={opt.cx} on={v=>cfg('cx',+v)}/><Field label="상하 위치" type="range" min="0" max="1" step=".02" v={opt.cy} on={v=>cfg('cy',+v)}/>
 <Check v={mask} on={sk}>카드를 눌러 해당 부분 가리기</Check><Field type="range" min=".02" max=".18" step=".005" label="가림 크기" v={radius} on={v=>sr(+v)}/><Btn onClick={()=>cfg('masks',opt.masks.slice(0,-1))}>최근 가림 취소</Btn></Group>
 {friends.length>0&&<Btn kind="primary" onClick={()=>send(sending==='app'?'':'app')}>앱 친구에게</Btn>}
 {friends.length>0&&sending==='app'&&<div class="box"><h3>이 꿈을 볼 수 있는 친구</h3>{!consent()&&<p>카카오톡으로 바로 보낼 수 있어요. 앱 안에서 게시하려면 <Btn kind="compact" onClick={()=>go('friends')}>친구 소식 연결 설정</Btn>을 열어 주세요.</p>}{Object.entries(social().groups||{}).map(([n,ids])=><Btn kind="compact" onClick={()=>st([...new Set([...targets,...ids])])}>{n} 그룹 선택</Btn>)}{friends.map(p=><Check v={targets.includes(p.key)} on={()=>addTarget(p.key)}>{p.name}</Check>)}
 <div class="toolbar"><Btn onClick={async()=>{await change(d.id,x=>{x.shareWith=targets;});notice(targets.length?'공개 대상을 저장했어요.':'나만 보기로 저장했어요.');}}>공개 범위 저장</Btn><Btn onClick={async()=>{await postDream(await payload(),targets);await change(d.id,x=>{x.shareWith=targets});}}>선택한 친구에게 게시</Btn></div>
 </div>}
 <Btn kind="yellow" onClick={()=>shareFile('kakao')}>카카오톡</Btn><div class="pair"><Btn onClick={()=>shareFile('share')}>다른 SNS로</Btn><Btn onClick={()=>shareFile('save')}>이미지 저장</Btn></div>
 <div class="toolbar"><Btn onClick={()=>window.Device?.copy([card.title,opt.date?displayDate(card):'',opt.rate?progress(card)+'%':'',opt.note?card.note:''].filter(Boolean).join('\n'))}>게시글 문구 복사</Btn>{friends.length>0&&<Btn onClick={()=>send(sending==='gift'?'':'gift')}>꿈 엽서 선물</Btn>}</div>
 {friends.length>0&&sending==='gift'&&<div class="box">{friends.map(p=><Btn onClick={async()=>gift(p.key,await payload())}>{p.name}에게 제안</Btn>)}</div>}
 <Group title="사진 슬라이드 영상"><p class="muted">진행 사진을 짧은 세로 영상으로 저장해요. 저장하는 동안 화면을 열어 두세요.</p><Btn onClick={()=>slideshow(d,photoIds.length?photoIds:photos)}>영상 만들기 · {(photoIds.length?photoIds:photos).length}장</Btn></Group></>;
}
