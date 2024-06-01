# DCLogging

DCLogging is a simple logging library that sends log messages to a Discord channel.

<!-- TOC -->
* [DCLogging](#dclogging)
  * [Setup](#setup)
    * [Maven](#maven)
    * [Gradle Kotlin](#gradle-kotlin)
    * [Gradle Groovy](#gradle-groovy)
  * [Installing and configuration](#installing-and-configuration)
    * [Templates](#templates)
  * [Usage](#usage)
<!-- TOC -->

## Setup

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
    <version>1.1.1</version>
</dependency>
```

### Gradle Kotlin

**Repository**

Add the following repository to your `build.gradle.kts`:

```kts
maven {
    name = "rettichlpRepository"
    url = uri("https://maven.rettichlp.de/<repository>")
}
```

**Dependency**

Add the following dependency to your `build.gradle.kts`:

```kts
implementation("de.rettichlp:dclogging:1.1.1")
```

### Gradle Groovy

**Repository**

Add the following repository to your `build.gradle`:

```groovy
maven {
    name "rettichlpRepository"
    url "https://maven.rettichlp.de/<repository>"
}
```

**Dependency**

Add the following dependency to your `build.gradle`:

```groovy
implementation "de.rettichlp:dclogging:1.1.1"
```

## Installing and configuration

Create your DiscordLogging instance via the builder pattern:

```java
DiscordLogging discordLogging = DiscordLogging.getBuilder()
        .botToken("<yout-bot-token>") // required
        .guildId("<your-guild-id>") // required
        .textChannelId("<textChannelId>") // optional (default = discord guild system channel)
        .appendStacktraceToError(false) // optional (default = true)
        .infoMessageTemplate(<template>) // optional
        .warnMessageTemplate(<template>) // optional
        .errorMessageTemplate(<template>) // optional
        .build();
```

### Templates

The templates are used to format the messages that are sent to the Discord channel. The following placeholders are available:

- `%timestamp%` - The timestamp of the log message
- `%message%` - The message that was logged

**The default templates:**

![](https://i.imgur.com/SqxgaIk.png)

## Usage

Now use the generated `discordLogging` instance to log messages:

```java
discordLogging.info("This is an info message");
discordLogging.warn("This is a warning message");
discordLogging.error("This is an error message");
```
