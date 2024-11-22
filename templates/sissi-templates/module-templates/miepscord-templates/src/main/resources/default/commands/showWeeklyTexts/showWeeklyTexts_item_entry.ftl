<#include "format_instant">
<#assign done=item.done>
<#assign text=item.text>
<#assign id=item.id>
<#assign created><@format_instant_long_date_time instant=item.created/></#assign>
<@safe_include "showWeeklyTexts_response_text"/>