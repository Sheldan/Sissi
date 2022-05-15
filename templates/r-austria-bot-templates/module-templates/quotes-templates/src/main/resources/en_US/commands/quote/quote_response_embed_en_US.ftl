{
    "embeds": [
        {
            <#include "abstracto_color">,
            "author": {
                <#assign authorName><@default_template_if_null authorName "quote_response_default_author_name"/></#assign>
                <#assign channelName><@default_template_if_null sourceChannelName "quote_response_default_channel_name"/></#assign>
                "name": "<@safe_include "quote_response_header_author_name"/>"
                <#if authorAvatarURL??>,"avatar": "${authorAvatarURL}"</#if>
            },
            <#assign quoteId=quoteId>
            <#assign quoteDescription=quoteContent>
            <#assign quoteJumpUrl=quotedMessage.jumpUrl>
            "description": "<@safe_include "quote_response_description"/>",
            "footer": {
                <#assign adderUserName><@default_template_if_null adderName "quote_response_default_adder_name"/></#assign>
                "text": "<@safe_include "quote_response_footer_adder_name" />"
                <#if adderAvatarURL??>,"icon": "${adderAvatarURL}"</#if>
            },
            <#if imageAttachmentURLs?size = 1>
                "imageUrl": "${imageAttachmentURLs[0]}",
            </#if>
            "timeStamp": "${creationDate}"
        }
    ]
}