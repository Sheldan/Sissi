{
    <#include "user_detail">
    <#assign userText>${user.name} (${user.userMention}: ${user.id})</#assign>
    "additionalMessage": "<@safe_include "user_left_text"/>"
}