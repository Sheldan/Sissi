<#include "format_instant">
{
    "components": [
<#list meetups as meetup>
    <#assign meetup=meetup>
    <#assign topic=meetup.topic>
    <#assign time><@format_instant_long_date_time instant=meetup.meetupTime/>
    </#assign><#assign timeRelative><@format_instant_relative instant=meetup.meetupTime/></#assign>
    <#assign link=meetup.meetupMessage.jumpUrl>
        {
            "type": "section",
            "components": [
                {
                    "type": "textDisplay",
                    "content": "<#include "meetup_list_meetup_display">"
                }
            ]
            ,"accessory": {
                "type": "button",
                "label": "<#include "meetup_list_jump_button_label"/>",
                "url": "${link}",
                "buttonStyle": "link"
            }
        }
    <#sep>,</#sep>
<#else>
    {
        "type": "textDisplay",
        "content": "<#include "meetup_list_no_meetups">"
    }
</#list>
    ],
    "messageConfig": {
        "useComponentsV2": true
    }
}