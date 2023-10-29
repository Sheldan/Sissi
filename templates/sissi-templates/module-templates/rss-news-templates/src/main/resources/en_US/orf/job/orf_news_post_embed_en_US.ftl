{
    "embeds": [
        {
            <#include "abstracto_color">,
            "author": {
                "name": "<@safe_include "orf_news_post_author"/>"
            },
            "title": {
                "title": "${title}",
                "url": "${url}"
            },
            "description": "${url}<#if description?has_content>\n-${description}</#if>",
            "footer": {
                "text": "${category}"
            }
            <#if imageURL?has_content>
                ,"imageUrl": "${imageURL}"
            </#if>
        }
    ]
}