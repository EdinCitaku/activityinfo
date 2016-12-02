
<#-- From http://apidocs.enketo.org/api/assets.css -->

/*! normalize.css v2.1.2 | MIT License | git.io/normalize */

/* ==========================================================================
HTML5 display definitions
========================================================================== */

/**
* Correct `block` display not defined in IE 8/9.
*/

article,
aside,
details,
figcaption,
figure,
footer,
header,
hgroup,
main,
nav,
section,
summary {
display: block;
}

/**
* Correct `inline-block` display not defined in IE 8/9.
*/

audio,
canvas,
video {
display: inline-block;
}

/**
* Prevent modern browsers from displaying `audio` without controls.
* Remove excess height in iOS 5 devices.
*/

audio:not([controls]) {
display: none;
height: 0;
}

/**
* Address `[hidden]` styling not present in IE 8/9.
* Hide the `template` element in IE, Safari, and Firefox < 22.
*/

[hidden],
template {
display: none;
}

/* ==========================================================================
Base
========================================================================== */

/**
* 1. Set default font family to sans-serif.
* 2. Prevent iOS text size adjust after orientation change, without disabling
*    user zoom.
*/

html {
font-family: sans-serif;
/* 1 */
-ms-text-size-adjust: 100%;
/* 2 */
-webkit-text-size-adjust: 100%;
/* 2 */
}

/**
* Remove default margin.
*/

body {
margin: 0;
}

/* ==========================================================================
Links
========================================================================== */

/**
* Remove the gray background color from active links in IE 10.
*/

a {
background: transparent;
}

/**
* Address `outline` inconsistency between Chrome and other browsers.
*/

a:focus {
outline: thin dotted;
}

/**
* Improve readability when focused and also mouse hovered in all browsers.
*/

a:active,
a:hover {
outline: 0;
}

/* ==========================================================================
Typography
========================================================================== */

/**
* Address variable `h1` font-size and margin within `section` and `article`
* contexts in Firefox 4+, Safari 5, and Chrome.
*/

h1 {
font-size: 2em;
margin: 0.67em 0;
}

/**
* Address styling not present in IE 8/9, Safari 5, and Chrome.
*/

abbr[title] {
border-bottom: 1px dotted;
}

/**
* Address style set to `bolder` in Firefox 4+, Safari 5, and Chrome.
*/

b,
strong {
font-weight: bold;
}

/**
* Address styling not present in Safari 5 and Chrome.
*/

dfn {
font-style: italic;
}

/**
* Address differences between Firefox and other browsers.
*/

hr {
-moz-box-sizing: content-box;
box-sizing: content-box;
height: 0;
}

/**
* Address styling not present in IE 8/9.
*/

mark {
background: #ff0;
color: #000;
}

/**
* Correct font family set oddly in Safari 5 and Chrome.
*/

code,
kbd,
pre,
samp {
font-family: monospace, serif;
font-size: 1em;
}

/**
* Improve readability of pre-formatted text in all browsers.
*/

pre {
white-space: pre-wrap;
}

/**
* Set consistent quote types.
*/

q {
quotes: "\201C" "\201D" "\2018" "\2019";
}

/**
* Address inconsistent and variable font size in all browsers.
*/

small {
font-size: 80%;
}

/**
* Prevent `sub` and `sup` affecting `line-height` in all browsers.
*/

sub,
sup {
font-size: 75%;
line-height: 0;
position: relative;
vertical-align: baseline;
}

sup {
top: -0.5em;
}

sub {
bottom: -0.25em;
}

/* ==========================================================================
Embedded content
========================================================================== */

/**
* Remove border when inside `a` element in IE 8/9.
*/

img {
border: 0;
}

/**
* Correct overflow displayed oddly in IE 9.
*/

svg:not(:root) {
overflow: hidden;
}

/* ==========================================================================
Figures
========================================================================== */

/**
* Address margin not present in IE 8/9 and Safari 5.
*/

figure {
margin: 0;
}

/* ==========================================================================
Forms
========================================================================== */

/**
* Define consistent border, margin, and padding.
*/

fieldset {
border: 1px solid #c0c0c0;
margin: 0 2px;
padding: 0.35em 0.625em 0.75em;
}

/**
* 1. Correct `color` not being inherited in IE 8/9.
* 2. Remove padding so people aren't caught out if they zero out fieldsets.
*/

legend {
border: 0;
/* 1 */
padding: 0;
/* 2 */
}

/**
* 1. Correct font family not being inherited in all browsers.
* 2. Correct font size not being inherited in all browsers.
* 3. Address margins set differently in Firefox 4+, Safari 5, and Chrome.
*/

button,
input,
select,
textarea {
font-family: inherit;
/* 1 */
font-size: 100%;
/* 2 */
margin: 0;
/* 3 */
}

/**
* Address Firefox 4+ setting `line-height` on `input` using `!important` in
* the UA stylesheet.
*/

button,
input {
line-height: normal;
}

/**
* Address inconsistent `text-transform` inheritance for `button` and `select`.
* All other form control elements do not inherit `text-transform` values.
* Correct `button` style inheritance in Chrome, Safari 5+, and IE 8+.
* Correct `select` style inheritance in Firefox 4+ and Opera.
*/

button,
select {
text-transform: none;
}

/**
* 1. Avoid the WebKit bug in Android 4.0.* where (2) destroys native `audio`
*    and `video` controls.
* 2. Correct inability to style clickable `input` types in iOS.
* 3. Improve usability and consistency of cursor style between image-type
*    `input` and others.
*/

button,
html input[type="button"],
input[type="reset"],
input[type="submit"] {
-webkit-appearance: button;
/* 2 */
cursor: pointer;
/* 3 */
}

/**
* Re-set default cursor for disabled elements.
*/

button[disabled],
html input[disabled] {
cursor: default;
}

/**
* 1. Address box sizing set to `content-box` in IE 8/9.
* 2. Remove excess padding in IE 8/9.
*/

input[type="checkbox"],
input[type="radio"] {
box-sizing: border-box;
/* 1 */
padding: 0;
/* 2 */
}

/**
* 1. Address `appearance` set to `searchfield` in Safari 5 and Chrome.
* 2. Address `box-sizing` set to `border-box` in Safari 5 and Chrome
*    (include `-moz` to future-proof).
*/

input[type="search"] {
-webkit-appearance: textfield;
/* 1 */
-moz-box-sizing: content-box;
-webkit-box-sizing: content-box;
/* 2 */
box-sizing: content-box;
}

/**
* Remove inner padding and search cancel button in Safari 5 and Chrome
* on OS X.
*/

input[type="search"]::-webkit-search-cancel-button,
input[type="search"]::-webkit-search-decoration {
-webkit-appearance: none;
}

/**
* Remove inner padding and border in Firefox 4+.
*/

button::-moz-focus-inner,
input::-moz-focus-inner {
border: 0;
padding: 0;
}

/**
* 1. Remove default vertical scrollbar in IE 8/9.
* 2. Improve readability and alignment in all browsers.
*/

textarea {
overflow: auto;
/* 1 */
vertical-align: top;
/* 2 */
}

/* ==========================================================================
Tables
========================================================================== */

/**
* Remove most spacing between table cells.
*/

table {
border-collapse: collapse;
border-spacing: 0;
}

/* GENERAL */

html,
body {
height: 100%;
}

body {
background: #fff;
}

body,
input,
textarea {
color: #191919;
font: 14px/140% 'Open Sans', Arial, sans-serif;
}

a {
color: #119bdf;
text-decoration: none;
}

a:hover {
color: #29aeef;
text-decoration: underline;
}

h1,
h2,
h3,
h4,
p,
ul,
code {
margin: 0 0 20px;
}

code {
font-family: 'Monaco', monospace, sans-serif;
}

/* SIDEBAR */
#sidebar {
background: #191919;
color: #fff;
float: left;
height: 100%;
overflow: auto;
position: fixed;
top: 0;
width: 240px;
}

#sidebar img {
display: block;
margin: 10px auto -10px auto;
}

#sidebar h1 {
background: #111;
font-size: 14px;
margin: 0;
padding: 20px;
text-align: center;
text-transform: uppercase;
}

#sidebar h2 {
color: #888;
font-size: 14px;
margin: 0;
}

#sidebar ul {
list-style: none;
margin: 0;
padding: 0;
}

#sidebar ul#links {
padding: 20px;
}

#sidebar ul ul {
padding-left: 20px;
}

#sidebar ul li a {
color: #fff;
display: block;
padding: 0 0 15px;
}

#sidebar ul li a:active,
#sidebar ul li a.active {
text-decoration: underline;
}

#sidebar label {
display: block;
margin: 0 0 0 10px;
}

#sidebar input {
background: #cccccc;
border: 0;
width: 200px;
border-radius: 2px;
-moz-border-radius: 2px;
-webkit-border-radius: 2px;
box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
-moz-box-shadow: inset 0 1px 5px rgba(0, 0, 0, 0.2);
-webkit-box-shadow: inset 0 1px 5px rgba(0, 0, 0, 0.2);
color: #111;
margin: 0 0 20px 10px;
padding: 5px 10px;
}

.sidebar__version-switcher {
margin: 10px 10px 20px 10px;
}

.sidebar__version-switcher a {
border: 1px solid  #52ce0e;
padding: 5px 10px;
border-radius: 1px;
display: inline-block;
box-sizing: border-box;
width: calc(50% - 2px);
text-align: center;
}

#sidebar.v1 .sidebar__version-switcher__v1,
#sidebar.v2 .sidebar__version-switcher__v2 {
background: #52ce0e;
color: white;
}

-webkit-input-placeholder {
/* WebKit browsers */
color: #444;
}

-moz-placeholder {
/* Mozilla Firefox 4 to 18 */
color: #444;
}

-moz-placeholder {
/* Mozilla Firefox 19+ */
color: #444;
}

-ms-input-placeholder {
/* Internet Explorer 10+ */
color: #444;
}

#sidebar input:active,
#sidebar input:focus {
border-color: #ccc;
box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.1);
-moz-box-shadow: inset 0 1px 5px rgba(0, 0, 0, 0.1);
-webkit-box-shadow: inset 0 1px 5px rgba(0, 0, 0, 0.1);
outline: none;
}

#sidebar #links input {
background-color: #555;
color: bbb;
margin-left: -10px;
}

/* References */
#content {
padding: 20px;
padding-left: 260px;
max-width: 900px;
}

#content .control {
cursor: pointer;
font-size: 12px;
margin-left: 15px;
}

#content .changes {
border: 1px solid #f27e61;
padding: 10px;
margin-bottom: 20px;
background: rgba(242, 126, 97, 0.05);
}

#content .changes p {
margin-bottom: 10px;
}

#content .changes p:last-child {
margin-bottom: 0px;
}

#content article {
clear: both;
margin: 15px 0;
}

#content article > a {
background: #333;
border-radius: 2px;
-moz-border-radius: 2px;
-webkit-border-radius: 2px;
cursor: pointer;
display: block;
padding: 15px;
}

#content article > a:hover {
background: #222;
text-decoration: none;
}

#content a h2 {
color: #fff;
font-size: 18px;
font-weight: normal;
margin: 0;
}

#content a h2 code {
float: right;
font-size: 14px;
}

#content a h2 code b {
border-radius: 2px;
-moz-border-radius: 2px;
-webkit-border-radius: 2px;
color: #fff;
font-weight: normal;
padding: 3px 6px;
}

/* Default */
#content article {
border-color: #e2e7ea;
}

#content article a h2 code {
color: #657e8b;
}

#content article a h2 code b {
background: #657e8b;
text-transform: uppercase;
}

/* GET */
#content article.GET a h2 code {
color: #119bdf;
}

#content article.GET a h2 code b {
background: #119bdf;
}

/* POST */
#content article.POST a h2 code {
color: #52ce0e;
}

#content article.POST a h2 code b {
background: #52ce0e;
}

/* PUT */
#content article.PUT a h2 code {
color: #e08f10;
}

#content article.PUT a h2 code b {
background: #e08f10;
}

/* DELETE */
#content article.DELETE a h2 code {
color: #e02a10;
}

#content article.DELETE a h2 code b {
background: #e02a10;
}

/* Reference's body */
#content h3 {}

#content .body {
border-top: 1px solid #eef1f2;
display: block;
padding: 15px 15px 0;
}

#content .body code, textarea.try {
border: 1px solid #e2e7ea;
background: #eef1f2;
border-radius: 2px;
-moz-border-radius: 2px;
-webkit-border-radius: 2px;
color: #546974;
padding: 0 1px;
}

#content .body p > code {
display: block;
padding: 15px;
white-space: pre;
overflow: auto;
}

a.try {
word-wrap: break-word;
}

textarea.try {
box-sizing: border-box;
width: 100%;
padding: 15px;
min-height: 100px;
}

.status-code {
padding: 5px 10px;
line-height: 40px;
border-radius: 10px;
background-color: #e2e7ea;
font-weight: bold;
border: 1px solid #aaa;
}

.status-code.error {
color: red;
}

.status-code.ok {
color: green;
}

.status-code:hover {
background-color: #eee;
position: relative;
}

.status-code[data-title]:hover:after {
content: attr(data-title);
padding: 0 8px;
color: #333;
position: absolute;
left: 0;
top: 110%;
white-space: nowrap;
z-index: 20;
border-radius: 5px;
box-shadow: 0px 0px 4px #222;
background-color: #F8EF1E;
}