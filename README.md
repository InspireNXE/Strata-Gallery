# Strata Gallery

Every [Strata](../Strata) control on a page of its own, running on Strata's Java2D adapter from [Strata-Swing](../Strata-Swing). Pick a control in the sidebar to see its kinds and states, try it, and watch the events it fires in the log under the page. Requests (the events a handler can refuse) show with a question mark and the value asked for; reports show the change.

The sidebar follows the packages the controls live in: Display, Buttons, Fields, Collections, Menus, Panes, Windows and Layout. The Screens section holds mock-ups of Almura's Exchange and Store screens, which are the reference for what Strata has to support, and a component written from scratch on `CustomComponent`.

Pages use the API only, as a mod or game would. The gallery builds against the Strata and Strata-Swing checkouts beside it, at `../Strata` and `../Strata-Swing`, so check all three out side by side. Nothing is published yet.

Building needs JDK 25.

```bash
./gradlew run
```

Without a display, render every page to a PNG, or the mock-ups in a dozen states:

```bash
./gradlew run --args="--snapshot snapshots"
./gradlew run --args="--mocks snapshots/mocks"
```

`./gradlew build` also runs a test that renders every page.

## Minecraft's look

When Minecraft Java Edition is installed, a selector beside the page title switches the gallery to the vanilla look, so you can see how Strata screens look in game without starting it. The gallery reads the GUI sprites, their `.png.mcmeta` slicing, the menu backgrounds and the default font straight out of the newest release's client jar and the launcher's asset index, and the selector offers each resource pack in the install's `resourcepacks` folder too, its files over the game's. Nothing is copied into this repository. It looks in `%APPDATA%\.minecraft`, `~/Library/Application Support/minecraft` or `~/.minecraft`, or wherever `-Dstrata.gallery.minecraft=<directory>` points.

Snapshots take the look with `--theme`:

```bash
./gradlew run --args="--snapshot snapshots --theme minecraft"
./gradlew run --args="--snapshot snapshots --theme minecraft:<pack>"
```

The gallery draws at 1.5 pixels per unit, so the game's font and sprites, which the game draws at whole multiples, look uneven in places. Font providers other than bitmaps and spaces, such as Unifont, are not read; their characters fall back to Dialog.

## Adding a page

Each section is a class in `org.inspirenxe.strata.gallery.pages` whose `pages()` returns one `Page` per control: a section, a title, a short summary and a function that builds the content from a `PageContext`. `Samples` has the captioned examples, rows and sample data the pages share. The log hears every attribute event (`Toggle`, `Ranged`, `SingleSelection` and the rest) on its own. Call `context.watch` for a control's own events. Overlays such as menus, flyouts and dialogs sit outside the page, so pass a popup to `context.watchPopup` to log its events, or call `context.log` from a handler for anything else.

## License

Strata Gallery is released under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
