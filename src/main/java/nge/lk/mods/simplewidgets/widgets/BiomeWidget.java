package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.simplewidgets.GuiHudEditor;
import nge.lk.mods.simplewidgets.api.MultilineWidget;

/**
 * A widget that shows the current biome.
 */
public class BiomeWidget extends MultilineWidget {

    /**
     * Formats the biome of the player.
     *
     * @return The formatted biome.
     */
    private static String formatPlayerBiome() {
        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) {
            return "[unknown]";
        }
        final BlockPos bPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        final Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(bPos);
        return "[" + chunk.getBiome(bPos, Minecraft.getMinecraft().theWorld.getWorldChunkManager()).biomeName + "]";
    }

    /**
     * Constructor.
     *
     * @param x The x-coordinate of the widget [0-1000].
     * @param y The y-coordinate of the widget [0-1000].
     */
    public BiomeWidget(final int x, final int y) {
        super("biome", x, y, new String[]{"[Redwood Taiga Hills M]"}, 0xFFFFFF, z -> true);
        enabled = false;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null
                && !(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor)) {
            return;
        }
        getText()[0] = formatPlayerBiome();
    }
}
