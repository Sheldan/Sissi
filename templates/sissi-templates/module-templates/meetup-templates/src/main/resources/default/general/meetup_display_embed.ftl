<#include "format_instant">
{
    "components": [
        {
            "type": "textDisplay",
            <#assign roleMention="<@&371419588619141121>"/>
            "content": "<#if cancelled>~~</#if>${roleMention?json_string} ${topic?json_string} - <@safe_include "meetup_message_id_display"/><#if cancelled>~~</#if>"
        },
        <#if description?has_content>
        {
            <#assign descriptionText>${description?json_string}</#assign>
            <#assign organizerText>${organizer.memberMention}</#assign>
            "type": "textDisplay",
            "content": "<#if cancelled>~~</#if><@safe_include "meetup_description_component"/><#if cancelled>~~</#if>"
        },
        </#if>
        {
            <#assign time><@format_instant_long_date_time instant=meetupTime/></#assign>
            <#assign timeRelative><@format_instant_relative instant=meetupTime/></#assign>

            <#if location?? && location != "%22%22">
            "type": "section",
            "components": [
                {
                    "type": "textDisplay",
                    "content": "<@safe_include "meetup_display_time_component"/>"
                }
            ],
            "accessory": {
                "type": "button",
                "label": "<@safe_include "meetup_message_location_button_label"/>",
                "url": "https://www.google.com/maps?q=${location?json_string}",
                "buttonStyle": "link"
            }
            <#else>
                "type": "textDisplay",
                "content": "<@safe_include "meetup_display_time_component"/>"
            </#if>
        }
        <#macro decision_component button_id button_style label_template content_template user>
            {
                "type": "section",
                "components": [
                    {
                        "type": "textDisplay",
                        "content": "<@safe_include content_template/>"
                    }
                ]
                ,"accessory": {
                    "type": "button",
                    "id": "${button_id}",
                    "label": "<@safe_include label_template/>",
                    "buttonStyle": "${button_style}"
                }
            }
        </#macro>
        <#assign participantsText> (${participants?size}) <#list participants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
        <#assign maybeParticipantsText> (${maybeParticipants?size}) <#list maybeParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
        <#assign noTimeParticipantsText> (${noTimeParticipants?size}) <#list noTimeParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
        <#assign declinedParticipantsText> (${declinedParticipants?size}) <#list declinedParticipants as member>${member.memberMention}<#sep>, </#sep><#else><#include "meetup_message_no_member"></#list></#assign>
        ,<@decision_component yesId "success" "meetup_message_yes_button_label" "meetup_user_display_participants" participantsText/>
        ,<@decision_component maybeId "secondary" "meetup_message_maybe_button_label" "meetup_user_display_maybe_participants" maybeParticipantsText/>
        ,<@decision_component noId "danger" "meetup_message_no_button_label" "meetup_user_display_declined_participants" declinedParticipantsText/>
        ,<@decision_component noTimeId "danger" "meetup_message_no_time_button_label" "meetup_user_display_no_time_participants" noTimeParticipantsText/>
        <#if meetupIcsModel.attachIcsFile>
            ,{
                "type": "fileDisplay",
                "fileName": "<@safe_include "meetup_ics_file_name"/>.ics",
                "fileContent": "<@safe_include "meetup_ice_file_download"/>"
            }
        </#if>
    ],
    "messageConfig": {
        "allowsRoleMention": true,
        "allowsUserMention": false,
        "useComponentsV2": true
    }
}