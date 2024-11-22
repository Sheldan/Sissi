{
    <#include "user_detail">
    <#assign user><@user_detail user=user/></#assign>
    "additionalMessage": "<@safe_include "user_left_text"/>"
}