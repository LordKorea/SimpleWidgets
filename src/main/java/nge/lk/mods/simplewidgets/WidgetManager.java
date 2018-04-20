package nge.lk.mods.simplewidgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import nge.lk.mods.simplewidgets.api.Widget;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the widgets that are shown on the HUD.
 */
public final class WidgetManager {

    /**
     * The registered widgets (SaveId -&gt; Widget).
     */
    private final Map<String, Widget> widgets = new HashMap<>();

    /**
     * The cached serialized widgets which have not yet been registered (SaveId -&gt; Line).
     */
    private final Map<String, String> serializedCache = new HashMap<>();

    /**
     * Returns any widget at the given position (as if the editor was active).
     * <p>
     * If there are multiple widgets at this position, any of them might be returned.
     *
     * @param x The x coordinate of the position.
     * @param y The y coordinate of the position.
     *
     * @return Any widget at this position, null if none are there.
     */
    public Widget getWidgetAt(final int x, final int y) {
        for (final Widget widget : widgets.values()) {
            if (widget.isVisible(true)) {
                if (widget.contains(x, y)) {
                    return widget;
                }
            }
        }
        return null;
    }

    /**
     * Propagates HUD resizing to all registered widgets.
     */
    public void onResize() {
        for (final Widget widget : widgets.values()) {
            widget.onResize();
        }
    }

    /**
     * Renders all widgets given the current resolution.
     *
     * @param scaledResolution The current resolution.
     */
    public void renderAll(final ScaledResolution scaledResolution) {
        final boolean editorActive = Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor;

        // Editor bounding boxes
        if (editorActive) {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GL11.glLineWidth(3.0f);
            for (final Widget widget : widgets.values()) {
                if (widget.isVisible(true)) {
                    widget.renderEditor(scaledResolution);
                }
            }
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        }

        // Normal widget rendering
        for (final Widget widget : widgets.values()) {
            if (widget.isVisible(editorActive) && (editorActive || widget.isEnabled())) {
                widget.render(scaledResolution);
            }
        }
    }

    /**
     * Registers a widget for display.
     *
     * @param widget The widget to register.
     *
     * @return The widget.
     */
    public Widget registerWidget(final Widget widget) {
        if (serializedCache.containsKey(widget.getSaveId())) {
            widget.deserialize(serializedCache.remove(widget.getSaveId()));
        }
        widgets.put(widget.getSaveId(), widget);
        return widget;
    }

    /**
     * Provides the serialized widget to the manager.
     *
     * @param saveId The save ID of the widget.
     * @param line The serialized widget line.
     */
    public void provideSerializedWidget(final String saveId, final String line) {
        if (widgets.containsKey(saveId)) {
            widgets.get(saveId).deserialize(line);
        } else {
            serializedCache.put(saveId, line);
        }
    }

    /**
     * Fetches a widget by save ID.
     *
     * @param saveId The save ID.
     *
     * @return The widget, or null.
     */
    public Widget getWidget(final String saveId) {
        return widgets.get(saveId);
    }

    /**
     * Returns an iterator of all widgets.
     *
     * @return The iterator.
     */
    public Iterator<Widget> getAll() {
        return widgets.values().iterator();
    }
}
