# Sissi Discord bot privacy policy

Last updated: 23.02.2025

## Description

The bot requires some information to function properly and in a reasonable way. These features heavily depend which features are enabled for which server, and this list just presents ALL of the features available.
The detailed list of what information is stored and processed is the following:

### General
* your Discord user ID (in combination with the server ID) is used to uniquely identify you and associate various properties, such as experience, level, opened modmail threads etc
* the IDs of the servers this bot is in
* the IDs of the channel in the servers this bot is in
* the names of channel groups which were given by their creator
* the server aliases which were created for commands
* the name of emotes which are used in the bot for convenience, if they are customized
* towards which channel (identified by ID) certain messages by the bot are posted. e.g. logging, news, starboard
* **no message content, username, channel name or role name is stored, except at the places where its mentioned**
* most of the stored records have a 'created' and 'updated' timestamp, in order to assist in examining bugs and malfunctions
* which commands have which cooldown in which channel group and in which channel group they are disabled
* which channel is in which channel group
* which role is allowed to execute which command
* which features are enabled
* which feature modes are enabled
* **aliases** created for the commands
* **information** necessary to handle components (buttons, select menus). This information is of varying nature and can be user identifiable information
* **emotes** which should be used for varying places (assignable roles, particular emotes which are overwriten, such as star board)


### Moderation
* **mute reason**, duration, mute date, who muted whom and in which message was the mute executed
* the names of filtered invite link servers in order to find out if it would be valid to allow the invite
* any configured allowed invite links the server ID and the actually used invite
    * this is necessary in order to determine the server via its ID and allow other unknown invite links. The invite link is necessary as there is no way to map server ID to actual server
* configured profanity regexes
    * reported profanities, including which message contains the profanity, and the message which was used to report the profanity, and whether it was identified as a true profanity
* **the text of notes regarding users**
    * this is used to enable taking notes about users, and the content is stored directly
* meta information regarding warnings
    * **reason for the warning**
    * date of the warning
    * the user who warned a user
    * whether the warning was decayed and when
* the infractions of each user accompanied with **reason** (if available): warns, mutes, bans, kicks

### Giveaway
* the give away information: **description**, provider, manager, target date, winners and participants

### Embedded messages
* embedded message information
    * this information includes who embedded which message in which channel and is deleted after a few days

### Emote usage tracking
* the name of emotes which are being tracked in the emote usage tracking system for purely convenience reasons
* **who** used which emote is **not** tracked
* at which day an emote was used how many times
* whether the emote was a reaction

### Reminder
* **the message content** in order to provide you with the reminder text
* the date it was created, and the date it is due
* the id of the message which contained the command
* whether you have been reminded
* the users who have joined the reminders

### Modmail
* the information that a modmail thread existed (creation, status and close date), and the IDs of the messages that have been sent in both directions

### News
* the ORF integration stores which posts from ORF RSS were already sent.
* which ORF RSS feeds were subscribed

### Polls
* the **text** of polls, the creator of polls and who made which decision in polls

### Starboard (best-of)
* the message which was the origin for the starboard post
* the message which was the resulting starboard post
* the author of the message and the amount of stars
* who reacted to a starboard post
    * this is necessary to provide the information about 'top star giver' and to disallow duplicate starboard reactions

### Suggestion
* **the message content** of the message used to create the suggestion
    * this was used for the message used to update the status of a suggestion, but this is currently disabled
* the author of the suggestion and the message which has been posted in the suggestions channel
* every suggestion will be deleted completely from the database a few days after it has reached a final state (rejected, denied, accepted)
* whether you voted for a suggestion and which decision you made

### Leveling system
* the amount of messages which were considered for the leveling system
    * it only considers a message once per minute, so it does not directly translate to your absolute message count
* the amount of experience, and the experience level you have
* whether experience gain has been disabled for you
* the role you received because of the experience system
* which roles are configured to be used as experience roles and at which level they are assigned
* which roles are used to disable experience gain

### Entertainment
* PressF: for **what** the pressF was initiated by whom and who participated

### Economy
* The amount of credits for each user

### Assignable roles
* the names of assignable role places and assignable role button text, together with the associated emote markdown (if given)
* the assigned assignable roles for each member in order to provide the 'unique' assignable role functionality
* custom configured conditions to enable a level restriction

### Weekly items (for Miepscord)
* The text of the weekly art posts and the creator of them

### Sticky roles
* The roles a user had when leaving the server to be re-applied when re-joining (opt out possible)

### Meetups
* The meta data of meetups: text, location, creator and the decision of each user interacting with the meetup. This information will be deleted a few days after a meetup has passed/is cancelled.

### Twitch
* The ID/**name** of streamers to follow accompanied with their discord ID
* The start/end dates of their streams
    * The individual sections of streams identified by title and game for updating the message

### Custom commands
* the **names** given to custom commands and the configured **response text**

## Grafana dashboard

There is also a [Grafana](https://grafana.com/) dashboard in order to inspect how the bot is operating.
The information visible in this dashboard is:

* message events
* Discord gateway ping
* starboard reactions
* amount of command executions
* emotes currently being processed for tracking
* embedded messages
* invite filter activity
* amount of experience which is currently being processed

All of this information cannot be linked to any user (or any server for that matter, if the bot would be in multiple servers) and is deleted after 15 days.


## How can I decide which information is collected?
It is not possible to opt-out of singular sub-services of the bot. Should you decide that your information should not be collected, please cease usage of the bot immediately (leave any guild the bot operates in).

_Should you decide to no longer utilize the bot, you may request your data to be erased within 30 days as per GDPR if you are a citizen of the EU. You can do this by sending a message to the user "sheldan" on Discord: GDPR Data removal <Username> <UserId>. If your request is incomplete, we cannot acknowledge it and therefore your data will not be removed. In order to identify authentic requests, please contact modmail beforehand by sending a direct message to the bot and stating your intention._

## Open source content
This bot uses the following open source libraries and frameworks:

* [abstracto](https://github.com/Sheldan/abstracto) is used as a base for this bot, providing most of the functionalities
* [JDA](https://github.com/DV8FromTheWorld/JDA/) The Discord API Wrapper used
* [Spring boot](https://github.com/spring-projects/spring-boot) is used as a framework to create standalone application in Java with Java EE methods. (including Dependency injection and more)
* [Hibernate](https://github.com/hibernate/hibernate-orm) is used as a reference implementation of JPA.
* [Freemarker](https://github.com/apache/freemarker) is used as a templating engine. This is used to provide internationalization for user facing text and enable dynamic embed configuration.
* [Ehcache](https://github.com/ehcache/ehcache3) is used as a caching implementation.
* [Lombok](https://github.com/rzwitserloot/lombok) is used as a framework in order to speed up creation of container classes and builders.
* [Quartz](https://github.com/quartz-scheduler/quartz) is used as a scheduling framework in order to provide functionalities which either require a delayed or cronjob behaviour.
* [Docker](https://github.com/docker) is used to package the application into a container and [k3s](https://k3s.io/) to orchestrate the containers
* [Liquibase](https://github.com/liquibase/liquibase) is used to manage changes to the database
* [Prometheus](https://prometheus.io) to scrap and collect the metrics about how the bot is operating

* [Grafana](https://grafana.com) to visualize metrics of the bot and [Loki](https://grafana.com/oss/loki/) for logging
