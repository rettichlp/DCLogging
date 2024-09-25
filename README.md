# DCLogging

DCLogging is a simple logging library that sends log messages to a Discord channel.

<!-- TOC -->
* [DCLogging](#dclogging)
  * [Repository and dependency](#repository-and-dependency)
    * [Maven](#maven)
    * [Gradle Kotlin](#gradle-kotlin)
    * [Gradle Groovy](#gradle-groovy)
  * [Setup](#setup)
    * [Prerequisites](#prerequisites)
    * [Logging instance](#logging-instance)
    * [Templates](#templates)
  * [Usage](#usage)
<!-- TOC -->

## Repository and dependency

The setup is simple and can be done via Maven or Gradle. You can find the latest version on
this [maven repository](https://maven.rettichlp.de/#/releases/de/rettichlp/dclogging).

### Maven

**Repository**

Add the following repository to your `pom.xml`:

```xml

<repository>
    <id>rettichlp-repository</id>
    <name>RettichLP Repository</name>
    <url>https://maven.rettichlp.de/releases</url>
</repository>
```

**Dependency**

Add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>de.rettichlp</groupId>
    <artifactId>dclogging</artifactId>
    <version>[VERSION]</version>
</dependency>
```

### Gradle Kotlin

**Repository**

Add the following repository to your `build.gradle.kts`:

```kts
maven {
    name = "rettichlpRepository"
    url = uri("https://maven.rettichlp.de/releases")
}
```

**Dependency**

Add the following dependency to your `build.gradle.kts`:

```kts
implementation("de.rettichlp:dclogging:[VERSION]")
```

### Gradle Groovy

**Repository**

Add the following repository to your `build.gradle`:

```groovy
maven {
    name "rettichlpRepository"
    url "https://maven.rettichlp.de/releases"
}
```

**Dependency**

Add the following dependency to your `build.gradle`:

```groovy
implementation "de.rettichlp:dclogging:[VERSION]"
```

## Setup

### Prerequisites
1. To use the library, you need to create your own Discord bot at the [Discord Developer Portal](https://discord.com/developers/applications). Save the bot token (handle it as a secret), you will need it later.
2. After creating the bot, you need to invite it to your server.
3. To get the guild ID, right-click on the server icon and click on "Copy ID". You need this ID to send messages to the correct server.
4. To get the text channel ID, right-click on the text channel and click on "Copy ID". You need this ID to send messages to the correct channel.

### Logging instance

Create a new instance of the `DiscordLogging` class. This class is used to send log messages to a Discord channel.

```java
DiscordLogging discordLogging = DiscordLogging.builder()
        .botToken("<yout-bot-token>") // required
        .guildId("<your-guild-id>") // required
        .textChannelId("<textChannelId>") // optional (default = discord guild system channel)
        .appendStacktraceToError(false) // optional (default = true)
        .build();
```

### Templates

The templates are used to format the messages that are sent to the Discord channel. There are default templates for the different log levels (INFO, WARN, ERROR). You can also create your own templates.

**Default templates:**

![](https://i.imgur.com/SqxgaIk.png)

**Custom templates:**

The following placeholders are available:

- `%timestamp%` - The timestamp of the log message
- `%message%` - The message that was logged

```java
MessageTemplate myCustomInfoMessageTemplate = new MessageTemplate("%timestamp% INFO: %message%");
MessageTemplate myCustomWarnMessageTemplate = new MessageTemplate("%timestamp% WARN: %message%");
MessageTemplate myCustomErrorMessageTemplate = new MessageTemplate("%timestamp% ERROR: %message%");

DiscordLogging discordLogging = DiscordLogging.builder()
        .botToken("<yout-bot-token>") // required
        .guildId("<your-guild-id>") // required
        .textChannelId("<textChannelId>") // optional (default = discord guild system channel)
        .appendStacktraceToError(false) // optional (default = true)
        .infoMessageTemplate(myCustomInfoMessageTemplate) // optional
        .warnMessageTemplate(myCustomWarnMessageTemplate) // optional
        .errorMessageTemplate(myCustomErrorMessageTemplate) // optional
        .build();
```

## Usage

Now use the generated `discordLogging` instance to log messages:

```java
discordLogging.info("This is an info message");
discordLogging.warn("This is a warning message");
discordLogging.error("This is an error message");
```

You can also use placeholders in the message:

```java
String messageType = "information";
discordLogging.info("This is an {} message", messageType);
```
