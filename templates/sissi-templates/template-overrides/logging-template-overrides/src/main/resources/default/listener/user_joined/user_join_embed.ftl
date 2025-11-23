{
        <#assign userMention>${member.asMention}</#assign>
        <#assign userText>${member.user.name} (${member.asMention}: ${member.id?c})</#assign>
        "additionalMessage": "<@safe_include "user_joined_text"/>"
}