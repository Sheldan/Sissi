{
<#assign mapping={
'bp': '340380695585095680',
'np': '400642855682572288',
'bk': '299162473775366144',
'mi': '400642854248120330',
'na': '400642590267146240',
'br': '400642587620278273',
'lo': '400643490020851712',
'lr': '400642584617418753',
'lm': '400642579881787402',
'bo': '400642539889229825',
'bm': '299162333668835328',
'gm': '400642460713484308',
'up': '299162129284726784',
'ap': '336786889589915659',
'dd': '336786578103992323',
'dr': '298790326943088670',
'ms': '298790213453479937',
'bs': '298790020192403456',
'di': '298790014974820356',
'in': '298789520084566018'
}>
<#assign groups={
'1': {'bp': 1, 'np': 2, 'bk': 3, 'mi': 4, 'na': 5, 'br': 6, 'lo': 7, 'lr': 8, 'lm': 9, 'bo': 10, 'bm': 11, 'gm': 12},
'2': {'up': 1, 'ap': 2},
'3': {'dd': 1, 'dr': 2},
'4': {'ms': 1, 'bs': 2},
'5': {'di': 1},
'6': {'in': 1}
}>
<#assign allowedRoleIds=[]>
<#assign reverseMapping={}>
<#list mapping as key, value>
    <#assign allowedRoleIds=allowedRoleIds + [value]>
    <#assign reverseMapping=reverseMapping + {value: key}>
</#list>
<#assign relevantRoles={}>
<#assign relevantRoleIds=[]>
<#list roles as role>
    <#if allowedRoleIds?seq_contains(role.roleId?c)>
        <#assign relevantRoles=relevantRoles + {role.roleId?c:role.roleName}>
        <#assign relevantRoleIds=relevantRoleIds+[role.roleId?c]>
    </#if>
</#list>
<#function is_printed role_id>
    <#assign group={}>
    <#assign found_prio=1>
    <#list groups as key,members>
        <#list members as roleshortcut,prio>
            <#if role_id=mapping[roleshortcut]>
                <#assign group=members>
                <#assign found_prio=prio>
            </#if>
        </#list>
    </#list>
    <#assign print=true>
    <#list group as shortcut,prio>
        <#if relevantRoleIds?seq_contains(mapping[shortcut])>
            <#if prio < found_prio>
                <#assign print=false>
            </#if>
        </#if>
    </#list>
    <#return print>
</#function>
<#macro get_title key>
    <#assign role_id=mapping[key]><#rt>
    <#if is_printed(role_id) && relevantRoleIds?seq_contains(role_id)><#rt>
        ${relevantRoles[role_id]}<#rt>
    </#if>
</#macro>
<#include "user_detail">
<#assign userText><@get_title 'bp'/><@get_title 'np'/><@get_title 'bk'/><@get_title 'mi'/><@get_title 'na'/><@get_title 'br'/><@get_title 'lo'/><@get_title 'lr'/><@get_title 'lm'/><@get_title 'bo'/><@get_title 'bm'/><@get_title 'gm'/><@get_title 'up'/><@get_title 'ap'/><@get_title 'dd'/><@get_title 'dr'/><@get_title 'di'/> ${user.name} <@get_title 'ms'/><@get_title 'bs'/><@get_title 'in'/> (${user.id?c})</#assign>
    "additionalMessage": "<#compress><@safe_include "user_left_text"/></#compress>"
}