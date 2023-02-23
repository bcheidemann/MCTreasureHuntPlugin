# Developers

## Setup

### Java

Install JDK version 18 and ensure that the `JAVA_HOME` environment variable is set to the install directory.

Running `echo $JAVA_HOME` should log something like:

```
/opt/jdk-18.0.2.1
```

### Maven

Install Apache Maven version 3.8.x and ensure that the `MAVEN_HOME` environment variable is set to the install directory.

Running `echo $MAVEN_HOME` should log something like:

```
/opt/apache-maven-3.8.6
```

### Dev Server

Run the following command to setup the development server.

IMPORTANT: By running setup-server, you are agreeing to the Minecraft EULA (https://aka.ms/MinecraftEULA).

```sh
make dev-setup-server
```

## Running the Development Server

To start the development server, run:

```sh
make dev
```

This will build the plugin from source, copy the resulting `.jar` file into the development servers `plugins` directory, and start the server in a screen session.

To watch for file changes and automatically hot-reload the server, first detach from the screen session using `C-a C-d`, or start a new terminal session. Next run the `watch` command.

```sh
make watch
```

Now, when a source file is saved, the plugin will be re-built from source, copied into the `plugins` directory, and the server will reload automatically.

## Debugging

You will first need to install the Red Hat Java VSCode extension. Once this is installed, to debug the plugin in VSCode, first [start the development server](#running-the-development-server), then run the `Attach` configuration from the `Run and Debug` tab. You can then set breakpoints, inspect variables, step through the source, etc.

Note that the server will spam errors if you hit a breakpoint. These can be ignored. After 10 minutes if you do not continue, the server will shut down with an error. The client will also disconnect after a short interval if it does not receive a response from the server.
