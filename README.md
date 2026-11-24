# Game-Reporter
Games built in Unity and Godot can be easily decompiled with tools like [dnSpy](https://github.com/dnspy/dnspy) and [GDS Decomp](https://github.com/GDRETools/gdsdecomp). This makes securely storing API tokens for external services difficult. Storing API tokens on an external server is much more secure and prevents bad actors from misusing secret API tokens. That is why I built this Java Spring Boot server that can hold bug-report-related API tokens securely. 

The Game Reporter API handles converting data sent into a Discord embedded message and sends it in a specified channel. This keeps your Discord webhook URL secure while still being able to receive bug reports. 

## Setup

> [!TIP]
> Game Reporter is meant to be easy for anyone to set up. If you run into any issues in the setup process, post a question in [Discussion](https://github.com/Ceichert31/Game-Reporter/discussions).

1. [Hosting Setup](https://github.com/Ceichert31/Game-Reporter/wiki/Hosting-Setup)
2. [In-Engine Implementation](https://github.com/Ceichert31/Game-Reporter/wiki/In%E2%80%90Engine-Implementation)

## Credit
Was inspired to build this from this Godot addon: https://github.com/ASecondGuy/BugReporter 
