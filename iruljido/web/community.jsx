import {h,Fragment} from 'preact';import {useState} from 'preact/hooks';
import {T,findDream,personal,cover,id,stamp,normalize,freshFrom,notice,num,putDream} from './store';
import {social,consent,enable,disable,netCount,contacts,groupSave,queued,flush,boards,posts,label,interact,createBoard,inviteToBoard,inviteReply,boardOp} from './social';
import {Meter,Head,Btn,Field,Text,Select,Check,Group,Empty,Pic,Pick,Chips,go,replace,safe} from './ui';
import {imageLoad,imageBlob,readURL} from './media';
const near=()=>{try{return JSON.parse(Device.people())}catch{return {name:'',id:'',peers:[],inbox:[]}}};
function goFromCard(c){go('edit',{seed:normalize({...freshFrom(c),cover:c.photo||'',status:'someday'})});}
function OnlineSettings(){
 let s=social(),[nick,sn]=useState(s.nick||s.cardNick||''),[servers,ss]=useState((s.relays||[]).join('\n'));
 return <div class="box"><h3>원격 친구 연결</h3><p class="muted">친구 소식과 공동 목록은 Nostr 중계 서비스를 통해 주고받아요. 선택한 친구에게 보내는 글과 사진은 암호화돼요. 중계 서비스는 수신 ID·접속 정보·암호문을 처리해요.</p>
 <Field label="친구에게 표시할 이름" maxLength={25} v={nick} on={sn}/><Group title="중계 서버 설정"><Text label="직접 사용할 wss 주소 (한 줄에 하나)" v={servers} on={ss}/><small>기본 중계 · relay.damus.io, auth.nostr1.com, nos.lol</small></Group>
 <Btn kind="primary" onClick={()=>enable(nick,servers.split(/\s+/).filter(Boolean))}>{consent()?'닉네임·연결 저장':'동의하고 원격 친구 켜기'}</Btn>{consent()&&<Btn kind="wide" onClick={disable}>원격 연결 끄기</Btn>}</div>;
}
export function Friends({initialTab='친구 소식'}) {
 const s=social();
 const [tab,st]=useState(initialTab), [group,sg]=useState(''), [members,sm]=useState([]);
 const [legacy]=useState(near);
 const friends=contacts(), oldFriends=(legacy.peers||[]).filter(p=>p.rel==='friend');
 const savedGroups=Object.entries(s.groups||{}), gifts=Object.values(s.gifts||{}), feed=posts();
 const hasRecords=friends.length || oldFriends.length || savedGroups.length || gifts.length || feed.length || legacy.inbox?.length || boards().length || Object.keys(s.invites||{}).length || consent();
 const tabs=['친구 소식'];
 if(friends.length || oldFriends.length)tabs.push('친구 목록');
 if(friends.length || savedGroups.length)tabs.push('친구 그룹');
 const page=tabs.includes(tab)?tab:'친구 소식';
 const dreams=personal();
 return <><Head title="친구와 공유"/>
 {tabs.length>1&&<Chips items={tabs} v={page} on={st}/>}
 {page==='친구 목록'&&<>
   {friends.length>0&&<><h2>내 친구 <small>{friends.length}명</small></h2>{friends.map(c=><div class="person" key={c.key}><strong>{c.name}</strong><small>{c.key.slice(0,10)}</small></div>)}</>}
   {oldFriends.length>0&&<Group title="이전 버전의 친구">{oldFriends.map(c=><div class="person" key={c.id}><b>{c.name}</b></div>)}</Group>}
 </>}
 {page==='친구 그룹'&&<><Field label="그룹 이름" v={group} on={sg} placeholder="가족, 독서 모임…"/>{friends.map(c=><Check key={c.key} v={members.includes(c.key)} on={v=>sm(v?[...members,c.key]:members.filter(k=>k!==c.key))}>{c.name}</Check>)}<Btn kind="primary" onClick={async()=>{await groupSave(group,members);sg('');sm([]);notice('친구 그룹을 저장했어요.');}}>그룹 저장</Btn>{savedGroups.map(([n,list])=><button key={n} class="dreamrow" onClick={()=>{sg(n);sm(list);}}><b>{n}</b><small>{list.map(label).join(', ')||'멤버 없음'}</small></button>)}</>}
 {page==='친구 소식'&&<>
   <h2>공유할 꿈</h2><p class="muted">꿈 카드를 카카오톡이나 다른 SNS로 보낼 수 있어요.</p>
   {dreams.length?<Select label="공유할 꿈 선택" v="" on={k=>k&&go('share',{id:k})} items={[['','꿈 고르기'],...dreams.map(d=>[d.id,d.title])]}/>:<Btn kind="primary" onClick={()=>go('edit')}>첫 꿈 작성</Btn>}
   {hasRecords?<><Btn kind="wide" onClick={()=>go('boards')}>함께 도전</Btn>
   {gifts.length>0&&<h2>받은 꿈 선물</h2>}{gifts.map(g=><div class="paper" key={g.id}><small>{label(g.from)}의 꿈 엽서</small>{g.card.photo&&<img class="preview" src={g.card.photo}/>}<h3>{g.card.title}</h3><p>{g.card.note}</p><Btn onClick={()=>goFromCard(g.card)}>내 목록에 담기</Btn></div>)}
   <h2>공유한 기록</h2>{feed.length?feed.map(p=><Post key={p.id} p={p}/>):<Empty>친구와 주고받은 기록과 응원이 모여요.</Empty>}
   {!!legacy.inbox?.length&&<Group title="이전에 받은 꿈 카드">{legacy.inbox.map(c=><button key={c.uuid} class="dreamrow" onClick={()=>go('nearCard',{card:c})}><b>{c.title}</b><small>{c.from}</small></button>)}</Group>}
   <Group title="친구 소식 연결 설정"><OnlineSettings/></Group></>:null}
 </>}
 {consent()&&<div class="between"><small>중계 {netCount}곳 연결 · 전송 대기 {queued()}건</small><Btn kind="compact" onClick={flush}>다시 보내기</Btn></div>}</>;
}
function Post({p}){let [msg,sm]=useState(''),[reply,sr]=useState('');let c=p.card;return <article class="paper"><small>{label(p.owner)} · {stamp(p.at)}</small>{c.photo&&<img class="preview" src={c.photo} alt="공유한 꿈 카드"/>}<h3>{c.title}</h3>{c.note&&<p>{c.note}</p>}{c.rate!=null&&<span class="badge">{c.rate}%</span>}<div class="toolbar"><Btn kind="compact" onClick={()=>goFromCard(c)}>내 꿈에 복사</Btn>{['응원해','멋지다','같이 가자'].map(t=><Btn kind="compact" onClick={()=>interact(p.id,'react',t)}>{t} {Object.values(p.reactions).filter(x=>x===t).length||''}</Btn>)}</div>
 <Group title={'댓글 · '+p.comments.length}>{p.comments.map(v=><div class="entry"><small>{v.reply?'↳ '+(p.comments.find(c=>c.id===v.reply)?.name||'댓글')+'에게 · ':''}{v.name}</small><p>{v.text}</p><Btn kind="compact" onClick={()=>sr(v.id)}>답글</Btn></div>)}{reply&&<Btn kind="compact" onClick={()=>sr('')}>답글 취소</Btn>}<Field label={reply?'답글':'댓글'} maxLength={500} v={msg} on={sm}/><Btn onClick={async()=>{if(!msg.trim())throw Error('내용을 적어 주세요.');await interact(p.id,'comment',msg,reply);sm('');sr('');}}>보내기</Btn></Group></article>}
export function NearCard({card}){let data=window.Device?.card(card.uuid);return <><Head title="받은 꿈 카드"/>{data&&<img class="preview cardpreview" src={'data:image/jpeg;base64,'+data}/>}<h2>{card.title}</h2><p>{card.from} · {stamp(card.date)}</p><Btn kind="primary" onClick={()=>goFromCard({title:card.title,photo:data?'data:image/jpeg;base64,'+data:''})}>내 꿈으로 가져오기</Btn></>;}
export function Boards({dreamId}){
 let s=social(),d=findDream(dreamId),[title,st]=useState(d?.title||''),[unit,su]=useState('회'),[goal,sg]=useState(''),[ids,si]=useState([]);
 return <><Head title="함께 도전"/>{!consent()&&<><p>서로 다른 장소에서 공동 목록을 주고받으려면 원격 친구 연결을 켜 주세요.</p><OnlineSettings/></>}
 <Group title="＋ 공동 목록 만들기" open={!!d}><Field label="도전 이름" v={title} on={st} maxLength={80}/><div class="splitcols"><Field label="협동 목표 수량" v={goal} on={sg} type="number" min="0"/><Field label="단위" v={unit} on={su} maxLength={12}/></div>{contacts().length>0&&<h3>같이 할 친구</h3>}{contacts().map(c=><Check v={ids.includes(c.key)} on={v=>si(v?[...ids,c.key]:ids.filter(k=>k!==c.key))}>{c.name}</Check>)}<Btn kind="primary" onClick={async()=>{let key=await createBoard(title,goal,unit,ids,d?.title);go('board',{bid:key})}}>도전 만들기</Btn></Group>
 {Object.values(s.invites||{}).map(i=><div class="box"><small>{label(i.from)}의 초대</small><h3>{i.title}</h3>{i.wait?<p>수락 전달 중 · 만든 사람의 앱에서 동기화되면 열려요.</p>:<div class="pair"><Btn onClick={()=>inviteReply(i.board,true)}>같이 할래</Btn><Btn onClick={()=>inviteReply(i.board,false)}>거절</Btn></div>}</div>)}
 <h2>우리의 목록</h2>{boards().map(b=><button class="dreamrow" onClick={()=>go('board',{bid:b.id})}><span class="grow"><b>{b.title}</b><small>{Object.values(b.members).join(', ')}</small></span>›</button>)}</>;
}
export function Board({bid}){
 let b=social().boards?.[bid],[item,si]=useState(''),[who,sw]=useState(''),[count,sc]=useState('1'),[personalGoal,sPg]=useState(''),[poll,sp]=useState('날짜'),[question,sq]=useState(''),[options,so]=useState(''),[caption,sCap]=useState('');
 if(!b)return <Empty>도전을 찾지 못했어요.</Empty>;let me=social().me,owner=b.owner===me,opts=[['','담당 미정'],...Object.entries(b.members)],sum=Object.values(b.counts).reduce((a,v)=>a+num(v),0);
 const act=op=>boardOp(b.id,op);
 return <><Head title="공동 버킷리스트"/><div class="paper"><h2>{b.title}</h2><small>{Object.values(b.members).join(' · ')}</small><p>협동 기록 <b>{sum}{b.unit}</b>{b.goal>0?' / '+b.goal+b.unit:''}</p></div>
 <p class="muted">각자의 기록은 도전을 만든 사람의 앱이 연결되면 참여자에게 동기화돼요.</p>
 <Group title="개인별 기록과 협동 목표" open>{Object.entries(b.members).map(([k,n])=><div><div class="between"><b>{n}</b><span>{b.counts[k]||0}{b.targets?.[k]?' / '+b.targets[k]:''}{b.unit}</span></div>{b.targets?.[k]>0&&<Meter n={100*(b.counts[k]||0)/b.targets[k]}/>}</div>)}<Field label={'내 기록 추가 ('+b.unit+')'} type="number" min="0" step="any" v={count} on={sc}/><Btn onClick={()=>{if(!num(count))throw Error('수량을 입력해 주세요.');return act({kind:'count',value:num(count)});}}>기록 더하기</Btn><Field label="내 개인 목표" type="number" min="0" v={personalGoal} on={sPg}/><Btn onClick={()=>{if(!num(personalGoal))throw Error('목표 수량을 적어 주세요.');return act({kind:'target',value:num(personalGoal)});}}>개인 목표 저장</Btn></Group>
 <Group title="함께 할 일과 역할" open>{b.items.map(i=><div class="box"><Check v={i.ok} on={v=>act({kind:'check',item:i.id,ok:v})}>{i.text}</Check><Select label="담당" v={i.who||''} on={k=>act({kind:'role',item:i.id,who:k})} items={opts}/></div>)}<Field label="새 할 일" v={item} on={si}/><Select label="담당자" v={who} on={sw} items={opts}/><Btn onClick={async()=>{if(!item.trim())throw Error('할 일을 적어 주세요.');await act({kind:'item',text:item,who});si('');}}>목록에 추가</Btn></Group>
 <Group title="일정 · 후보지 투표" open>{b.polls.map(p=><Poll key={p.id} p={p} b={b}/>)}<Select label="투표 종류" v={poll} on={sp} items={['날짜','후보지']}/><Field label="질문" v={question} on={sq} placeholder={poll==='날짜'?'언제 갈까요?':'어디로 갈까요?'}/><Text label="선택지 (한 줄에 하나)" v={options} on={so} placeholder={poll==='날짜'?'12월 5일\n12월 12일':'제주\n강릉'}/><Btn onClick={async()=>{let o=Array.from(new Set(options.split('\n').map(s=>s.trim()).filter(Boolean)));if(!question.trim()||o.length<2)throw Error('질문과 선택지 두 개 이상을 적어 주세요.');await act({kind:'poll',text:question,pollType:poll,options:o});sq('');so('');}}>투표 열기</Btn></Group>
 <Group title={'공동 인증 앨범 · '+b.photos.length} open>{b.photos.map(p=><div class="paper">{p.photo?<img class="preview" src={p.photo}/>:<small>사진을 받는 중…</small>}<b>{p.text}</b><small> {b.members[p.by]} · {stamp(p.at)}</small></div>)}<Field label="사진 설명" v={caption} on={sCap}/><Pick label="공동 앨범에 사진 올리기" on={async f=>{let photo=await readURL(await imageBlob(f[0],400,.67));await act({kind:'photo',photo,text:caption});sCap('');}}/></Group>
 {owner&&contacts().some(p=>!b.members[p.key])&&<Group title="공동 도전에 초대">{contacts().filter(p=>!b.members[p.key]).map(p=><Btn onClick={()=>inviteToBoard(bid,p.key)}>{p.name} {b.pending[p.key]?'다시 초대':'초대'}</Btn>)}</Group>}</>;
}
function Poll({p,b}){let me=social().me,initial=p.votes[me]||[],[choices,sc]=useState(initial);return <div class="box"><span class="badge">{p.kind}</span><h3>{p.title}</h3>{p.options.map(o=><Check v={choices.includes(o)} on={v=>sc(v?[...choices,o]:choices.filter(x=>x!==o))}>{o} · {Object.values(p.votes).filter(a=>a.includes(o)).length}표</Check>)}<Btn onClick={()=>boardOp(b.id,{kind:'vote',poll:p.id,options:choices})}>내 투표 저장</Btn></div>}
