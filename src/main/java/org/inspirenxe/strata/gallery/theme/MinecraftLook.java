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

import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.geometry.Size;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.api.theme.Theme;
import org.inspirenxe.strata.api.theme.ThemeId;
import org.inspirenxe.strata.api.theme.ThemeRef;
import org.inspirenxe.strata.spi.ThemeDocument;
import org.inspirenxe.strata.swing.SwingThemeSource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Strata-Minecraft's vanilla theme, drawn from a client jar and a resource pack read at runtime. The theme comes
 * from the gallery's copy of Strata-Minecraft's {@code vanilla.json}, registered as the base file of
 * {@code minecraft:vanilla} on a {@link SwingThemeSource} and resolved, so every key names the same sprite as in
 * game. The gallery cannot use Strata-Minecraft itself, which needs the game's classes, so when the file changes
 * there, copy it to {@code src/main/resources/strata/minecraft/themes/vanilla.json} here.
 *
 * <p>The game slices every sprite by its {@code .png.mcmeta}, whatever paint the theme names, so this reads the
 * {@code .mcmeta} that comes with each of the game's images and builds the paint from it: a nine-slice of the
 * declared size, tiled unless the {@code .mcmeta} stretches it, a tiled sprite, or a plain sprite when the image has
 * no slicing. Each keeps the tint the theme gives it. Sized paints draw a pack's larger images with the game's
 * borders. The theme's own borders, those of 26.3, are used only when a nine-slice {@code .mcmeta} cannot be
 * read.</p>
 */
final class MinecraftLook {
    private static final ThemeId VANILLA = ThemeId.of("minecraft", "vanilla");
    private static final String VANILLA_FILE = "/strata/minecraft/themes/vanilla.json";

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
     * Resolves {@code minecraft:vanilla} from the copy of {@code vanilla.json}, then gives each of the game's sprites
     * the slicing its {@code .mcmeta} declares.
     */
    private Theme theme() {
        final SwingThemeSource source = new SwingThemeSource(MinecraftLook.class.getClassLoader());
        source.register(VANILLA, List.of(MinecraftLook.vanillaFile()));
        final Theme resolved = source.resolve(ThemeRef.of(VANILLA));
        final Theme.Builder builder = resolved.toBuilder();
        resolved.paints().forEach((key, paint) -> builder.paint(key, this.sliced(paint)));
        return builder.build();
    }

    /**
     * Reads the gallery's copy of {@code vanilla.json} from the class path.
     */
    private static ThemeDocument vanillaFile() {
        try (InputStream in = MinecraftLook.class.getResourceAsStream(VANILLA_FILE)) {
            if (in == null) {
                throw new IllegalStateException("The vanilla theme is missing from the class path: " + VANILLA_FILE);
            }
            return new ThemeDocument(VANILLA_FILE.substring(1), new String(in.readAllBytes(), StandardCharsets.UTF_8),
                    "vanilla");
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot read the vanilla theme", e);
        }
    }

    /**
     * Rebuilds a paint of one of the game's sprites as the game slices it, keeping its tint. Other paints come back
     * as they are.
     */
    private Paint sliced(final Paint paint) {
        return switch (paint) {
            case Paint.Sprite sprite when MinecraftLook.isGame(sprite.sprite()) ->
                    MinecraftLook.tinted(this.paint(sprite.sprite(), null), sprite.tint());
            case Paint.NineSlice slice when MinecraftLook.isGame(slice.sprite()) ->
                    MinecraftLook.tinted(this.paint(slice.sprite(), slice.border()), slice.tint());
            default -> paint;
        };
    }

    private static boolean isGame(final SpriteId sprite) {
        return "minecraft".equals(sprite.namespace());
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
     * @param sprite one of the game's sprites
     * @param fallback the 26.3 border if the sprite is a nine-slice there, or null if it is a plain sprite
     */
    private Paint paint(final SpriteId sprite, final Insets fallback) {
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
