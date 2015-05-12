## AppConfig

Small application configuration library to provide different properties for applications deployed to different environments


## Usage

First, do a static import `AppConfig.p(...)` method:


```java
import static org.javalite.app_configAppConfig.p;
```

then, simply call a `p(..)` metod in places where you need to inject a property:

```java
String name = p("name");
```

## Description

Allows configuration of applications that is specific for different deployment environments.

Applications could have environment-specific files, whose names follow this pattern:
`environment.properties`, where `environment` is a name of a deployment environment, such as `development`,
`staging`, `production`, etc.

You can also provide a global file, properties from which will be loaded in all environments: `global.properties`.

In all cases the files need to be on the classpath under directory/package `/app_config`.

Environment-specific file will have an "environment" part of the file name match to an environment variable called "ACTIVE_ENV".
Such configuration is easy to achieve in Unix shell:

```
export ACTIVE_ENV=test
```

## Typical file structure

```
/app_config
        |
        +--global.properties
        |
        +--development.properties
        |
        +--staging.properties
        |
        +--production.properties
```

Global property file will always be loaded, while others will be loaded depending on the value of `ACTIVE_ENV` environment variable.

## Default

If environment variable `ACTIVE_ENV` is missing, it defaults to `development`.

## System property

You can also provide an environment as a system property `active_env`. System property overrides environment
variable `ACTIVE_ENV`

