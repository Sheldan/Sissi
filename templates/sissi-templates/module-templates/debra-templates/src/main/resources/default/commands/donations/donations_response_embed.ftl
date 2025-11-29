{
    <#assign donationAmount=totalAmount>
    "additionalMessage": "<#include "donations_response_description">",
    "embeds": [
        {
            "imageUrl": "https://cdn.discordapp.com/attachments/299115929206390784/1047306670319079474/dotpict-1.png"
            <#if donations?size gt 0>
                ,<#if type.name() == "LATEST">
                    "description": "<#include "donations_response_latest_donations_description">"
                <#else>
                    "description": "<#include "donations_response_top_donations_description">"
                </#if>
                ,"fields": [
                <#list donations as donation>
                    {
                        "name": "<#if donation.anonymous><#include "donations_response_anonymous"><#else>${donation.name}</#if>",
                        "value": "${donation.donationAmount}€",
                        "inline": true
                    }
                <#sep>,</#list>
                ]
            </#if>
        }
    ]
}