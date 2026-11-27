if(location.hash==='#memo'){var splash=document.getElementById('splash');if(splash)splash.style.display='none';}
if(typeof globalThis==='undefined')window.globalThis=window;
if(!String.prototype.replaceAll)String.prototype.replaceAll=function(a,b){return this.split(a).join(b)};
if(!Object.fromEntries)Object.fromEntries=function(it){var o={};for(var e of it)o[e[0]]=e[1];return o};
if(!Array.prototype.flat)Array.prototype.flat=function(){return [].concat.apply([],this)};
if(!Blob.prototype.arrayBuffer)Blob.prototype.arrayBuffer=function(){var b=this;return new Promise(function(ok,no){var r=new FileReader();r.onload=function(){ok(r.result)};r.onerror=no;r.readAsArrayBuffer(b)})};
