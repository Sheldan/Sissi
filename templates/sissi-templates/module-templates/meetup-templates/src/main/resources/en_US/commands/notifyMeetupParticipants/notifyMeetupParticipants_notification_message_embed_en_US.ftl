{
    <#assign userMentions><#list participants as user>${user.memberMention}<#sep>, </#list></#assign>
    "additionalMessage": "${userMentions?json_string}
${notificationMessage}"
}