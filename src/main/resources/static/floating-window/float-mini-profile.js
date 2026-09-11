(function(){var e=globalThis,t=e.ShadowRoot&&(e.ShadyCSS===void 0||e.ShadyCSS.nativeShadow)&&`adoptedStyleSheets`in Document.prototype&&`replace`in CSSStyleSheet.prototype,n=Symbol(),r=new WeakMap,i=class{constructor(e,t,r){if(this._$cssResult$=!0,r!==n)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=e,this.t=t}get styleSheet(){let e=this.o,n=this.t;if(t&&e===void 0){let t=n!==void 0&&n.length===1;t&&(e=r.get(n)),e===void 0&&((this.o=e=new CSSStyleSheet).replaceSync(this.cssText),t&&r.set(n,e))}return e}toString(){return this.cssText}},a=e=>new i(typeof e==`string`?e:e+``,void 0,n),o=(e,...t)=>new i(e.length===1?e[0]:t.reduce((t,n,r)=>t+(e=>{if(!0===e._$cssResult$)return e.cssText;if(typeof e==`number`)return e;throw Error(`Value passed to 'css' function must be a 'css' function result: `+e+`. Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.`)})(n)+e[r+1],e[0]),e,n),s=(n,r)=>{if(t)n.adoptedStyleSheets=r.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(let t of r){let r=document.createElement(`style`),i=e.litNonce;i!==void 0&&r.setAttribute(`nonce`,i),r.textContent=t.cssText,n.appendChild(r)}},c=t?e=>e:e=>e instanceof CSSStyleSheet?(e=>{let t=``;for(let n of e.cssRules)t+=n.cssText;return a(t)})(e):e,{is:l,defineProperty:u,getOwnPropertyDescriptor:d,getOwnPropertyNames:ee,getOwnPropertySymbols:te,getPrototypeOf:ne}=Object,f=globalThis,p=f.trustedTypes,re=p?p.emptyScript:``,ie=f.reactiveElementPolyfillSupport,m=(e,t)=>e,h={toAttribute(e,t){switch(t){case Boolean:e=e?re:null;break;case Object:case Array:e=e==null?e:JSON.stringify(e)}return e},fromAttribute(e,t){let n=e;switch(t){case Boolean:n=e!==null;break;case Number:n=e===null?null:Number(e);break;case Object:case Array:try{n=JSON.parse(e)}catch{n=null}}return n}},g=(e,t)=>!l(e,t),_={attribute:!0,type:String,converter:h,reflect:!1,useDefault:!1,hasChanged:g};Symbol.metadata??=Symbol(`metadata`),f.litPropertyMetadata??=new WeakMap;var v=class extends HTMLElement{static addInitializer(e){this._$Ei(),(this.l??=[]).push(e)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(e,t=_){if(t.state&&(t.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(e)&&((t=Object.create(t)).wrapped=!0),this.elementProperties.set(e,t),!t.noAccessor){let n=Symbol(),r=this.getPropertyDescriptor(e,n,t);r!==void 0&&u(this.prototype,e,r)}}static getPropertyDescriptor(e,t,n){let{get:r,set:i}=d(this.prototype,e)??{get(){return this[t]},set(e){this[t]=e}};return{get:r,set(t){let a=r?.call(this);i?.call(this,t),this.requestUpdate(e,a,n)},configurable:!0,enumerable:!0}}static getPropertyOptions(e){return this.elementProperties.get(e)??_}static _$Ei(){if(this.hasOwnProperty(m(`elementProperties`)))return;let e=ne(this);e.finalize(),e.l!==void 0&&(this.l=[...e.l]),this.elementProperties=new Map(e.elementProperties)}static finalize(){if(this.hasOwnProperty(m(`finalized`)))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(m(`properties`))){let e=this.properties,t=[...ee(e),...te(e)];for(let n of t)this.createProperty(n,e[n])}let e=this[Symbol.metadata];if(e!==null){let t=litPropertyMetadata.get(e);if(t!==void 0)for(let[e,n]of t)this.elementProperties.set(e,n)}this._$Eh=new Map;for(let[e,t]of this.elementProperties){let n=this._$Eu(e,t);n!==void 0&&this._$Eh.set(n,e)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(e){let t=[];if(Array.isArray(e)){let n=new Set(e.flat(1/0).reverse());for(let e of n)t.unshift(c(e))}else e!==void 0&&t.push(c(e));return t}static _$Eu(e,t){let n=t.attribute;return!1===n?void 0:typeof n==`string`?n:typeof e==`string`?e.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(e=>this.enableUpdating=e),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(e=>e(this))}addController(e){(this._$EO??=new Set).add(e),this.renderRoot!==void 0&&this.isConnected&&e.hostConnected?.()}removeController(e){this._$EO?.delete(e)}_$E_(){let e=new Map,t=this.constructor.elementProperties;for(let n of t.keys())this.hasOwnProperty(n)&&(e.set(n,this[n]),delete this[n]);e.size>0&&(this._$Ep=e)}createRenderRoot(){let e=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return s(e,this.constructor.elementStyles),e}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(e=>e.hostConnected?.())}enableUpdating(e){}disconnectedCallback(){this._$EO?.forEach(e=>e.hostDisconnected?.())}attributeChangedCallback(e,t,n){this._$AK(e,n)}_$ET(e,t){let n=this.constructor.elementProperties.get(e),r=this.constructor._$Eu(e,n);if(r!==void 0&&!0===n.reflect){let i=(n.converter?.toAttribute===void 0?h:n.converter).toAttribute(t,n.type);this._$Em=e,i==null?this.removeAttribute(r):this.setAttribute(r,i),this._$Em=null}}_$AK(e,t){let n=this.constructor,r=n._$Eh.get(e);if(r!==void 0&&this._$Em!==r){let e=n.getPropertyOptions(r),i=typeof e.converter==`function`?{fromAttribute:e.converter}:e.converter?.fromAttribute===void 0?h:e.converter;this._$Em=r;let a=i.fromAttribute(t,e.type);this[r]=a??this._$Ej?.get(r)??a,this._$Em=null}}requestUpdate(e,t,n,r=!1,i){if(e!==void 0){let a=this.constructor;if(!1===r&&(i=this[e]),n??=a.getPropertyOptions(e),!((n.hasChanged??g)(i,t)||n.useDefault&&n.reflect&&i===this._$Ej?.get(e)&&!this.hasAttribute(a._$Eu(e,n))))return;this.C(e,t,n)}!1===this.isUpdatePending&&(this._$ES=this._$EP())}C(e,t,{useDefault:n,reflect:r,wrapped:i},a){n&&!(this._$Ej??=new Map).has(e)&&(this._$Ej.set(e,a??t??this[e]),!0!==i||a!==void 0)||(this._$AL.has(e)||(this.hasUpdated||n||(t=void 0),this._$AL.set(e,t)),!0===r&&this._$Em!==e&&(this._$Eq??=new Set).add(e))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(e){Promise.reject(e)}let e=this.scheduleUpdate();return e!=null&&await e,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(let[e,t]of this._$Ep)this[e]=t;this._$Ep=void 0}let e=this.constructor.elementProperties;if(e.size>0)for(let[t,n]of e){let{wrapped:e}=n,r=this[t];!0!==e||this._$AL.has(t)||r===void 0||this.C(t,void 0,n,r)}}let e=!1,t=this._$AL;try{e=this.shouldUpdate(t),e?(this.willUpdate(t),this._$EO?.forEach(e=>e.hostUpdate?.()),this.update(t)):this._$EM()}catch(t){throw e=!1,this._$EM(),t}e&&this._$AE(t)}willUpdate(e){}_$AE(e){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(e)),this.updated(e)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(e){return!0}update(e){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(e){}firstUpdated(e){}};v.elementStyles=[],v.shadowRootOptions={mode:`open`},v[m(`elementProperties`)]=new Map,v[m(`finalized`)]=new Map,ie?.({ReactiveElement:v}),(f.reactiveElementVersions??=[]).push(`2.1.2`);var y=globalThis,ae=e=>e,b=y.trustedTypes,x=b?b.createPolicy(`lit-html`,{createHTML:e=>e}):void 0,S=`$lit$`,C=`lit$${Math.random().toFixed(9).slice(2)}$`,w=`?`+C,oe=`<${w}>`,T=document,E=()=>T.createComment(``),D=e=>e===null||typeof e!=`object`&&typeof e!=`function`,O=Array.isArray,se=e=>O(e)||typeof e?.[Symbol.iterator]==`function`,k=`[ 	
\f\r]`,A=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,ce=/-->/g,le=/>/g,j=RegExp(`>|${k}(?:([^\\s"'>=/]+)(${k}*=${k}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,`g`),ue=/'/g,M=/"/g,N=/^(?:script|style|textarea|title)$/i,P=(e=>(t,...n)=>({_$litType$:e,strings:t,values:n}))(1),F=Symbol.for(`lit-noChange`),I=Symbol.for(`lit-nothing`),L=new WeakMap,R=T.createTreeWalker(T,129);function z(e,t){if(!O(e)||!e.hasOwnProperty(`raw`))throw Error(`invalid template strings array`);return x===void 0?t:x.createHTML(t)}var de=(e,t)=>{let n=e.length-1,r=[],i,a=t===2?`<svg>`:t===3?`<math>`:``,o=A;for(let t=0;t<n;t++){let n=e[t],s,c,l=-1,u=0;for(;u<n.length&&(o.lastIndex=u,c=o.exec(n),c!==null);)u=o.lastIndex,o===A?c[1]===`!--`?o=ce:c[1]===void 0?c[2]===void 0?c[3]!==void 0&&(o=j):(N.test(c[2])&&(i=RegExp(`</`+c[2],`g`)),o=j):o=le:o===j?c[0]===`>`?(o=i??A,l=-1):c[1]===void 0?l=-2:(l=o.lastIndex-c[2].length,s=c[1],o=c[3]===void 0?j:c[3]===`"`?M:ue):o===M||o===ue?o=j:o===ce||o===le?o=A:(o=j,i=void 0);let d=o===j&&e[t+1].startsWith(`/>`)?` `:``;a+=o===A?n+oe:l>=0?(r.push(s),n.slice(0,l)+S+n.slice(l)+C+d):n+C+(l===-2?t:d)}return[z(e,a+(e[n]||`<?>`)+(t===2?`</svg>`:t===3?`</math>`:``)),r]},B=class e{constructor({strings:t,_$litType$:n},r){let i;this.parts=[];let a=0,o=0,s=t.length-1,c=this.parts,[l,u]=de(t,n);if(this.el=e.createElement(l,r),R.currentNode=this.el.content,n===2||n===3){let e=this.el.content.firstChild;e.replaceWith(...e.childNodes)}for(;(i=R.nextNode())!==null&&c.length<s;){if(i.nodeType===1){if(i.hasAttributes())for(let e of i.getAttributeNames())if(e.endsWith(S)){let t=u[o++],n=i.getAttribute(e).split(C),r=/([.?@])?(.*)/.exec(t);c.push({type:1,index:a,name:r[2],strings:n,ctor:r[1]===`.`?pe:r[1]===`?`?me:r[1]===`@`?he:U}),i.removeAttribute(e)}else e.startsWith(C)&&(c.push({type:6,index:a}),i.removeAttribute(e));if(N.test(i.tagName)){let e=i.textContent.split(C),t=e.length-1;if(t>0){i.textContent=b?b.emptyScript:``;for(let n=0;n<t;n++)i.append(e[n],E()),R.nextNode(),c.push({type:2,index:++a});i.append(e[t],E())}}}else if(i.nodeType===8){if(i.data===w)c.push({type:2,index:a});else{let e=-1;for(;(e=i.data.indexOf(C,e+1))!==-1;)c.push({type:7,index:a}),e+=C.length-1}}a++}}static createElement(e,t){let n=T.createElement(`template`);return n.innerHTML=e,n}};function V(e,t,n=e,r){if(t===F)return t;let i=r===void 0?n._$Cl:n._$Co?.[r],a=D(t)?void 0:t._$litDirective$;return i?.constructor!==a&&(i?._$AO?.(!1),a===void 0?i=void 0:(i=new a(e),i._$AT(e,n,r)),r===void 0?n._$Cl=i:(n._$Co??=[])[r]=i),i!==void 0&&(t=V(e,i._$AS(e,t.values),i,r)),t}var fe=class{constructor(e,t){this._$AV=[],this._$AN=void 0,this._$AD=e,this._$AM=t}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(e){let{el:{content:t},parts:n}=this._$AD,r=(e?.creationScope??T).importNode(t,!0);R.currentNode=r;let i=R.nextNode(),a=0,o=0,s=n[0];for(;s!==void 0;){if(a===s.index){let t;s.type===2?t=new H(i,i.nextSibling,this,e):s.type===1?t=new s.ctor(i,s.name,s.strings,this,e):s.type===6&&(t=new ge(i,this,e)),this._$AV.push(t),s=n[++o]}a!==s?.index&&(i=R.nextNode(),a++)}return R.currentNode=T,r}p(e){let t=0;for(let n of this._$AV)n!==void 0&&(n.strings===void 0?n._$AI(e[t]):(n._$AI(e,n,t),t+=n.strings.length-2)),t++}},H=class e{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(e,t,n,r){this.type=2,this._$AH=I,this._$AN=void 0,this._$AA=e,this._$AB=t,this._$AM=n,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let e=this._$AA.parentNode,t=this._$AM;return t!==void 0&&e?.nodeType===11&&(e=t.parentNode),e}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(e,t=this){e=V(this,e,t),D(e)?e===I||e==null||e===``?(this._$AH!==I&&this._$AR(),this._$AH=I):e!==this._$AH&&e!==F&&this._(e):e._$litType$===void 0?e.nodeType===void 0?se(e)?this.k(e):this._(e):this.T(e):this.$(e)}O(e){return this._$AA.parentNode.insertBefore(e,this._$AB)}T(e){this._$AH!==e&&(this._$AR(),this._$AH=this.O(e))}_(e){this._$AH!==I&&D(this._$AH)?this._$AA.nextSibling.data=e:this.T(T.createTextNode(e)),this._$AH=e}$(e){let{values:t,_$litType$:n}=e,r=typeof n==`number`?this._$AC(e):(n.el===void 0&&(n.el=B.createElement(z(n.h,n.h[0]),this.options)),n);if(this._$AH?._$AD===r)this._$AH.p(t);else{let e=new fe(r,this),n=e.u(this.options);e.p(t),this.T(n),this._$AH=e}}_$AC(e){let t=L.get(e.strings);return t===void 0&&L.set(e.strings,t=new B(e)),t}k(t){O(this._$AH)||(this._$AH=[],this._$AR());let n=this._$AH,r,i=0;for(let a of t)i===n.length?n.push(r=new e(this.O(E()),this.O(E()),this,this.options)):r=n[i],r._$AI(a),i++;i<n.length&&(this._$AR(r&&r._$AB.nextSibling,i),n.length=i)}_$AR(e=this._$AA.nextSibling,t){for(this._$AP?.(!1,!0,t);e!==this._$AB;){let t=ae(e).nextSibling;ae(e).remove(),e=t}}setConnected(e){this._$AM===void 0&&(this._$Cv=e,this._$AP?.(e))}},U=class{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(e,t,n,r,i){this.type=1,this._$AH=I,this._$AN=void 0,this.element=e,this.name=t,this._$AM=r,this.options=i,n.length>2||n[0]!==``||n[1]!==``?(this._$AH=Array(n.length-1).fill(new String),this.strings=n):this._$AH=I}_$AI(e,t=this,n,r){let i=this.strings,a=!1;if(i===void 0)e=V(this,e,t,0),a=!D(e)||e!==this._$AH&&e!==F,a&&(this._$AH=e);else{let r=e,o,s;for(e=i[0],o=0;o<i.length-1;o++)s=V(this,r[n+o],t,o),s===F&&(s=this._$AH[o]),a||=!D(s)||s!==this._$AH[o],s===I?e=I:e!==I&&(e+=(s??``)+i[o+1]),this._$AH[o]=s}a&&!r&&this.j(e)}j(e){e===I?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,e??``)}},pe=class extends U{constructor(){super(...arguments),this.type=3}j(e){this.element[this.name]=e===I?void 0:e}},me=class extends U{constructor(){super(...arguments),this.type=4}j(e){this.element.toggleAttribute(this.name,!!e&&e!==I)}},he=class extends U{constructor(e,t,n,r,i){super(e,t,n,r,i),this.type=5}_$AI(e,t=this){if((e=V(this,e,t,0)??I)===F)return;let n=this._$AH,r=e===I&&n!==I||e.capture!==n.capture||e.once!==n.once||e.passive!==n.passive,i=e!==I&&(n===I||r);r&&this.element.removeEventListener(this.name,this,n),i&&this.element.addEventListener(this.name,this,e),this._$AH=e}handleEvent(e){typeof this._$AH==`function`?this._$AH.call(this.options?.host??this.element,e):this._$AH.handleEvent(e)}},ge=class{constructor(e,t,n){this.element=e,this.type=6,this._$AN=void 0,this._$AM=t,this.options=n}get _$AU(){return this._$AM._$AU}_$AI(e){V(this,e)}},_e=y.litHtmlPolyfillSupport;_e?.(B,H),(y.litHtmlVersions??=[]).push(`3.3.3`);var ve=(e,t,n)=>{let r=n?.renderBefore??t,i=r._$litPart$;if(i===void 0){let e=n?.renderBefore??null;r._$litPart$=i=new H(t.insertBefore(E(),e),e,void 0,n??{})}return i._$AI(e),i},W=globalThis,G=class extends v{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){let e=super.createRenderRoot();return this.renderOptions.renderBefore??=e.firstChild,e}update(e){let t=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(e),this._$Do=ve(t,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return F}};G._$litElement$=!0,G.finalized=!0,W.litElementHydrateSupport?.({LitElement:G});var ye=W.litElementPolyfillSupport;ye?.({LitElement:G}),(W.litElementVersions??=[]).push(`4.2.2`);var K=window.__UNI_HALO_FLOAT_MINI_PROFILE__,q=`uh-fmp-closed`,J=`/apis/api.unihalo.ialley.cn/v1alpha1/plugins/plugin-uni-halo`,be=J+`/captcha/generate`;J+``;var xe=J+`/mini-program-links/submissions`,Se=J+`/getConfigs`,Ce=J+`/mini-program-links/types`;function we(e){let t=window.location.pathname;if(!e||!e.trim())return t===`/`||t===``;let n=e.split(`
`).map(e=>e.trim()).filter(Boolean);return n.length===1&&n[0]===`/`?t===`/`||t===``:n.some(e=>{let n=e.replace(/[.+?^${}()|[\]\\]/g,`\\$&`).replace(/\*\*/g,`{{DOUBLE}}`).replace(/\*/g,`[^/]*`).replace(/\{\{DOUBLE\}\}/g,`.*`);try{return RegExp(`^`+n+`$`).test(t)}catch{return!1}})}function Te(){let e=K?.pageScope||`all`;if(e===`all`)return!0;let t=we(K?.pagePatterns);return e===`only`?t:!t}var Ee=o`
  :host {
    display: block;
  }

  .uh-fmp {
    position: fixed;
    z-index: 9999;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 10px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC",
      "Microsoft YaHei", sans-serif;
    background: rgba(255, 255, 255, 1);
    border: 2px solid rgba(255, 255, 255, 0.65);
    border-radius: 14px;
    box-shadow: 0 16px 60px rgba(0, 0, 0, 0.06);
    user-select: none;
    -webkit-user-select: none;
    cursor: grab;
    max-width: 40vw;
    line-height: 1.4;
    transition: transform 0.3s ease;
  }

  /* ===== 9 向锚点（配合 JS inline transform 偏移） ===== */
  .uh-fmp.uh-fmp-pos-top-left { top: 8px; left: 8px; }
  .uh-fmp.uh-fmp-pos-top-center { top: 8px; left: 50%; }
  .uh-fmp.uh-fmp-pos-top-right { top: 8px; right: 8px; }
  .uh-fmp.uh-fmp-pos-right-center { top: 50%; right: 8px; }
  .uh-fmp.uh-fmp-pos-bottom-right { bottom: 8px; right: 8px; }
  .uh-fmp.uh-fmp-pos-bottom-center { bottom: 8px; left: 50%; }
  .uh-fmp.uh-fmp-pos-bottom-left { bottom: 8px; left: 8px; }
  .uh-fmp.uh-fmp-pos-left-center { top: 50%; left: 8px; }
  .uh-fmp.uh-fmp-pos-center { top: 50%; left: 50%; }

  /* ===== 内容 ===== */
  .uh-fmp-img {
    display: block;
    max-width: 100%;
    height: auto;
    border-radius: 8px;
  }
  .uh-fmp-name {
    font-weight: 600;
    text-align: center;
  }
  .uh-fmp-desc {
    text-align: center;
    word-break: break-all;
  }

  /* ===== 关闭按钮（右上角） ===== */
  .uh-fmp-close {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    border-radius: 6px;
    border: 1px solid rgba(255,255,255, 0.5);
    background: rgba(255, 255, 255, 0.75);
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.075);
    color: #999999;
    font-size: 14px;
    line-height: 20px;
    text-align: center;
    cursor: pointer;
  }
  .uh-fmp-close:hover {
    color: #333;
  }

  /* ===== 拖拽 ===== */
  .uh-fmp.uh-fmp-dragging {
    transition: none !important;
    cursor: grabbing;
  }

  /* ===== 贴边隐藏（JS 按最近边缘加 uh-fmp-edge-*，露出 var(--uh-fmp-edge) 宽把手） ===== */
  .uh-fmp.uh-fmp-edge-left { transform: translateX(calc(-100% + var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-right { transform: translateX(calc(100% - var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-top { transform: translateY(calc(-100% + var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-bottom { transform: translateY(calc(100% - var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge:hover,
  .uh-fmp.uh-fmp-edge:focus-within {
    transform: translate(0, 0);
  }

  /* ===== 关闭动画 ===== */
  .uh-fmp.uh-fmp-closing {
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  /* ===== 底部操作按钮（小程序申请开关开启后显示） ===== */
  .uh-fmp-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
    margin-top: 2px;
  }
  .uh-fmp-btn {
    flex: 1;
    box-sizing: border-box;
    border: 1px solid rgba(0,0,0,0.05);
    border-radius: 8px;
    background: #ffffff;
    color: #0E1731;
    font-size: 12px;
    line-height: 1;
    padding: 8px 0;
    cursor: pointer;
    text-align: center;
    font-family: inherit;
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
    transition: background 0.15s ease;
  }
  .uh-fmp-btn:hover {
    background: #f1f5f9;
  }
  .uh-fmp-btn-primary {
    /* 主色 #0E1731 + 白色文字 */
    background: #0E1731;
    border-color: transparent;
    color: #ffffff;
  }
  .uh-fmp-btn-primary:hover {
    background: #16244a;
  }
  .uh-fmp-hint {
    flex-basis: 100%;
    text-align: center;
    font-size: 10px;
    line-height: 1;
    color: rgba(0, 0, 0, 0.45);
  }

  /* ===== 弹窗（遮罩 + 居中卡片，渲染于 shadow DOM 内） ===== */
  .uh-fmp-overlay {
    position: fixed;
    inset: 0;
    z-index: 2147483000;
    display: flex;
    align-items: center;
    justify-content: center;
    /* glass 遮罩：半透明 + 轻微毛玻璃（对齐 app 弹窗遮罩） */
    background: rgba(0, 0, 0, 0.45);
    -webkit-backdrop-filter: blur(4px);
    backdrop-filter: blur(4px);
    padding: 16px;
    box-sizing: border-box;
  }
  .uh-fmp-modal {
    box-sizing: border-box;
    width: 100%;
    max-width: 420px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.98);
    -webkit-backdrop-filter: blur(16px);
    backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: 14px;
    box-shadow: 0 16px 60px rgba(0, 0, 0, 0.18);
    overflow: hidden;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC",
      "Microsoft YaHei", sans-serif;
  }
  .uh-fmp-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }
  .uh-fmp-modal-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
  }
  .uh-fmp-modal-close {
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    border-radius: 6px;
    border: 1px solid rgba(255,255,255, 0.5);
    background: rgba(255, 255, 255, 0.75);
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.075);
    font-size: 14px;
    color: #999999;
    cursor: pointer;
  }
  .uh-fmp-modal-close:hover {
    color: #333333;
  }
  .uh-fmp-modal-body {
    padding: 14px;
    overflow-y: auto;
  }

  /* ===== 申请表单 ===== */
  .uh-fmp-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .uh-fmp-field {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;
    color: #666666;
  }
  /* 申请弹窗面板内字段上下间距（footer 内验证码区不受影响） */
  .uh-fmp-apply-panel .uh-fmp-field {
    margin-bottom: 10px;
  }
  .uh-fmp-apply-panel .uh-fmp-field:last-child {
    margin-bottom: 0;
  }
  .uh-fmp-field input[type="text"] {
    box-sizing: border-box;
    width: 100%;
    height: 32px;
    padding: 0 10px;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
  }
  .uh-fmp-field input[type="text"]:focus,
  .uh-fmp-field select:focus,
  .uh-fmp-field textarea:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-field select,
  .uh-fmp-field textarea {
    box-sizing: border-box;
    width: 100%;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
    background: #ffffff;
  }
  .uh-fmp-field select {
    height: 32px;
    padding: 0 8px;
  }
  .uh-fmp-field textarea {
    padding: 6px 10px;
    resize: vertical;
  }
  .uh-fmp-captcha-input {
    display: flex;
    gap: 8px;
  }
  .uh-fmp-captcha-input input {
    flex: 1;
  }
  .uh-fmp-captcha-img {
    width: 100px;
    height: 32px;
    border-radius: 8px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    cursor: pointer;
    object-fit: cover;
  }
  .uh-fmp-form-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }

  /* ===== 申请弹窗：分段器 + 面板 + 底部固定操作区 ===== */
  .uh-fmp-modal-apply {
    max-height: 80vh;
  }
  /* shadcn Tabs（radix tabs）风格分段器：track 连体浅灰背景，激活项浮起 */
  .uh-fmp-segmented {
    display: flex;
    gap: 4px;
    margin: 8px 14px 0;
    padding: 4px; /* track 内边距（对齐 tabs-list p-1） */
    background: rgba(0, 0, 0, 0.05);
    border-radius: 8px;
  }
  .uh-fmp-seg-item {
    flex: 1;
    box-sizing: border-box;
    padding: 6px 0;
    border: none;
    border-radius: 8px;
    background: transparent;
    color: #666666;
    font-size: 13px;
    cursor: pointer;
    font-family: inherit;
  }
  .uh-fmp-seg-active {
    /* 激活块：主色 #0E1731 + 白字 + 轻投影 */
    background: #0E1731;
    color: #ffffff;
    font-weight: 600;
    box-shadow: 0 1px 2px rgba(14, 23, 49, 0.35);
  }
  .uh-fmp-apply-body {
    display: flex;
    flex-direction: column;
    padding: 10px 14px 14px;
    overflow: hidden;
  }
  .uh-fmp-apply-panels {
    flex: 1;
    min-height: 0;
    max-height: 40vh; /* 内容区最大高度 40vh + 滚动 */
    overflow-y: auto;
    scrollbar-width: thin; /* Firefox */
    scrollbar-color: rgba(120, 130, 150, 0.4) transparent;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar {
    width: 6px;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-track {
    background: transparent;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-thumb {
    background: rgba(120, 130, 150, 0.4);
    border-radius: 3px;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-thumb:hover {
    background: rgba(120, 130, 150, 0.6);
  }
  .uh-fmp-apply-footer {
    flex-shrink: 0;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    padding-top: 10px;
    margin-top: 10px;
  }
  /* 预览图动态行 */
  .uh-fmp-shot-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 6px; /* 预览图行之间上下间距 */
  }
  .uh-fmp-shot-input {
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    height: 32px;
    padding: 0 10px;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
    background: #ffffff;
  }
  .uh-fmp-shot-input:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-shot-remove {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 6px;
    background: rgba(0, 0, 0, 0.05);
    color: #999999;
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    padding: 0;
  }
  .uh-fmp-shot-remove:hover {
    background: rgba(0, 0, 0, 0.1);
    color: #333333;
  }
  .uh-fmp-shot-add {
    margin-top: 2px;
  }

  /* ===== 友链信息（小程序信息 + 博主信息，输入框行 + 复制） ===== */
  .uh-fmp-info-card {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px;
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.03);
    margin-bottom: 10px;
  }
  .uh-fmp-info-card-title {
    font-size: 13px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 2px;
  }
  .uh-fmp-copy-row {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .uh-fmp-copy-label {
    flex-shrink: 0;
    font-size: 12px;
    color: #000000; /* label 正常黑色 */
    min-width: 64px;
    text-align: right;
  }
  .uh-fmp-copy-input {
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    height: 28px;
    padding: 0 8px;
    /* 只读输入框：浅白背景 + 白色边框（与玻璃弹窗背景区分） */
    border: 1px solid #ffffff;
    border-radius: 6px;
    font-size: 12px;
    color: #1a1a1a;
    background: rgba(255, 255, 255, 0.75);
    outline: none;
    font-family: inherit;
  }
  .uh-fmp-copy-input:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-copy-textarea {
    height: auto;
    min-height: 40px;
    padding: 5px 8px;
    line-height: 1.4;
    resize: none;
    font-family: inherit;
  }
  .uh-fmp-copy-btn {
    flex-shrink: 0;
    flex: none;
    width: 60px;
    padding: 8px 0;
  }
  .uh-fmp-copy-all {
    width: 100%;
  }
  .uh-fmp-loading,
  .uh-fmp-empty {
    padding: 20px 0;
    text-align: center;
    font-size: 13px;
    color: #999999;
  }

  /* ===== 深色模式 ===== */
  @media (prefers-color-scheme: dark) {
    .uh-fmp {
      background: rgba(28, 28, 32, 0.85);
      border-color: rgba(255, 255, 255, 0.08);
      box-shadow: 0 16px 60px rgba(0, 0, 0, 0.35);
    }
    .uh-fmp-close {
      background: rgba(0, 0, 0, 0.75);
      border-color: rgba(0, 0, 0, 0.9);
    }
    .uh-fmp-close:hover {
      color: #fff;
    }
    .uh-fmp-modal {
      background: rgba(28, 28, 32, 0.9);
      border-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-header {
      border-bottom-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-title,
    .uh-fmp-info-card-title {
      color: #f5f5f5;
    }
    .uh-fmp-field,
    .uh-fmp-copy-label {
      color: #999999;
    }
    .uh-fmp-field input[type="text"],
    .uh-fmp-field select,
    .uh-fmp-field textarea {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-segmented {
      background: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-seg-item {
      background: transparent;
      color: #999999;
    }
    .uh-fmp-seg-active {
      background: #0E1731;
      color: #ffffff;
    }
    .uh-fmp-shot-input {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-shot-remove {
      background: rgba(255, 255, 255, 0.1);
      color: #999999;
    }
    .uh-fmp-apply-footer {
      border-top-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-info-card {
      background: rgba(255, 255, 255, 0.06);
    }
    .uh-fmp-copy-input {
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-btn {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.12);
      color: #f5f5f5;
    }
    .uh-fmp-btn:hover {
      background: rgba(255, 255, 255, 0.14);
    }
    .uh-fmp-btn-primary {
      background: #0E1731;
      color: #ffffff;
    }
    .uh-fmp-btn-primary:hover {
      background: #16244a;
    }
    .uh-fmp-hint {
      color: rgba(255, 255, 255, 0.4);
    }
  }
`,Y=[{key:`displayName`,label:`小程序名称`,required:!0},{key:`miniProgramCode`,label:`太阳码图片`,required:!0},{key:`link`,label:`小程序地址`,required:!1},{key:`groupName`,label:`申请分组`,required:!1,type:`select`},{key:`description`,label:`申请描述`,required:!1,type:`textarea`},{key:`applyRemark`,label:`申请说明`,required:!1,type:`textarea`}],X=[{key:`authorName`,label:`作者昵称`,required:!1},{key:`website`,label:`作者网站`,required:!1},{key:`email`,label:`邮箱（选填，用于审核结果通知）`,required:!1}],Z=`uh-fmp-apply-draft`;function Q(e){return e?/^(https?:)?\/\//.test(e)||/^data:/i.test(e)?e:window.location.origin+(e.startsWith(`/`)?e:`/`+e):``}var De=class extends G{static{this.styles=[Ee]}static{this.properties={applyOpen:{state:!0},linksOpen:{state:!0},applySubmitting:{state:!0},captchaId:{state:!0},captchaSrc:{state:!0},linksLoading:{state:!0},linksError:{state:!0},miniInfo:{state:!0},blogger:{state:!0},groupOptions:{state:!0},applyTab:{state:!0},screenshotRows:{state:!0}}}constructor(){super(),this.dragState=null,this.config=K,this.applyOpen=!1,this.linksOpen=!1,this.applySubmitting=!1,this.captchaId=``,this.captchaSrc=``,this.linksLoading=!1,this.linksError=!1,this.miniInfo=null,this.blogger=null,this.groupOptions=[],this.applyTab=`basic`,this.screenshotRows=[``]}connectedCallback(){super.connectedCallback(),(!Te()||this.isClosed())&&this.remove()}firstUpdated(){this.applyPosition()}isClosed(){if(this.config.rememberClosed===!1)return!1;try{return localStorage.getItem(q)===`1`}catch{return!1}}get cardEl(){return this.renderRoot.querySelector(`.uh-fmp`)}applyPosition(){let e=this.cardEl;if(!e)return;let t=this.config,n=t.position||`bottom-right`;e.classList.add(`uh-fmp-pos-`+n);let r=Number(t.offsetX)||0,i=Number(t.offsetY)||0,a=n===`center`||n===`top-center`||n===`bottom-center`?`calc(-50% + `+r+`px)`:r+`px`,o=n===`center`||n===`left-center`||n===`right-center`?`calc(-50% + `+i+`px)`:i+`px`;e.style.transform=`translate(`+a+`, `+o+`)`,e.style.setProperty(`--uh-fmp-edge`,(Number(t.edgeHideDistance)||24)+`px`)}onPointerDown(e){let t=e.target;if(t&&(t.closest(`.uh-fmp-close`)||t.closest(`.uh-fmp-actions`)||t.closest(`.uh-fmp-overlay`)))return;let n=this.cardEl;if(!n)return;let r=n.getBoundingClientRect();this.dragState={startX:e.clientX,startY:e.clientY,left:r.left,top:r.top},n.classList.add(`uh-fmp-dragging`),n.style.touchAction=`none`,n.setPointerCapture(e.pointerId),e.preventDefault()}onPointerMove(e){let t=this.dragState,n=this.cardEl;if(!t||!n)return;let r=t.left+(e.clientX-t.startX),i=t.top+(e.clientY-t.startY);r=Math.max(0,Math.min(r,window.innerWidth-n.offsetWidth)),i=Math.max(0,Math.min(i,window.innerHeight-n.offsetHeight)),this.setFree(r,i)}onPointerEnd(){let e=this.cardEl;this.dragState&&e&&(this.dragState=null,e.classList.remove(`uh-fmp-dragging`),e.style.touchAction=``,this.maybeEdgeHide())}setFree(e,t){let n=this.cardEl;n&&(n.style.left=e+`px`,n.style.top=t+`px`,n.style.right=`auto`,n.style.bottom=`auto`,n.style.transform=`translate(0, 0)`,n.classList.remove(`uh-fmp-edge`))}maybeEdgeHide(){if(!this.config.edgeHideEnabled)return;let e=this.cardEl;if(!e)return;let t=e.getBoundingClientRect(),n={left:t.left,right:window.innerWidth-t.right,top:t.top,bottom:window.innerHeight-t.bottom},r=null,i=80;[`left`,`right`,`top`,`bottom`].forEach(e=>{n[e]<i&&(i=n[e],r=e)}),r&&e.classList.add(`uh-fmp-edge`,`uh-fmp-edge-`+r)}onCloseClick(){if(this.config.rememberClosed!==!1)try{localStorage.setItem(q,`1`)}catch{}let e=this.cardEl;e?(e.classList.add(`uh-fmp-closing`),setTimeout(()=>this.remove(),200)):this.remove()}onOverlayClick(e){e.target.classList.contains(`uh-fmp-overlay`)&&this.closeModals()}closeModals(){this.applyOpen=!1,this.linksOpen=!1}openApply(){this.applyOpen=!0,this.refreshCaptcha(),this.loadGroupOptions(),this.updateComplete.then(()=>this.restoreDraft())}loadGroupOptions(){fetch(Ce).then(e=>e.json()).then(e=>{Array.isArray(e)&&(this.groupOptions=e.map(e=>({value:e.name||``,label:e.displayName||e.name||`未命名`})))}).catch(()=>{})}restoreDraft(){let e=this.renderRoot.querySelector(`.uh-fmp-form`);if(!e)return;let t=this.loadDraft();[...Y,...X].forEach(n=>{let r=e.elements.namedItem(n.key);r&&t[n.key]&&(r.value=String(t[n.key]))});let n=t.screenshots;Array.isArray(n)?this.screenshotRows=n.length?n.slice():[``]:typeof n==`string`&&n.trim()&&(this.screenshotRows=n.split(`
`).map(e=>e.trim()).filter(Boolean),this.screenshotRows.length||(this.screenshotRows=[``]))}loadDraft(){try{let e=localStorage.getItem(Z);return e?JSON.parse(e):{}}catch{return{}}}saveDraft(){let e=this.renderRoot.querySelector(`.uh-fmp-form`);if(!e)return;let t={};[...Y,...X].forEach(n=>{let r=e.elements.namedItem(n.key);t[n.key]=r?.value||``}),t.screenshots=this.screenshotRows.filter(e=>e.trim());try{localStorage.setItem(Z,JSON.stringify(t))}catch{}}clearDraft(){try{localStorage.removeItem(Z)}catch{}}resetApply(){let e=this.renderRoot.querySelector(`.uh-fmp-form`);e&&e.reset(),this.screenshotRows=[``],this.applyTab=`basic`,this.clearDraft()}onFormInput(){this.saveDraft()}refreshCaptcha(){fetch(be).then(e=>e.json()).then(e=>{e&&e.imageBase64&&this.setCaptcha(e)}).catch(()=>{})}setCaptcha(e){this.captchaId=e.id,this.captchaSrc=e.imageBase64}async onApplySubmit(e){e.preventDefault();let t=e.target;for(let e of[{key:`displayName`,message:`请填写小程序名称`,panel:`basic`},{key:`miniProgramCode`,message:`请填写太阳码图片地址`,panel:`basic`},{key:`captchaCode`,message:`请输入验证码`}]){let n=t.elements.namedItem(e.key);if(!n||!n.value.trim()){e.panel&&(this.applyTab=e.panel),alert(e.message);return}}let n={};[...Y,...X].forEach(e=>{let r=t.elements.namedItem(e.key);r&&r.value.trim()&&(n[e.key]=r.value.trim())});let r=this.screenshotRows.map(e=>e.trim()).filter(Boolean);r.length&&(n.screenshots=r);let i=(t.elements.namedItem(`captchaCode`)?.value||``).trim(),a=`?captchaId=`+encodeURIComponent(this.captchaId||``)+`&captchaCode=`+encodeURIComponent(i);this.applySubmitting=!0;try{let e=await fetch(xe+a,{method:`POST`,headers:{"Content-Type":`application/json`},body:JSON.stringify({spec:n})}),t=await e.json().catch(()=>({}));if(e.status===200||e.status===201){this.applyOpen=!1,this.clearDraft(),alert(`申请提交成功，请等待审核`);return}e.status===403&&t.captcha&&this.setCaptcha(t.captcha),alert(t.message||`提交失败，请重试`)}catch{alert(`网络异常，请稍后重试`)}finally{this.applySubmitting=!1}}openLinks(){this.linksOpen=!0,this.linksLoading=!0,this.linksError=!1,this.miniInfo=null,this.blogger=null,fetch(Se).then(e=>e.json()).then(e=>{let t=e?.pluginConfig,n=e?.authorConfig;this.miniInfo=t?.linkInfo?.miniInfo||null,this.blogger=n?.blogger||null}).catch(()=>{this.linksError=!0}).finally(()=>{this.linksLoading=!1})}render(){let e=this.config,t=Number(e.imageSize)||100;return P`
      ${this.applyOpen?this.renderApplyModal():``}
      ${this.linksOpen?this.renderLinksModal():``}
      <div
        class="uh-fmp"
        @pointerdown=${this.onPointerDown}
        @pointermove=${this.onPointerMove}
        @pointerup=${this.onPointerEnd}
        @pointercancel=${this.onPointerEnd}
      >
        ${e.closeEnabled===!1?``:P`<button type="button" class="uh-fmp-close" aria-label="关闭悬浮窗" @click=${this.onCloseClick}>&times;</button>`}
        ${e.imageUrl?P`<img class="uh-fmp-img" src=${e.imageUrl} alt=${e.name||`小程序太阳码`} style="width:${t}px;height:${t}px" />`:``}
        ${e.name?P`<div class="uh-fmp-name" style="font-size:${Number(e.nameSize)||14}px;color:${e.nameColor||`#333333`}">${e.name}</div>`:``}
        ${e.description?P`<div class="uh-fmp-desc" style="font-size:${Number(e.descSize)||12}px;color:${e.descColor||`#999999`}">${e.description}</div>`:``}
        ${e.miniProgramApply?P`
              <div class="uh-fmp-actions">
                <button type="button" class="uh-fmp-btn" @click=${this.openApply}>我要申请</button>
                <button type="button" class="uh-fmp-btn" @click=${this.openLinks}>友链信息</button>
                <div class="uh-fmp-hint">小程序申请和友链信息</div>
              </div>`:``}
      </div>
    `}renderApplyField(e){let t=P`<span>${e.label}${e.required?` *`:``}</span>`;return e.type===`select`?P`
        <label class="uh-fmp-field">
          ${t}
          <select name=${e.key} class="uh-fmp-select">
            <option value="">未分组</option>
            ${this.groupOptions.map(e=>P`<option value=${e.value}>${e.label}</option>`)}
          </select>
        </label>`:e.type===`textarea`?P`
        <label class="uh-fmp-field">
          ${t}
          <textarea name=${e.key} rows="2" class="uh-fmp-textarea"></textarea>
        </label>`:P`
      <label class="uh-fmp-field">
        ${t}
        <input type="text" name=${e.key} ?required=${e.required} />
      </label>`}renderApplyModal(){return P`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal uh-fmp-modal-apply">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序申请</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-segmented">
            <button
              type="button"
              class="uh-fmp-seg-item ${this.applyTab===`basic`?`uh-fmp-seg-active`:``}"
              @click=${()=>this.applyTab=`basic`}
            >基础信息</button>
            <button
              type="button"
              class="uh-fmp-seg-item ${this.applyTab===`author`?`uh-fmp-seg-active`:``}"
              @click=${()=>this.applyTab=`author`}
            >作者信息</button>
          </div>
          <div class="uh-fmp-modal-body uh-fmp-apply-body">
            <!-- novalidate：隐藏面板的 required 不参与原生校验，由提交时手动校验 -->
            <form class="uh-fmp-form" novalidate @submit=${this.onApplySubmit} @input=${this.onFormInput}>
              <div class="uh-fmp-apply-panels">
                <div class="uh-fmp-apply-panel" ?hidden=${this.applyTab!==`basic`}>
                  ${Y.map(e=>this.renderApplyField(e))}
                  ${this.renderScreenshotRows()}
                </div>
                <div class="uh-fmp-apply-panel" ?hidden=${this.applyTab!==`author`}>
                  ${X.map(e=>this.renderApplyField(e))}
                </div>
              </div>
              <div class="uh-fmp-apply-footer">
                <label class="uh-fmp-field uh-fmp-captcha-row">
                  <span>验证码 *</span>
                  <span class="uh-fmp-captcha-input">
                    <input type="text" name="captchaCode" required autocomplete="off" />
                    <img
                      class="uh-fmp-captcha-img"
                      alt="验证码"
                      title="看不清？点击刷新"
                      src=${this.captchaSrc}
                      @click=${this.refreshCaptcha}
                    />
                  </span>
                </label>
                <div class="uh-fmp-form-actions">
                  <button type="button" class="uh-fmp-btn" @click=${this.resetApply}>重置</button>
                  <button type="button" class="uh-fmp-btn" @click=${this.closeModals}>取消</button>
                  <button type="submit" class="uh-fmp-btn uh-fmp-btn-primary" ?disabled=${this.applySubmitting}>
                    ${this.applySubmitting?`提交中…`:`提交申请`}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    `}renderScreenshotRows(){return P`
      <div class="uh-fmp-field">
        <span>预览图（可选）</span>
        ${this.screenshotRows.map((e,t)=>P`
            <div class="uh-fmp-shot-row">
              <input
                class="uh-fmp-shot-input"
                type="text"
                name="screenshots"
                placeholder="https://…/image.png"
                value=${e}
                @input=${e=>this.updateScreenshotRow(t,e.target.value)}
              />
              <button
                type="button"
                class="uh-fmp-shot-remove"
                aria-label="删除该预览图"
                @click=${()=>this.removeScreenshotRow(t)}
              >&times;</button>
            </div>`)}
        <button type="button" class="uh-fmp-btn uh-fmp-shot-add" @click=${this.addScreenshotRow}>
          + 添加一张预览图
        </button>
      </div>`}updateScreenshotRow(e,t){let n=this.screenshotRows.slice();n[e]=t,this.screenshotRows=n}addScreenshotRow(){this.screenshotRows=[...this.screenshotRows,``],this.updateComplete.then(()=>{let e=this.renderRoot.querySelectorAll(`.uh-fmp-shot-row`),t=e[e.length-1];t&&t.scrollIntoView({block:`nearest`,behavior:`smooth`})})}removeScreenshotRow(e){let t=this.screenshotRows.filter((t,n)=>n!==e);this.screenshotRows=t.length?t:[``]}renderLinksModal(){let e=[{label:`小程序名称`,value:this.miniInfo?.displayName},{label:`太阳码地址`,value:Q(this.miniInfo?.miniProgramCode)},{label:`小程序地址`,value:this.miniInfo?.link},{label:`小程序描述`,value:this.miniInfo?.description,textarea:!0},{label:`申请说明`,value:this.miniInfo?.applyRemark,textarea:!0,copyable:!1}],t=[{label:`博主昵称`,value:this.blogger?.nickname},{label:`博主头像`,value:Q(this.blogger?.avatar)},{label:`博主主页`,value:this.blogger?.website},{label:`博主简介`,value:this.blogger?.description}],n=e.some(e=>e.value)||t.some(e=>e.value);return P`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序友链信息</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-modal-body">
            ${this.linksLoading?P`<div class="uh-fmp-loading">加载中…</div>`:this.linksError?P`<div class="uh-fmp-empty">加载失败，请稍后重试</div>`:n?P`
                      <div class="uh-fmp-info-card">
                        <div class="uh-fmp-info-card-title">小程序信息</div>
                        ${e.map(e=>this.renderCopyRow(e.label,e.value,e))}
                      </div>
                      <div class="uh-fmp-info-card">
                        <div class="uh-fmp-info-card-title">博主信息</div>
                        ${t.map(e=>this.renderCopyRow(e.label,e.value,e))}
                      </div>
                      <button
                        type="button"
                        class="uh-fmp-btn uh-fmp-copy-all"
                        @click=${e=>this.copyText(this.collectLinkText(),e.target)}
                      >复制全部</button>
                    `:P`<div class="uh-fmp-empty">暂无友链信息</div>`}
          </div>
        </div>
      </div>
    `}renderCopyRow(e,t,n){if(!t)return``;let r=!!n?.textarea,i=n?.copyable??!0;return P`
      <div class="uh-fmp-copy-row">
        <span class="uh-fmp-copy-label">${e}</span>
        ${r?P`
          <textarea
            class="uh-fmp-copy-input uh-fmp-copy-textarea"
            readonly
            rows="2"
            @click=${e=>e.target.select()}
          >${t}</textarea>`:P`
          <input
            class="uh-fmp-copy-input"
            type="text"
            readonly
            value=${t}
            @click=${e=>e.target.select()}
          />`}
        ${i?P`
              <button
                type="button"
                class="uh-fmp-btn uh-fmp-copy-btn"
                @click=${e=>this.copyText(t,e.target)}
              >复制</button>`:``}
      </div>`}async copyText(e,t){try{await navigator.clipboard.writeText(e)}catch{let t=document.createElement(`textarea`);t.value=e,t.style.position=`fixed`,t.style.opacity=`0`,document.body.appendChild(t),t.select();try{document.execCommand(`copy`)}catch{}t.remove()}if(t){let e=t.textContent;t.textContent=`已复制`,setTimeout(()=>{t.isConnected&&(t.textContent=e)},800)}}collectLinkText(){let e=[],t=(t,n)=>{n&&e.push(`${t}：${n}`)},n=this.miniInfo;t(`小程序名称`,n?.displayName),t(`太阳码地址`,Q(n?.miniProgramCode)),t(`小程序地址`,n?.link),t(`描述`,n?.description),t(`申请说明`,n?.applyRemark);let r=this.blogger;return t(`博主昵称`,r?.nickname),t(`博主头像`,Q(r?.avatar)),t(`博主主页`,r?.website),t(`博主简介`,r?.description),e.join(`
`)}};customElements.define(`uh-float-mini-profile`,De);function $(){if(document.querySelector(`uh-float-mini-profile`))return;let e=document.createElement(`uh-float-mini-profile`);document.body.appendChild(e)}K&&typeof K==`object`&&(document.body?$():document.addEventListener(`DOMContentLoaded`,$))})();