# DCLogging

## Setup

Create your DiscordLogging instance via the builder pattern:

```java
DiscordLogging discordLogging = DiscordLogging.getBuilder()
        .botToken("<yout-bot-token>") // required
        .guildId("<your-guild-id>") // required
        .textChannelId("<textChannelId>") // optional (default = discord guild system channel)
        .appendStacktraceToError(false) // optional (default = true)
        .infoMessageTemplate(<template>)) // optional
        .warnMessageTemplate(<template>)) // optional
        .errorMessageTemplate(<template>)) // optional
        .build();
```

## Templates

The templates are used to format the messages that are sent to the Discord channel. The following placeholders are available:

- `%timestamp%` - The timestamp of the log message
- `%message%` - The message that was logged

**The default templates:**

![](https://i.imgur.com/SqxgaIk.png)
