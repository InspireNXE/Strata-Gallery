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

## Adding a page

Each section is a class in `org.inspirenxe.strata.gallery.pages` whose `pages()` returns one `Page` per control: a section, a title, a short summary and a function that builds the content from a `PageContext`. `Samples` has the captioned examples, rows and sample data the pages share. The log hears every attribute event (`Toggle`, `Ranged`, `SingleSelection` and the rest) on its own. Call `context.watch` for a control's own events, and `context.log` for events from overlays such as menus and dialogs, which sit outside the page.

## License

Strata Gallery is released under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
