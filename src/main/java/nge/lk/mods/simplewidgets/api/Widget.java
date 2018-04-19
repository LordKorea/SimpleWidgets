package nge.lk.mods.simplewidgets.api;

import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

/**
 * A widget for the HUD.
 */
public abstract class Widget {

    /**
     * The widget format version. Used for serializing.
     */
    public static final int WIDGET_VERSION = 1;

    /**
     * The save ID of this widget. Used for serializing, needs to be unique.
     */
    @Getter private final String saveId;

    /**
     * The side alignment of this widget (N,E,S,W); true means aligned to that side.
     */
    private final boolean[] sideAlignment = new boolean[4];

    /**
     * A function which is used to check whether the element should be shown.
     * <p>
     * The argument indicates whether the check happens for the editor.
     */
    private final Function<Boolean, Boolean> visibilityCheck;

    /**
     * The width of the widget [0-1000], in 1/1000ths of the screen width.
     */
    @Getter protected int width;

    /**
     * The height of the widget [0-1000], in 1/1000ths of the screen height.
     */
    @Getter protected int height;

    /**
     * Whether the widget is enabled (shown outside of the editor).
     */
    @Getter protected boolean enabled = true;

    /**
     * The x position of the widget [0-1000], in 1/1000ths of the screen width.
     */
    protected double positionX;

    /**
     * The y position of the widget [0-1000], in 1/1000ths of the screen height.
     */
    protected double positionY;

    /**
     * Constructor.
     *
     * @param saveId The save ID for this widget.
     * @param visibilityCheck A visibility check function.
     */
    protected Widget(final String saveId, final Function<Boolean, Boolean> visibilityCheck) {
        this.saveId = saveId;
        this.visibilityCheck = visibilityCheck;
    }

    /**
     * Check whether this widgets boundaries contain the given position.
     *
     * @param x The x coordinate of the position.
     * @param y The y coordinate of the position.
     *
     * @return Whether the position is inside the boundaries.
     */
    public boolean contains(final int x, final int y) {
        return x >= positionX && y >= positionY && x <= positionX + width && y <= positionY + height;
    }

    /**
     * Renders the editor part for this widget. This draws a rectangular bounding box.
     *
     * @param scaledResolution The current scaling.
     */
    public void renderEditor(final ScaledResolution scaledResolution) {
        final double posX = Math.max(0, Math.min(1000 - width, positionX)) * scaledResolution.getScaledWidth() / 1000.0;
        final double posY =
                Math.max(0, Math.min(1000 - height, positionY)) * scaledResolution.getScaledHeight() / 1000.0;
        final double renderWidth = width * scaledResolution.getScaledWidth() / 1000.0;
        final double renderHeight = height * scaledResolution.getScaledHeight() / 1000.0;

        // Outline
        GL11.glColor4f(enabled ? 0.0f : 0.8f, 0.0f, 0.0f, 0.7f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(posX, posY, 0);
        GL11.glVertex3d(posX, posY + renderHeight, 0);
        GL11.glVertex3d(posX, posY + renderHeight, 0);
        GL11.glVertex3d(posX + renderWidth, posY + renderHeight, 0);
        GL11.glVertex3d(posX + renderWidth, posY + renderHeight, 0);
        GL11.glVertex3d(posX + renderWidth, posY, 0);
        GL11.glVertex3d(posX + renderWidth, posY, 0);
        GL11.glVertex3d(posX, posY, 0);
        GL11.glEnd();

        // Background area
        GL11.glColor4f(enabled ? 0.0f : 0.8f, 0.0f, 0.0f, 0.4f);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(posX, posY, 0);
        GL11.glVertex3d(posX, posY + renderHeight, 0);
        GL11.glVertex3d(posX + renderWidth, posY, 0);
        GL11.glVertex3d(posX + renderWidth, posY, 0);
        GL11.glVertex3d(posX, posY + renderHeight, 0);
        GL11.glVertex3d(posX + renderWidth, posY + renderHeight, 0);
        GL11.glEnd();
    }

    /**
     * Moves the widget relative to the current position.
     *
     * @param x delta x [-1000..1000]
     * @param y delta y [-1000..1000]
     */
    public void moveRelative(final double x, final double y) {
        positionX = Math.max(0, Math.min(1000 - width, positionX + x));
        positionY = Math.max(0, Math.min(1000 - height, positionY + y));
        updateAlignments();
    }

    /**
     * Renders this widget with the given resolution.
     *
     * @param scaledResolution The resolution.
     */
    public abstract void render(ScaledResolution scaledResolution);

    /**
     * Handle a resize of the HUD.
     */
    public void onResize() {
        // Width/Height might have changed. Make sure position is within boundaries.
        positionX = Math.max(0, Math.min(1000 - width, positionX));
        positionY = Math.max(0, Math.min(1000 - height, positionY));

        // Re-apply alignments.
        if (sideAlignment[0]) {
            positionY = 0;
        }
        if (sideAlignment[1]) {
            positionX = 1000 - width;
        }
        if (sideAlignment[2]) {
            positionY = 1000 - height;
        }
        if (sideAlignment[3]) {
            positionX = 0;
        }
    }

    /**
     * Toggles the enabled state of this widget.
     */
    public void toggleEnabledState() {
        enabled = !enabled;
    }

    /**
     * This method is called when a user requests to configure a widget. The widget can then for example open a GUI.
     */
    public void configure() {
        // Not all widgets need configuration
    }

    /**
     * Deserializes the serialized widget.
     *
     * @param line The serialized widget.
     *
     * @return The last used array element.
     */
    public int deserialize(final String line) {
        final String[] split = line.split(",");
        positionX = Double.parseDouble(split[1]);
        positionY = Double.parseDouble(split[2]);
        enabled = Boolean.parseBoolean(split[3]);
        sideAlignment[0] = Boolean.parseBoolean(split[4]);
        sideAlignment[1] = Boolean.parseBoolean(split[5]);
        sideAlignment[2] = Boolean.parseBoolean(split[6]);
        sideAlignment[3] = Boolean.parseBoolean(split[7]);
        return 7;
    }

    /**
     * Serializes the widget.
     *
     * @return The serialized widget.
     */
    public String serialize() {
        return saveId + "," + positionX + "," + positionY + "," + enabled + "," + sideAlignment[0] + ","
                + sideAlignment[1] + "," + sideAlignment[2] + "," + sideAlignment[3];
    }

    /**
     * Checks if the widget is visible in the current mode.
     *
     * @param editorMode true if the current mode is editor mode.
     *
     * @return true if the widget is currently visible.
     */
    public final boolean isVisible(final boolean editorMode) {
        return visibilityCheck.apply(editorMode);
    }

    /**
     * Updates the alignments to reflect changes to the position in the alignment.
     */
    protected void updateAlignments() {
        for (int i = 0; i < 4; i++) {
            sideAlignment[i] = false;
        }

        if (positionX == 0) {
            sideAlignment[3] = true;
        }
        if (positionY == 0) {
            sideAlignment[0] = true;
        }
        if ((int) positionX == 1000 - width) {
            sideAlignment[1] = true;
        }
        if ((int) positionY == 1000 - height) {
            sideAlignment[2] = true;
        }
    }
}
