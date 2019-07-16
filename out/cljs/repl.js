// Compiled by ClojureScript 1.10.520 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
goog.require('cljs.spec.alpha');
goog.require('goog.string');
goog.require('goog.string.format');
cljs.repl.print_doc = (function cljs$repl$print_doc(p__60570){
var map__60571 = p__60570;
var map__60571__$1 = (((((!((map__60571 == null))))?(((((map__60571.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60571.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60571):map__60571);
var m = map__60571__$1;
var n = cljs.core.get.call(null,map__60571__$1,new cljs.core.Keyword(null,"ns","ns",441598760));
var nm = cljs.core.get.call(null,map__60571__$1,new cljs.core.Keyword(null,"name","name",1843675177));
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,(function (){var or__4131__auto__ = new cljs.core.Keyword(null,"spec","spec",347520401).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return [(function (){var temp__5735__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__5735__auto__)){
var ns = temp__5735__auto__;
return [cljs.core.str.cljs$core$IFn$_invoke$arity$1(ns),"/"].join('');
} else {
return null;
}
})(),cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join('');
}
})());

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__60573_60605 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__60574_60606 = null;
var count__60575_60607 = (0);
var i__60576_60608 = (0);
while(true){
if((i__60576_60608 < count__60575_60607)){
var f_60609 = cljs.core._nth.call(null,chunk__60574_60606,i__60576_60608);
cljs.core.println.call(null,"  ",f_60609);


var G__60610 = seq__60573_60605;
var G__60611 = chunk__60574_60606;
var G__60612 = count__60575_60607;
var G__60613 = (i__60576_60608 + (1));
seq__60573_60605 = G__60610;
chunk__60574_60606 = G__60611;
count__60575_60607 = G__60612;
i__60576_60608 = G__60613;
continue;
} else {
var temp__5735__auto___60614 = cljs.core.seq.call(null,seq__60573_60605);
if(temp__5735__auto___60614){
var seq__60573_60615__$1 = temp__5735__auto___60614;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__60573_60615__$1)){
var c__4550__auto___60616 = cljs.core.chunk_first.call(null,seq__60573_60615__$1);
var G__60617 = cljs.core.chunk_rest.call(null,seq__60573_60615__$1);
var G__60618 = c__4550__auto___60616;
var G__60619 = cljs.core.count.call(null,c__4550__auto___60616);
var G__60620 = (0);
seq__60573_60605 = G__60617;
chunk__60574_60606 = G__60618;
count__60575_60607 = G__60619;
i__60576_60608 = G__60620;
continue;
} else {
var f_60621 = cljs.core.first.call(null,seq__60573_60615__$1);
cljs.core.println.call(null,"  ",f_60621);


var G__60622 = cljs.core.next.call(null,seq__60573_60615__$1);
var G__60623 = null;
var G__60624 = (0);
var G__60625 = (0);
seq__60573_60605 = G__60622;
chunk__60574_60606 = G__60623;
count__60575_60607 = G__60624;
i__60576_60608 = G__60625;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_60626 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__4131__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_60626);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_60626)))?cljs.core.second.call(null,arglists_60626):arglists_60626));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,["\n  Please see http://clojure.org/",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,["\n  Please see http://clojure.org/special_forms#",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"spec","spec",347520401).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Spec");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__60577_60627 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__60578_60628 = null;
var count__60579_60629 = (0);
var i__60580_60630 = (0);
while(true){
if((i__60580_60630 < count__60579_60629)){
var vec__60591_60631 = cljs.core._nth.call(null,chunk__60578_60628,i__60580_60630);
var name_60632 = cljs.core.nth.call(null,vec__60591_60631,(0),null);
var map__60594_60633 = cljs.core.nth.call(null,vec__60591_60631,(1),null);
var map__60594_60634__$1 = (((((!((map__60594_60633 == null))))?(((((map__60594_60633.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60594_60633.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60594_60633):map__60594_60633);
var doc_60635 = cljs.core.get.call(null,map__60594_60634__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_60636 = cljs.core.get.call(null,map__60594_60634__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_60632);

cljs.core.println.call(null," ",arglists_60636);

if(cljs.core.truth_(doc_60635)){
cljs.core.println.call(null," ",doc_60635);
} else {
}


var G__60637 = seq__60577_60627;
var G__60638 = chunk__60578_60628;
var G__60639 = count__60579_60629;
var G__60640 = (i__60580_60630 + (1));
seq__60577_60627 = G__60637;
chunk__60578_60628 = G__60638;
count__60579_60629 = G__60639;
i__60580_60630 = G__60640;
continue;
} else {
var temp__5735__auto___60641 = cljs.core.seq.call(null,seq__60577_60627);
if(temp__5735__auto___60641){
var seq__60577_60642__$1 = temp__5735__auto___60641;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__60577_60642__$1)){
var c__4550__auto___60643 = cljs.core.chunk_first.call(null,seq__60577_60642__$1);
var G__60644 = cljs.core.chunk_rest.call(null,seq__60577_60642__$1);
var G__60645 = c__4550__auto___60643;
var G__60646 = cljs.core.count.call(null,c__4550__auto___60643);
var G__60647 = (0);
seq__60577_60627 = G__60644;
chunk__60578_60628 = G__60645;
count__60579_60629 = G__60646;
i__60580_60630 = G__60647;
continue;
} else {
var vec__60596_60648 = cljs.core.first.call(null,seq__60577_60642__$1);
var name_60649 = cljs.core.nth.call(null,vec__60596_60648,(0),null);
var map__60599_60650 = cljs.core.nth.call(null,vec__60596_60648,(1),null);
var map__60599_60651__$1 = (((((!((map__60599_60650 == null))))?(((((map__60599_60650.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60599_60650.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60599_60650):map__60599_60650);
var doc_60652 = cljs.core.get.call(null,map__60599_60651__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_60653 = cljs.core.get.call(null,map__60599_60651__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_60649);

cljs.core.println.call(null," ",arglists_60653);

if(cljs.core.truth_(doc_60652)){
cljs.core.println.call(null," ",doc_60652);
} else {
}


var G__60654 = cljs.core.next.call(null,seq__60577_60642__$1);
var G__60655 = null;
var G__60656 = (0);
var G__60657 = (0);
seq__60577_60627 = G__60654;
chunk__60578_60628 = G__60655;
count__60579_60629 = G__60656;
i__60580_60630 = G__60657;
continue;
}
} else {
}
}
break;
}
} else {
}

if(cljs.core.truth_(n)){
var temp__5735__auto__ = cljs.spec.alpha.get_spec.call(null,cljs.core.symbol.call(null,cljs.core.str.cljs$core$IFn$_invoke$arity$1(cljs.core.ns_name.call(null,n)),cljs.core.name.call(null,nm)));
if(cljs.core.truth_(temp__5735__auto__)){
var fnspec = temp__5735__auto__;
cljs.core.print.call(null,"Spec");

var seq__60601 = cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"args","args",1315556576),new cljs.core.Keyword(null,"ret","ret",-468222814),new cljs.core.Keyword(null,"fn","fn",-1175266204)], null));
var chunk__60602 = null;
var count__60603 = (0);
var i__60604 = (0);
while(true){
if((i__60604 < count__60603)){
var role = cljs.core._nth.call(null,chunk__60602,i__60604);
var temp__5735__auto___60658__$1 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5735__auto___60658__$1)){
var spec_60659 = temp__5735__auto___60658__$1;
cljs.core.print.call(null,["\n ",cljs.core.name.call(null,role),":"].join(''),cljs.spec.alpha.describe.call(null,spec_60659));
} else {
}


var G__60660 = seq__60601;
var G__60661 = chunk__60602;
var G__60662 = count__60603;
var G__60663 = (i__60604 + (1));
seq__60601 = G__60660;
chunk__60602 = G__60661;
count__60603 = G__60662;
i__60604 = G__60663;
continue;
} else {
var temp__5735__auto____$1 = cljs.core.seq.call(null,seq__60601);
if(temp__5735__auto____$1){
var seq__60601__$1 = temp__5735__auto____$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__60601__$1)){
var c__4550__auto__ = cljs.core.chunk_first.call(null,seq__60601__$1);
var G__60664 = cljs.core.chunk_rest.call(null,seq__60601__$1);
var G__60665 = c__4550__auto__;
var G__60666 = cljs.core.count.call(null,c__4550__auto__);
var G__60667 = (0);
seq__60601 = G__60664;
chunk__60602 = G__60665;
count__60603 = G__60666;
i__60604 = G__60667;
continue;
} else {
var role = cljs.core.first.call(null,seq__60601__$1);
var temp__5735__auto___60668__$2 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5735__auto___60668__$2)){
var spec_60669 = temp__5735__auto___60668__$2;
cljs.core.print.call(null,["\n ",cljs.core.name.call(null,role),":"].join(''),cljs.spec.alpha.describe.call(null,spec_60669));
} else {
}


var G__60670 = cljs.core.next.call(null,seq__60601__$1);
var G__60671 = null;
var G__60672 = (0);
var G__60673 = (0);
seq__60601 = G__60670;
chunk__60602 = G__60671;
count__60603 = G__60672;
i__60604 = G__60673;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
} else {
return null;
}
}
});
/**
 * Constructs a data representation for a Error with keys:
 *  :cause - root cause message
 *  :phase - error phase
 *  :via - cause chain, with cause keys:
 *           :type - exception class symbol
 *           :message - exception message
 *           :data - ex-data
 *           :at - top stack element
 *  :trace - root cause stack elements
 */
cljs.repl.Error__GT_map = (function cljs$repl$Error__GT_map(o){
var base = (function (t){
return cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"type","type",1174270348),(((t instanceof cljs.core.ExceptionInfo))?new cljs.core.Symbol(null,"ExceptionInfo","ExceptionInfo",294935087,null):(((t instanceof EvalError))?new cljs.core.Symbol("js","EvalError","js/EvalError",1793498501,null):(((t instanceof RangeError))?new cljs.core.Symbol("js","RangeError","js/RangeError",1703848089,null):(((t instanceof ReferenceError))?new cljs.core.Symbol("js","ReferenceError","js/ReferenceError",-198403224,null):(((t instanceof SyntaxError))?new cljs.core.Symbol("js","SyntaxError","js/SyntaxError",-1527651665,null):(((t instanceof URIError))?new cljs.core.Symbol("js","URIError","js/URIError",505061350,null):(((t instanceof Error))?new cljs.core.Symbol("js","Error","js/Error",-1692659266,null):null
)))))))], null),(function (){var temp__5735__auto__ = cljs.core.ex_message.call(null,t);
if(cljs.core.truth_(temp__5735__auto__)){
var msg = temp__5735__auto__;
return new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"message","message",-406056002),msg], null);
} else {
return null;
}
})(),(function (){var temp__5735__auto__ = cljs.core.ex_data.call(null,t);
if(cljs.core.truth_(temp__5735__auto__)){
var ed = temp__5735__auto__;
return new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"data","data",-232669377),ed], null);
} else {
return null;
}
})());
});
var via = (function (){var via = cljs.core.PersistentVector.EMPTY;
var t = o;
while(true){
if(cljs.core.truth_(t)){
var G__60674 = cljs.core.conj.call(null,via,t);
var G__60675 = cljs.core.ex_cause.call(null,t);
via = G__60674;
t = G__60675;
continue;
} else {
return via;
}
break;
}
})();
var root = cljs.core.peek.call(null,via);
return cljs.core.merge.call(null,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"via","via",-1904457336),cljs.core.vec.call(null,cljs.core.map.call(null,base,via)),new cljs.core.Keyword(null,"trace","trace",-1082747415),null], null),(function (){var temp__5735__auto__ = cljs.core.ex_message.call(null,root);
if(cljs.core.truth_(temp__5735__auto__)){
var root_msg = temp__5735__auto__;
return new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"cause","cause",231901252),root_msg], null);
} else {
return null;
}
})(),(function (){var temp__5735__auto__ = cljs.core.ex_data.call(null,root);
if(cljs.core.truth_(temp__5735__auto__)){
var data = temp__5735__auto__;
return new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"data","data",-232669377),data], null);
} else {
return null;
}
})(),(function (){var temp__5735__auto__ = new cljs.core.Keyword("clojure.error","phase","clojure.error/phase",275140358).cljs$core$IFn$_invoke$arity$1(cljs.core.ex_data.call(null,o));
if(cljs.core.truth_(temp__5735__auto__)){
var phase = temp__5735__auto__;
return new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"phase","phase",575722892),phase], null);
} else {
return null;
}
})());
});
/**
 * Returns an analysis of the phase, error, cause, and location of an error that occurred
 *   based on Throwable data, as returned by Throwable->map. All attributes other than phase
 *   are optional:
 *  :clojure.error/phase - keyword phase indicator, one of:
 *    :read-source :compile-syntax-check :compilation :macro-syntax-check :macroexpansion
 *    :execution :read-eval-result :print-eval-result
 *  :clojure.error/source - file name (no path)
 *  :clojure.error/line - integer line number
 *  :clojure.error/column - integer column number
 *  :clojure.error/symbol - symbol being expanded/compiled/invoked
 *  :clojure.error/class - cause exception class symbol
 *  :clojure.error/cause - cause exception message
 *  :clojure.error/spec - explain-data for spec error
 */
cljs.repl.ex_triage = (function cljs$repl$ex_triage(datafied_throwable){
var map__60678 = datafied_throwable;
var map__60678__$1 = (((((!((map__60678 == null))))?(((((map__60678.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60678.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60678):map__60678);
var via = cljs.core.get.call(null,map__60678__$1,new cljs.core.Keyword(null,"via","via",-1904457336));
var trace = cljs.core.get.call(null,map__60678__$1,new cljs.core.Keyword(null,"trace","trace",-1082747415));
var phase = cljs.core.get.call(null,map__60678__$1,new cljs.core.Keyword(null,"phase","phase",575722892),new cljs.core.Keyword(null,"execution","execution",253283524));
var map__60679 = cljs.core.last.call(null,via);
var map__60679__$1 = (((((!((map__60679 == null))))?(((((map__60679.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60679.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60679):map__60679);
var type = cljs.core.get.call(null,map__60679__$1,new cljs.core.Keyword(null,"type","type",1174270348));
var message = cljs.core.get.call(null,map__60679__$1,new cljs.core.Keyword(null,"message","message",-406056002));
var data = cljs.core.get.call(null,map__60679__$1,new cljs.core.Keyword(null,"data","data",-232669377));
var map__60680 = data;
var map__60680__$1 = (((((!((map__60680 == null))))?(((((map__60680.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60680.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60680):map__60680);
var problems = cljs.core.get.call(null,map__60680__$1,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814));
var fn = cljs.core.get.call(null,map__60680__$1,new cljs.core.Keyword("cljs.spec.alpha","fn","cljs.spec.alpha/fn",408600443));
var caller = cljs.core.get.call(null,map__60680__$1,new cljs.core.Keyword("cljs.spec.test.alpha","caller","cljs.spec.test.alpha/caller",-398302390));
var map__60681 = new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(cljs.core.first.call(null,via));
var map__60681__$1 = (((((!((map__60681 == null))))?(((((map__60681.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60681.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60681):map__60681);
var top_data = map__60681__$1;
var source = cljs.core.get.call(null,map__60681__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397));
return cljs.core.assoc.call(null,(function (){var G__60686 = phase;
var G__60686__$1 = (((G__60686 instanceof cljs.core.Keyword))?G__60686.fqn:null);
switch (G__60686__$1) {
case "read-source":
var map__60687 = data;
var map__60687__$1 = (((((!((map__60687 == null))))?(((((map__60687.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60687.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60687):map__60687);
var line = cljs.core.get.call(null,map__60687__$1,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471));
var column = cljs.core.get.call(null,map__60687__$1,new cljs.core.Keyword("clojure.error","column","clojure.error/column",304721553));
var G__60689 = cljs.core.merge.call(null,new cljs.core.Keyword(null,"data","data",-232669377).cljs$core$IFn$_invoke$arity$1(cljs.core.second.call(null,via)),top_data);
var G__60689__$1 = (cljs.core.truth_(source)?cljs.core.assoc.call(null,G__60689,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),source):G__60689);
var G__60689__$2 = (cljs.core.truth_(new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,source))?cljs.core.dissoc.call(null,G__60689__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397)):G__60689__$1);
if(cljs.core.truth_(message)){
return cljs.core.assoc.call(null,G__60689__$2,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message);
} else {
return G__60689__$2;
}

break;
case "compile-syntax-check":
case "compilation":
case "macro-syntax-check":
case "macroexpansion":
var G__60690 = top_data;
var G__60690__$1 = (cljs.core.truth_(source)?cljs.core.assoc.call(null,G__60690,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),source):G__60690);
var G__60690__$2 = (cljs.core.truth_(new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,source))?cljs.core.dissoc.call(null,G__60690__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397)):G__60690__$1);
var G__60690__$3 = (cljs.core.truth_(type)?cljs.core.assoc.call(null,G__60690__$2,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type):G__60690__$2);
var G__60690__$4 = (cljs.core.truth_(message)?cljs.core.assoc.call(null,G__60690__$3,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message):G__60690__$3);
if(cljs.core.truth_(problems)){
return cljs.core.assoc.call(null,G__60690__$4,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595),data);
} else {
return G__60690__$4;
}

break;
case "read-eval-result":
case "print-eval-result":
var vec__60691 = cljs.core.first.call(null,trace);
var source__$1 = cljs.core.nth.call(null,vec__60691,(0),null);
var method = cljs.core.nth.call(null,vec__60691,(1),null);
var file = cljs.core.nth.call(null,vec__60691,(2),null);
var line = cljs.core.nth.call(null,vec__60691,(3),null);
var G__60694 = top_data;
var G__60694__$1 = (cljs.core.truth_(line)?cljs.core.assoc.call(null,G__60694,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471),line):G__60694);
var G__60694__$2 = (cljs.core.truth_(file)?cljs.core.assoc.call(null,G__60694__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),file):G__60694__$1);
var G__60694__$3 = (cljs.core.truth_((function (){var and__4120__auto__ = source__$1;
if(cljs.core.truth_(and__4120__auto__)){
return method;
} else {
return and__4120__auto__;
}
})())?cljs.core.assoc.call(null,G__60694__$2,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994),(new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[source__$1,method],null))):G__60694__$2);
var G__60694__$4 = (cljs.core.truth_(type)?cljs.core.assoc.call(null,G__60694__$3,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type):G__60694__$3);
if(cljs.core.truth_(message)){
return cljs.core.assoc.call(null,G__60694__$4,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message);
} else {
return G__60694__$4;
}

break;
case "execution":
var vec__60695 = cljs.core.first.call(null,trace);
var source__$1 = cljs.core.nth.call(null,vec__60695,(0),null);
var method = cljs.core.nth.call(null,vec__60695,(1),null);
var file = cljs.core.nth.call(null,vec__60695,(2),null);
var line = cljs.core.nth.call(null,vec__60695,(3),null);
var file__$1 = cljs.core.first.call(null,cljs.core.remove.call(null,((function (vec__60695,source__$1,method,file,line,G__60686,G__60686__$1,map__60678,map__60678__$1,via,trace,phase,map__60679,map__60679__$1,type,message,data,map__60680,map__60680__$1,problems,fn,caller,map__60681,map__60681__$1,top_data,source){
return (function (p1__60677_SHARP_){
var or__4131__auto__ = (p1__60677_SHARP_ == null);
if(or__4131__auto__){
return or__4131__auto__;
} else {
return new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["NO_SOURCE_PATH",null,"NO_SOURCE_FILE",null], null), null).call(null,p1__60677_SHARP_);
}
});})(vec__60695,source__$1,method,file,line,G__60686,G__60686__$1,map__60678,map__60678__$1,via,trace,phase,map__60679,map__60679__$1,type,message,data,map__60680,map__60680__$1,problems,fn,caller,map__60681,map__60681__$1,top_data,source))
,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"file","file",-1269645878).cljs$core$IFn$_invoke$arity$1(caller),file], null)));
var err_line = (function (){var or__4131__auto__ = new cljs.core.Keyword(null,"line","line",212345235).cljs$core$IFn$_invoke$arity$1(caller);
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return line;
}
})();
var G__60698 = new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890),type], null);
var G__60698__$1 = (cljs.core.truth_(err_line)?cljs.core.assoc.call(null,G__60698,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471),err_line):G__60698);
var G__60698__$2 = (cljs.core.truth_(message)?cljs.core.assoc.call(null,G__60698__$1,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742),message):G__60698__$1);
var G__60698__$3 = (cljs.core.truth_((function (){var or__4131__auto__ = fn;
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
var and__4120__auto__ = source__$1;
if(cljs.core.truth_(and__4120__auto__)){
return method;
} else {
return and__4120__auto__;
}
}
})())?cljs.core.assoc.call(null,G__60698__$2,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994),(function (){var or__4131__auto__ = fn;
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return (new cljs.core.PersistentVector(null,2,(5),cljs.core.PersistentVector.EMPTY_NODE,[source__$1,method],null));
}
})()):G__60698__$2);
var G__60698__$4 = (cljs.core.truth_(file__$1)?cljs.core.assoc.call(null,G__60698__$3,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397),file__$1):G__60698__$3);
if(cljs.core.truth_(problems)){
return cljs.core.assoc.call(null,G__60698__$4,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595),data);
} else {
return G__60698__$4;
}

break;
default:
throw (new Error(["No matching clause: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(G__60686__$1)].join('')));

}
})(),new cljs.core.Keyword("clojure.error","phase","clojure.error/phase",275140358),phase);
});
/**
 * Returns a string from exception data, as produced by ex-triage.
 *   The first line summarizes the exception phase and location.
 *   The subsequent lines describe the cause.
 */
cljs.repl.ex_str = (function cljs$repl$ex_str(p__60702){
var map__60703 = p__60702;
var map__60703__$1 = (((((!((map__60703 == null))))?(((((map__60703.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__60703.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__60703):map__60703);
var triage_data = map__60703__$1;
var phase = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","phase","clojure.error/phase",275140358));
var source = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","source","clojure.error/source",-2011936397));
var line = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","line","clojure.error/line",-1816287471));
var column = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","column","clojure.error/column",304721553));
var symbol = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","symbol","clojure.error/symbol",1544821994));
var class$ = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","class","clojure.error/class",278435890));
var cause = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","cause","clojure.error/cause",-1879175742));
var spec = cljs.core.get.call(null,map__60703__$1,new cljs.core.Keyword("clojure.error","spec","clojure.error/spec",2055032595));
var loc = [cljs.core.str.cljs$core$IFn$_invoke$arity$1((function (){var or__4131__auto__ = source;
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return "<cljs repl>";
}
})()),":",cljs.core.str.cljs$core$IFn$_invoke$arity$1((function (){var or__4131__auto__ = line;
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return (1);
}
})()),(cljs.core.truth_(column)?[":",cljs.core.str.cljs$core$IFn$_invoke$arity$1(column)].join(''):"")].join('');
var class_name = cljs.core.name.call(null,(function (){var or__4131__auto__ = class$;
if(cljs.core.truth_(or__4131__auto__)){
return or__4131__auto__;
} else {
return "";
}
})());
var simple_class = class_name;
var cause_type = ((cljs.core.contains_QMARK_.call(null,new cljs.core.PersistentHashSet(null, new cljs.core.PersistentArrayMap(null, 2, ["RuntimeException",null,"Exception",null], null), null),simple_class))?"":[" (",simple_class,")"].join(''));
var format = goog.string.format;
var G__60705 = phase;
var G__60705__$1 = (((G__60705 instanceof cljs.core.Keyword))?G__60705.fqn:null);
switch (G__60705__$1) {
case "read-source":
return format.call(null,"Syntax error reading source at (%s).\n%s\n",loc,cause);

break;
case "macro-syntax-check":
return format.call(null,"Syntax error macroexpanding %sat (%s).\n%s",(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,(cljs.core.truth_(spec)?(function (){var sb__4661__auto__ = (new goog.string.StringBuffer());
var _STAR_print_newline_STAR__orig_val__60706_60715 = cljs.core._STAR_print_newline_STAR_;
var _STAR_print_fn_STAR__orig_val__60707_60716 = cljs.core._STAR_print_fn_STAR_;
var _STAR_print_newline_STAR__temp_val__60708_60717 = true;
var _STAR_print_fn_STAR__temp_val__60709_60718 = ((function (_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (x__4662__auto__){
return sb__4661__auto__.append(x__4662__auto__);
});})(_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
;
cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__temp_val__60708_60717;

cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__temp_val__60709_60718;

try{cljs.spec.alpha.explain_out.call(null,cljs.core.update.call(null,spec,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814),((function (_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,_STAR_print_fn_STAR__temp_val__60709_60718,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (probs){
return cljs.core.map.call(null,((function (_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,_STAR_print_fn_STAR__temp_val__60709_60718,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (p1__60700_SHARP_){
return cljs.core.dissoc.call(null,p1__60700_SHARP_,new cljs.core.Keyword(null,"in","in",-1531184865));
});})(_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,_STAR_print_fn_STAR__temp_val__60709_60718,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
,probs);
});})(_STAR_print_newline_STAR__orig_val__60706_60715,_STAR_print_fn_STAR__orig_val__60707_60716,_STAR_print_newline_STAR__temp_val__60708_60717,_STAR_print_fn_STAR__temp_val__60709_60718,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
)
);
}finally {cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__orig_val__60707_60716;

cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__orig_val__60706_60715;
}
return cljs.core.str.cljs$core$IFn$_invoke$arity$1(sb__4661__auto__);
})():format.call(null,"%s\n",cause)));

break;
case "macroexpansion":
return format.call(null,"Unexpected error%s macroexpanding %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "compile-syntax-check":
return format.call(null,"Syntax error%s compiling %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "compilation":
return format.call(null,"Unexpected error%s compiling %sat (%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);

break;
case "read-eval-result":
return format.call(null,"Error reading eval result%s at %s (%s).\n%s\n",cause_type,symbol,loc,cause);

break;
case "print-eval-result":
return format.call(null,"Error printing return value%s at %s (%s).\n%s\n",cause_type,symbol,loc,cause);

break;
case "execution":
if(cljs.core.truth_(spec)){
return format.call(null,"Execution error - invalid arguments to %s at (%s).\n%s",symbol,loc,(function (){var sb__4661__auto__ = (new goog.string.StringBuffer());
var _STAR_print_newline_STAR__orig_val__60710_60719 = cljs.core._STAR_print_newline_STAR_;
var _STAR_print_fn_STAR__orig_val__60711_60720 = cljs.core._STAR_print_fn_STAR_;
var _STAR_print_newline_STAR__temp_val__60712_60721 = true;
var _STAR_print_fn_STAR__temp_val__60713_60722 = ((function (_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (x__4662__auto__){
return sb__4661__auto__.append(x__4662__auto__);
});})(_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
;
cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__temp_val__60712_60721;

cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__temp_val__60713_60722;

try{cljs.spec.alpha.explain_out.call(null,cljs.core.update.call(null,spec,new cljs.core.Keyword("cljs.spec.alpha","problems","cljs.spec.alpha/problems",447400814),((function (_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,_STAR_print_fn_STAR__temp_val__60713_60722,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (probs){
return cljs.core.map.call(null,((function (_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,_STAR_print_fn_STAR__temp_val__60713_60722,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec){
return (function (p1__60701_SHARP_){
return cljs.core.dissoc.call(null,p1__60701_SHARP_,new cljs.core.Keyword(null,"in","in",-1531184865));
});})(_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,_STAR_print_fn_STAR__temp_val__60713_60722,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
,probs);
});})(_STAR_print_newline_STAR__orig_val__60710_60719,_STAR_print_fn_STAR__orig_val__60711_60720,_STAR_print_newline_STAR__temp_val__60712_60721,_STAR_print_fn_STAR__temp_val__60713_60722,sb__4661__auto__,G__60705,G__60705__$1,loc,class_name,simple_class,cause_type,format,map__60703,map__60703__$1,triage_data,phase,source,line,column,symbol,class$,cause,spec))
)
);
}finally {cljs.core._STAR_print_fn_STAR_ = _STAR_print_fn_STAR__orig_val__60711_60720;

cljs.core._STAR_print_newline_STAR_ = _STAR_print_newline_STAR__orig_val__60710_60719;
}
return cljs.core.str.cljs$core$IFn$_invoke$arity$1(sb__4661__auto__);
})());
} else {
return format.call(null,"Execution error%s at %s(%s).\n%s\n",cause_type,(cljs.core.truth_(symbol)?[cljs.core.str.cljs$core$IFn$_invoke$arity$1(symbol)," "].join(''):""),loc,cause);
}

break;
default:
throw (new Error(["No matching clause: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(G__60705__$1)].join('')));

}
});
cljs.repl.error__GT_str = (function cljs$repl$error__GT_str(error){
return cljs.repl.ex_str.call(null,cljs.repl.ex_triage.call(null,cljs.repl.Error__GT_map.call(null,error)));
});

//# sourceMappingURL=repl.js.map
