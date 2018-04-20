package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import nge.lk.mods.commonlib.gui.factory.GuiFactory;
import nge.lk.mods.commonlib.gui.factory.Positioning;
import nge.lk.mods.commonlib.gui.factory.element.ButtonElement;
import nge.lk.mods.simplewidgets.api.Marker;
import nge.lk.mods.simplewidgets.api.Widget;
import nge.lk.mods.simplewidgets.api.WidgetAPI;
import org.lwjgl.opengl.GL11;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The compass widget shows a compass with markers.
 */
public class CompassWidget extends Widget {

    /**
     * The markers that are being shown.
     */
    private final Map<String, Marker> markers = new LinkedHashMap<>();

    /**
     * Whether high contrast is enabled.
     */
    private boolean highContrast;

    /**
     * Whether the compass uses facing numbers or cardinal directions.
     */
    private boolean numericCompass;

    /**
     * Returns the facing of the given vector [0-3999] if the input vector is longer than or
     * equally long as the unit vector.
     * <p>
     * If the input vector is shorter, -1 will be returned.
     *
     * @param dx The x component of the vector.
     * @param dz The z component of the vector.
     *
     * @return The facing in the range of 0..3999 or -1 for short vectors.
     */
    private static int getLongVectorFacing(final double dx, final double dz) {
        final double dLen = dx * dx + dz * dz;
        if (dLen <= 1) {
            return -1;
        }

        double atan = MathHelper.atan2(dx, -dz) * (180.0D / Math.PI);
        atan += 180.0D;
        atan /= 90.0D;
        return (int) (atan * 1000);
    }

    /**
     * Converts facing [0-3999] to cardinal directions.
     *
     * @param facing The facing from 0..3999.
     *
     * @return The cardinal direction.
     */
    private static String getNonNumericFacing(int facing) {
        facing /= 500;
        switch (facing) {
            case 0:
                return "S";
            case 1:
                return "SW";
            case 2:
                return "W";
            case 3:
                return "NW";
            case 4:
                return "N";
            case 5:
                return "NE";
            case 6:
                return "E";
            case 7:
                return "SE";
            default:
                throw new IllegalArgumentException("Invalid facing " + facing);
        }
    }

    /**
     * Calculates the difference to the first mark (heading marker on the compass).
     *
     * @param facing The facing from 0..3999.
     *
     * @return The difference to the first mark.
     */
    private static int getDifferenceToMark(final int facing) {
        if (facing > 3950) {
            return 4000 - facing;
        }
        return (int) (50 * Math.ceil(facing / 50.0)) - facing;
    }

    /**
     * Draws a rectangle.
     *
     * @param x The x position of the rectangle.
     * @param y The y position of the rectangle.
     * @param w The width of the rectangle.
     * @param h The height of the rectangle.
     * @param color The color of the rectangle.
     * @param highContrast Whether the contrast is artificially increased.
     */
    private static void drawRect(final int x, final int y, final int w, final int h, final int color,
                                 final boolean highContrast) {
        final float alphaComponent = (float) (color >> 24 & 255) / 255.0f;
        final float redComponent = (float) (color >> 16 & 255) / 255.0f;
        final float greenComponent = (float) (color >> 8 & 255) / 255.0f;
        final float blueComponent = (float) (color & 255) / 255.0f;

        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        if (highContrast) {
            // Blend equation for high contrast:
            // s = Source, d = Destination, c = Color
            // equ(sf(s,d,c), dg(s,d,c)) = (1-A)d + A(1-d) = (1-A)d + (1-d)A = (1-d)A + (1-A)d
            //   => equ: ADD (default), s = A, f = 1-d, g = 1-A
            GlStateManager.color(alphaComponent, alphaComponent, alphaComponent, alphaComponent);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            GlStateManager.color(redComponent, greenComponent, blueComponent, alphaComponent);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE, GL11.GL_ZERO);
        }
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, y + h, 0.0D).endVertex();
        bufferbuilder.pos(x + w, y + h, 0.0D).endVertex();
        bufferbuilder.pos(x + w, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Constructor.
     */
    public CompassWidget() {
        super("compass", z -> true);
        positionX = 350;
        positionY = 0;
        onResize();
        updateAlignments();
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final int posX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        final int posY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);
        final double renderWidth = width * scaledResolution.getScaledWidth() / 1000.0;
        final double renderHeight = height * scaledResolution.getScaledHeight() / 1000.0;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        double rawFacing = MathHelper.wrapDegrees(
                Minecraft.getMinecraft().getRenderViewEntity().rotationYaw + 180);
        rawFacing += 180.0D;
        rawFacing /= 90.0D;

        // The leftmost facing on the compass. The compass displays 1000 units of facing, left is middle - 500.
        int facingLeft = (int) (rawFacing * 1000) - 500;
        if (facingLeft < 0) {
            facingLeft += 4000;
        }

        // Get the offset to the first marked facing value.
        final int facingOffset = getDifferenceToMark(facingLeft);

        // This will be the cursor for stepping from left to right.
        int facing = facingLeft + facingOffset;

        // This represents the last possible x position.
        final float xEnd = (float) (posX + renderWidth);

        // The length of a unit interval in x coordinates.
        final float xPerFacing = (xEnd - posX) / 1000.0f;

        // Position the x cursor at the first marker position.
        float xCursor = posX + facingOffset * xPerFacing;

        // Draw all markers for facing values.
        while (xCursor <= xEnd) {
            // Calculate the distance from the middle and adjust it to the interval 0..1.
            final double distance = Math.pow(1 - Math.abs(posX + renderWidth / 2 - xCursor) * 2.0 / renderWidth, 0.8);
            final int alpha = (int) (255 * distance);

            // Don't draw too transparent markers.
            if (alpha > 30) {
                // The length of the marker depends on the exact facing.
                final int lengthMod;
                if (facing % 500 == 0) {
                    lengthMod = 4;
                } else if (facing % 250 == 0) {
                    lengthMod = 5;
                } else {
                    lengthMod = 7;
                }
                drawRect(((int) xCursor) - 1, posY + (int) (renderHeight / 5), 2,
                        Math.max(2, (int) renderHeight / lengthMod), alpha << 24, highContrast);

                // Some facings are labeled.
                if (facing % 500 == 0) {
                    final String facingLabel;
                    if (numericCompass) {
                        facingLabel = Double.toString(((int) (((facing % 4000) / 1000.0f) * 10)) / 10.0);
                    } else {
                        facingLabel = getNonNumericFacing(facing % 4000);
                    }
                    fontRenderer.drawString(facingLabel,
                            (int) xCursor - fontRenderer.getStringWidth(facingLabel) / 2,
                            posY + (int) (renderHeight / 5) + Math.max(2, (int) renderHeight / 4) + 2,
                            0xFFFFFF);
                }
            }

            // Step through in steps of 50.
            xCursor += xPerFacing * 50;
            facing += 50;
            if (facing == 4000) {
                facing = 0;
            }
        }

        final double srcX = Minecraft.getMinecraft().thePlayer.posX;
        final double srcZ = Minecraft.getMinecraft().thePlayer.posZ;
        for (final Marker marker : markers.values()) {
            if (!marker.isEnabled()) {
                continue;
            }

            // Get the orientation of the marker given the current position.
            final int orientation = getLongVectorFacing(marker.getWorldX() - srcX, marker.getWorldZ() - srcZ);

            // If the marker is too close to the source, don't display it.
            if (orientation == -1) {
                continue;
            }

            // Get the position of the marker on the compass.
            final int difference = (facingLeft < orientation ? orientation : orientation + 4000) - facingLeft;
            final float markerX = posX + difference * xPerFacing;

            // Boundary check, is the marker still on the compass?
            if (markerX > xEnd) {
                continue;
            }

            // Apply alpha attenuation based on distance to the center.
            final double distance = Math.pow(1 - Math.abs(posX + renderWidth / 2 - markerX) * 2.0 / renderWidth, 0.8);
            final int alpha = (int) (222 * distance) + 33;
            drawRect((int) markerX, posY, 2, (int) (renderHeight / 5) + Math.max(2, (int) renderHeight / 4) - 2,
                    (alpha << 24) | marker.getColor(), false);
        }

        // Draw a marker in the middle.
        drawRect(posX + (int) (0.5 * renderWidth) - 1, posY, 2,
                (int) (renderHeight / 5) + Math.max(2, (int) renderHeight / 4), 0x66FF0000, false);
    }

    @Override
    public void onResize() {
        height = 70;
        width = 300;
        super.onResize();
    }

    /**
     * Sets a marker on the compass.
     *
     * @param key The key of the marker.
     * @param marker The marker.
     */
    public void setMarker(final String key, final Marker marker) {
        if (marker == null) {
            markers.remove(key);
        } else {
            markers.put(key, marker);
        }
    }

    @Override
    public void configure() {
        Minecraft.getMinecraft().displayGuiScreen(new WidgetConfigGUI(Minecraft.getMinecraft().currentScreen));
    }

    @Override
    public String serialize() {
        return super.serialize() + "," + numericCompass + "," + highContrast;
    }

    @Override
    public int deserialize(final String line) {
        final int k = super.deserialize(line);
        final String[] data = line.split(",");
        numericCompass = Boolean.parseBoolean(data[k + 1]);
        highContrast = Boolean.parseBoolean(data[k + 2]);
        return k + 2;
    }

    /**
     * The widget configuration screen.
     */
    private final class WidgetConfigGUI extends GuiFactory implements Consumer<ButtonElement> {

        /**
         * The parent screen of the GUI.
         */
        private final GuiScreen parentScreen;

        /**
         * The button for the numeric compass.
         */
        private ButtonElement numericCompassButton;

        /**
         * The button for the high contrast.
         */
        private ButtonElement highContrastButton;

        /**
         * The button for finishing configuring the widget.
         */
        private ButtonElement saveButton;

        /**
         * Constructor.
         *
         * @param parent The parent screen.
         */
        private WidgetConfigGUI(final GuiScreen parent) {
            parentScreen = parent;
            createGui();
        }

        @Override
        public void accept(final ButtonElement buttonElement) {
            if (buttonElement == numericCompassButton) {
                numericCompass = !numericCompass;
                numericCompassButton.getButton().displayString = "Numeric Compass: " + (numericCompass ? "Yes" : "No");
            } else if (buttonElement == highContrastButton) {
                highContrast = !highContrast;
                highContrastButton.getButton().displayString = "High Contrast: " + (highContrast ? "Yes" : "No");
            } else if (buttonElement == saveButton) {
                WidgetAPI.saveWidgets();
                closeGui();
            }
        }

        @Override
        protected void createGui() {
            setPadding(0.05, 0.05, 0.1, 0.05);

            addText(new Positioning().center()).setText("Compass Widget", 0xA0A0A0);
            addBlank(new Positioning().breakRow().absoluteHeight(25));

            numericCompassButton = addButton(this,
                    new Positioning().center().relativeWidth(30).absoluteHeight(20));
            numericCompassButton.getButton().displayString = "Numeric Compass: " + (numericCompass ? "Yes" : "No");
            addBlank(new Positioning().breakRow().absoluteHeight(15));

            highContrastButton = addButton(this,
                    new Positioning().center().relativeWidth(30).absoluteHeight(20));
            highContrastButton.getButton().displayString = "High Contrast: " + (highContrast ? "Yes" : "No");

            saveButton = addButton(this,
                    new Positioning().alignBottom().center().relativeWidth(30).absoluteHeight(20));
            saveButton.getButton().displayString = "Save & Close";
        }

        @Override
        protected void closeGui() {
            mc.displayGuiScreen(parentScreen);
        }
    }
}
