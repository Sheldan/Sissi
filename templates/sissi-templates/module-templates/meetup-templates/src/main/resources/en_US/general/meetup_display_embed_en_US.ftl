<#include "format_instant">
{
    <#assign roleMention="<@&371419588619141121>"/>
    "additionalMessage": "${roleMention?json_string}",
    "embeds": [
        {
            <#include "abstracto_color">,
            "title": {
                "title": "${topic?json_string}"
            },
            <#assign time><@format_instant_date_time instant=meetupTime/></#assign>
            <#assign timeRelative><@format_instant_relative instant=meetupTime/></#assign>
            <#assign descriptionText>${description?json_string}</#assign>
            <#assign participantsText><#list participants as member>${member.memberMention}<#else><#include "meetup_message_no_member"></#list></#assign>
            <#assign maybeParticipantsText><#list maybeParticipants as member>${member.memberMention}<#else><#include "meetup_message_no_member"></#list></#assign>
            <#assign declinedParticipantsText><#list declinedParticipants as member>${member.memberMention}<#else><#include "meetup_message_no_member"></#list></#assign>
            "description": "<#if cancelled>~~</#if><#include "meetup_display_description"><#if cancelled>~~</#if>"
        }
    ],
    <#if yesId?has_content && noId?has_content && maybeId?has_content && !cancelled>
    "buttons": [
        {
            "label": "<@safe_include "meetup_message_yes_button_label"/>",
            "id": "${yesId}",
            "buttonStyle": "success"
        },
        {
            "label": "<@safe_include "meetup_message_maybe_button_label"/>",
            "id": "${maybeId}",
            "buttonStyle": "secondary"
        },
        {
            "label": "<@safe_include "meetup_message_no_button_label"/>",
            "id": "${noId}",
            "buttonStyle": "danger"
        },
        {
            "label": "<@safe_include "meetup_message_cancel_button_label"/>",
            "id": "${cancelId}",
            "buttonStyle": "danger"
        }
    ],
    </#if>
    "messageConfig": {
        "allowsRoleMention": true
    }
}