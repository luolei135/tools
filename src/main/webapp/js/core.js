Watcher.prototype = {
    update: function(){
        this.get();
        this.node.nodeValue = this.value;
    },
    get: function(){
        this.value=this.vm[this.name];
    }
}
Dep.prototype = {
    addSub: function(sub){
        this.subs.push(sub);
    },
    notify:function(){
        this.subs.forEach((function(sub){
            sub.update();
        }))
    }
}
function nodeToFragment(node,vm){
    var flag = document.createDocumentFragment();
    var child;
    while(child = node.firstChild){
        compile(child,vm);
        flag.appendChild(child);
    }
    return flag;
}

function compile(node,vm){
    var reg=/\{\{(.*)\}\}/;
    if(node.nodeType===1){
        var attr = node.attributes;
        for(var i=0;i<attr.length;i++){
            if(attr[i].nodeName=='v-model'){
                var name=attr[i].nodeValue;

                node.addEventListener('input',function(e){
                    vm[name]=e.target.value;
                });

                node.value=vm[name];
                node.removeAttribute('v-model');
            }
        }
    }else if(node.nodeType===3){
        if(reg.test(node.nodeValue)){
            var name = RegExp.$1;
            name = name.trim();
            node.nodeValue=vm[name];

            new Watcher(vm,node,name);
        }
    }

    if(node.childNodes){
        for(var i=0;i<node.childNodes.length;i++){
            compile(node.childNodes[i],vm);
        }
    }
}

function defineReactive(obj,key,val){

    var dep = new Dep();

    Object.defineProperty(obj,key,{
        get: function(){

            if(Dep.target) dep.addSub(Dep.target);
            return val;
        },
        set:function(newVal){
            if(newVal===val) return;
            val = newVal;
            dep.notify();
            console.log(val);
        }
    })
}

function observe(obj,vm){
    Object.keys(obj).forEach(function(key){
        defineReactive(vm,key,obj[key]);
    })
}

function Dep(){
    this.subs=[];
}

function Watcher(vm,node,name){
    Dep.target=this;
    this.name=name;
    this.node=node;
    this.vm=vm;
    this.update();
    Dep.target=null;
}

function Vue(options){
    this.data=options.data;

    var data = this.data;

    observe(data,this);

    var id = options.el;
    var dom=nodeToFragment(document.getElementById(id),this);
    document.getElementById(id).appendChild(dom);
}