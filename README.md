### Econics

A flexible currency plugin for Paper servers. Manage multiple custom currencies
easily with a clean and efficient system.

**Features**

- Multi-Currency: Create as many currencies as you need.
- Database Support: MySQL and SQLite support with HikariCP pooling.
- Developer API: Simple API for other plugins to hook into.
- Customizable: Change all messages and currency formatting to fit your
  server.
- PlaceholderAPI: Built-in placeholders to show balances.
- Command Control: Enable/disable commands and set permissions for each
  currency.
- Modern Code: Written in Kotlin with Exposed ORM for stable data handling.


**Admin Commands**
- econics reload (required permission: `econics.admin.reload`)

**Soft Dependencies**
- PlaceholderAPI

**Requirements**
- Version: 1.21 or higher.
- Java: 21 or higher.

**For Developers**

Econics is built with Gradle. You can add the API to your project like this:

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
dependencis {
    compileOnly("com.github.tuhkanens-dev:econics:2.2.1")
}
```
**Maven (Xml)**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.tuhkanens-dev</groupId>
        <artifactId>econics</artifactId>
        <version>2.2.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```