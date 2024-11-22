{
    "embeds": [
        {
        <#include "abstracto_color">,
        <#macro categoryDisplay category>
            <#assign categoryName=category.name>
            <@safe_include "showNewsCategories_category_display"/>

            <#list category.mappings as mapping>
                <#assign channelDisplay = mapping.channel>
                <#assign statusIndicator>${mapping.enabled?string('✅', '❌')}</#assign>
                <@safe_include "showNewsCategories_mapping_display"/>

            <#else> <@safe_include "showNewsCategories_no_mappings"/>
            </#list>
            <#list category.subscriptions as subscription>
                <#assign feedName = subscription.newsFeedName>
                <#assign categories>`${subscription.newsFeedCategories?join("`, `")}`</#assign>
                <@safe_include "showNewsCategories_subscription_display"/>

            <#else> <@safe_include "showNewsCategories_no_subscriptions"/>
            </#list>
        </#macro>
        "description": "<#list newsCategories as category><@categoryDisplay category=category/><#else><@safe_include "showNewsCategories_no_categories"/></#list>"
        }
    ]
}