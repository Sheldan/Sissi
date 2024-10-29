{
    "embeds": [
    {
        "description": "<#list row as item><#assign item=item><@safe_include "showWeeklyTexts_item_entry"/>\n</#list>"
    }
    ],
    "buttons": [
        <#include "paginator_buttons">
    ]
}