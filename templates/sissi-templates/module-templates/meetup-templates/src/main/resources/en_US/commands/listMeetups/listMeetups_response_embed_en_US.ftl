<#include "format_instant">
{
    "embeds": [
        {
            <#include "abstracto_color">,
            "description": "<#list meetups as meetup><#assign meetup=meetup><#assign topic=meetup.topic><#assign time><@format_instant_long_date_time instant=meetup.meetupTime/></#assign><#assign timeRelative><@format_instant_relative instant=meetup.meetupTime/></#assign><#assign link=meetup.meetupMessage.jumpUrl><#include "meetup_list_meetup_display">
<#else><#include "meetup_list_no_meetups"></#list>"
        }
    ]
}