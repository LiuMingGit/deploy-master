webpackJsonp([4],{"7Rk/":function(n,t,i){var a=i("VU/8")(i("ZPsk"),i("x+SK"),function(n){i("bidZ")},"data-v-018239da",null);n.exports=a.exports},ARoL:function(n,t,i){var a=i("VU/8")(i("bPRz"),i("cgHr"),function(n){i("JhvE")},"data-v-662ade56",null);n.exports=a.exports},JhvE:function(n,t,i){var a=i("yYM3");"string"==typeof a&&(a=[[n.i,a,""]]),a.locals&&(n.exports=a.locals);i("rjj0")("17fdf4a8",a,!0)},Pn0r:function(n,t,i){(n.exports=i("FZ+f")(!1)).push([n.i,"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",""])},ZPsk:function(n,t,i){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default={name:"vue-matrix-raindrop",props:{canvasWidth:{type:Number,default:800},canvasHeight:{type:Number,default:600},fontSize:{type:Number,default:20},fontFamily:{type:String,default:"arial"},textContent:{type:String,default:"abcdefghijklmnopqrstuvwxyz"},textColor:{type:String,default:"#0F0",validator:function(n){return/^#([0-9a-fA-F]{6})|([0-9a-fA-F]{3})$/g.test(n)}},backgroundColor:{type:String,default:"rgba(0,0,0,0.1)",validator:function(n){return/^[rR][gG][Bb][Aa][\(]((2[0-4][0-9]|25[0-5]|[01]?[0-9][0-9]?),){2}(2[0-4][0-9]|25[0-5]|[01]?[0-9][0-9]?),?(0\.\d{1,2}|1|0)?[\)]{1}$/.test(n)}},speed:{type:Number,default:2,validator:function(n){return n%1==0}}},mounted:function(){this.initRAF(),this.initCanvas(),this.initRainDrop(),this.animationUpdate()},methods:{initRAF:function(){window.requestAnimationFrame=window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||window.oRequestAnimationFrame||function(n){window.setTimeout(n,1e3/60)},window.cancelAnimationFrame=window.cancelAnimationFrame||window.webkitCancelAnimationFrame||window.mozCancelAnimationFrame||window.oCancelAnimationFrame||function(n){window.clearTimeout(n)}},initCanvas:function(){this.canvas=document.getElementById("vue-matrix-raindrop"),"canvas"!==this.canvas.tagName.toLowerCase()&&console.error("Error! Invalid canvas! Please check the canvas's id!"),this.canvas.width=this.canvasWidth,this.canvas.height=this.canvasHeight,this.canvasCtx=this.canvas.getContext("2d"),this.canvasCtx.font=this.fontSize+"px "+this.fontFamily,this.columns=this.canvas.width/this.fontSize},initRainDrop:function(){for(var n=0;n<this.columns;n++)this.rainDropPositionArray.push(0)},animationUpdate:function(){if(this.speedCnt++,this.speedCnt===this.speed){this.speedCnt=0,this.canvasCtx.fillStyle=this.backgroundColor,this.canvasCtx.fillRect(0,0,this.canvas.width,this.canvas.height),this.canvasCtx.fillStyle=this.textColor;for(var n=0,t=this.rainDropPositionArray.length;n<t;n++){this.rainDropPositionArray[n]++;var i=Math.floor(Math.random()*this.textContent.length),a=this.textContent[i],e=this.rainDropPositionArray[n]*this.fontSize;this.canvasCtx.fillText(a,n*this.fontSize,e),e>this.canvasHeight&&Math.random()>.9&&(this.rainDropPositionArray[n]=0)}}window.requestAnimationFrame(this.animationUpdate)}},data:function(){return{canvasCtx:null,canvas:null,columns:0,rainDropPositionArray:[],speedCnt:0}}}},bPRz:function(n,t,i){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=i("Dd8w"),e=i.n(a),o=i("NYxO"),r=i("7Rk/"),s=i.n(r);t.default={name:"Dashboard",components:{VueMatrixRaindrop:s.a},computed:e()({},Object(o.b)(["name","roles"]))}},bidZ:function(n,t,i){var a=i("Pn0r");"string"==typeof a&&(a=[[n.i,a,""]]),a.locals&&(n.exports=a.locals);i("rjj0")("6825c755",a,!0)},cgHr:function(n,t){n.exports={render:function(){var n=this.$createElement,t=this._self._c||n;return t("div",{staticClass:"dashboard-container"},[t("VueMatrixRaindrop",{attrs:{textContent:"01abcdefghijklmnopqrstuvwxyz"}})],1)},staticRenderFns:[]}},"x+SK":function(n,t){n.exports={render:function(){var n=this.$createElement;return(this._self._c||n)("canvas",{attrs:{id:"vue-matrix-raindrop"}})},staticRenderFns:[]}},yYM3:function(n,t,i){(n.exports=i("FZ+f")(!1)).push([n.i,"\n.dashboard-container[data-v-662ade56] {\n  margin: 2px;\n}\n.dashboard-text[data-v-662ade56] {\n  font-size: 30px;\n  line-height: 46px;\n}\n",""])}});