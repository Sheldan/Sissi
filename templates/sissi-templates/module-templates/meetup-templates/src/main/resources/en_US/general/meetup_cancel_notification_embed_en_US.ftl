<#include "format_instant">
{
    "embeds": [
        {
        <#include "abstracto_color">,
            <#assign time><@format_instant_date_time instant=meetupTime/></#assign>
            <#assign topicText>${topic?json_string}</#assign>
            "description": "<#include "meetup_cancel_notification_description">"
        }
    ]
}