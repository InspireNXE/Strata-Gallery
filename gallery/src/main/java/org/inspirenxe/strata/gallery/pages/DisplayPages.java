package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.text.TextOverflow;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;

import java.util.List;

/**
 * The Display section: controls that show something and take no input.
 */
final class DisplayPages {
    private static final String SECTION = "Display";
    private static final String LONG_TEXT = "Ana listed three crates of apples at the morning market, and Ben bought "
            + "two of them before noon.";

    private DisplayPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Label", "Text the user reads but does not edit. A label can wrap, cut itself "
                        + "short with an ellipsis, mix styles in spans and draw larger or smaller.", context -> {
                    final ComponentFactory ui = context.ui();
                    final TextStyle gold = TextStyle.DEFAULT.withColor(0xFFE8C547).withBold(true);
                    final TextStyle gray = TextStyle.DEFAULT.withColor(Samples.MUTED);
                    return Samples.column(ui,
                            Samples.section(ui, "Text",
                                    Samples.example(ui, "Plain", ui.label("Fresh pears")),
                                    Samples.example(ui, "Line breaks", ui.label("Lisbon\nOslo\nKyoto")),
                                    Samples.example(ui, "Wrapping at 180 units", ui.label(DisplayPages.LONG_TEXT)
                                            .setWrap(true)
                                            .setWidth(180)),
                                    Samples.example(ui, "Ellipsis at 120 units", ui.label(DisplayPages.LONG_TEXT)
                                            .setOverflow(TextOverflow.ELLIPSIS)
                                            .setWidth(120))),
                            Samples.section(ui, "Spans and scale",
                                    Samples.example(ui, "Spans", ui.label().setSpans(List.of(
                                            new TextSpan("125.50", gold),
                                            new TextSpan(" per crate from ", gray),
                                            new TextSpan("Cleo", TextStyle.DEFAULT.withUnderline(true))))),
                                    Samples.example(ui, "Scale 0.8", ui.label("Small print").setTextScale(0.8f)),
                                    Samples.example(ui, "Scale 1.5", ui.label("Larger").setTextScale(1.5f)),
                                    Samples.example(ui, "Scale 2", ui.label("Title").setTextScale(2f))),
                            Samples.section(ui, "Styles",
                                    Samples.example(ui, "Bold", ui.label("Bold")
                                            .setStyle(TextStyle.DEFAULT.withBold(true))),
                                    Samples.example(ui, "Italic", ui.label("Italic")
                                            .setStyle(TextStyle.DEFAULT.withItalic(true))),
                                    Samples.example(ui, "Underline", ui.label("Underline")
                                            .setStyle(TextStyle.DEFAULT.withUnderline(true))),
                                    Samples.example(ui, "Strikethrough", ui.label("Sold out")
                                            .setStyle(TextStyle.DEFAULT.withStrikethrough(true))),
                                    Samples.example(ui, "Shadow", ui.label("Shadow")
                                            .setStyle(TextStyle.DEFAULT.withShadow(true))),
                                    Samples.example(ui, "Color", ui.label("Plum")
                                            .setStyle(TextStyle.DEFAULT.withColor(0xFFC77DFF)))));
                }),
                new Page(SECTION, "Image", "A picture drawn at a size you choose. Stretch scaling covers the bounds, "
                        + "fit scaling keeps the image's aspect ratio and centers it, and the tint multiplies its "
                        + "colors.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Sizes",
                                    Samples.example(ui, "Natural, 16 units", ui.image(Samples.icon())),
                                    Samples.example(ui, "32 units", ui.image(Samples.icon()).setImageSize(32, 32)),
                                    Samples.example(ui, "48 units", ui.image(Samples.icon()).setImageSize(48, 48))),
                            Samples.section(ui, "Scaling in an 80 by 32 slot",
                                    Samples.example(ui, "Stretch", DisplayPages.framed(ui, ui.image(Samples.icon())
                                            .setSize(80, 32))),
                                    Samples.example(ui, "Fit", DisplayPages.framed(ui, ui.image(Samples.icon())
                                            .setScaling(Image.Scaling.FIT)
                                            .setSize(80, 32)))),
                            Samples.section(ui, "Tint",
                                    Samples.example(ui, "None", ui.image(Samples.icon(0xFFFFFFFF))
                                            .setImageSize(32, 32)),
                                    Samples.example(ui, "Red", ui.image(Samples.icon(0xFFFFFFFF))
                                            .setImageSize(32, 32)
                                            .setTint(0xFFE05050)),
                                    Samples.example(ui, "Half alpha", ui.image(Samples.icon())
                                            .setImageSize(32, 32)
                                            .setTint(0x80FFFFFF))));
                }),
                new Page(SECTION, "Progress bar", "A bar filled from the left to show how far a value is through its "
                        + "range, with an optional caption. When the length of the work is unknown, it shows a moving "
                        + "segment instead.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Values",
                                    Samples.example(ui, "Empty", ui.progressBar().setWidth(160)),
                                    Samples.example(ui, "A third", ui.progressBar().setValue(0.33).setWidth(160)),
                                    Samples.example(ui, "Full", ui.progressBar().setValue(1).setWidth(160))),
                            Samples.section(ui, "Captions",
                                    Samples.example(ui, "Range 0 to 64", ui.progressBar()
                                            .setRange(0, 64)
                                            .setValue(40)
                                            .setText("40/64")
                                            .setWidth(160)),
                                    Samples.example(ui, "Percentage", ui.progressBar()
                                            .setValue(0.75)
                                            .setText("75%")
                                            .setWidth(160))),
                            Samples.section(ui, "Indeterminate",
                                    Samples.example(ui, "No caption", ui.progressBar()
                                            .setIndeterminate(true)
                                            .setWidth(160)),
                                    Samples.example(ui, "With a caption", ui.progressBar()
                                            .setIndeterminate(true)
                                            .setText("Connecting")
                                            .setWidth(160))));
                }),
                new Page(SECTION, "Busy indicator", "Three dots with one lit, stepping back and forth while work is "
                        + "under way, with an optional caption above. It takes no input and fires no events.",
                        context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Dots only", ui.busyIndicator()),
                                    Samples.example(ui, "With a caption", ui.busyIndicator("Loading offers")),
                                    Samples.example(ui, "Centered in 160 units", ui.busyIndicator("Joining")
                                            .setWidth(160))));
                }),
                new Page(SECTION, "Separator", "A thin line across or down a panel. A horizontal separator stretches "
                        + "across its slot and is one unit thick unless told otherwise.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Horizontal",
                                    Samples.example(ui, "Between two lines", DisplayPages.stacked(ui,
                                            ui.label("Apples"),
                                            ui.separator().setWidth(160),
                                            ui.label("Oranges"))),
                                    Samples.example(ui, "Three units thick", DisplayPages.stacked(ui,
                                            ui.label("Lisbon"),
                                            ui.separator().setThickness(3).setWidth(160),
                                            ui.label("Oslo")))),
                            Samples.section(ui, "Vertical",
                                    Samples.example(ui, "Between names", Samples.row(ui,
                                            ui.label("Ana"),
                                            ui.separator(Orientation.VERTICAL).setHeight(12),
                                            ui.label("Ben"),
                                            ui.separator(Orientation.VERTICAL).setHeight(12),
                                            ui.label("Cleo")))));
                }));
    }

    /**
     * Stacks components top to bottom, closer together than a column.
     */
    private static Component<?> stacked(final ComponentFactory ui, final Component<?>... children) {
        return Samples.column(ui, children).setSpacing(4);
    }

    /**
     * Puts a component on a dark panel so the bounds it fills can be seen.
     */
    private static Component<?> framed(final ComponentFactory ui, final Component<?> content) {
        return ui.stackPanel()
                .setBackground(Paint.solid(0xFF4A4D55))
                .add(content);
    }
}
