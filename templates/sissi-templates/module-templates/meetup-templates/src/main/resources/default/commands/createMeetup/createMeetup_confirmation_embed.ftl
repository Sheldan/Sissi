<#include "format_instant">
{
    "components": [
        {
            <#assign hasLocation=location?has_content>
            "type": "textDisplay",
            "content": "<#include "createMeetup_meetup_information">"
        },
        {
            "type": "actionRow",
            "actionRowItems": [
                {
                    "label": "<@safe_include "createMeetup_confirm_button_label"/>",
                    "id": "${confirmationId}",
                    "buttonStyle": "success",
                    "type": "button"
                },
                {
                    "label": "<@safe_include "createMeetup_cancel_button_label"/>",
                    "id": "${cancelId}",
                    "buttonStyle": "danger",
                    "type": "button"
                }
            ]
        }
    ],
    "messageConfig": {
        "useComponentsV2": true
    }
}