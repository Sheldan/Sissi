<#include "format_instant">
{
    "embeds": [
        {
            <#include "abstracto_color">,
            "title": {
                "title": "${topic?json_string}"
            },
            "description": "<@format_instant_long_date_time instant=meetupTime/>
${description?json_string}"
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