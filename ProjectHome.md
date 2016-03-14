# Sceye-Fi #
## Introduction ##
The software that comes with [Eye-Fi](http://www.eye.fi/) card doesn't suit the needs of all users (there is no Linux version for instance).

The goal I had in my mind when I started to write this program was to integrate it in my event-photography software; but it can be used independently.

## Requirements ##

Sceye-Fi requires a JRE or JDK 1.6+ to run.
It's a Netbeans project, but it can be built with Ant 1.7+

## Running Sceye-Fi ##

The server can be started with the following command:

```
java -jar sceye-fi.jar [<configuration-file>]
```

A configuration file may optionally be specified, otherwise, it will search for one in the default location:

  * **user-home**`/Application Data/Eye-Fi/Settings.xml`, or
  * **user-home**`/Library/Eye-Fi/Settings.xml`

## Logging ##

To see what Sceye-Fi is doing, you can supply a logging.properties file on the command line:

```
java -Djava.util.logging.config.file=logging.properties -jar sceye-fi.jar
```

Here is an example of a logging.properties file:

```
handlers = java.util.logging.ConsoleHandler

.level = WARNING

java.util.logging.ConsoleHandler.level = ALL
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

org.tastefuljava.sceyefi.level = FINE
```

## Documentation ##

You will find documentation about

  * Configuring Sceye-Fi [here](http://code.google.com/p/sceye-fi/wiki/Configuration)
  * Embedding Sceye-Fi in your application [here](http://code.google.com/p/sceye-fi/wiki/Embedding)
  * The upload protocol [here](http://code.google.com/p/sceye-fi/wiki/UploadProtocol)

## Related projects ##

I have benefited from the experience of other persons that have reversed-engineered the Eye-Fi protocol for their own projects.
Here is a list of related projects:

  * http://code.google.com/p/eyefiserver/
  * http://code.google.com/p/eyefidroid/
  * http://code.google.com/p/eyefi-config/ http://sr71.net/projects/eyefi/
  * https://github.com/kristofg/rifec
  * https://github.com/tachang/EyeFiServer

[Dave Hansen's blog](http://dave-hansen.blogspot.com/) also contains a lot of useful information.

Also, if you're into open-source hardware, check out these cool projects:

  * http://code.google.com/p/partybooth/
  * http://www.photoduino.com/