<p align="center"><img src=".github/assets/logo.svg" width="128" alt="Strata Gallery logo"></p>

# Strata Gallery

Every [Strata](https://github.com/InspireNXE/Strata) control on a page of its own, drawn with Java2D through [Strata-Swing](https://github.com/InspireNXE/Strata-Swing) or with Vulkan through [Strata-Vulkan](https://github.com/InspireNXE/Strata-Vulkan). Pick a control in the sidebar to see its kinds and states, try it, and watch the events it fires in the log under the page. Requests, the events a handler can refuse, show with a question mark and the value asked for. Reports show the change.

The Screens section holds a component written from scratch on `CustomComponent`, data binding, work from other threads, animation, a screen that refuses to close and styling. Input has commands and shortcuts, focus, cursors, file dialogs and drag and drop, Drawing the shapes, pixels and hairlines a component can paint, and Language a page laid out right to left and plural forms that follow the language.

Beside the page title, a selector sets the panel's scale, and a check box lays the page out right to left. A page can turn on the theme's built-in motion, switch the language or set theme metrics for as long as it is open.

## Running

Building needs JDK 25. With Strata, Strata-Swing or Strata-Vulkan checked out beside this one, at `../Strata`, `../Strata-Swing` and `../Strata-Vulkan`, the gallery builds against them; otherwise it takes them from Maven Central.

```bash
./gradlew runJava2d
./gradlew runVulkan
```

`runJava2d` opens the gallery in a Swing window, drawn with Java2D. `runVulkan` opens it in a GLFW window drawn with Vulkan, which needs a Vulkan driver. Both are `./gradlew run` with `--renderer java2d` or `--renderer vulkan`, and plain `run` uses Java2D. The window's title names the renderer. The two show the same pages with each adapter's own fonts and drawing, so they are a quick way to compare adapters.

Without a display, render every page to a PNG with either renderer:

```bash
./gradlew run --args="--snapshot snapshots"
./gradlew run --args="--renderer vulkan --snapshot snapshots-vulkan"
```

`./gradlew build` also runs a test that renders every page with each renderer, skipping Vulkan on a machine without a driver.

## Minecraft's look

When Minecraft Java Edition is installed, a selector beside the page title switches the gallery to the vanilla look, and to any resource pack in the install. The gallery reads the sprites, menu backgrounds and font straight out of the game's files, and nothing is copied into this repository. It looks in the default install folder, or wherever `-Dstrata.gallery.minecraft=<directory>` points. Snapshots take the look with `--theme minecraft` or `--theme minecraft:<pack>`.

## Adding a page

Each section is a class in `org.inspirenxe.strata.gallery.pages` whose `pages()` returns one `Page` per control. `Samples` holds the shared examples and sample data. The log hears every attribute event on its own. Call `context.watch` for a control's own events, `context.watchPopup` for an overlay, or `context.log` from a handler.

## License

Strata Gallery is released under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
