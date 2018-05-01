package nge.lk.mods.simplewidgets.widgets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.simplewidgets.api.Widget;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The key stroke widget shows the WASD keys, Space, Shift and the mouse buttons and shows when they are pressed.
 * It also displays the click speed.
 */
public class KeystrokeWidget extends Widget {

    /**
     * All currently scheduled tick runnables.
     */
    private final Queue<TickRunnable> tickRunnables = new LinkedList<>();

    /**
     * Counts the ticks, for decreasing click speeds.
     */
    private long tickCounter;

    /**
     * The click speed of the left mouse button.
     */
    private int leftClickSpeed;

    /**
     * The click speed of the right mouse button.
     */
    private int rightClickSpeed;

    /**
     * Draws a key rectangle.
     *
     * @param font The font renderer.
     * @param x The x position of the upper left corner.
     * @param y The y position of the upper left corner.
     * @param w The width of the rectangle.
     * @param h The height of the rectangle.
     * @param pressed Whether the key is pressed.
     * @param caption The caption of the key.
     */
    private static void drawKeyRect(final FontRenderer font, final int x, final int y, final int w, final int h,
                                    final boolean pressed, final String caption) {
        if (pressed) {
            Gui.drawRect(x, y, x + w, y + h, 0xDD000000);
        } else {
            Gui.drawRect(x, y, x + w, y + h, 0x99777777);
            Gui.drawRect(x - 1, y - 1, x + w - 1, y + h - 1, 0x99777777);
        }
        font.drawString(caption, x + w / 2 - font.getStringWidth(caption) / 2 - (!pressed ? 1 : 0), y + h / 2 - 4 - (!pressed ? 1 : 0), 0xFFFFFF);
    }

    /**
     * Constructor.
     */
    public KeystrokeWidget() {
        super("keystroke", z -> true);
        positionY = 0;
        onResize();
        positionX = 1000 - width;
        updateAlignments();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final int baseX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        final int baseY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);
        final double renderHeight = height * scaledResolution.getScaledHeight() / 1000.0;
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        // Draw keys
        drawKeyRect(font, baseX + 3, (int) (baseY + renderHeight - 22), 20, 20,
                Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown(), "A");
        drawKeyRect(font, baseX + 3, (int) (baseY + renderHeight - 41), 20, 14,
                Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown(),
                leftClickSpeed > 0 ? Integer.toString(leftClickSpeed) : "LM");
        drawKeyRect(font, baseX + 28, (int) (baseY + renderHeight - 22), 20, 20,
                Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown(), "S");
        drawKeyRect(font, baseX + 53, (int) (baseY + renderHeight - 22), 20, 20,
                Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown(), "D");
        drawKeyRect(font, baseX + 53, (int) (baseY + renderHeight - 41), 20, 14,
                Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown(),
                rightClickSpeed > 0 ? Integer.toString(rightClickSpeed) : "RM");
        drawKeyRect(font, baseX + 28, (int) (baseY + renderHeight - 47), 20, 20,
                Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown(), "W");
        drawKeyRect(font, baseX + 78, (int) (baseY + renderHeight - 47), 34, 20,
                Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), "Shift");
        drawKeyRect(font, baseX + 78, (int) (baseY + renderHeight - 22), 34, 20,
                Keyboard.isKeyDown(Keyboard.KEY_SPACE), "Space");
    }

    @Override
    public void onResize() {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        height = 1000 * 50 / scaledResolution.getScaledHeight();
        width = 1000 * 115 / scaledResolution.getScaledWidth();
        super.onResize();
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) {
            return;
        }
        tickCounter++;
        while (!tickRunnables.isEmpty()) {
            if (tickRunnables.peek().getTick() == tickCounter) {
                tickRunnables.poll().getTask().run();
            } else {
                break;
            }
        }
    }

    @SubscribeEvent
    public void mouseInput(final MouseEvent e) {
        if (e.getButton() == 0 && e.isButtonstate()) {
            leftClickSpeed++;
            tickRunnables.add(new TickRunnable(tickCounter + 20, () -> leftClickSpeed--));
        } else if (e.getButton() == 1 && e.isButtonstate()) {
            rightClickSpeed++;
            tickRunnables.add(new TickRunnable(tickCounter + 20, () -> rightClickSpeed--));
        }
    }

    /**
     * A runnable that runs in a certain tick.
     */
    @RequiredArgsConstructor
    private static class TickRunnable {

        @Getter private final long tick;

        @Getter private final Runnable task;
    }
}
