{
    <#include "user_detail">
    <#assign user><@user_detail user=user/></#assign>
    <#setting locale="de_DE">
    "additionalMessage": "<@safe_include "user_left_text"/>"
}