<#include "format_instant">
{
    "embeds": [
        {
            <#include "abstracto_color">,
            "title": {
                "title": "${topic?json_string}"
            },
            "description": "<@format_instant_date_time instant=meetupTime/>
${description?json_string}"
            <#if location?? && location != "%22%22">,
            "fields": [
                {
                    "name": "<@safe_include "createMeetup_confirmation_location_field_title"/>",
                    "value": "https://www.google.com/maps?q=${location?json_string}"
                }
            ]
            </#if>
        }
    ],
    "buttons": [
        {
            "label": "<@safe_include "createMeetup_confirm_button_label"/>",
            "id": "${confirmationId}",
            "buttonStyle": "success"
        },
        {
            "label": "<@safe_include "createMeetup_cancel_button_label"/>",
            "id": "${cancelId}",
            "buttonStyle": "danger"
        }
    ]
}