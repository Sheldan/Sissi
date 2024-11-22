<#include "format_instant">
{
    "embeds": [
        {
            <#include "abstracto_color">,
            <#assign topicText=meetupTopic>
            <#assign oldTime><@format_instant_long_date_time instant=oldDate/></#assign>
            <#assign newTime><@format_instant_long_date_time instant=newDate/></#assign>
            <#assign messageLink=meetupMessage.jumpUrl>
            "description": "<@safe_include "changeMeetupTime_notification_text"/>"
        }
    ]
}