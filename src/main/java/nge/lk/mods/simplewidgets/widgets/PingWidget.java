package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.simplewidgets.api.Widget;
import org.lwjgl.opengl.GL11;

/**
 * A widget that shows the player's ping.
 */
public class PingWidget extends Widget {

    /**
     * The current ping.
     */
    private int ping;

    /**
     * Constructor.
     */
    public PingWidget(final int y) {
        super("ping", z -> true);
        positionX = 0;
        positionY = y;
        onResize();
        updateAlignments();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final int baseX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        final int baseY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        font.drawString(ping > 0 ? Integer.toString(ping) : "???", baseX + 17, baseY + 4, 0xFFFFFF);

        final int imageMode;
        if (ping <= 0) {
            imageMode = 5;
        } else if (ping < 150) {
            imageMode = 0;
        } else if (ping < 300) {
            imageMode = 1;
        } else if (ping < 600) {
            imageMode = 2;
        } else if (ping < 1000) {
            imageMode = 3;
        } else {
            imageMode = 4;
        }

        final int imageSel = imageMode * 8 + 176;
        final double vUnit = 0.00390625F;
        final double uUnit = 0.0390625F;
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer rdr = tessellator.getBuffer();
        rdr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        rdr.pos(baseX + 4, baseY + 11, 0).tex(0, (imageSel + 8) * vUnit).endVertex();
        rdr.pos(baseX + 14, baseY + 11, 0).tex(uUnit, (imageSel + 8) * vUnit).endVertex();
        rdr.pos(baseX + 14, baseY + 3, 0).tex(uUnit, imageSel * vUnit).endVertex();
        rdr.pos(baseX + 4, baseY + 3, 0).tex(0, imageSel * vUnit).endVertex();
        tessellator.draw();
    }

    @Override
    public void onResize() {
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        height = 1000 * 16 / resolution.getScaledHeight();
        width = 18 + font.getStringWidth("9999");
        width = 1000 * width / resolution.getScaledWidth();
        super.onResize();
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        ping = -1;
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.connection != null) {
            final NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().thePlayer.connection.getPlayerInfo(
                    Minecraft.getMinecraft().thePlayer.getUniqueID());
            if (playerInfo != null) {
                ping = playerInfo.getResponseTime();
            }
        }
    }
}
