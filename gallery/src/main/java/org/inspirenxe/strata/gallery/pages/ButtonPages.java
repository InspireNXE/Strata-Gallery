package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.attribute.item.SingleSelection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.RadioGroup;
import org.inspirenxe.strata.gallery.Page;

import java.util.List;

/**
 * The Buttons section: buttons and the controls that flip or cycle a value.
 */
final class ButtonPages {
    private static final String SECTION = "Buttons";

    private ButtonPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Button", "Runs an action when clicked, or on Enter or Space while focused. A "
                        + "button can carry an icon, or look like a link.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Caption", ui.button("Save")),
                                    Samples.example(ui, "Icon and caption", ui.button("Refresh").setIcon(Samples.icon())),
                                    Samples.example(ui, "Icon only", ui.button().setIcon(Samples.icon())),
                                    Samples.example(ui, "Link", ui.button("Terms of sale").setLink(true))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", ui.button("Delete").setEnabled(false)),
                                    Samples.example(ui, "Disabled link", ui.button("Seller gone").setLink(true)
                                            .setEnabled(false)),
                                    Samples.example(ui, "Wide", ui.button("Buy all").setWidth(160))),
                            Samples.section(ui, "Refusing a press",
                                    Samples.example(ui, "Canceled in a Button.PRESS handler", ui.button("Locked")
                                            .onEvent(Button.PRESS, event -> event.cancel()))));
                }),
                new Page(SECTION, "Cycle button", "Steps through a list of values: a left click goes forward, a right "
                        + "click or Shift goes back. The caption shows the current value.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "With a caption", ui.<String>cycleButton("Order")
                                            .setItems(List.of("Newest", "Cheapest", "Ending soon"))),
                                    Samples.example(ui, "Value only", ui.<Integer>cycleButton("", count -> count
                                            + " per page")
                                            .setItems(List.of(10, 25, 50)))),
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Disabled", ui.<String>cycleButton("Mode")
                                            .setItems(List.of("Easy", "Hard"))
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Check box", "An on and off choice with a caption. Clicking the caption toggles it "
                        + "too.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Off", ui.checkBox("Show prices")),
                                    Samples.example(ui, "On", ui.checkBox("Show sold").setSelected(true)),
                                    Samples.example(ui, "Disabled", ui.checkBox("Admin only").setEnabled(false)),
                                    Samples.example(ui, "Disabled and on", ui.checkBox("Required").setSelected(true)
                                            .setEnabled(false))));
                }),
                new Page(SECTION, "Radio button", "One choice out of a few. A radio group keeps one of its buttons "
                        + "selected and reports the change; it is not a component, so its events are logged here by "
                        + "hand.", context -> {
                    final ComponentFactory ui = context.ui();
                    final RadioGroup<String> sort = ui.radioGroup();
                    sort.add("Name", "name");
                    sort.add("Price", "price");
                    sort.add("Seller", "seller").setEnabled(false);
                    sort.selectItem("price");
                    sort.onEvent(SingleSelection.SELECTED, event -> context.log("selection.selected   RadioGroup   "
                            + sort.selected().orElse("none")));
                    return Samples.column(ui,
                            Samples.section(ui, "A group",
                                    Samples.example(ui, "Sort by (Seller is disabled)", Samples.row(ui, sort.buttons()))));
                }),
                new Page(SECTION, "Toggle switch", "An on and off setting that applies at once, drawn as a track with "
                        + "a knob.", context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "States",
                                    Samples.example(ui, "Off", ui.toggleSwitch("Hide sold")),
                                    Samples.example(ui, "On", ui.toggleSwitch("Only mine").setSelected(true)),
                                    Samples.example(ui, "Disabled", ui.toggleSwitch("Locked").setEnabled(false))));
                }));
    }
}
