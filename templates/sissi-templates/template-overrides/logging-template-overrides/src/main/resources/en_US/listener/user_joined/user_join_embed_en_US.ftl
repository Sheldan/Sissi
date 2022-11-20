{
        <#assign userMention>${member.asMention}</#assign>
        <#assign user>${member.user.name}#${member.user.discriminator} (${member.asMention}: ${member.id})</#assign>
        <#setting locale="de_DE">
        "additionalMessage": "<@safe_include "user_joined_text"/>"
}