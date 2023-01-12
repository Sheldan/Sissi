{
    <#assign userMentions><#list participants as user>${user.memberMention}<#sep>, </#list></#assign>
    "additionalMessage": "${userMentions?json_string}
${notificationMessage}"
<#if meetupMessageId??>,
    "referencedMessageId": ${meetupMessageId?c}
</#if>
}