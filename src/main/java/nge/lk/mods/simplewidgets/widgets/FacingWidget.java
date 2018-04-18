package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.simplewidgets.GuiHudEditor;
import nge.lk.mods.simplewidgets.api.MultilineWidget;

/**
 * A widget that shows the heading of the player.
 */
public class FacingWidget extends MultilineWidget {

    /**
     * Returns a formatted facing string.
     *
     * @return The formatted facing string.
     */
    private static String getFacingText() {
        if (Minecraft.getMinecraft().getRenderViewEntity() == null) {
            return "?";
        }
        String heading = "[?] ?? ";
        switch (Minecraft.getMinecraft().getRenderViewEntity().getHorizontalFacing()) {
            case DOWN:
            case UP:
                throw new IllegalStateException("illegal horizontal facing");
            case NORTH:
                heading = "[N] -Z ";
                break;
            case SOUTH:
                heading = "[S] +Z ";
                break;
            case WEST:
                heading = "[E] +X ";
                break;
            case EAST:
                heading = "[W] -X ";
                break;
        }
        double fval = MathHelper.wrapAngleTo180_double(
                Minecraft.getMinecraft().getRenderViewEntity().rotationYaw + 180);
        fval = (fval + 180.0) / 90.0;
        return heading + (((int) (fval * 10)) / 10.0);
    }

    /**
     * Constructor.
     *
     * @param x The x-coordinate of the widget [0-1000].
     * @param y The y-coordinate of the widget [0-1000].
     */
    public FacingWidget(final int x, final int y) {
        super("facing", x, y, new String[]{"[W] -Z 9.9"}, 0xFFFFFF, z -> true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null
                && !(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor)) {
            return;
        }
        getText()[0] = getFacingText();
    }
}
