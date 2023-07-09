BEGIN:VCALENDAR
VERSION:2.0
PRODID:Sissi-Bot
BEGIN:VEVENT
UID:sissi-meetup-${meetupId}
DTSTAMP:${meetupIcsModel.icsFormattedCreationTime}
DTSTART:${meetupIcsModel.icsFormattedStartTime}
DTEND:${meetupIcsModel.icsFormattedEndTime}
SUMMARY:${topic}
<#if description?has_content>DESCRIPTION:${description}</#if>
<#if decodedLocation?? && decodedLocation != "">LOCATION:${decodedLocation}</#if>
END:VEVENT
END:VCALENDAR