<#include "format_instant">
{
    <#assign roleMention="<@&371419588619141121>"/>
    "additionalMessage": "${roleMention?json_string}",
    "embeds": [
        {
            <#include "abstracto_color">,
            "title": {
                "title": "${topic?json_string} - <@safe_include "meetup_message_id_display"/>"
            },
            <#assign time><@format_instant_long_date_time instant=meetupTime/></#assign>
            <#assign timeRelative><@format_instant_relative instant=meetupTime/></#assign>
            <#assign organizerText>${organizer.memberMention}</#assign>
            <#assign meetupId=meetupId/>
            <#assign descriptionText>${description?json_string}</#assign>
            <#assign participantsText> (${participants?size}) <#list participants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
            <#assign maybeParticipantsText> (${maybeParticipants?size}) <#list maybeParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
            <#assign noTimeParticipantsText> (${noTimeParticipants?size}) <#list noTimeParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
            <#assign declinedParticipantsText> (${declinedParticipants?size}) <#list declinedParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
            "description": "<#if cancelled>~~</#if><@safe_include "meetup_display_description"/><#if cancelled>~~</#if>"
        }
    ],
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
            "label": "<@safe_include "meetup_message_no_time_button_label"/>",
            "id": "${noTimeId}",
            "buttonStyle": "danger"
        },
        {
            "label": "<@safe_include "meetup_message_no_button_label"/>",
            "id": "${noId}",
            "buttonStyle": "danger"
        }
        <#if location?? && location != "%22%22">,
        {
            "label": "<@safe_include "meetup_message_location_button_label"/>",
            "url": "https://www.google.com/maps?q=${location?json_string}",
            "buttonStyle": "link"
        }
        </#if>
    ],
    <#if meetupIcsModel.attachIcsFile>
    "files": [
        {
            "fileName": "<@safe_include "meetup_ics_file_name"/>.ics",
            "fileContent": "<@safe_include "meetup_ice_file_download"/>"
        }
    ],
    </#if>
    "messageConfig": {
        "allowsRoleMention": true
    }
}