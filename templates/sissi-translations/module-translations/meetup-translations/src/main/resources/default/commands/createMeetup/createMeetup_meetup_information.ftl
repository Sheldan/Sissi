${topic?json_string} - <@format_instant_long_date_time instant=meetupTime/>

${description?json_string}

<#if hasLocation>https://www.google.com/maps?q=${location?json_string}</#if>