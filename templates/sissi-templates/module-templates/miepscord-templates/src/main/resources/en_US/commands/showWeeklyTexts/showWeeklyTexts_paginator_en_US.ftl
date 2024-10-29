{
    <#assign exitOnly=innerModel.items?size lt 11/>
    "embedConfigs": [
        <#assign chunks=innerModel.items?chunk(10)>
        <#list chunks as row><#assign counter=row?index><#assign row=row><#include "showWeeklyTexts_response_item_entry"><#sep>,</#list>
    ],
    "timeoutSeconds": 120,
    "restrictUser": true
}