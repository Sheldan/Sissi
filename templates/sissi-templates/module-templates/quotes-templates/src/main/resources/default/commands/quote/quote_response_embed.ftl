<#include "format_instant">
{
    "components": [
        {
            <#assign userFound=authorUserDisplay?has_content>
            <#assign authorName><#if authorMemberDisplay?has_content>${authorMemberDisplay.name}<#elseif authorUserDisplay?has_content>${authorUserDisplay.name}<#else><@safe_include "quote_response_default_author_name"/></#if></#assign>
            <#assign adderUserName><#if adderMemberDisplay?has_content>${adderMemberDisplay.name}<#elseif adderUserDisplay?has_content>${adderUserDisplay.name}<#else><@safe_include "quote_response_default_adder_name"/></#if></#assign>
            <#assign channelName><@default_template_if_null sourceChannelName "quote_response_default_channel_name"/></#assign>
            <#assign creationDate><@format_instant_date_time instant=creationDate/></#assign>
            <#if userFound>
                <#assign authorAvatar><#if userFound>${authorUserDisplay.avatarUrl}</#if></#assign>
                "type": "section",
                "components": [
                    {
                        "type": "textDisplay",
                        "content": "<@safe_include "quote_response_header_author_name"/>"
                    }
                ],
                "accessory": {
                    "type": "thumbnail",
                    "url": "${authorAvatar}"
                }
            <#else>
                "type": "textDisplay",
                "content": "<@safe_include "quote_response_header_author_name"/>"
            </#if>

        },
        {
            "type": "section",
            "components": [
                {
                    <#assign quoteId=quoteId>
                    "type": "textDisplay",
                    "content": "<@safe_include "quote_response_title"/>"
                }
            ],
            <#assign quoteJumpUrl=quotedMessage.jumpUrl>
            "accessory": {
                "type": "button",
                "label": "<@safe_include "quote_response_jump_label"/>",
                "url": "${quoteJumpUrl}",
                "buttonStyle": "link"
            }
        },
        {
            "type": "container",
            "components": [
            <#assign hasContent=false>
            <#if quoteContent?has_content>
                <#assign hasContent=true>
                {
                    "type": "textDisplay",
                    <#assign quoteDescription=quoteContent?json_string>
                    "content": "${quoteDescription}"
                }
            </#if>
            <#if mediaAttachmentURLs?size gt 0>
                <#assign hasContent=true>
                ,{
                    "type": "mediaGallery",
                    "images": [
                <#list mediaAttachmentURLs as image>
                        {
                            "url": "${image}"
                        }<#sep>,</#list>
                    ]
                }
            </#if>
            <#if hasContent==false>
                {
                    "type": "textDisplay",
                    "content": "<@safe_include "quote_response_no_content"/>"
                }
            </#if>
            ]
        }
    ],
    "messageConfig": {
        "allowsUserMention": false,
        "useComponentsV2": true
    }
}