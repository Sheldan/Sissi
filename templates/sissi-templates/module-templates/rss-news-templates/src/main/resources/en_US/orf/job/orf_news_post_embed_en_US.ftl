{
    "embeds": [
        {
            <#include "abstracto_color">,
            "author": {
                "name": "<@safe_include "orf_news_post_author"/>"
            },
            "title": {
                "title": "${title?json_string}",
                "url": "${url}"
            },
            "description": "${url?json_string}<#if description?has_content>\n${description?json_string}</#if>",
            "footer": {
                "text": "${category}"
            }
            <#if imageURL?has_content>
                ,"imageUrl": "${imageURL}"
            </#if>
        }
    ]
}