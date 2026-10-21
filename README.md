<p align="center"><img src=".github/assets/logo.svg" width="128" alt="Strata Gallery logo"></p>

# Strata Gallery

Every [Strata](https://github.com/InspireNXE/Strata) control on a page of its own, running on the Java2D adapter from [Strata-Swing](https://github.com/InspireNXE/Strata-Swing). Pick a control in the sidebar to see its kinds and states, try it, and watch the events it fires in the log under the page. Requests, the events a handler can refuse, show with a question mark and the value asked for. Reports show the change.

The Screens section holds a component written from scratch on `CustomComponent`.

## Running

Building needs JDK 25. With Strata or Strata-Swing checked out beside this one, at `../Strata` and `../Strata-Swing`, the gallery builds against them; otherwise it takes them from Maven Central.

```bash
./gradlew run
```

Without a display, render every page to a PNG:

```bash
./gradlew run --args="--snapshot snapshots"
```

`./gradlew build` also runs a test that renders every page.

## Minecraft's look

When Minecraft Java Edition is installed, a selector beside the page title switches the gallery to the vanilla look, and to any resource pack in the install. The gallery reads the sprites, menu backgrounds and font straight out of the game's files, and nothing is copied into this repository. It looks in the default install folder, or wherever `-Dstrata.gallery.minecraft=<directory>` points. Snapshots take the look with `--theme minecraft` or `--theme minecraft:<pack>`.

## Adding a page

Each section is a class in `org.inspirenxe.strata.gallery.pages` whose `pages()` returns one `Page` per control. `Samples` holds the shared examples and sample data. The log hears every attribute event on its own. Call `context.watch` for a control's own events, `context.watchPopup` for an overlay, or `context.log` from a handler.

## License

Strata Gallery is released under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
