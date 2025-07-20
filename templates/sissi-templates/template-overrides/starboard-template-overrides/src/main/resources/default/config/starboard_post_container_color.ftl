"color" : {
<#if starCount gte 15>
    "r": 192,
    "g": 31,
    "b": 1
<#elseif starCount gte 10>
    "r": 67,
    "g": 150,
    "b": 154
<#elseif starCount gte 5>
    "r": 212,
    "g": 175,
    "b": 55
<#elseif starCount gte 3>
    "r": 49,
    "g": 55,
    "b": 61
<#else>
    "r": 0,
    "g": 0,
    "b": 0
</#if>
},
