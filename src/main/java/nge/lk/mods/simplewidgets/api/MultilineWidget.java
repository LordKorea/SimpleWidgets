package nge.lk.mods.simplewidgets.api;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Arrays;
import java.util.function.Function;

/**
 * A widget that contains one or more lines of text.
 */
public class MultilineWidget extends Widget {

    /**
     * The text of this widget. An array reference is exposed intentionally for editing the arrays contents.
     */
    @Getter private final String[] text;

    /**
     * The color of the text.
     */
    private final int color;

    /**
     * A sample of text that is used for calculating the dimensions.
     */
    private final String[] maximumText;

    /**
     * Returns the height of a single line given the global height.
     *
     * @param scaledHeight The global height.
     *
     * @return The height of a single line.
     */
    public static int getSingleLineHeight(final int scaledHeight) {
        // 11000 = 1000 (widget coordinates range [0-1000]) * 11 (height of text in scaled units)
        // Effectively: textHeight * (widgetScale / globalScale)
        return 11000 / scaledHeight;
    }

    /**
     * Constructor.
     *
     * @param saveId The save id of this widget.
     * @param x The x-coordinate of the widget [0-1000].
     * @param y The y-coordinate of the widget [0-1000].
     * @param maximumLines A sample of text used for determining the maximum dimensions.
     * @param color The color of the text.
     * @param visibilityCheck A function which is used to check if the element should be shown.
     */
    public MultilineWidget(final String saveId, final int x, final int y, final String[] maximumLines, final int color,
                           final Function<Boolean, Boolean> visibilityCheck) {
        super(saveId, visibilityCheck);
        positionX = x;
        positionY = y;
        text = new String[maximumLines.length];
        Arrays.setAll(text, i -> "");
        this.color = color;
        maximumText = maximumLines;

        onResize();
        updateAlignments();
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final int posX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        final int posY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);
        final FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int yOffset = 2;
        for (final String line : text) {
            renderer.drawString(line, posX + 3, posY + yOffset, color);
            yOffset += 10;
        }
    }

    @Override
    public void onResize() {
        final FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        height = 1000 * 11 * maximumText.length / scaledResolution.getScaledHeight();
        width = 0;
        for (final String line : maximumText) {
            final int width = renderer.getStringWidth(line);
            if (width > this.width) {
                this.width = width;
            }
        }
        width += 4;
        width = 1000 * width / scaledResolution.getScaledWidth();

        super.onResize();
    }
}
