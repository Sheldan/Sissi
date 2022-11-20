{
    <#assign userName>${user.name}#${user.discriminator} (${user.id})</#assign>
    <#setting locale="de_DE">
    "additionalMessage": "<@safe_include "user_left_text"/>"
}