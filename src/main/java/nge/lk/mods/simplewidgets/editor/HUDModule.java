package nge.shinseiki.modules.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.shinseiki.Keys;
import nge.shinseiki.gui.hud.MultilineWidget;
import nge.shinseiki.gui.hud.WidgetManager;
import nge.shinseiki.gui.hud.WidgetVisibility;
import nge.shinseiki.modules.Module;
import nge.shinseiki.util.scheduler.ShinSeikiScheduler;
import org.lwjgl.input.Mouse;

/**
 * The HUD module provides a way to show widgets on the screen
 */
public class HUDModule implements Module {

    /**
     * A widget showing the current FPS
     */
    private final MultilineWidget fpsWidget;

    /**
     * A widget showing the direction the user is facing
     */
    private final MultilineWidget facingWidget;

    /**
     * A widget showing the coordinates of the user
     */
    private final MultilineWidget coordinateWidget;

    /**
     * A widget showing the current biome
     */
    private final MultilineWidget biomeWidget;

    /**
     * A widget showing the armor damage in percent
     */
    private final ArmorWidget armorWidget;

    /**
     * A widget showing how much arrows the user has
     */
    private final ArrowWidget arrowWidget;

    /**
     * A widget showing which keys are pressed
     */
    private final KeystrokeWidget keystrokeWidget;

    /**
     * A widget with a compass with markers
     */
    private final CompassWidget compassWidget;

    /**
     * A widget that shows the players ping
     */
    private final PingWidget pingWidget;

    /**
     * The last width of the screen to detect resizing
     */
    private int lastWidth;

    /**
     * The last height of the screen to detect resizing
     */
    private int lastHeight;

    /**
     * The current click speed, for the keystroke widget
     */
    private int clickSpeed;

    /**
     * The number of arrows that are present
     */
    private boolean arrowsPresent;

    /**
     * Formats the player coordinates into a string
     *
     * @return the formatted player coordinates
     */
    private static String formatPlayerCoordinates() {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) {
            return "<Unknown>";
        }
        BlockPos bPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {
            return bPos.getX() + "/" + bPos.getY() + "/" + bPos.getZ();
        } else {
            int xRounded = Math.round(bPos.getX() / 100.0f);
            int zRounded = Math.round(bPos.getZ() / 100.0f);
            return xRounded + "/" + bPos.getY() + "/" + zRounded;
        }
    }

    /**
     * Formats the player biome into a string
     *
     * @return the formatted player biome
     */
    private static String formatPlayerBiome() {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) {
            return "<Unknown>";
        }
        BlockPos bPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        Chunk chunk = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(bPos);
        return "[" + chunk.getBiome(bPos, Minecraft.getMinecraft().theWorld.getWorldChunkManager()).biomeName + "]";
    }

    /**
     * Formats the players facing into a string
     *
     * @return the formatted player facing
     */
    private static String getPlayerFacing() {
        if (Minecraft.getMinecraft().getRenderViewEntity() == null) {
            return "?";
        }
        String s = "[?] ?!";
        switch (Minecraft.getMinecraft().getRenderViewEntity().getHorizontalFacing()) {
            case DOWN:
            case UP:
                throw new IllegalStateException("Illegal horizontal facing");
            case NORTH:
                s = "[N] -Z ";
                break;
            case SOUTH:
                s = "[S] +Z ";
                break;
            case EAST:
                s = "[E] +X ";
                break;
            case WEST:
                s = "[W] -X ";
                break;
        }
        double d = MathHelper.wrapAngleTo180_double(Minecraft.getMinecraft().getRenderViewEntity().rotationYaw + 180);
        d += 180.0D;
        d /= 90.0D;
        return s + (((int) (d * 10)) / 10.0D);
    }

    /**
     * Constructor
     */
    public HUDModule() {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int height = MultilineWidget.getSingleLineHeight(scaledResolution.getScaledHeight());
        int height16 = 1000 * 16 / scaledResolution.getScaledHeight();

        fpsWidget = new MultilineWidget(
                "fps",
                0,
                0,
                new String[]{"[999]"},
                0xFFFFFF,
                WidgetVisibility.visibilityWithModule(this)
        );
        WidgetManager.registerWidget(fpsWidget);

        facingWidget = new MultilineWidget(
                "facing",
                fpsWidget.getWidth(),
                0,
                new String[]{"[N] -Z 9.9"},
                0xFFFFFF,
                WidgetVisibility.visibilityWithModule(this)
        );
        WidgetManager.registerWidget(facingWidget);

        coordinateWidget = new MultilineWidget(
                "coordinate",
                0,
                height,
                new String[]{"00000/000/00000"},
                0xFFFFFF,
                WidgetVisibility.visibilityWithModule(this)
        );
        WidgetManager.registerWidget(coordinateWidget);

        biomeWidget = new MultilineWidget(
                "biome",
                0,
                2 * height,
                new String[]{"[MushroomIsland]"},
                0xFFFFFF,
                WidgetVisibility.visibilityWithModule(this)
        );
        biomeWidget.toggleEnabledState();
        WidgetManager.registerWidget(biomeWidget);

        armorWidget = new ArmorWidget(
                0,
                1000 - height,
                WidgetVisibility.visibilityWithModule(this)
        );
        WidgetManager.registerWidget(armorWidget);

        //noinspection LambdaCanBeReplacedWithAnonymous: NOT A MINECRAFT INTERFACE
        arrowWidget = new ArrowWidget(
                0,
                1000 - height - height16,
                WidgetVisibility.visibilityWithModuleAndCondition(this, () -> arrowsPresent)
        );
        WidgetManager.registerWidget(arrowWidget);

        keystrokeWidget = new KeystrokeWidget(
                1000,
                height,
                WidgetVisibility.visibilityWithModule(this)
        );
        keystrokeWidget.toggleEnabledState();
        WidgetManager.registerWidget(keystrokeWidget);

        compassWidget = new CompassWidget(
                350,
                0,
                WidgetVisibility.visibilityWithModule(this)
        );
        compassWidget.toggleEnabledState();
        WidgetManager.registerWidget(compassWidget);

        pingWidget = new PingWidget(
                0,
                1000 - height - 2 * height16,
                this
        );
        pingWidget.toggleEnabledState();
        WidgetManager.registerWidget(pingWidget);
    }

    @SubscribeEvent
    public void mouseInput(MouseInputEvent e) {
        if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
            addClickSpeed();
        }
    }

    @SubscribeEvent
    public void onKeyPress(KeyInputEvent event) {
        if (Keys.HUD_EDITOR.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiHudEditor());
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null && !(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor)) {
            return;
        }

        fpsWidget.getText()[0] = "[" + Minecraft.getDebugFPS() + "]";
        facingWidget.getText()[0] = getPlayerFacing();
        biomeWidget.getText()[0] = formatPlayerBiome();
        coordinateWidget.getText()[0] = formatPlayerCoordinates();

        if (Minecraft.getMinecraft().thePlayer != null) {
            int i = 0;
            int[] armorPercentages = new int[4];
            for (ItemStack its : Minecraft.getMinecraft().thePlayer.inventory.armorInventory) {
                armorPercentages[i] = (its == null || !(its.getItem() instanceof ItemArmor)) ? -1 : (int) (((its.getMaxDamage() - its.getItemDamage()) * 100.0f) / its.getMaxDamage());
                i++;
            }
            armorWidget.updateArmorData(armorPercentages);

            int arrows = 0;
            for (ItemStack its : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
                if (its == null) {
                    continue;
                }
                if (its.getItem() == Items.arrow) {
                    arrows += its.stackSize;
                }
            }
            arrowsPresent = arrows > 0;
            arrowWidget.setArrowCount(arrows);
        }

        keystrokeWidget.setClickSpeed(clickSpeed);
        compassWidget.updateMarkers();

        pingWidget.setPing(-1);
        if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.sendQueue != null) {
            NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfo(Minecraft.getMinecraft().thePlayer.getUniqueID());
            if (playerInfo != null) {
                pingWidget.setPing(playerInfo.getResponseTime());
            }
        }
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        if (mc.displayHeight != lastHeight || mc.displayWidth != lastWidth) {
            lastWidth = mc.displayWidth;
            lastHeight = mc.displayHeight;
            WidgetManager.onResize();
        }
        WidgetManager.renderAll(scaledResolution);
    }

    /**
     * Adds one to the click speed and schedules the decrease a second later
     */
    private void addClickSpeed() {
        clickSpeed++;
        //noinspection LambdaCanBeReplacedWithAnonymous: NOT A MINECRAFT INTERFACE
        ShinSeikiScheduler.scheduleDelayedTask(() -> clickSpeed--, 20L);
    }
}
