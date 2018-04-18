package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.simplewidgets.api.MultilineWidget;

/**
 * A widget that shows the FPS.
 */
public class FPSWidget extends MultilineWidget {

    /**
     * Constructor.
     *
     * @param x The x-coordinate of the widget [0-1000].
     * @param y The y-coordinate of the widget [0-1000].
     */
    public FPSWidget(final int x, final int y) {
        super("fps", x, y, new String[]{"[999]"}, 0xFFFFFF, z -> true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        getText()[0] = "[" + Minecraft.getDebugFPS() + "]";
    }
}
