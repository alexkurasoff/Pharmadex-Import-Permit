(window.webpackJsonp=window.webpackJsonp||[]).push([[35],{90:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var o=function(){function e(e,t){for(var n=0;n<t.length;n++){var o=t[n];o.enumerable=o.enumerable||!1,o.configurable=!0,"value"in o&&(o.writable=!0),Object.defineProperty(e,o.key,o)}}return function(t,n,o){return n&&e(t.prototype,n),o&&e(t,o),t}}(),r=n(1),u=l(r),a=l(n(18)),i=n(11);function l(e){return e&&e.__esModule?e:{default:e}}var f=function(e){function t(e,n){!function(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}(this,t);var o=function(e,t){if(!e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!t||"object"!=typeof t&&"function"!=typeof t?e:t}(this,(t.__proto__||Object.getPrototypeOf(t)).call(this,e,n));return o.state={labels:{termsAndConditions_text:""}},o}return function(e,t){if("function"!=typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function, not "+typeof t);e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}),t&&(Object.setPrototypeOf?Object.setPrototypeOf(e,t):e.__proto__=t)}(t,e),o(t,[{key:"componentDidMount",value:function(){a.default.resolveLabels(this)}},{key:"render",value:function(){return this.state.labels.termsAndConditions_text.length>0?u.default.createElement(i.Container,{fluid:!0},t.formatText(this.state.labels.termsAndConditions_text)):[]}}],[{key:"formatText",value:function(e){var t=e.split("\n"),n=[];return t.forEach((function(e){n.push(u.default.createElement("span",{key:n.length},e,u.default.createElement("br",null)))})),n}}]),t}(r.Component);t.default=f}}]);