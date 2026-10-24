/*
 * Copyright (c) InspireNXE <https://github.com/InspireNXE>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inspirenxe.strata.gallery.pages;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.appearance.Appearance;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.Tooltip;
import org.inspirenxe.strata.api.control.attribute.value.Toggle;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.display.Avatar;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.InfoBar;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextOverflow;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.text.TextStylePatch;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.util.List;
import java.util.Map;

/**
 * The Display section: controls that show something and take no input.
 */
final class DisplayPages {
    private static final String SECTION = "Display";
    private static final String LONG_TEXT = "Ana listed three crates of apples at the morning market, and Ben bought "
            + "two of them before noon.";

    static {
        // A member shows with their picture, name and role wherever a control has no cell factory for them
        Strata.appearances().register(Member.class, Appearance.<Member>builder()
                .icon(member -> Samples.icon(member.color()))
                .name(member -> Text.of(member.name()))
                .tooltip(member -> Tooltip.text(member.name() + ", " + member.role()))
                .build());
    }

    private DisplayPages() {
    }

    /**
     * A member of a market, shown through its appearance.
     *
     * @param name the member's name
     * @param role what the member does
     * @param color the color of the member's stand-in picture
     */
    record Member(String name, String role, int color) {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Label", "Text the user reads but does not edit. A label can wrap, cut itself "
                        + "short with an ellipsis, mix styles in one text, take a text size and a font, and draw "
                        + "larger or smaller.", """
                        final Label title = ui.label("Fresh pears")
                                .setStyle(TextStyle.DEFAULT.withBold(true).withSize(14f));
                        // A size for everything in a panel, and a font the platform knows by id
                        panel.setTextStyle(TextStylePatch.of().size(12f).font(FontId.of("app", "ubuntu")));
                        final Label note = ui.label("Ana listed three crates of apples at the morning market.")
                                .setWrap(true)
                                .setWidth(180);
                        final Label price = ui.label(Text.of("125.50").color(0xFFE8C547).bold()
                                .append(" per crate from ")
                                .append(Text.of("Cleo").underline()));
                        final Label seller = ui.label("Sold by a seller with a very long name")
                                .setOverflow(TextOverflow.ELLIPSIS)
                                .setWidth(120);
                        note.setText("Ben bought two crates before noon.");
                        panel.add(title).add(note).add(price).add(seller);

                        // The letter after _ is a mnemonic: Alt+N focuses the box after the label
                        final Label caption = ui.label("_Name").setMnemonicParsing(true);
                        panel.add(caption).add(ui.textBox());
                        """, context -> {
                    final ComponentFactory ui = context.ui();
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
                            Samples.section(ui, "Sizes",
                                    Samples.example(ui, "Size 7", sized(ui, 7f, "Small print")),
                                    Samples.example(ui, "The default size", ui.label("Fresh pears")),
                                    Samples.example(ui, "Size 14", sized(ui, 14f, "Fresh pears")),
                                    Samples.example(ui, "Size 20", sized(ui, 20f, "Title"))),
                            Samples.section(ui, "Styled text and scale",
                                    Samples.example(ui, "Styled text", ui.label(Text.of("125.50").color(0xFFE8C547).bold()
                                            .append(Text.of(" per crate from ").color(Samples.MUTED))
                                            .append(Text.of("Cleo").underline()))),
                                    Samples.example(ui, "Scale 0.8", ui.label("Small print").setTextScale(0.8f)),
                                    Samples.example(ui, "Scale 1.5", ui.label("Larger").setTextScale(1.5f)),
                                    Samples.example(ui, "Scale 2", ui.label("Title").setTextScale(2f))),
                            Samples.section(ui, "Pictures in text",
                                    Samples.example(ui, "A visual at its size", ui.label(Text.concat(
                                            Text.visual(Samples.icon()), Text.of(" Fresh pears")))),
                                    Samples.example(ui, "A sprite as tall as the text", ui.label(Text.of("Costs 12 ")
                                            .append(Text.sprite(SpriteId.of("gallery", "coin")))
                                            .append(" each"))),
                                    Samples.example(ui, "Wrapped", ui.label(Text.concat(Text.visual(Samples.icon()),
                                                    Text.of(" Ana listed three crates of "),
                                                    Text.visual(Samples.icon(0xFF5BB974)),
                                                    Text.of(" apples at the morning market.")))
                                            .setWrap(true)
                                            .setWidth(180))),
                            Samples.section(ui, "A mnemonic",
                                    Samples.example(ui, "Alt+N focuses the box after the label; Alt shows the "
                                            + "underline", Samples.row(ui,
                                            ui.label("_Name").setMnemonicParsing(true),
                                            ui.textBox().setWidth(140)).setSpacing(6))),
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
                        + "colors.", """
                        // The adapter finds the sprite; Swing reads assets/shop/sprites/icon/pear.png
                        final Image pear = ui.image(SpriteId.of("shop", "icon/pear"))
                                .setImageSize(32, 32)
                                .setTooltip("Pears");
                        // FIT keeps the aspect ratio inside the bounds; the tint multiplies the colors
                        final Image banner = ui.image(SpriteId.of("shop", "banner"))
                                .setScaling(Image.Scaling.FIT)
                                .setTint(0x80FFFFFF)
                                .setSize(80, 32);
                        panel.add(pear).add(banner);

                        // COVER fills the slot and cuts what overflows; NONE keeps the image's own size
                        final Image cover = ui.image(bannerVisual).setScaling(Image.Scaling.COVER).setSize(48, 48);
                        final Image none = ui.image(bannerVisual).setScaling(Image.Scaling.NONE).setSize(48, 48);
                        """, context -> {
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
                            Samples.section(ui, "A wide banner in a 48 by 48 slot",
                                    Samples.example(ui, "STRETCH", DisplayPages.framed(ui, ui.image(Samples.banner())
                                            .setSize(48, 48))),
                                    Samples.example(ui, "FIT", DisplayPages.framed(ui, ui.image(Samples.banner())
                                            .setScaling(Image.Scaling.FIT)
                                            .setSize(48, 48))),
                                    Samples.example(ui, "COVER", DisplayPages.framed(ui, ui.image(Samples.banner())
                                            .setScaling(Image.Scaling.COVER)
                                            .setSize(48, 48))),
                                    Samples.example(ui, "NONE", DisplayPages.framed(ui, ui.image(Samples.banner())
                                            .setScaling(Image.Scaling.NONE)
                                            .setSize(48, 48)))),
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
                new Page(SECTION, "Avatar", "Who someone or something is, in a small square: a picture, or else "
                        + "initials from a name, or else a placeholder, with an optional badge for a count or a status. "
                        + "It takes no input and fires no events.", """
                        // A value with an appearance shows its icon and its tooltip; without an icon, its initials
                        final Avatar seller = ui.avatar(member).setAvatarSize(32);
                        // Initials come from the first and last words of the name
                        final Avatar ana = ui.avatar().setName("Ana Lima").setBadge(3);
                        // A picture of your own, and a status dot in place of the count
                        final Avatar team = ui.avatar()
                                .setSprite(SpriteId.of("shop", "team"))
                                .setGroup(true)
                                .setBadgeVisual(onlineDot);
                        panel.add(seller).add(ana).add(team);

                        // A theme rounds every avatar with avatar.radius; half the size makes them round
                        final ThemeRef round = ThemeRef.platform().override(Avatar.STYLE_RADIUS, 100f);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final CheckBox round = ui.checkBox("Round: avatar.radius half the size")
                            .setEnabled(context.canMotion());
                    round.onEvent(Toggle.TOGGLED, event -> context.setMetrics(event.value()
                            ? Map.of(Avatar.STYLE_RADIUS, 100f)
                            : Map.of()));
                    final Member fay = new Member("Fay Ito", "seller", 0xFF43A047);
                    return Samples.column(ui,
                            Samples.section(ui, "Corners",
                                    Samples.example(ui, "Square by default; a theme with avatar.radius set rounds "
                                            + "every avatar, and half the size makes it round", round)),
                            Samples.section(ui, "Pictures",
                                    Samples.example(ui, "A visual", ui.avatar().setVisual(Samples.icon(0xFFE0A030))),
                                    Samples.example(ui, "A value with an appearance", ui.avatar(fay)),
                                    Samples.example(ui, "A group, no picture or name", ui.avatar().setGroup(true))),
                            Samples.section(ui, "Initials",
                                    Samples.example(ui, "Ana Lima", ui.avatar().setName("Ana Lima")),
                                    Samples.example(ui, "Ben", ui.avatar().setName("Ben")),
                                    Samples.example(ui, "Cleo Dev", ui.avatar().setName("Cleo Dev")),
                                    Samples.example(ui, "Set by hand", ui.avatar().setInitials("Q"))),
                            Samples.section(ui, "Badges",
                                    Samples.example(ui, "A count of 3", ui.avatar().setName("Ana Lima").setBadge(3)),
                                    Samples.example(ui, "A count of 120", ui.avatar().setName("Ben").setBadge(120)),
                                    Samples.example(ui, "A status dot", ui.avatar(fay)
                                            .setBadgeVisual(Samples.icon(0xFF43A047)))),
                            Samples.section(ui, "Sizes",
                                    Samples.example(ui, "16 units", ui.avatar().setName("Cleo Dev").setAvatarSize(16)),
                                    Samples.example(ui, "24 units", ui.avatar().setName("Cleo Dev").setBadge(5)),
                                    Samples.example(ui, "48 units", ui.avatar().setName("Cleo Dev").setBadge(5)
                                            .setAvatarSize(48)),
                                    Samples.example(ui, "48 units, a picture", ui.avatar(fay).setAvatarSize(48)
                                            .setBadge(12))));
                }),
                new Page(SECTION, "Info bar", "A strip in the layout that tells the user something about the "
                        + "screen, with a severity, a title, a message, an optional action and a close button. It "
                        + "stays until it is closed; a closed bar takes no space.", """
                        final InfoBar failed = ui.infoBar(InfoBar.Severity.ERROR, "Not saved",
                                        "The server did not answer.")
                                .setAction("Try again", this::save);
                        // Asked before the user closes it; cancel to keep it open
                        failed.onEvent(Closable.CLOSE, event -> {
                            if (this.saving) {
                                event.cancel();
                            }
                        });
                        panel.add(failed);

                        // Later, from code: shows it again and reports OPENED
                        failed.open();
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    final List<InfoBar> bars = List.of(
                            ui.infoBar(InfoBar.Severity.INFO, "Market day", "Stalls open at nine."),
                            ui.infoBar(InfoBar.Severity.SUCCESS, "Saved", "Your prices are up to date."),
                            ui.infoBar(InfoBar.Severity.WARNING, "Low stock", "Only three crates of pears are left."),
                            ui.infoBar(InfoBar.Severity.ERROR, "Not saved", "The server did not answer.")
                                    .setAction("Try again", () -> context.log("Trying again")),
                            ui.infoBar(InfoBar.Severity.INFO, "A long message", DisplayPages.LONG_TEXT + " "
                                    + DisplayPages.LONG_TEXT),
                            ui.infoBar(InfoBar.Severity.WARNING, "Not closable", "This one closes only from code.")
                                    .setClosable(false));
                    final Button reopen = ui.button("Open them again")
                            .onEvent(Button.PRESSED, event -> bars.forEach(InfoBar::open));
                    final Button closeAll = ui.button("Close them from code")
                            .onEvent(Button.PRESSED, event -> bars.forEach(InfoBar::close));
                    final StackPanel column = ui.stackPanel().setSpacing(4).setWidth(420);
                    bars.forEach(column::add);
                    return Samples.column(ui,
                            Samples.section(ui, "Severities, a long message, an action and one that is not closable",
                                    column),
                            Samples.row(ui, reopen, closeAll));
                }),
                new Page(SECTION, "Progress bar", "A bar filled from the left to show how far a value is through its "
                        + "range, with an optional caption. When the length of the work is unknown, it shows a moving "
                        + "segment instead.", """
                        final ProgressBar upload = ui.progressBar()
                                .setRange(0, 64)
                                .setValue(40)
                                .setText("40/64")
                                .setWidth(160);
                        // While the length of the work is unknown, a segment moves back and forth
                        final ProgressBar connecting = ui.progressBar()
                                .setIndeterminate(true)
                                .setText("Connecting")
                                .setWidth(160);
                        panel.add(upload).add(connecting);

                        // Later, as the work moves on
                        upload.setValue(48).setText("48/64");
                        """, context -> {
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
                            Samples.section(ui, "Vertical",
                                    Samples.example(ui, "A health bar fills from the bottom", ui.progressBar()
                                            .setOrientation(Orientation.VERTICAL)
                                            .setValue(0.7)
                                            .setHeight(110)),
                                    Samples.example(ui, "With a caption", ui.progressBar()
                                            .setOrientation(Orientation.VERTICAL)
                                            .setValue(0.4)
                                            .setText("40%")
                                            .setHeight(110)),
                                    Samples.example(ui, "Indeterminate", ui.progressBar()
                                            .setOrientation(Orientation.VERTICAL)
                                            .setIndeterminate(true)
                                            .setHeight(110)),
                                    Samples.example(ui, "The fill eases to a new value with motion on",
                                            DisplayPages.easing(ui, context))),
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
                        + "under way, with an optional caption above. It takes no input and fires no events.", """
                        final BusyIndicator loading = ui.busyIndicator("Loading offers")
                                .setWidth(160);
                        panel.add(loading);

                        // Hide it when the work is done; a hidden component takes no space
                        loading.setVisible(false);
                        """, context -> {
                    final ComponentFactory ui = context.ui();
                    return Samples.column(ui,
                            Samples.section(ui, "Kinds",
                                    Samples.example(ui, "Dots only", ui.busyIndicator()),
                                    Samples.example(ui, "With a caption", ui.busyIndicator("Loading offers")),
                                    Samples.example(ui, "Centered in 160 units", ui.busyIndicator("Joining")
                                            .setWidth(160))));
                }),
                new Page(SECTION, "Separator", "A thin line across or down a panel. A horizontal separator stretches "
                        + "across its slot and is one unit thick unless told otherwise.", """
                        panel.add(ui.label("Apples"))
                                .add(ui.separator().setWidth(160))
                                .add(ui.label("Oranges"))
                                .add(ui.separator().setThickness(3).setWidth(160));

                        final StackPanel names = ui.stackPanel(Orientation.HORIZONTAL)
                                .setSpacing(4)
                                .add(ui.label("Ana"))
                                .add(ui.separator(Orientation.VERTICAL).setHeight(12))
                                .add(ui.label("Ben"));
                        panel.add(names);
                        """, context -> {
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
     * A label in a panel that sets a text size, so the theme's color and the rest of its style stay.
     */
    private static Component<?> sized(final ComponentFactory ui, final float size, final String text) {
        return ui.stackPanel().setTextStyle(TextStylePatch.of().size(size)).add(ui.label(text));
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

    /**
     * A progress bar with buttons that set it, and the motion check box, for the ease of its fill.
     */
    private static Component<?> easing(final ComponentFactory ui, final PageContext context) {
        final ProgressBar bar = ui.progressBar().setValue(0.2).setWidth(160);
        return ui.stackPanel().setSpacing(4)
                .add(bar)
                .add(Samples.row(ui,
                        ui.button("0").onEvent(Button.PRESSED, event -> bar.setValue(0)),
                        ui.button("50%").onEvent(Button.PRESSED, event -> bar.setValue(0.5)),
                        ui.button("100%").onEvent(Button.PRESSED, event -> bar.setValue(1)),
                        Samples.motion(ui, context)).setSpacing(4));
    }
}
