{
    <#assign userMentions><#list participants as user>${user.memberMention}<#sep>, </#list></#assign>
    "additionalMessage": "${userMentions?json_string}
${notificationMessage?json_string}"
<#if meetupMessageId??>,
    "referencedMessageId": ${meetupMessageId?c}
</#if>
}