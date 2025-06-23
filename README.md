# archit
Minecraft scripting language for automated structure generation, an AGH UST compilers project.

## Documentation

- User Guide: &nbsp;&nbsp; **[English] &nbsp;&nbsp; [[Polski]](docs/user_guide_pl.md)**
- Technical Documentation: &nbsp;&nbsp; **[English] &nbsp;&nbsp; [[Polski]](docs/technical_pl.md)**

## `TL;DR`: How to run
`archit` supports two modes of operation:
1. As a Minecraft mod, which allows you to run scripts in the game and watch results in real time.
2. As a standalone program, which allows you to run scripts from the terminal and export results to a 3D object.

### As a Minecraft mod, latest build
- Create a Minecraft instance with version set to **1.21.5**
- Install [Fabric](https://fabricmc.net/) modloader
- Put [Fabric API](https://modrinth.com/mod/fabric-api/versions) into your `mods` folder
- Download the latest version of the mod from [GitHub Actions](https://github.com/Kacper0510/archit/actions?query=branch%3Amaster) and place it in the `mods` folder as well

### As a Minecraft mod, locally
- Clone the repository:
```bash
$ git clone https://github.com/Kacper0510/archit
$ cd archit
```
- Run a Gradle task to open Minecraft client or server:
```bash
$ ./gradlew runClient # OR ./gradlew runServer
```

### As a standalone program, latest build
- Download the latest JAR from [GitHub Actions](https://github.com/Kacper0510/archit/actions?query=branch%3Amaster)
- Run with Java:
```bash
$ java -jar archit.jar <path to input file>
```

### As a standalone program, locally
- Clone the repository:
```bash
$ git clone https://github.com/Kacper0510/archit
$ cd archit
```
- Run with Gradle:
```bash
$ ./gradlew run --args="<path to input file>"
```

## Authors
* [Emil Wajda](https://github.com/Atras19)
* [Dawid Węcirz](https://github.com/Yeetoo45)
* [Kacper Wojciuch](https://github.com/Kacper0510)
