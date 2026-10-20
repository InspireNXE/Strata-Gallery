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
package org.inspirenxe.strata.gallery.theme;

import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.RadioButton;
import org.inspirenxe.strata.api.control.button.ToggleSwitch;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.GridView;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.collection.TableView;
import org.inspirenxe.strata.api.control.display.BusyIndicator;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.control.display.Separator;
import org.inspirenxe.strata.api.control.field.ColorPicker;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.pane.Expander;
import org.inspirenxe.strata.api.control.pane.ScrollBar;
import org.inspirenxe.strata.api.control.pane.TabBar;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.overlay.Overlays;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The gallery's copy of Strata-Minecraft's {@code MinecraftThemes.vanilla()}, drawn from a client jar and a resource
 * pack read at runtime. Every key names the same sprite as in game. The gallery cannot use Strata-Minecraft itself,
 * which needs the game's classes, so keep the two in step.
 *
 * <p>The game slices every sprite by its {@code .png.mcmeta}, whatever paint the theme names, so this reads the
 * {@code .mcmeta} that comes with each image and builds the paint from it: a nine-slice of the declared size,
 * tiled unless the {@code .mcmeta} stretches it, a tiled sprite, or a plain sprite when the image has no slicing.
 * Sized paints draw a pack's larger images with the game's borders. The borders below, those of 26.3, are used
 * only when a nine-slice {@code .mcmeta} cannot be read.</p>
 */
final class MinecraftLook {
    private static final Insets BUTTON_BORDER = Insets.of(3f);
    private static final Insets BUTTON_DISABLED_BORDER = Insets.of(1f);
    private static final Insets FIELD_BORDER = Insets.of(1f);
    private static final Insets SLIDER_BORDER = Insets.of(1f);
    private static final Insets SLIDER_HANDLE_BORDER = new Insets(2f, 2f, 2f, 3f);
    private static final Insets SCROLLER_BORDER = Insets.of(1f);
    private static final Insets TAB_BORDER = new Insets(2f, 2f, 2f, 0f);
    private static final Insets POPUP_BORDER = Insets.of(6f);

    private static final SpriteId BACKDROP = SpriteId.of(MinecraftAssets.GENERATED, "minecraft/backdrop");
    private static final String TEXTURES = "assets/minecraft/textures/gui/";
    private static final int BACKDROP_WIDTH = 960;
    private static final int BACKDROP_HEIGHT = 540;
    // Vanilla blurs the panorama; shrinking it this much and smoothing it back up is close
    private static final int BLUR = 16;

    private final MinecraftAssets assets;
    private final Set<String> notes;

    private MinecraftLook(final MinecraftAssets assets) {
        this.assets = assets;
        this.notes = new LinkedHashSet<>();
    }

    /**
     * Builds the vanilla theme and a frame like a vanilla menu screen from the assets.
     *
     * @param assets the client jar and pack to read
     * @return the theme, the frame, and notes on anything the gallery shows differently from the game
     */
    static Result build(final MinecraftAssets assets) {
        final MinecraftLook look = new MinecraftLook(assets);
        final Theme theme = look.theme();
        final Chrome chrome = look.chrome();
        return new Result(theme, chrome, List.copyOf(look.notes));
    }

    /**
     * Mirrors {@code MinecraftThemes.vanilla()} key for key. Only the container screen slot keys are left out: they
     * belong to Strata-Minecraft's {@code MenuPanel}, which the gallery does not have.
     */
    private Theme theme() {
        final TextStyle text = TextStyle.DEFAULT.withColor(0xFFFFFFFF).withShadow(true);
        return Theme.builder("minecraft:vanilla")
                .paint(Button.STYLE_NORMAL, this.paint("widget/button", BUTTON_BORDER))
                .paint(Button.STYLE_HOVERED, this.paint("widget/button_highlighted", BUTTON_BORDER))
                // Vanilla has no pressed sprite; the highlighted one darkened, as MinecraftThemes does
                .paint(Button.STYLE_PRESSED, tinted(this.paint("widget/button_highlighted", BUTTON_BORDER), 0xFFB0B0B0))
                .paint(Button.STYLE_DISABLED, this.paint("widget/button_disabled", BUTTON_DISABLED_BORDER))
                .textStyle(Button.STYLE_TEXT, text)
                .textStyle(Button.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .textStyle(Label.STYLE_TEXT, text)
                .paint(CheckBox.STYLE_NORMAL, this.paint("widget/checkbox", null))
                .paint(CheckBox.STYLE_HOVERED, this.paint("widget/checkbox_highlighted", null))
                .paint(CheckBox.STYLE_CHECKED, this.paint("widget/checkbox_selected", null))
                .paint(CheckBox.STYLE_CHECKED_HOVERED, this.paint("widget/checkbox_selected_highlighted", null))
                // Vanilla has no mixed sprite; a square in the text color over the empty box
                .paint(CheckBox.STYLE_MARK_MIXED, Paint.solid(0xFFE0E0E0))
                .paint(CheckBox.STYLE_MARK_MIXED_DISABLED, Paint.solid(0xFFA0A0A0))
                .textStyle(CheckBox.STYLE_TEXT, text)
                .textStyle(CheckBox.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(Slider.STYLE_TRACK, this.paint("widget/slider", SLIDER_BORDER))
                .paint(Slider.STYLE_TRACK_FOCUSED, this.paint("widget/slider_highlighted", SLIDER_BORDER))
                .paint(Slider.STYLE_THUMB, this.paint("widget/slider_handle", SLIDER_HANDLE_BORDER))
                .paint(Slider.STYLE_THUMB_HOVERED, this.paint("widget/slider_handle_highlighted", SLIDER_HANDLE_BORDER))
                .textStyle(Slider.STYLE_TEXT, text)
                .textStyle(Slider.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(TextBox.STYLE_NORMAL, this.paint("widget/text_field", FIELD_BORDER))
                .paint(TextBox.STYLE_FOCUSED, this.paint("widget/text_field_highlighted", FIELD_BORDER))
                .paint(TextBox.STYLE_DISABLED, this.paint("widget/text_field", FIELD_BORDER))
                .paint(TextBox.STYLE_SELECTION, Paint.solid(0x800000FF))
                .paint(TextBox.STYLE_CARET, Paint.solid(0xFFD0D0D0))
                .textStyle(TextBox.STYLE_TEXT, text.withColor(0xFFE0E0E0))
                .textStyle(TextBox.STYLE_TEXT_DISABLED, text.withColor(0xFF707070))
                .textStyle(TextBox.STYLE_TEXT_READ_ONLY, text.withColor(0xFFA0A0A0))
                .paint(ScrollPanel.STYLE_THUMB, this.paint("widget/scroller", SCROLLER_BORDER))
                .paint(ScrollPanel.STYLE_TRACK, this.paint("widget/scroller_background", SCROLLER_BORDER))
                .paint(ListView.STYLE_ROW_HOVERED, Paint.solid(0x30FFFFFF))
                .paint(ListView.STYLE_ROW_SELECTED, Paint.solid(0x60FFFFFF))
                .textStyle(ListView.STYLE_PLACEHOLDER, text.withColor(0xFFA0A0A0))
                .paint(ComboBox.STYLE_NORMAL, this.paint("widget/button", BUTTON_BORDER))
                .paint(ComboBox.STYLE_HOVERED, this.paint("widget/button_highlighted", BUTTON_BORDER))
                .paint(ComboBox.STYLE_DISABLED, this.paint("widget/button_disabled", BUTTON_DISABLED_BORDER))
                .paint(ComboBox.STYLE_POPUP, Paint.solid(0xF0101010))
                .textStyle(ComboBox.STYLE_TEXT, text)
                .textStyle(ComboBox.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(Window.STYLE_BACKGROUND, this.paint("popup/background", POPUP_BORDER))
                .textStyle(Window.STYLE_TITLE_TEXT, text)
                .textStyle(Window.STYLE_CLOSE, text.withColor(0xFFA0A0A0))
                .textStyle(Window.STYLE_CLOSE_HOVERED, text)
                .paint(TabBar.STYLE_NORMAL, this.paint("widget/tab", TAB_BORDER))
                .paint(TabBar.STYLE_HOVERED, this.paint("widget/tab_highlighted", TAB_BORDER))
                .paint(TabBar.STYLE_SELECTED, this.paint("widget/tab_selected", TAB_BORDER))
                .textStyle(TabBar.STYLE_TEXT, text.withColor(0xFFA0A0A0))
                .textStyle(TabBar.STYLE_TEXT_SELECTED, text)
                .textStyle(TabBar.STYLE_TEXT_DISABLED, text.withColor(0xFF606060))
                // widget/tab is empty in its top 4 pixels and edged in the next 2, so its inside starts 6 down;
                // both paddings are 10 tall, so tabs are a line plus 10, nearer vanilla's 24
                .insets(TabBar.STYLE_PADDING, new Insets(8f, 7f, 8f, 3f))
                .insets(TabBar.STYLE_PADDING_SELECTED, new Insets(8f, 5f, 8f, 5f))
                .paint(ProgressBar.STYLE_TRACK, Paint.solid(0xFF000000))
                .paint(ProgressBar.STYLE_FILL, Paint.solid(0xFF008200))
                .textStyle(ProgressBar.STYLE_TEXT, text)
                .paint(Separator.STYLE_LINE, Paint.solid(0xB9FFFFFF))
                .paint(RadioButton.STYLE_NORMAL, this.paint("widget/checkbox", null))
                .paint(RadioButton.STYLE_HOVERED, this.paint("widget/checkbox_highlighted", null))
                .paint(RadioButton.STYLE_SELECTED, this.paint("widget/checkbox_selected", null))
                .paint(RadioButton.STYLE_SELECTED_HOVERED, this.paint("widget/checkbox_selected_highlighted", null))
                .textStyle(RadioButton.STYLE_TEXT, text)
                .textStyle(RadioButton.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(ToggleSwitch.STYLE_TRACK, this.paint("widget/slider", SLIDER_BORDER))
                .paint(ToggleSwitch.STYLE_TRACK_ON, this.paint("widget/slider_highlighted", SLIDER_BORDER))
                .paint(ToggleSwitch.STYLE_KNOB, this.paint("widget/slider_handle", SLIDER_HANDLE_BORDER))
                .paint(ToggleSwitch.STYLE_KNOB_HOVERED,
                        this.paint("widget/slider_handle_highlighted", SLIDER_HANDLE_BORDER))
                .textStyle(ToggleSwitch.STYLE_TEXT, text)
                .textStyle(ToggleSwitch.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(TableView.STYLE_HEADER, Paint.solid(0x80000000))
                .paint(TableView.STYLE_HEADER_HOVERED, Paint.solid(0x30FFFFFF))
                .textStyle(TableView.STYLE_HEADER_TEXT, text)
                .paint(TableView.STYLE_GRID, Paint.solid(0x40FFFFFF))
                .paint(Menu.STYLE_BACKGROUND, Paint.solid(0xF0101010))
                .paint(Menu.STYLE_ITEM_HOVERED, Paint.solid(0x60FFFFFF))
                .textStyle(Menu.STYLE_TEXT, text)
                .textStyle(Menu.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(Expander.STYLE_HEADER, this.paint("widget/button", BUTTON_BORDER))
                .paint(Expander.STYLE_HEADER_HOVERED, this.paint("widget/button_highlighted", BUTTON_BORDER))
                .textStyle(Expander.STYLE_TEXT, text)
                .textStyle(Expander.STYLE_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(GridView.STYLE_CELL, this.paint("container/slot", null))
                .paint(GridView.STYLE_CELL_HOVERED, Paint.solid(0x80FFFFFF))
                .textStyle(Button.STYLE_LINK_TEXT, text)
                .textStyle(Button.STYLE_LINK_TEXT_HOVERED, text.withColor(0xFFFFFF55))
                .textStyle(Button.STYLE_LINK_TEXT_DISABLED, text.withColor(0xFFA0A0A0))
                .paint(ScrollBar.STYLE_SLIM_TRACK, Paint.NONE)
                .paint(ScrollBar.STYLE_SLIM_THUMB, Paint.solid(0xFF3333AA))
                .paint(ScrollBar.STYLE_SLIM_THUMB_HOVERED, Paint.solid(0xFFCCCCCC))
                .paint(ScrollBar.STYLE_SLIM_THUMB_DISABLED, Paint.solid(0xFF555555))
                .paint(BusyIndicator.STYLE_DOT, Paint.solid(0xFF404040))
                .paint(BusyIndicator.STYLE_DOT_ACTIVE, Paint.solid(0xFF808080))
                .textStyle(BusyIndicator.STYLE_TEXT, text)
                .paint(TextBox.STYLE_SUGGESTIONS, Paint.solid(0xD0000000))
                .textStyle(TextBox.STYLE_SUGGESTION_TEXT, text.withColor(0xFFAAAAAA))
                .paint(ColorPicker.STYLE_FRAME, Paint.solid(0xFFA0A0A0))
                .paint(ColorPicker.STYLE_MARKER, Paint.solid(0xFFFFFFFF))
                .paint(ColorPicker.STYLE_MARKER_OUTLINE, Paint.solid(0xFF000000))
                .paint(ColorPicker.STYLE_DISABLED, Paint.solid(0xA0000000))
                .paint(Window.STYLE_GRIP, Paint.solid(0xFFA0A0A0))
                .paint(Window.STYLE_GRIP_HOVERED, Paint.solid(0xFFFFFFFF))
                .paint(Overlays.STYLE_BACKDROP, Paint.solid(0x60000000))
                // The advancement frame as a nine-slice; toast/advancement itself has no .mcmeta and would stretch
                .paint(Overlays.STYLE_TOAST, this.paint("toast/now_playing", Insets.of(4f)))
                .textStyle(Overlays.STYLE_TOAST_TEXT, TextStyle.DEFAULT.withColor(0xFFFFFFFF))
                .build();
    }

    /**
     * A frame like a vanilla menu screen: the title panorama, blurred and darkened by {@code menu_background}, with
     * panels of {@code menu_list_background} tiled as vanilla's lists tile it, edged in the color of
     * {@code header_separator}. Each falls back to a plain color when the texture is missing.
     */
    private Chrome chrome() {
        final int darken = this.average(TEXTURES + "menu_background.png").orElse(0x40000000);
        final Paint background = this.backdrop(darken)
                .map(image -> {
                    this.assets.put(BACKDROP, image);
                    return Paint.sprite(BACKDROP);
                })
                .orElse(Paint.gradient(0xFF202020, 0xFF101010));
        // Vanilla's lists repeat the 16-pixel texture every 32 GUI pixels
        final Paint panel = this.assets.image(TEXTURES + "menu_list_background.png").isPresent()
                ? Paint.tiled(SpriteId.of(MinecraftAssets.TEXTURE, "gui/menu_list_background"), new Size(32, 32))
                : Paint.solid(0x70000000);
        final int edge = this.average(TEXTURES + "header_separator.png").orElse(0x79808080);
        return new Chrome(background, panel, Border.of(edge, 1f),
                TextStyle.DEFAULT.withColor(0xFFFFFFFF).withShadow(true), 0xFFA0A0A0);
    }

    /**
     * Returns a sprite paint with another tint; other paints come back as they are.
     */
    private static Paint tinted(final Paint paint, final int tint) {
        return switch (paint) {
            case Paint.Sprite sprite -> new Paint.Sprite(sprite.sprite(), tint);
            case Paint.Tiled tiled -> new Paint.Tiled(tiled.sprite(), tiled.size(), tint);
            case Paint.NineSlice slice -> new Paint.NineSlice(slice.sprite(), slice.border(), tint, slice.size(),
                    slice.tiled());
            default -> paint;
        };
    }

    /**
     * Builds the paint for a sprite the way the game slices it, from the {@code .png.mcmeta} that comes with the
     * image: a nine-slice of the declared size, tiled unless {@code stretch_inner} is set, a tiled sprite, or a
     * stretched one.
     *
     * @param path the sprite's path in the {@code minecraft} namespace
     * @param fallback the 26.3 border if the sprite is a nine-slice there, or null if it is a plain sprite
     */
    private Paint paint(final String path, final Insets fallback) {
        final SpriteId sprite = SpriteId.of("minecraft", path);
        final String file = MinecraftAssets.path(sprite);
        if (this.assets.image(file).isEmpty()) {
            this.notes.add(sprite + " is missing, so it draws magenta");
            return fallback == null ? Paint.sprite(sprite) : Paint.nineSlice(sprite, fallback);
        }
        final Optional<Object> scaling = this.assets.readBeside(file, ".mcmeta")
                .flatMap(bytes -> Json.parse(new String(bytes, StandardCharsets.UTF_8)))
                .flatMap(json -> Json.path(json, "gui", "scaling"));
        // Without gui scaling the game stretches the image over the rectangle
        if (scaling.isEmpty()) {
            return Paint.sprite(sprite);
        }
        final Object type = Json.path(scaling.get(), "type").orElse("stretch");
        final Optional<Size> size = MinecraftLook.number(scaling.get(), "width")
                .flatMap(width -> MinecraftLook.number(scaling.get(), "height")
                        .map(height -> new Size(width.floatValue(), height.floatValue())));
        try {
            if ("tile".equals(type) && size.isPresent()) {
                return Paint.tiled(sprite, size.get());
            }
            if (!"nine_slice".equals(type)) {
                return Paint.sprite(sprite);
            }
            final Optional<Insets> border = Json.path(scaling.get(), "border").flatMap(MinecraftLook::border);
            if (size.isPresent() && border.isPresent()) {
                final boolean stretch = Boolean.TRUE.equals(Json.path(scaling.get(), "stretch_inner").orElse(false));
                return stretch
                        ? Paint.nineSlice(sprite, size.get(), border.get())
                        : Paint.tiledNineSlice(sprite, size.get(), border.get());
            }
        } catch (final IllegalArgumentException e) {
            // Borders that do not fit, which the game refuses too
        }
        this.notes.add(sprite + " has a .mcmeta the gallery cannot read; using the 26.3 slicing");
        return fallback == null ? Paint.sprite(sprite) : Paint.nineSlice(sprite, fallback);
    }

    /**
     * Reads a nine-slice border: one number for every side, or an object with each side.
     */
    private static Optional<Insets> border(final Object border) {
        if (border instanceof final Double all) {
            return Optional.of(Insets.of(all.floatValue()));
        }
        final Optional<Double> left = MinecraftLook.number(border, "left");
        final Optional<Double> top = MinecraftLook.number(border, "top");
        final Optional<Double> right = MinecraftLook.number(border, "right");
        final Optional<Double> bottom = MinecraftLook.number(border, "bottom");
        if (left.isEmpty() || top.isEmpty() || right.isEmpty() || bottom.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Insets(left.get().floatValue(), top.get().floatValue(), right.get().floatValue(),
                bottom.get().floatValue()));
    }

    private static Optional<Double> number(final Object object, final String key) {
        return Json.path(object, key).filter(Double.class::isInstance).map(Double.class::cast);
    }

    /**
     * Makes the screen backdrop: the front face of the title panorama, cut to 16:9, blurred, with the menu
     * background's darkening over it.
     */
    private Optional<BufferedImage> backdrop(final int darken) {
        final Optional<BufferedImage> face = this.assets.image(TEXTURES + "title/background/panorama_0.png");
        if (face.isEmpty()) {
            this.notes.add("the title panorama is missing, so the backdrop is plain");
            return Optional.empty();
        }
        final BufferedImage source = face.get();
        if (source.getWidth() < BACKDROP_WIDTH / BLUR || source.getHeight() < BACKDROP_HEIGHT / BLUR) {
            this.notes.add("the title panorama is a placeholder, so the backdrop is plain");
            return Optional.empty();
        }
        final int cropHeight = Math.min(source.getHeight(), source.getWidth() * BACKDROP_HEIGHT / BACKDROP_WIDTH);
        final BufferedImage band = source.getSubimage(0, (source.getHeight() - cropHeight) / 2, source.getWidth(),
                cropHeight);
        // Area averaging shrinks cleanly; drawing the result into a buffered image lets bicubic smooth it back up
        final Image averaged = band.getScaledInstance(BACKDROP_WIDTH / BLUR, BACKDROP_HEIGHT / BLUR,
                Image.SCALE_AREA_AVERAGING);
        final BufferedImage small = new BufferedImage(BACKDROP_WIDTH / BLUR, BACKDROP_HEIGHT / BLUR,
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D shrink = small.createGraphics();
        try {
            shrink.drawImage(averaged, 0, 0, null);
        } finally {
            shrink.dispose();
        }

        final BufferedImage out = new BufferedImage(BACKDROP_WIDTH, BACKDROP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(small, 0, 0, BACKDROP_WIDTH, BACKDROP_HEIGHT, null);
            g.setColor(new Color(darken, true));
            g.fillRect(0, 0, BACKDROP_WIDTH, BACKDROP_HEIGHT);
        } finally {
            g.dispose();
        }
        return Optional.of(out);
    }

    /**
     * Returns a texture's average color, weighted by alpha, with the average alpha. The menu background is fine
     * noise, so its average stands in for laying it over the backdrop.
     */
    private Optional<Integer> average(final String file) {
        final Optional<BufferedImage> image = this.assets.image(file);
        if (image.isEmpty()) {
            this.notes.add(file + " is missing, so a plain color stands in");
            return Optional.empty();
        }
        final BufferedImage texture = image.get();
        long alpha = 0;
        long red = 0;
        long green = 0;
        long blue = 0;
        for (int y = 0; y < texture.getHeight(); y++) {
            for (int x = 0; x < texture.getWidth(); x++) {
                final int argb = texture.getRGB(x, y);
                final int a = argb >>> 24;
                alpha += a;
                red += (long) ((argb >> 16) & 0xFF) * a;
                green += (long) ((argb >> 8) & 0xFF) * a;
                blue += (long) (argb & 0xFF) * a;
            }
        }
        final long pixels = (long) texture.getWidth() * texture.getHeight();
        if (pixels == 0 || alpha == 0) {
            return Optional.of(0);
        }
        return Optional.of((int) (alpha / pixels) << 24 | (int) (red / alpha) << 16 | (int) (green / alpha) << 8
                | (int) (blue / alpha));
    }

    /**
     * What {@link #build} makes.
     *
     * @param theme the vanilla theme
     * @param chrome the gallery frame
     * @param notes what shows differently from the game, once each
     */
    record Result(Theme theme, Chrome chrome, List<String> notes) {
    }
}
