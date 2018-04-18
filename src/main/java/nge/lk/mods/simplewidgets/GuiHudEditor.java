package nge.lk.mods.simplewidgets;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import nge.lk.mods.simplewidgets.api.Widget;
import nge.lk.mods.simplewidgets.api.WidgetManager;

import java.io.IOException;

/**
 * The HUD editor provides a way to move and enable/disable widgets.
 */
@RequiredArgsConstructor
public class GuiHudEditor extends GuiScreen {

    /**
     * The widget IO manager.
     */
    private final WidgetIO widgetIO;

    /**
     * The widget manager.
     */
    private final WidgetManager widgetManager;

    /**
     * The currently dragged widget.
     */
    private Widget dragWidget;

    /**
     * The last mouse position x (for determining movement).
     */
    private int lastMouseX;

    /**
     * The last mouse position y (for determining movement).
     */
    private int lastMouseY;

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawCenteredString(fontRendererObj, "Move widgets by dragging them", width / 2, height / 2 - 10,
                0xDCDCDC);
        drawCenteredString(fontRendererObj, "Right click widgets to toggle their visibility", width / 2,
                height / 2, 0xDCDCDC);
        drawCenteredString(fontRendererObj, "Close with ESC", width / 2, height / 2 + 10, 0xDCDCDC);

        drawRect(width / 4, 0, width / 4 + 1, height, 0x77777777);
        drawRect(width / 2, 0, width / 2 + 1, height, 0x77777777);
        drawRect(3 * width / 4, 0, 3 * width / 4 + 1, height, 0x77777777);
        drawRect(0, height / 4, width, height / 4 + 1, 0x77777777);
        drawRect(0, height / 2, width, height / 2 + 1, 0x77777777);
        drawRect(0, 3 * height / 4, width, 3 * height / 4 + 1, 0x77777777);

        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        widgetManager.renderAll(scaledResolution);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widgetIO.saveAll(widgetManager.getAll());
    }

    @Override
    public void onResize(final Minecraft mcIn, final int w, final int h) {
        widgetManager.onResize();
        super.onResize(mcIn, w, h);
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        final int scaledX = 1000 * mouseX / width;
        final int scaledY = 1000 * mouseY / height;
        dragWidget = widgetManager.getWidgetAt(scaledX, scaledY);
        if (mouseButton == 1 && dragWidget != null) {
            dragWidget.toggleEnabledState();
        }
        lastMouseX = scaledX;
        lastMouseY = scaledY;
    }

    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        dragWidget = null;
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        if (clickedMouseButton == 0) {
            final int scaledX = 1000 * mouseX / width;
            final int scaledY = 1000 * mouseY / height;
            if (dragWidget != null) {
                dragWidget.moveRelative(scaledX - lastMouseX, scaledY - lastMouseY);
            }
            lastMouseX = scaledX;
            lastMouseY = scaledY;
        }
    }
}
