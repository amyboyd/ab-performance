/**
@fileOverview

store.js uses localStorage when available, and falls back on globalStorage for
earlier versions of Firefox and the userData behavior in IE6 and IE7. No flash
to slow down your page load. No cookies to fatten your network requests.

From https://github.com/marcuswestin/store.js
Copyright (c) 2010-2011 Marcus Westin

Examples:

// Store 'marcus' at 'username'
store.set('username', 'marcus')

// Get 'username'
store.get('username')

// Remove 'username'
store.remove('username')

// Clear all keys
store.clear()

// Store an object literal - store.js uses JSON.stringify under the hood
store.set('user', { name: 'marcus', likes: 'javascript' })

// Get the stored object - store.js uses JSON.parse under the hood
var user = store.get('user')
alert(user.name + ' likes ' + user.likes)
*/

goog.provide('store');

var store=function(){var b={},e=window,g=e.document,c;b.disabled=false;b.set=function(){};b.get=function(){};b.remove=function(){};b.clear=function(){};b.transact=function(a,d){var f=b.get(a);if(typeof f=='undefined')f={};d(f);b.set(a,f)};b.serialize=function(a){return JSON.stringify(a)};b.deserialize=function(a){if(typeof a=='string')return JSON.parse(a)};var h;try{h='localStorage'in e&&e.localStorage}catch(k){h=false}if(h){c=e.localStorage;b.set=function(a,d){c.setItem(a,b.serialize(d))};b.get=
function(a){return b.deserialize(c.getItem(a))};b.remove=function(a){c.removeItem(a)};b.clear=function(){c.clear()}}else{var i;try{i='globalStorage'in e&&e.globalStorage&&e.globalStorage[e.location.hostname]}catch(l){i=false}if(i){c=e.globalStorage[e.location.hostname];b.set=function(a,d){c[a]=b.serialize(d)};b.get=function(a){return b.deserialize(c[a]&&c[a].value)};b.remove=function(a){delete c[a]};b.clear=function(){for(var a in c)delete c[a]}}else if(g.documentElement.addBehavior){c=g.createElement('div');
e=function(a){return function(){var d=Array.prototype.slice.call(arguments,0);d.unshift(c);g.body.appendChild(c);c.addBehavior('#default#userData');c.load('localStorage');d=a.apply(b,d);g.body.removeChild(c);return d}};b.set=e(function(a,d,f){a.setAttribute(d,b.serialize(f));a.save('localStorage')});b.get=e(function(a,d){return b.deserialize(a.getAttribute(d))});b.remove=e(function(a,d){a.removeAttribute(d);a.save('localStorage')});b.clear=e(function(a){var d=a.XMLDocument.documentElement.attributes;
a.load('localStorage');for(var f=0,j;j=d[f];f++)a.removeAttribute(j.name);a.save('localStorage')})}}try{b.set('__storejs__','__storejs__');if(b.get('__storejs__')!='__storejs__')b.disabled=true;b.remove('__storejs__')}catch(m){b.disabled=true}return b}();
