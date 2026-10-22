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
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.SelectionMode;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.event.input.DragEvent;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.event.input.TransferEvent;
import org.inspirenxe.strata.api.extension.CustomComponent;
import org.inspirenxe.strata.api.extension.MeasureContext;
import org.inspirenxe.strata.api.extension.Painter;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.text.TextSpan;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.transfer.DataFormat;
import org.inspirenxe.strata.api.transfer.DropEffect;
import org.inspirenxe.strata.api.transfer.Transfer;
import org.inspirenxe.strata.gallery.Page;
import org.inspirenxe.strata.gallery.PageContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The drag and drop page of the Input section: rows moved between lists, a tile and a bin written as custom
 * components, text dragged between boxes, edge scrolling, a drop zone for files from the desktop, and the clipboard
 * with a format of its own.
 */
final class DragPages {
    private static final String SECTION = "Input";
    /**
     * Rows of a list, as objects. A local format never leaves the process, so another program gets the text.
     */
    private static final DataFormat<Rows> ROWS = DataFormat.local("gallery:rows", Rows.class);
    private static final DataFormat<Chip> CHIP = DataFormat.local("gallery:chip", Chip.class);
    private static final Paint ZONE = Paint.solid(0xFF3A3D43);
    private static final Border ZONE_BORDER = Border.of(0xFF5A5E66, 1f);

    private DragPages() {
    }

    static List<Page> pages() {
        return List.of(
                new Page(SECTION, "Drag and drop", "Drags carry a transfer: typed data, plus the effects a target "
                        + "may take. Rows move between lists, a tile drags a value with its own image, a bin takes "
                        + "only text, boxes trade text, a panel scrolls itself near its edge, and files from the "
                        + "desktop can be dropped on the zone. Hold Control to copy. The log shows every event with "
                        + "its effect, source and pointer.", """
                        // A local format holds objects in this process; text goes to other programs too
                        final DataFormat<Rows> rows = DataFormat.local("gallery:rows", Rows.class);
                        left.setDragTransfer(selected -> Transfer.builder()
                                .add(rows, new Rows(selected))
                                .add(DataFormat.TEXT, String.join("\\n", selected))
                                .build());

                        // The target accepts in DRAG_OVER and takes the rows in DROP
                        right.onEvent(InputEvents.DRAG_OVER, event -> {
                            if (event.transfer().has(rows)) {
                                event.accept();
                            }
                        });
                        right.onEvent(InputEvents.DROP, event -> event.transfer().get(rows)
                                .ifPresent(moved -> right.setItems(concat(right.items(), moved.names()))));
                        // The source removes what moved once the drop is done
                        left.onEvent(InputEvents.TRANSFER_DONE, event -> {
                            if (event.effect().orElse(null) == DropEffect.MOVE) {
                                left.setItems(without(left.items(), moved));
                            }
                        });

                        // A component starts a drag from DRAG_START, or from onDragStart when it is a CustomComponent
                        tile.onEvent(InputEvents.DRAG_START, event -> event.startTransfer(
                                Transfer.text("Ana"), Set.of(DropEffect.COPY),
                                factory -> factory.label("Ana")));
                        """, context -> DragPages.content(context))
        );
    }

    private static Component<?> content(final PageContext context) {
        final ComponentFactory ui = context.ui();
        final List<String> leftRows = new ArrayList<>(List.of("Ana", "Ben", "Cleo", "Dev"));
        final List<String> rightRows = new ArrayList<>(List.of("Eli", "Fay"));
        final ListView<String> left = DragPages.rows(ui, leftRows);
        final ListView<String> right = DragPages.rows(ui, rightRows);
        final Object[] target = {null};
        DragPages.exchange(left, right, target);
        DragPages.exchange(right, left, target);
        TransferLog.watch(context, left, "Left list");
        TransferLog.watch(context, right, "Right list");

        final Tile ana = new Tile(ui, "Ana", 0xFF4C86E0);
        final Tile ben = new Tile(ui, "Ben", 0xFF43A047);
        final Bin bin = new Bin(ui);
        TransferLog.watch(context, ana, "Tile Ana");
        TransferLog.watch(context, ben, "Tile Ben");
        TransferLog.watch(context, bin, "Bin");

        final TextBox from = ui.textBox().setText("Ana, Ben and Cleo").setWidth(170);
        final TextBox to = ui.textBox().setWidth(170);
        TransferLog.watch(context, from, "Text box one");
        TransferLog.watch(context, to, "Text box two");

        final StackPanel tall = ui.stackPanel().setSpacing(2).setPadding(4);
        for (int i = 1; i <= 40; i++) {
            tall.add(ui.label("Row " + i + ", " + Samples.PEOPLE.get(i % Samples.PEOPLE.size())));
        }
        final Component<?> scroller = ui.scrollPanel()
                .add(tall)
                .setBackground(ZONE)
                .setSize(170, 110);

        final Label names = ui.label("Nothing dropped yet");
        final AnchorPanel zone = ui.anchorPanel()
                .setBackground(ZONE)
                .setBorder(ZONE_BORDER)
                .add(ui.label("Drop files here").setAnchor(Anchor.CENTER))
                .setSize(170, 60);
        zone.onEvent(InputEvents.DRAG_OVER, event -> {
            if (event.transfer().has(DataFormat.FILES)) {
                event.accept();
            } else {
                event.reject();
            }
        });
        zone.onEvent(InputEvents.DROP, event -> event.transfer().get(DataFormat.FILES).ifPresent(files ->
                names.setText(files.size() + " file(s): " + files.stream().map(Path::getFileName)
                        .map(Path::toString).collect(Collectors.joining(", ")))));
        TransferLog.watch(context, zone, "File zone");

        final Label pasted = ui.label("Copy, then Paste");
        final Button copy = ui.button("Copy the left rows").onEvent(Button.PRESSED, event -> ui.setClipboard(
                DragPages.transfer(List.copyOf(left.items()))));
        final Button paste = ui.button("Paste").onEvent(Button.PRESSED, event -> pasted.setText(ui.clipboard()
                .map(transfer -> "Formats back: " + transfer.formats().stream().map(DataFormat::name).toList()
                        + (transfer.has(ROWS) ? ", the rows arrive as objects" : ", text only"))
                .orElse("The clipboard is empty")));

        return Samples.column(ui,
                Samples.section(ui, "Lists that trade rows",
                        Samples.example(ui, "Drag rows across to move them, with Control to copy; each list also "
                                + "reorders its own", left),
                        Samples.example(ui, "A drop adds the rows, and TRANSFER_DONE removes them here", right)),
                Samples.section(ui, "A tile and a bin written as custom components",
                        Samples.example(ui, "Drag a tile: it carries text and a value, and an image", Samples.row(ui,
                                ana, ben).setSpacing(8)),
                        Samples.example(ui, "The bin takes only text; a file gets the no-drop cursor", bin)),
                Samples.section(ui, "Text between boxes",
                        Samples.example(ui, "Select, then drag out; Control copies", from),
                        Samples.example(ui, "Drop the text in", to)),
                Samples.section(ui, "Edge scrolling and files",
                        Samples.example(ui, "Hold a drag within 16 units of the top or bottom edge", scroller),
                        Samples.example(ui, "Files dropped from the desktop", ui.stackPanel().setSpacing(4)
                                .add(zone)
                                .add(names))),
                Samples.section(ui, "The clipboard",
                        Samples.example(ui, "Copy puts the rows as text and as a local format; Paste shows what "
                                + "came back. Pasting in a text editor gets the text", Samples.row(ui, copy, paste)
                                .setSpacing(6)),
                        Samples.example(ui, "What Paste found", pasted)));
    }

    /**
     * A multiple selection list whose rows are dragged as the local format and as text.
     */
    private static ListView<String> rows(final ComponentFactory ui, final List<String> items) {
        final ListView<String> list = ui.<String>listView()
                .setItems(items)
                .setSelectionMode(SelectionMode.MULTIPLE)
                .setDragTransfer(DragPages::transfer);
        list.setSize(130, 130);
        return list;
    }

    /**
     * The transfer of dragged rows: the rows themselves, and their names for programs that take text.
     */
    private static Transfer transfer(final List<String> rows) {
        return Transfer.builder()
                .add(ROWS, new Rows(List.copyOf(rows)))
                .add(DataFormat.TEXT, String.join("\n", rows))
                .build();
    }

    /**
     * Lets a list take the rows of another and give its own rows away. The list reorders its own rows itself; this
     * handles rows from the other, and removes rows that were moved to it.
     */
    private static void exchange(final ListView<String> self, final ListView<String> other, final Object[] target) {
        self.onEvent(InputEvents.DRAG_OVER, event -> {
            if (event.transfer().has(ROWS) && event.source().filter(source -> source == other).isPresent()) {
                event.accept();
            }
        });
        self.onEvent(InputEvents.DROP, event -> {
            if (event.source().filter(source -> source == other).isEmpty()) {
                return;
            }
            event.transfer().get(ROWS).ifPresent(rows -> {
                final List<String> items = new ArrayList<>(self.items());
                items.addAll(rows.names());
                self.setItems(items);
                target[0] = self;
            });
        });
        self.onEvent(InputEvents.TRANSFER_DONE, event -> {
            // Only a move to the other list takes rows away; a move inside this one was the list's own
            if (target[0] == other && event.effect().orElse(null) == DropEffect.MOVE) {
                event.transfer().get(ROWS).ifPresent(rows -> {
                    final List<String> items = new ArrayList<>(self.items());
                    items.removeAll(rows.names());
                    self.setItems(items);
                });
            }
            target[0] = null;
        });
    }

    /**
     * The rows of a list in a drag.
     *
     * @param names the names
     */
    private record Rows(List<String> names) {
    }

    /**
     * A value a tile carries.
     *
     * @param name the name
     * @param color the color
     */
    private record Chip(String name, int color) {
    }

    /**
     * A colored tile that starts a drag carrying its name as text, its value, and an image of itself.
     */
    private static final class Tile extends CustomComponent<Tile> {
        private final String name;
        private final int color;

        Tile(final ComponentFactory ui, final String name, final int color) {
            super(ui);
            this.name = name;
            this.color = color;
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth,
                               final float availableHeight) {
            return new Size(48, 32);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, this.color);
            DragPages.centered(painter, this.environment().text().width(TextSpan.of(this.name)),
                    this.environment().text().lineHeight(TextStyle.DEFAULT), bounds, this.name, TextStyle.DEFAULT);
        }

        @Override
        protected void onDragStart(final DragEvent event) {
            event.startTransfer(Transfer.builder()
                            .add(DataFormat.TEXT, this.name)
                            .add(CHIP, new Chip(this.name, this.color))
                            .build(),
                    Set.of(DropEffect.COPY, DropEffect.MOVE),
                    factory -> factory.stackPanel()
                            .setPadding(4)
                            .setBackground(Paint.solid(this.color))
                            .add(factory.label(this.name)));
        }
    }

    /**
     * A bin that takes anything with text and refuses the rest. It counts what it got and keeps the last text.
     */
    private static final class Bin extends CustomComponent<Bin> {
        private int count;
        private String last = "";
        private boolean over;

        Bin(final ComponentFactory ui) {
            super(ui);
        }

        @Override
        protected Size measure(final MeasureContext context, final float availableWidth,
                               final float availableHeight) {
            return new Size(150, 44);
        }

        @Override
        protected void paint(final Painter painter, final Rect bounds) {
            painter.fill(bounds, this.over ? 0xFF4A6A52 : 0xFF3A3D43);
            final String text = this.count == 0 ? "Bin: drop text" : "Bin: " + this.count + ", last " + this.last;
            DragPages.centered(painter, this.environment().text().width(TextSpan.of(text)),
                    this.environment().text().lineHeight(TextStyle.DEFAULT), bounds, text, TextStyle.DEFAULT);
        }

        @Override
        protected void onDragEnter(final TransferEvent event) {
            this.over = true;
            this.repaint();
        }

        @Override
        protected void onDragOver(final TransferEvent event) {
            if (event.transfer().has(DataFormat.TEXT)) {
                event.accept();
            } else {
                event.reject();
            }
        }

        @Override
        protected void onDragExit(final TransferEvent event) {
            this.over = false;
            this.repaint();
        }

        @Override
        protected void onDrop(final TransferEvent event) {
            this.count++;
            this.last = event.transfer().get(DataFormat.TEXT).orElse("");
            this.over = false;
            this.repaint();
        }
    }

    /**
     * Draws text in the middle of a rectangle.
     */
    static void centered(final Painter painter, final float width, final float height, final Rect bounds,
                                 final String text, final TextStyle style) {
        painter.text(new TextSpan(text, style), bounds.x() + (bounds.width() - width) / 2f,
                bounds.y() + (bounds.height() - height) / 2f);
    }
}
