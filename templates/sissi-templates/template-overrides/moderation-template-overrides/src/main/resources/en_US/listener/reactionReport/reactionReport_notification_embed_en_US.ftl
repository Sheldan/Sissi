{
    "embeds": [
        {
            <#include "abstracto_color">,
            <#assign messageLink=reportedMessage.messageUrl>
            <#assign userMention>${reportedMessage.author.asMention}</#assign>
            <#assign messageContent><#if reportedMessage.content?has_content>`${reportedMessage.content?json_string}`<#else><@safe_include "reactionReport_no_content"/></#if></#assign>
            "description" : "<#if context?has_content><@safe_include "reactionReport_additional_context_label"/>: `${context?json_string}`</#if>
<@safe_include "reactionReport_notification_embed_description"/>",
            "footer": {
                "text": "<@safe_include "reaction_report_custom_text"/>"
            }
            <#if reportedMessage.attachments?size gt 0>
                ,"imageUrl": "${reportedMessage.attachments[0].proxyUrl}"
            <#elseif reportedMessage.attachments?size = 0 && reportedMessage.embeds?size gt 0 && reportedMessage.embeds[0].cachedThumbnail??>
                ,"imageUrl": "${reportedMessage.embeds[0].cachedThumbnail.proxyUrl}"
            <#elseif reportedMessage.attachments?size = 0 && reportedMessage.embeds?size gt 0 && reportedMessage.embeds[0].cachedImageInfo??>
                ,"imageUrl": "${reportedMessage.embeds[0].cachedImageInfo.proxyUrl}"
            </#if>
<#if singularMessage>
            ,"fields": [
                {
                    "name": "<@safe_include "reactionReport_notification_embed_report_counter_field_title"/>",
                    "value": "${reportCount}"
                }
            ]
</#if>
        }
    ],
    "buttons": [
        {
            "label": "<@safe_include "reactionReport_jump_button_label"/>",
            "url": "${messageLink?json_string}",
            "buttonStyle": "link",
            "metaConfig": {
                "persistCallback": false
            }
        }
        <#if moderationActionComponents?size gt 0>,</#if>
        <#list moderationActionComponents as moderactionAction>
        {
            "label": "<@safe_include "moderation_action_${moderactionAction.action}_button_label"/>",
            "id": "${moderactionAction.componentId}",
            "buttonStyle": "danger",
            "metaConfig": {
                "persistCallback": false
            }
        }<#sep>,</#list>
    ]
}