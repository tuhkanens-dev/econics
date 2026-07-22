### Econics

A powerful and flexible currency plugin for Paper servers that allows you to create and manage multiple custom currencies with ease.

**Features**
- Multi-Currency System — Create unlimited custom currencies with independent configurations
- Database Support — MySQL and SQLite support with automatic connection pooling via HikariCP
- Developer-Friendly API — Clean API for other plugins
- Customizable Messages — Fully translatable and customizable plugin messages
- PlaceholderAPI Integration — Display currency balances using placeholders
- Currency Formatting — Support for custom formatting and symbols
- Per-Currency Commands — Enable or disable commands individually for each currency
- Custom Permissions — Assign custom permissions to commands per currency
- Modern Stack — Written in Kotlin with Exposed ORM

**For Developers**

The plugin is written in Kotlin with Gradle. You can import the API library using `compileOnly`.

**Gradle (Kotlin DSL)**
```kotlin
compileOnly("dev.tuhkanens.econicsapi:econics-api:1.0.0")
```
**Maven (Xml)**
```xml
<dependency>
    <groupId>dev.tuhkanens.econicsapi</groupId>
    <artifactId>econics-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```