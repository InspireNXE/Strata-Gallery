package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.Event;
import org.inspirenxe.strata.api.event.EventType;
import org.inspirenxe.strata.api.event.control.ValueEvent;
import org.inspirenxe.strata.api.event.control.ValueRequestEvent;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;

/**
 * The Windows section: windows, message boxes, toasts and tooltips. Dialogs and toasts are overlays, outside the
 * page's tree, so their events are logged by hand from handlers on them.
 */
final class WindowPages {
    private static final String SECTION = "Windows";
    private static final Paint SLOT = Paint.solid(0xFF3A3D43);
    private static final Paint CARD = Paint.solid(0xF0202226);
    private static final Border CARD_BORDER = Border.of(0xFF4C86E0, 1f);

    private WindowPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Window", "A framed panel with a title bar and a close button. It can be moved "
                        + "by its title bar and resized by its grip or its edges, inside the slot its parent gives "
                        + "it.", """
                        final Window settings = ui.window("Settings")
                                .setResizable(true)
                                .setEdgeResizable(true)
                                .add(ui.stackPanel()
                                        .setSpacing(4)
                                        .add(ui.checkBox("Show prices"))
                                        .add(ui.checkBox("Sound on sale")))
                                .setSize(200, 110)
                                .setMinSize(140, 90);
                        // The close button and Escape ask to close; hide or remove the window when it does
                        settings.onEvent(Closable.CLOSED, event -> settings.setVisible(false));
                        panel.add(settings);

                        // Or open it over the screen as a dialog
                        ui.overlays().dialog(ui.window("Notice").add(ui.label("Saved.")));
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(Window.RESIZE, Window.RESIZED);
                    final Window settings = ui.window("Settings")
                            .setResizable(true)
                            .setEdgeResizable(true)
                            .add(WindowPages.options(ui))
                            .setSize(200, 110)
                            .setMinSize(140, 90);
                    settings.onEvent(Closable.CLOSED, event -> settings.setVisible(false));
                    final Component<?> movable = ui.anchorPanel()
                            .setBackground(SLOT)
                            .add(settings)
                            .setSize(340, 150);
                    final Component<?> fixed = ui.anchorPanel()
                            .setBackground(SLOT)
                            .add(ui.window("Notice")
                                    .setMovable(false)
                                    .setClosable(false)
                                    .add(ui.label("This one stays put.").setAnchor(Anchor.CENTER))
                                    .setSize(160, 70))
                            .setSize(190, 150);
                    return Samples.column(ui,
                            Samples.section(ui, "Inline",
                                    Samples.example(ui, "Movable, closable, resizable by grip and edges",
                                            ui.stackPanel()
                                                    .setSpacing(4)
                                                    .add(movable)
                                                    .add(ui.button("Show Settings again after closing it")
                                                            .onEvent(Button.PRESSED, event ->
                                                                    settings.setVisible(true)))),
                                    Samples.example(ui, "Not movable or closable", fixed)),
                            Samples.section(ui, "As a dialog",
                                    Samples.example(ui, "A small form", ui.button("Enter a quantity")
                                            .onEvent(Button.PRESSED, event -> WindowPages.quantity(context))),
                                    Samples.example(ui, "Resizable, Escape closes it", ui.button("Open Settings")
                                            .onEvent(Button.PRESSED, event -> {
                                                final Window window = ui.window("Settings")
                                                        .setResizable(true)
                                                        .setEdgeResizable(true)
                                                        .add(WindowPages.options(ui))
                                                        .setSize(220, 120)
                                                        .setMinSize(140, 90);
                                                WindowPages.logHere(context, window, "dialog", Closable.CLOSE,
                                                        Closable.CLOSED, Window.RESIZE, Window.RESIZED);
                                                ui.overlays().dialog(window);
                                            })),
                                    Samples.example(ui, "Closing refused", ui.button("Open a stubborn window")
                                            .onEvent(Button.PRESSED, event -> {
                                                final Window window = ui.window("Stubborn");
                                                window.add(ui.stackPanel()
                                                                .setSpacing(6)
                                                                .add(ui.label("Close and Escape are canceled."))
                                                                .add(ui.button("Close from code")
                                                                        .onEvent(Button.PRESSED, press ->
                                                                                window.close()))
                                                                .setAnchor(Anchor.CENTER))
                                                        .setSize(200, 90)
                                                        .onEvent(Closable.CLOSE, close -> close.cancel());
                                                WindowPages.logHere(context, window, "dialog", Closable.CLOSE,
                                                        Closable.CLOSED);
                                                ui.overlays().dialog(window);
                                            }))));
                }),
                new Page(SECTION, "Message box", "A window with a message and a row of buttons, opened as a dialog. "
                        + "The first button takes focus, and the close button and Escape give each set's dismiss "
                        + "answer.", """
                        final Label status = ui.label("");
                        final Button ask = ui.button("Sell the lamp?");
                        ask.onEvent(Button.PRESSED, press -> {
                            final MessageBox box = ui.messageBox()
                                    .setTitle("Sell")
                                    .setMessage("Sell the brass lamp for 40 coins?")
                                    .setButtons(MessageBox.Buttons.YES_NO)
                                    .setButtonText(MessageBox.Result.YES, "Sell")
                                    .setButtonText(MessageBox.Result.NO, "Keep it")
                                    .onEvent(MessageBox.ANSWERED, event -> status.setText("Answered " + event.value()));
                            ui.overlays().dialog(box);
                        });
                        panel.add(ask).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    context.watch(MessageBox.ANSWER, MessageBox.ANSWERED);
                    final StackPanel sets = Samples.row(ui).setSpacing(6);
                    for (final MessageBox.Buttons buttons : MessageBox.Buttons.values()) {
                        sets.add(ui.button(buttons.name()).onEvent(Button.PRESSED, event -> WindowPages.message(
                                context, ui.messageBox("Buttons." + buttons.name(), "This box offers "
                                        + buttons.results() + ". Dismissing it answers "
                                        + buttons.dismissResult() + ".", buttons))));
                    }
                    return Samples.column(ui,
                            Samples.section(ui, "Each set of buttons", sets),
                            Samples.section(ui, "Changes",
                                    Samples.example(ui, "Captions from setButtonText", ui.button("Sell the lamp?")
                                            .onEvent(Button.PRESSED, event -> WindowPages.message(context,
                                                    ui.messageBox("Sell", "Sell the brass lamp for 40 coins?",
                                                                    MessageBox.Buttons.YES_NO)
                                                            .setButtonText(MessageBox.Result.YES, "Sell")
                                                            .setButtonText(MessageBox.Result.NO, "Keep it")))),
                                    Samples.example(ui, "Only OK closes it", ui.button("Read the terms")
                                            .onEvent(Button.PRESSED, event -> WindowPages.message(context,
                                                    ui.messageBox("Terms", "Cancel and Escape are refused in a "
                                                                    + "MessageBox.ANSWER handler.",
                                                                    MessageBox.Buttons.OK_CANCEL)
                                                            .onEvent(MessageBox.ANSWER, answer -> {
                                                                if (answer.value() != MessageBox.Result.OK) {
                                                                    answer.cancel();
                                                                }
                                                            }))))),
                            Samples.section(ui, "What one looks like",
                                    Samples.example(ui, "Shown inline for the snapshot", ui.messageBox("Delete",
                                            "Delete the listing for the oak chair?", MessageBox.Buttons.YES_NO))));
                }),
                new Page(SECTION, "Toast", "A short notice in the top-right corner that closes itself after a "
                        + "while. A click closes it early, and it never takes focus.", """
                        final Label status = ui.label("");
                        final Button save = ui.button("Save")
                                .onEvent(Button.PRESSED, event -> ui.overlays().toast("Saved.")
                                        .onEvent(Closable.CLOSED, closed -> status.setText("Toast gone")));
                        // Any component can be a toast, shown for as long as you say
                        final Button trade = ui.button("Trade done")
                                .onEvent(Button.PRESSED, event -> ui.overlays().toast(ui.stackPanel()
                                        .setPadding(6)
                                        .setBackground(Paint.solid(0xF0202226))
                                        .add(ui.label("Ben bought 3 lanterns.")), 6000L));
                        panel.add(save).add(trade).add(status);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Text",
                                    Samples.example(ui, "One line", ui.button("Save")
                                            .onEvent(Button.PRESSED, event -> WindowPages.toast(context,
                                                    ui.overlays().toast("Saved.")))),
                                    Samples.example(ui, "Two lines", ui.button("Post a listing")
                                            .onEvent(Button.PRESSED, event -> WindowPages.toast(context,
                                                    ui.overlays().toast("Listing posted.\nBuyers see it in a "
                                                            + "minute.")))),
                                    Samples.example(ui, "Long text wraps", ui.button("Explain")
                                            .onEvent(Button.PRESSED, event -> WindowPages.toast(context,
                                                    ui.overlays().toast("A text toast wraps at a third of the "
                                                            + "host's width, so a long notice like this one "
                                                            + "breaks over a few lines.")))),
                                    Samples.example(ui, "Several stack down", ui.button("Three at once")
                                            .onEvent(Button.PRESSED, event -> {
                                                for (final String name : Samples.PEOPLE.subList(0, 3)) {
                                                    WindowPages.toast(context, ui.overlays().toast(name
                                                            + " joined."));
                                                }
                                            }))),
                            Samples.section(ui, "Custom content",
                                    Samples.example(ui, "Shows for 6 seconds", ui.button("Trade done")
                                            .onEvent(Button.PRESSED, event -> WindowPages.toast(context,
                                                    ui.overlays().toast(WindowPages.card(ui, "Trade done",
                                                            "Ben bought 3 lanterns."), 6000L)))),
                                    Samples.example(ui, "The same content inline", WindowPages.card(ui,
                                            "Trade done", "Ben bought 3 lanterns."))));
                }),
                new Page(SECTION, "Tooltip", "Shown next to the pointer while it rests on a component. A tooltip "
                        + "can be plain text, lines of styled text, or any component.", """
                        final Button refresh = ui.button("Refresh")
                                .setTooltip("Fetch the latest prices.");
                        final Button lamp = ui.button("Brass lamp")
                                .setTooltip(Tooltip.lines(List.of(
                                        List.of(TextSpan.of("Brass lamp").withStyle(TextStyle.DEFAULT.withBold(true))),
                                        List.of(TextSpan.of("Price: "), TextSpan.of("40 coins")))));
                        // A component tooltip is built when it shows, with the factory it is given
                        final Button ben = ui.button("Ben")
                                .setTooltip(Tooltip.of(factory -> factory.stackPanel()
                                        .setPadding(6)
                                        .setBackground(Paint.solid(0xF0202226))
                                        .add(factory.label("Trading since spring."))));
                        panel.add(refresh).add(lamp).add(ben);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final TextStyle gold = TextStyle.DEFAULT.withColor(0xFFF0C050);
                    final TextStyle muted = TextStyle.DEFAULT.withColor(Samples.MUTED);
                    final Tooltip styled = Tooltip.lines(List.of(
                            List.of(TextSpan.of("Brass lamp").withStyle(gold.withBold(true))),
                            List.of(TextSpan.of("Price: ").withStyle(muted), TextSpan.of("40 coins")),
                            List.of(TextSpan.of("Seller: ").withStyle(muted), TextSpan.of("Cleo")
                                    .withStyle(TextStyle.DEFAULT.withItalic(true)))));
                    final Component<?> area = ui.anchorPanel()
                            .setBackground(SLOT)
                            .setTooltip("Containers take tooltips too.")
                            .add(ui.label("Hover here").setAnchor(Anchor.CENTER))
                            .setSize(120, 40);
                    return Samples.column(ui,
                            Samples.section(ui, "Plain text on controls",
                                    Samples.example(ui, "Button", ui.button("Refresh")
                                            .setTooltip("Fetch the latest prices.")),
                                    Samples.example(ui, "Label", ui.label("Fee: 2%")
                                            .setTooltip("Taken from the seller when an item sells.")),
                                    Samples.example(ui, "Check box", ui.checkBox("Auto relist")
                                            .setTooltip("List unsold items again after a day.")),
                                    Samples.example(ui, "Image", ui.image(Samples.icon())
                                            .setTooltip("A stand-in icon.")),
                                    Samples.example(ui, "Text box", ui.textBox()
                                            .setTooltip("Type a name to search for.")
                                            .setWidth(120))),
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Text with a line break", ui.button("Two lines")
                                            .setTooltip("First line.\nSecond line.")),
                                    Samples.example(ui, "Lines of styled spans", ui.button("Brass lamp")
                                            .setTooltip(styled)),
                                    Samples.example(ui, "A component", ui.button("Ben")
                                            .setTooltip(Tooltip.of(factory -> WindowPages.card(factory, "Ben",
                                                    "Trading since spring.")))),
                                    Samples.example(ui, "Tooltip.of with no background", ui.button("Bare")
                                            .setTooltip(Tooltip.of(factory -> factory.label("Draws its own "
                                                    + "background, or none."))))),
                            Samples.section(ui, "Elsewhere",
                                    Samples.example(ui, "On a panel", area),
                                    Samples.example(ui, "On a disabled control", ui.button("Withdraw")
                                            .setEnabled(false)
                                            .setTooltip("Nothing to withdraw yet."))),
                            Samples.note(ui, "Rest the pointer on each control to see its tooltip."));
                }));
    }

    /**
     * A few check boxes for a settings window.
     */
    private static Component<?> options(final ComponentFactory ui) {
        return ui.stackPanel()
                .setSpacing(4)
                .add(ui.checkBox("Show prices").setSelected(true))
                .add(ui.checkBox("Show sold"))
                .add(ui.checkBox("Sound on sale"))
                .setAnchor(Anchor.TOP_LEFT);
    }

    /**
     * Opens a window with a quantity field as a dialog. Buy and Enter close it.
     */
    private static void quantity(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final TextBox amount = ui.textBox().setText("1").setSelectAllOnFocus(true);
        amount.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        final Window window = ui.window("Enter a quantity");
        final Button buy = ui.button("Buy").onEvent(Button.PRESSED, event -> {
            context.log("Bought " + amount.text());
            window.close();
        });
        final Button cancel = ui.button("Cancel").onEvent(Button.PRESSED, event -> window.close());
        amount.onEvent(TextBox.SUBMITTED, event -> buy.press());
        window.add(ui.stackPanel()
                        .setSpacing(6)
                        .setPadding(4)
                        .add(amount)
                        .add(ui.stackPanel(Orientation.HORIZONTAL)
                                .setSpacing(4)
                                .add(buy)
                                .add(cancel)
                                .setAnchor(Anchor.TOP_RIGHT))
                        .setAnchor(Anchor.FILL))
                .setWidth(160);
        WindowPages.logHere(context, window, "dialog", Closable.CLOSE, Closable.CLOSED, TextBox.SUBMITTED);
        ui.overlays().dialog(window);
    }

    /**
     * Opens a message box as a dialog and logs its answers.
     */
    private static void message(final PageContext context, final MessageBox box) {
        WindowPages.logHere(context, box, "dialog", MessageBox.ANSWER, MessageBox.ANSWERED);
        context.ui().overlays().dialog(box);
    }

    /**
     * Logs when a toast closes, since the toast is not in the page's tree.
     */
    private static void toast(final PageContext context, final Overlay toast) {
        toast.onEvent(Closable.CLOSED, event -> context.log(Closable.CLOSED.name() + "   toast"));
    }

    /**
     * A small framed card with an icon, a bold title and a line of text, for custom toasts and tooltips.
     */
    private static Component<?> card(final ComponentFactory ui, final String title, final String text) {
        return ui.stackPanel(Orientation.HORIZONTAL)
                .setSpacing(6)
                .setPadding(6)
                .setBackground(CARD)
                .setBorder(CARD_BORDER)
                .add(ui.image(Samples.icon()).setAnchor(Anchor.MIDDLE_LEFT))
                .add(ui.stackPanel()
                        .setSpacing(2)
                        .add(ui.label(title).setStyle(TextStyle.DEFAULT.withBold(true)))
                        .add(ui.label(text).setStyle(TextStyle.DEFAULT.withColor(Samples.MUTED))));
    }

    /**
     * Logs events from a component in an overlay, which the page's log cannot hear, in the log's own format.
     */
    private static void logHere(final PageContext context, final Component<?> source, final String where,
            final EventType<?>... types) {
        for (final EventType<?> type : types) {
            WindowPages.logOne(context, source, where, type);
        }
    }

    private static <E extends Event> void logOne(final PageContext context, final Component<?> source,
            final String where, final EventType<E> type) {
        source.onEvent(type, event -> {
            final StringBuilder line = new StringBuilder(type.name()).append("   ")
                    .append(event.target().getClass().getSimpleName().replace("Impl", ""))
                    .append(" (").append(where).append(")");
            if (event instanceof final ValueRequestEvent<?, ?> request) {
                line.append("   ? ").append(request.value());
            } else if (event instanceof final ValueEvent<?, ?> report) {
                line.append("   ").append(report.previous()).append(" -> ").append(report.value());
            }
            context.log(line.toString());
        });
    }
}
