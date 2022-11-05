{
    "embeds": [
        {
            "title": {
                <#setting locale="de_DE">
                <#assign donatorName=donation.donatorName>
                <#assign donationAmount=donation.amount>
                "title": "<@safe_include "debra_donation_notification_embed_title"/>"
            },
            <#assign donationMessage=donation.message>
            "description": "${donationMessage?json_string}",
            "fields": [
                {
                    <#assign totalDonationAmount=totalDonationAmount>
                    "name": "<@safe_include "debra_donation_notification_embed_field_amount_title"/>",
                    "value": "<@safe_include "debra_donation_notification_embed_field_amount_value"/>"
                }
            ],
            "footer": {
                "text": "<@safe_include "debra_donation_notification_embed_footer"/>"
            }
        }
    ]
}