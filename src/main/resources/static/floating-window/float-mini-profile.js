(function(){var e=globalThis,t=e.ShadowRoot&&(e.ShadyCSS===void 0||e.ShadyCSS.nativeShadow)&&`adoptedStyleSheets`in Document.prototype&&`replace`in CSSStyleSheet.prototype,n=Symbol(),r=new WeakMap,i=class{constructor(e,t,r){if(this._$cssResult$=!0,r!==n)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=e,this.t=t}get styleSheet(){let e=this.o,n=this.t;if(t&&e===void 0){let t=n!==void 0&&n.length===1;t&&(e=r.get(n)),e===void 0&&((this.o=e=new CSSStyleSheet).replaceSync(this.cssText),t&&r.set(n,e))}return e}toString(){return this.cssText}},a=e=>new i(typeof e==`string`?e:e+``,void 0,n),o=(e,...t)=>new i(e.length===1?e[0]:t.reduce((t,n,r)=>t+(e=>{if(!0===e._$cssResult$)return e.cssText;if(typeof e==`number`)return e;throw Error(`Value passed to 'css' function must be a 'css' function result: `+e+`. Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.`)})(n)+e[r+1],e[0]),e,n),s=(n,r)=>{if(t)n.adoptedStyleSheets=r.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(let t of r){let r=document.createElement(`style`),i=e.litNonce;i!==void 0&&r.setAttribute(`nonce`,i),r.textContent=t.cssText,n.appendChild(r)}},c=t?e=>e:e=>e instanceof CSSStyleSheet?(e=>{let t=``;for(let n of e.cssRules)t+=n.cssText;return a(t)})(e):e,{is:l,defineProperty:u,getOwnPropertyDescriptor:d,getOwnPropertyNames:ee,getOwnPropertySymbols:te,getPrototypeOf:ne}=Object,f=globalThis,p=f.trustedTypes,re=p?p.emptyScript:``,ie=f.reactiveElementPolyfillSupport,m=(e,t)=>e,h={toAttribute(e,t){switch(t){case Boolean:e=e?re:null;break;case Object:case Array:e=e==null?e:JSON.stringify(e)}return e},fromAttribute(e,t){let n=e;switch(t){case Boolean:n=e!==null;break;case Number:n=e===null?null:Number(e);break;case Object:case Array:try{n=JSON.parse(e)}catch{n=null}}return n}},g=(e,t)=>!l(e,t),_={attribute:!0,type:String,converter:h,reflect:!1,useDefault:!1,hasChanged:g};Symbol.metadata??=Symbol(`metadata`),f.litPropertyMetadata??=new WeakMap;var v=class extends HTMLElement{static addInitializer(e){this._$Ei(),(this.l??=[]).push(e)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(e,t=_){if(t.state&&(t.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(e)&&((t=Object.create(t)).wrapped=!0),this.elementProperties.set(e,t),!t.noAccessor){let n=Symbol(),r=this.getPropertyDescriptor(e,n,t);r!==void 0&&u(this.prototype,e,r)}}static getPropertyDescriptor(e,t,n){let{get:r,set:i}=d(this.prototype,e)??{get(){return this[t]},set(e){this[t]=e}};return{get:r,set(t){let a=r?.call(this);i?.call(this,t),this.requestUpdate(e,a,n)},configurable:!0,enumerable:!0}}static getPropertyOptions(e){return this.elementProperties.get(e)??_}static _$Ei(){if(this.hasOwnProperty(m(`elementProperties`)))return;let e=ne(this);e.finalize(),e.l!==void 0&&(this.l=[...e.l]),this.elementProperties=new Map(e.elementProperties)}static finalize(){if(this.hasOwnProperty(m(`finalized`)))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(m(`properties`))){let e=this.properties,t=[...ee(e),...te(e)];for(let n of t)this.createProperty(n,e[n])}let e=this[Symbol.metadata];if(e!==null){let t=litPropertyMetadata.get(e);if(t!==void 0)for(let[e,n]of t)this.elementProperties.set(e,n)}this._$Eh=new Map;for(let[e,t]of this.elementProperties){let n=this._$Eu(e,t);n!==void 0&&this._$Eh.set(n,e)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(e){let t=[];if(Array.isArray(e)){let n=new Set(e.flat(1/0).reverse());for(let e of n)t.unshift(c(e))}else e!==void 0&&t.push(c(e));return t}static _$Eu(e,t){let n=t.attribute;return!1===n?void 0:typeof n==`string`?n:typeof e==`string`?e.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(e=>this.enableUpdating=e),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(e=>e(this))}addController(e){(this._$EO??=new Set).add(e),this.renderRoot!==void 0&&this.isConnected&&e.hostConnected?.()}removeController(e){this._$EO?.delete(e)}_$E_(){let e=new Map,t=this.constructor.elementProperties;for(let n of t.keys())this.hasOwnProperty(n)&&(e.set(n,this[n]),delete this[n]);e.size>0&&(this._$Ep=e)}createRenderRoot(){let e=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return s(e,this.constructor.elementStyles),e}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(e=>e.hostConnected?.())}enableUpdating(e){}disconnectedCallback(){this._$EO?.forEach(e=>e.hostDisconnected?.())}attributeChangedCallback(e,t,n){this._$AK(e,n)}_$ET(e,t){let n=this.constructor.elementProperties.get(e),r=this.constructor._$Eu(e,n);if(r!==void 0&&!0===n.reflect){let i=(n.converter?.toAttribute===void 0?h:n.converter).toAttribute(t,n.type);this._$Em=e,i==null?this.removeAttribute(r):this.setAttribute(r,i),this._$Em=null}}_$AK(e,t){let n=this.constructor,r=n._$Eh.get(e);if(r!==void 0&&this._$Em!==r){let e=n.getPropertyOptions(r),i=typeof e.converter==`function`?{fromAttribute:e.converter}:e.converter?.fromAttribute===void 0?h:e.converter;this._$Em=r;let a=i.fromAttribute(t,e.type);this[r]=a??this._$Ej?.get(r)??a,this._$Em=null}}requestUpdate(e,t,n,r=!1,i){if(e!==void 0){let a=this.constructor;if(!1===r&&(i=this[e]),n??=a.getPropertyOptions(e),!((n.hasChanged??g)(i,t)||n.useDefault&&n.reflect&&i===this._$Ej?.get(e)&&!this.hasAttribute(a._$Eu(e,n))))return;this.C(e,t,n)}!1===this.isUpdatePending&&(this._$ES=this._$EP())}C(e,t,{useDefault:n,reflect:r,wrapped:i},a){n&&!(this._$Ej??=new Map).has(e)&&(this._$Ej.set(e,a??t??this[e]),!0!==i||a!==void 0)||(this._$AL.has(e)||(this.hasUpdated||n||(t=void 0),this._$AL.set(e,t)),!0===r&&this._$Em!==e&&(this._$Eq??=new Set).add(e))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(e){Promise.reject(e)}let e=this.scheduleUpdate();return e!=null&&await e,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(let[e,t]of this._$Ep)this[e]=t;this._$Ep=void 0}let e=this.constructor.elementProperties;if(e.size>0)for(let[t,n]of e){let{wrapped:e}=n,r=this[t];!0!==e||this._$AL.has(t)||r===void 0||this.C(t,void 0,n,r)}}let e=!1,t=this._$AL;try{e=this.shouldUpdate(t),e?(this.willUpdate(t),this._$EO?.forEach(e=>e.hostUpdate?.()),this.update(t)):this._$EM()}catch(t){throw e=!1,this._$EM(),t}e&&this._$AE(t)}willUpdate(e){}_$AE(e){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(e)),this.updated(e)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(e){return!0}update(e){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(e){}firstUpdated(e){}};v.elementStyles=[],v.shadowRootOptions={mode:`open`},v[m(`elementProperties`)]=new Map,v[m(`finalized`)]=new Map,ie?.({ReactiveElement:v}),(f.reactiveElementVersions??=[]).push(`2.1.2`);var y=globalThis,b=e=>e,x=y.trustedTypes,S=x?x.createPolicy(`lit-html`,{createHTML:e=>e}):void 0,C=`$lit$`,w=`lit$${Math.random().toFixed(9).slice(2)}$`,T=`?`+w,ae=`<${T}>`,E=document,D=()=>E.createComment(``),O=e=>e===null||typeof e!=`object`&&typeof e!=`function`,k=Array.isArray,oe=e=>k(e)||typeof e?.[Symbol.iterator]==`function`,A=`[ 	
\f\r]`,j=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,M=/-->/g,N=/>/g,P=RegExp(`>|${A}(?:([^\\s"'>=/]+)(${A}*=${A}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,`g`),F=/'/g,I=/"/g,L=/^(?:script|style|textarea|title)$/i,R=(e=>(t,...n)=>({_$litType$:e,strings:t,values:n}))(1),z=Symbol.for(`lit-noChange`),B=Symbol.for(`lit-nothing`),V=new WeakMap,H=E.createTreeWalker(E,129);function U(e,t){if(!k(e)||!e.hasOwnProperty(`raw`))throw Error(`invalid template strings array`);return S===void 0?t:S.createHTML(t)}var se=(e,t)=>{let n=e.length-1,r=[],i,a=t===2?`<svg>`:t===3?`<math>`:``,o=j;for(let t=0;t<n;t++){let n=e[t],s,c,l=-1,u=0;for(;u<n.length&&(o.lastIndex=u,c=o.exec(n),c!==null);)u=o.lastIndex,o===j?c[1]===`!--`?o=M:c[1]===void 0?c[2]===void 0?c[3]!==void 0&&(o=P):(L.test(c[2])&&(i=RegExp(`</`+c[2],`g`)),o=P):o=N:o===P?c[0]===`>`?(o=i??j,l=-1):c[1]===void 0?l=-2:(l=o.lastIndex-c[2].length,s=c[1],o=c[3]===void 0?P:c[3]===`"`?I:F):o===I||o===F?o=P:o===M||o===N?o=j:(o=P,i=void 0);let d=o===P&&e[t+1].startsWith(`/>`)?` `:``;a+=o===j?n+ae:l>=0?(r.push(s),n.slice(0,l)+C+n.slice(l)+w+d):n+w+(l===-2?t:d)}return[U(e,a+(e[n]||`<?>`)+(t===2?`</svg>`:t===3?`</math>`:``)),r]},W=class e{constructor({strings:t,_$litType$:n},r){let i;this.parts=[];let a=0,o=0,s=t.length-1,c=this.parts,[l,u]=se(t,n);if(this.el=e.createElement(l,r),H.currentNode=this.el.content,n===2||n===3){let e=this.el.content.firstChild;e.replaceWith(...e.childNodes)}for(;(i=H.nextNode())!==null&&c.length<s;){if(i.nodeType===1){if(i.hasAttributes())for(let e of i.getAttributeNames())if(e.endsWith(C)){let t=u[o++],n=i.getAttribute(e).split(w),r=/([.?@])?(.*)/.exec(t);c.push({type:1,index:a,name:r[2],strings:n,ctor:r[1]===`.`?le:r[1]===`?`?ue:r[1]===`@`?de:q}),i.removeAttribute(e)}else e.startsWith(w)&&(c.push({type:6,index:a}),i.removeAttribute(e));if(L.test(i.tagName)){let e=i.textContent.split(w),t=e.length-1;if(t>0){i.textContent=x?x.emptyScript:``;for(let n=0;n<t;n++)i.append(e[n],D()),H.nextNode(),c.push({type:2,index:++a});i.append(e[t],D())}}}else if(i.nodeType===8){if(i.data===T)c.push({type:2,index:a});else{let e=-1;for(;(e=i.data.indexOf(w,e+1))!==-1;)c.push({type:7,index:a}),e+=w.length-1}}a++}}static createElement(e,t){let n=E.createElement(`template`);return n.innerHTML=e,n}};function G(e,t,n=e,r){if(t===z)return t;let i=r===void 0?n._$Cl:n._$Co?.[r],a=O(t)?void 0:t._$litDirective$;return i?.constructor!==a&&(i?._$AO?.(!1),a===void 0?i=void 0:(i=new a(e),i._$AT(e,n,r)),r===void 0?n._$Cl=i:(n._$Co??=[])[r]=i),i!==void 0&&(t=G(e,i._$AS(e,t.values),i,r)),t}var ce=class{constructor(e,t){this._$AV=[],this._$AN=void 0,this._$AD=e,this._$AM=t}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(e){let{el:{content:t},parts:n}=this._$AD,r=(e?.creationScope??E).importNode(t,!0);H.currentNode=r;let i=H.nextNode(),a=0,o=0,s=n[0];for(;s!==void 0;){if(a===s.index){let t;s.type===2?t=new K(i,i.nextSibling,this,e):s.type===1?t=new s.ctor(i,s.name,s.strings,this,e):s.type===6&&(t=new fe(i,this,e)),this._$AV.push(t),s=n[++o]}a!==s?.index&&(i=H.nextNode(),a++)}return H.currentNode=E,r}p(e){let t=0;for(let n of this._$AV)n!==void 0&&(n.strings===void 0?n._$AI(e[t]):(n._$AI(e,n,t),t+=n.strings.length-2)),t++}},K=class e{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(e,t,n,r){this.type=2,this._$AH=B,this._$AN=void 0,this._$AA=e,this._$AB=t,this._$AM=n,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let e=this._$AA.parentNode,t=this._$AM;return t!==void 0&&e?.nodeType===11&&(e=t.parentNode),e}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(e,t=this){e=G(this,e,t),O(e)?e===B||e==null||e===``?(this._$AH!==B&&this._$AR(),this._$AH=B):e!==this._$AH&&e!==z&&this._(e):e._$litType$===void 0?e.nodeType===void 0?oe(e)?this.k(e):this._(e):this.T(e):this.$(e)}O(e){return this._$AA.parentNode.insertBefore(e,this._$AB)}T(e){this._$AH!==e&&(this._$AR(),this._$AH=this.O(e))}_(e){this._$AH!==B&&O(this._$AH)?this._$AA.nextSibling.data=e:this.T(E.createTextNode(e)),this._$AH=e}$(e){let{values:t,_$litType$:n}=e,r=typeof n==`number`?this._$AC(e):(n.el===void 0&&(n.el=W.createElement(U(n.h,n.h[0]),this.options)),n);if(this._$AH?._$AD===r)this._$AH.p(t);else{let e=new ce(r,this),n=e.u(this.options);e.p(t),this.T(n),this._$AH=e}}_$AC(e){let t=V.get(e.strings);return t===void 0&&V.set(e.strings,t=new W(e)),t}k(t){k(this._$AH)||(this._$AH=[],this._$AR());let n=this._$AH,r,i=0;for(let a of t)i===n.length?n.push(r=new e(this.O(D()),this.O(D()),this,this.options)):r=n[i],r._$AI(a),i++;i<n.length&&(this._$AR(r&&r._$AB.nextSibling,i),n.length=i)}_$AR(e=this._$AA.nextSibling,t){for(this._$AP?.(!1,!0,t);e!==this._$AB;){let t=b(e).nextSibling;b(e).remove(),e=t}}setConnected(e){this._$AM===void 0&&(this._$Cv=e,this._$AP?.(e))}},q=class{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(e,t,n,r,i){this.type=1,this._$AH=B,this._$AN=void 0,this.element=e,this.name=t,this._$AM=r,this.options=i,n.length>2||n[0]!==``||n[1]!==``?(this._$AH=Array(n.length-1).fill(new String),this.strings=n):this._$AH=B}_$AI(e,t=this,n,r){let i=this.strings,a=!1;if(i===void 0)e=G(this,e,t,0),a=!O(e)||e!==this._$AH&&e!==z,a&&(this._$AH=e);else{let r=e,o,s;for(e=i[0],o=0;o<i.length-1;o++)s=G(this,r[n+o],t,o),s===z&&(s=this._$AH[o]),a||=!O(s)||s!==this._$AH[o],s===B?e=B:e!==B&&(e+=(s??``)+i[o+1]),this._$AH[o]=s}a&&!r&&this.j(e)}j(e){e===B?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,e??``)}},le=class extends q{constructor(){super(...arguments),this.type=3}j(e){this.element[this.name]=e===B?void 0:e}},ue=class extends q{constructor(){super(...arguments),this.type=4}j(e){this.element.toggleAttribute(this.name,!!e&&e!==B)}},de=class extends q{constructor(e,t,n,r,i){super(e,t,n,r,i),this.type=5}_$AI(e,t=this){if((e=G(this,e,t,0)??B)===z)return;let n=this._$AH,r=e===B&&n!==B||e.capture!==n.capture||e.once!==n.once||e.passive!==n.passive,i=e!==B&&(n===B||r);r&&this.element.removeEventListener(this.name,this,n),i&&this.element.addEventListener(this.name,this,e),this._$AH=e}handleEvent(e){typeof this._$AH==`function`?this._$AH.call(this.options?.host??this.element,e):this._$AH.handleEvent(e)}},fe=class{constructor(e,t,n){this.element=e,this.type=6,this._$AN=void 0,this._$AM=t,this.options=n}get _$AU(){return this._$AM._$AU}_$AI(e){G(this,e)}},pe=y.litHtmlPolyfillSupport;pe?.(W,K),(y.litHtmlVersions??=[]).push(`3.3.3`);var me=(e,t,n)=>{let r=n?.renderBefore??t,i=r._$litPart$;if(i===void 0){let e=n?.renderBefore??null;r._$litPart$=i=new K(t.insertBefore(D(),e),e,void 0,n??{})}return i._$AI(e),i},J=globalThis,Y=class extends v{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){let e=super.createRenderRoot();return this.renderOptions.renderBefore??=e.firstChild,e}update(e){let t=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(e),this._$Do=me(t,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return z}};Y._$litElement$=!0,Y.finalized=!0,J.litElementHydrateSupport?.({LitElement:Y});var he=J.litElementPolyfillSupport;he?.({LitElement:Y}),(J.litElementVersions??=[]).push(`4.2.2`);var X=window.__UNI_HALO_FLOAT_MINI_PROFILE__,Z=`uh-fmp-closed`,Q=`/apis/api.unihalo.ialley.cn/v1alpha1/plugins/plugin-uni-halo`,ge=Q+`/captcha/generate`;Q+``;var _e=Q+`/mini-program-links/submissions`,ve=Q+`/getConfigs`;function ye(e){let t=window.location.pathname;if(!e||!e.trim())return t===`/`||t===``;let n=e.split(`
`).map(e=>e.trim()).filter(Boolean);return n.length===1&&n[0]===`/`?t===`/`||t===``:n.some(e=>{let n=e.replace(/[.+?^${}()|[\]\\]/g,`\\$&`).replace(/\*\*/g,`{{DOUBLE}}`).replace(/\*/g,`[^/]*`).replace(/\{\{DOUBLE\}\}/g,`.*`);try{return RegExp(`^`+n+`$`).test(t)}catch{return!1}})}function be(){let e=X?.pageScope||`all`;if(e===`all`)return!0;let t=ye(X?.pagePatterns);return e===`only`?t:!t}var xe=o`
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
    background: rgba(255, 255, 255, 0.92);
    -webkit-backdrop-filter: blur(12px);
    backdrop-filter: blur(12px);
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
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    color: #ffffff;
    font-size: 14px;
    line-height: 20px;
    text-align: center;
    cursor: pointer;
    padding: 0;
  }
  .uh-fmp-close:hover {
    background: rgba(0, 0, 0, 0.65);
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
    flex-wrap: wrap; /* 两个按钮同一行，提示文字（flex-basis:100%）换行独占一行 */
    gap: 8px;
    width: 100%;
    margin-top: 2px;
  }
  .uh-fmp-btn {
    flex: 1;
    box-sizing: border-box;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.85);
    color: #333333;
    font-size: 12px;
    line-height: 1;
    padding: 7px 0;
    cursor: pointer;
    text-align: center;
    font-family: inherit;
  }
  .uh-fmp-btn:hover {
    background: #ffffff;
  }
  .uh-fmp-btn-primary {
    background: rgba(22, 119, 255, 0.92);
    border-color: transparent;
    color: #ffffff;
  }
  .uh-fmp-btn-primary:hover {
    background: #1677ff;
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
    background: rgba(0, 0, 0, 0.45);
    padding: 16px;
    box-sizing: border-box;
  }
  .uh-fmp-modal {
    box-sizing: border-box;
    width: 100%;
    max-width: 360px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    background: #ffffff;
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
    border: none;
    background: transparent;
    font-size: 18px;
    line-height: 1;
    color: #999999;
    cursor: pointer;
    padding: 2px 4px;
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
  .uh-fmp-field input[type="text"]:focus {
    border-color: #1677ff;
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
    margin-top: 2px;
  }

  /* ===== 友链信息（小程序信息 + 博主信息） ===== */
  .uh-fmp-info-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px;
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.03);
    margin-bottom: 10px;
  }
  .uh-fmp-info-card:last-child {
    margin-bottom: 0;
  }
  .uh-fmp-info-card-img {
    width: 72px;
    height: 72px;
    border-radius: 12px;
    object-fit: cover;
  }
  .uh-fmp-info-card-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
  }
  .uh-fmp-info-card-title {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
  }
  .uh-fmp-info-card-desc {
    font-size: 12px;
    color: #999999;
    text-align: center;
    word-break: break-all;
    line-height: 1.5;
  }
  .uh-fmp-info-card-link {
    font-size: 12px;
    color: #1677ff;
    text-decoration: none;
    word-break: break-all;
  }
  .uh-fmp-info-card-link:hover {
    text-decoration: underline;
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
      background: rgba(255, 255, 255, 0.25);
    }
    .uh-fmp-close:hover {
      background: rgba(255, 255, 255, 0.4);
    }
    .uh-fmp-modal {
      background: #1c1c20;
    }
    .uh-fmp-modal-header {
      border-bottom-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-title,
    .uh-fmp-info-card-title {
      color: #f5f5f5;
    }
    .uh-fmp-field,
    .uh-fmp-info-card-desc {
      color: #999999;
    }
    .uh-fmp-field input[type="text"] {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-info-card {
      background: rgba(255, 255, 255, 0.06);
    }
    .uh-fmp-btn {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.12);
      color: #f5f5f5;
    }
    .uh-fmp-btn:hover {
      background: rgba(255, 255, 255, 0.16);
    }
    .uh-fmp-btn-primary {
      background: rgba(22, 119, 255, 0.9);
      color: #ffffff;
    }
    .uh-fmp-hint {
      color: rgba(255, 255, 255, 0.4);
    }
  }
`,$=[{key:`displayName`,label:`小程序名称`,required:!0},{key:`miniProgramCode`,label:`太阳码图片地址`,required:!0},{key:`link`,label:`小程序地址`,required:!1},{key:`authorName`,label:`作者昵称`,required:!1},{key:`website`,label:`作者网站`,required:!1},{key:`description`,label:`描述`,required:!1},{key:`applyRemark`,label:`申请说明`,required:!1},{key:`email`,label:`邮箱（选填，用于审核结果通知）`,required:!1}],Se=class extends Y{static{this.styles=[xe]}static{this.properties={applyOpen:{state:!0},linksOpen:{state:!0},applySubmitting:{state:!0},captchaId:{state:!0},captchaSrc:{state:!0},linksLoading:{state:!0},linksError:{state:!0},miniInfo:{state:!0},blogger:{state:!0}}}constructor(){super(),this.dragState=null,this.config=X,this.applyOpen=!1,this.linksOpen=!1,this.applySubmitting=!1,this.captchaId=``,this.captchaSrc=``,this.linksLoading=!1,this.linksError=!1,this.miniInfo=null,this.blogger=null}connectedCallback(){super.connectedCallback(),(!be()||this.isClosed())&&this.remove()}firstUpdated(){this.applyPosition()}isClosed(){if(this.config.rememberClosed===!1)return!1;try{return localStorage.getItem(Z)===`1`}catch{return!1}}get cardEl(){return this.renderRoot.querySelector(`.uh-fmp`)}applyPosition(){let e=this.cardEl;if(!e)return;let t=this.config,n=t.position||`bottom-right`;e.classList.add(`uh-fmp-pos-`+n);let r=Number(t.offsetX)||0,i=Number(t.offsetY)||0,a=n===`center`||n===`top-center`||n===`bottom-center`?`calc(-50% + `+r+`px)`:r+`px`,o=n===`center`||n===`left-center`||n===`right-center`?`calc(-50% + `+i+`px)`:i+`px`;e.style.transform=`translate(`+a+`, `+o+`)`,e.style.setProperty(`--uh-fmp-edge`,(Number(t.edgeHideDistance)||24)+`px`)}onPointerDown(e){let t=e.target;if(t&&(t.closest(`.uh-fmp-close`)||t.closest(`.uh-fmp-actions`)||t.closest(`.uh-fmp-overlay`)))return;let n=this.cardEl;if(!n)return;let r=n.getBoundingClientRect();this.dragState={startX:e.clientX,startY:e.clientY,left:r.left,top:r.top},n.classList.add(`uh-fmp-dragging`),n.style.touchAction=`none`,n.setPointerCapture(e.pointerId),e.preventDefault()}onPointerMove(e){let t=this.dragState,n=this.cardEl;if(!t||!n)return;let r=t.left+(e.clientX-t.startX),i=t.top+(e.clientY-t.startY);r=Math.max(0,Math.min(r,window.innerWidth-n.offsetWidth)),i=Math.max(0,Math.min(i,window.innerHeight-n.offsetHeight)),this.setFree(r,i)}onPointerEnd(){let e=this.cardEl;this.dragState&&e&&(this.dragState=null,e.classList.remove(`uh-fmp-dragging`),e.style.touchAction=``,this.maybeEdgeHide())}setFree(e,t){let n=this.cardEl;n&&(n.style.left=e+`px`,n.style.top=t+`px`,n.style.right=`auto`,n.style.bottom=`auto`,n.style.transform=`translate(0, 0)`,n.classList.remove(`uh-fmp-edge`))}maybeEdgeHide(){if(!this.config.edgeHideEnabled)return;let e=this.cardEl;if(!e)return;let t=e.getBoundingClientRect(),n={left:t.left,right:window.innerWidth-t.right,top:t.top,bottom:window.innerHeight-t.bottom},r=null,i=80;[`left`,`right`,`top`,`bottom`].forEach(e=>{n[e]<i&&(i=n[e],r=e)}),r&&e.classList.add(`uh-fmp-edge`,`uh-fmp-edge-`+r)}onCloseClick(){if(this.config.rememberClosed!==!1)try{localStorage.setItem(Z,`1`)}catch{}let e=this.cardEl;e?(e.classList.add(`uh-fmp-closing`),setTimeout(()=>this.remove(),200)):this.remove()}onOverlayClick(e){e.target.classList.contains(`uh-fmp-overlay`)&&this.closeModals()}closeModals(){this.applyOpen=!1,this.linksOpen=!1}openApply(){this.applyOpen=!0,this.refreshCaptcha()}refreshCaptcha(){fetch(ge).then(e=>e.json()).then(e=>{e&&e.imageBase64&&this.setCaptcha(e)}).catch(()=>{})}setCaptcha(e){this.captchaId=e.id,this.captchaSrc=`data:image/png;base64,`+e.imageBase64}async onApplySubmit(e){e.preventDefault();let t=e.target,n={};$.forEach(e=>{let r=t.elements.namedItem(e.key);r&&r.value.trim()&&(n[e.key]=r.value.trim())});let r=(t.elements.namedItem(`captchaCode`)?.value||``).trim(),i=`?captchaId=`+encodeURIComponent(this.captchaId||``)+`&captchaCode=`+encodeURIComponent(r);this.applySubmitting=!0;try{let e=await fetch(_e+i,{method:`POST`,headers:{"Content-Type":`application/json`},body:JSON.stringify({spec:n})}),t=await e.json().catch(()=>({}));if(e.status===200||e.status===201){this.applyOpen=!1,alert(`申请提交成功，请等待审核`);return}e.status===403&&t.captcha&&this.setCaptcha(t.captcha),alert(t.message||`提交失败，请重试`)}catch{alert(`网络异常，请稍后重试`)}finally{this.applySubmitting=!1}}openLinks(){this.linksOpen=!0,this.linksLoading=!0,this.linksError=!1,this.miniInfo=null,this.blogger=null,fetch(ve).then(e=>e.json()).then(e=>{let t=e?.pluginConfig,n=e?.authorConfig;this.miniInfo=t?.linkInfo?.miniInfo||null,this.blogger=n?.blogger||null}).catch(()=>{this.linksError=!0}).finally(()=>{this.linksLoading=!1})}render(){let e=this.config,t=Number(e.imageSize)||100;return R`
      ${this.applyOpen?this.renderApplyModal():``}
      ${this.linksOpen?this.renderLinksModal():``}
      <div
        class="uh-fmp"
        @pointerdown=${this.onPointerDown}
        @pointermove=${this.onPointerMove}
        @pointerup=${this.onPointerEnd}
        @pointercancel=${this.onPointerEnd}
      >
        ${e.closeEnabled===!1?``:R`<button type="button" class="uh-fmp-close" aria-label="关闭悬浮窗" @click=${this.onCloseClick}>&times;</button>`}
        ${e.imageUrl?R`<img class="uh-fmp-img" src=${e.imageUrl} alt=${e.name||`小程序太阳码`} style="width:${t}px;height:${t}px" />`:``}
        ${e.name?R`<div class="uh-fmp-name" style="font-size:${Number(e.nameSize)||14}px;color:${e.nameColor||`#333333`}">${e.name}</div>`:``}
        ${e.description?R`<div class="uh-fmp-desc" style="font-size:${Number(e.descSize)||12}px;color:${e.descColor||`#999999`}">${e.description}</div>`:``}
        ${e.miniProgramApply?R`
              <div class="uh-fmp-actions">
                <button type="button" class="uh-fmp-btn" @click=${this.openApply}>申请</button>
                <button type="button" class="uh-fmp-btn" @click=${this.openLinks}>友链信息</button>
                <div class="uh-fmp-hint">小程序申请和友链信息</div>
              </div>`:``}
      </div>
    `}renderApplyModal(){return R`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序申请</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-modal-body">
            <form class="uh-fmp-form" @submit=${this.onApplySubmit}>
              ${$.map(e=>R`
                  <label class="uh-fmp-field">
                    <span>${e.label}${e.required?` *`:``}</span>
                    <input type="text" name=${e.key} ?required=${e.required} />
                  </label>`)}
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
                <button type="submit" class="uh-fmp-btn uh-fmp-btn-primary" ?disabled=${this.applySubmitting}>
                  ${this.applySubmitting?`提交中…`:`提交申请`}
                </button>
                <button type="button" class="uh-fmp-btn" @click=${this.closeModals}>取消</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    `}renderLinksModal(){let e=this.miniInfo,t=this.blogger,n=!!(e&&Object.keys(e).length)||!!(t&&Object.keys(t).length);return R`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">友链信息</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-modal-body">
            ${this.linksLoading?R`<div class="uh-fmp-loading">加载中…</div>`:this.linksError?R`<div class="uh-fmp-empty">加载失败，请稍后重试</div>`:n?R`
                      ${e&&Object.keys(e).length?R`
                            <div class="uh-fmp-info-card">
                              ${e.miniProgramCode?R`<img class="uh-fmp-info-card-img" src=${e.miniProgramCode} alt="小程序太阳码" />`:``}
                              ${e.displayName?R`<div class="uh-fmp-info-card-title">${e.displayName}</div>`:``}
                              ${e.description?R`<div class="uh-fmp-info-card-desc">${e.description}</div>`:``}
                              ${e.applyRemark?R`<div class="uh-fmp-info-card-desc">${e.applyRemark}</div>`:``}
                              ${e.link?R`<a class="uh-fmp-info-card-link" href=${e.link} target="_blank" rel="noopener noreferrer">${e.link}</a>`:``}
                            </div>`:``}
                      ${t&&Object.keys(t).length?R`
                            <div class="uh-fmp-info-card">
                              ${t.avatar?R`<img class="uh-fmp-info-card-avatar" src=${t.avatar} alt="博主头像" />`:``}
                              ${t.nickname?R`<div class="uh-fmp-info-card-title">${t.nickname}</div>`:``}
                              ${t.description?R`<div class="uh-fmp-info-card-desc">${t.description}</div>`:``}
                              ${t.website?R`<a class="uh-fmp-info-card-link" href=${t.website} target="_blank" rel="noopener noreferrer">${t.website}</a>`:``}
                            </div>`:``}
                    `:R`<div class="uh-fmp-empty">暂无友链信息</div>`}
          </div>
        </div>
      </div>
    `}};customElements.define(`uh-float-mini-profile`,Se);function Ce(){if(document.querySelector(`uh-float-mini-profile`))return;let e=document.createElement(`uh-float-mini-profile`);document.body.appendChild(e)}X&&typeof X==`object`&&(document.body?Ce():document.addEventListener(`DOMContentLoaded`,Ce))})();