var jQuery = {};
jQuery.extend = function(fun){
  for(var key in fun){
    jQuery[key]=fun[key];
  }
}
var window={innerWidth:1293,innerHeight:701}