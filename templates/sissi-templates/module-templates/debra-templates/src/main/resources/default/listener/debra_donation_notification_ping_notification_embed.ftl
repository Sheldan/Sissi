{
    <#assign userPing="<@186558865851154435>">
    "additionalMessage": "${userPing}",
    "embeds": [
        {
            "title": {
                <#assign donatorName><#if donation.anonymous><#include "donations_response_anonymous"><#else>${donation.donatorName}</#if></#assign>
                <#assign donationAmount=donation.amount>
                "title": "<@safe_include "debra_donation_notification_embed_title"/>"
            },
            <#if donation.message != 'gespendet'>
                <#assign donationMessage=donation.message>
                "description": "${donationMessage?json_string}",
            </#if>
            "fields": [
                {
                    <#assign totalDonationAmount=totalDonationAmount>
                    "name": "<@safe_include "debra_donation_notification_embed_field_amount_title"/>",
                    "value": "<@safe_include "debra_donation_notification_embed_field_amount_value"/>"
                }
            ],
            "imageUrl": "https://cdn.discordapp.com/attachments/299115929206390784/1047306670319079474/dotpict-1.png",
            "footer": {
                "text": "<@safe_include "debra_donation_notification_embed_footer"/>"
            }
        }
    ],
    "buttons": [
        {
            "label": "<@safe_include "debra_donation_notification_link_button_label"/>",
            "url": "https://tinyurl.com/debra25",
            "buttonStyle": "link",
            "metaConfig": {
                "persistCallback": false
            }
        }
    ]
}