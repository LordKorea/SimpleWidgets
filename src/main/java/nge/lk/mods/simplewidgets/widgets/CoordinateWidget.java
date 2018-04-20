package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.commonlib.gui.factory.GuiFactory;
import nge.lk.mods.commonlib.gui.factory.Positioning;
import nge.lk.mods.commonlib.gui.factory.element.ButtonElement;
import nge.lk.mods.simplewidgets.GuiHudEditor;
import nge.lk.mods.simplewidgets.api.MultilineWidget;
import nge.lk.mods.simplewidgets.api.WidgetAPI;

import java.util.function.Consumer;

/**
 * The coordinate widget displays the current coordinates.
 */
public class CoordinateWidget extends MultilineWidget {

    /**
     * Whether coordinates should be rounded by default.
     */
    private boolean round;

    /**
     * Constructor.
     *
     * @param x The x-coordinate of the widget [0-1000].
     * @param y The y-coordinate of the widget [0-1000].
     */
    public CoordinateWidget(final int x, final int y) {
        super("coordinate", x, y, new String[]{"00000/000/00000"}, 0xFFFFFF, z -> true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null
                && !(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor)) {
            return;
        }
        getText()[0] = formatCoordinates();
    }

    @Override
    public void configure() {
        Minecraft.getMinecraft().displayGuiScreen(new WidgetConfigGUI(Minecraft.getMinecraft().currentScreen));
    }

    @Override
    public String serialize() {
        return super.serialize() + "," + round;
    }

    @Override
    public int deserialize(final String line) {
        final int k = super.deserialize(line);
        final String[] split = line.split(",");
        round = Boolean.parseBoolean(split[k + 1]);
        return k + 1;
    }

    /**
     * Formats the coordinates of the player.
     *
     * @return The formatted coordinates.
     */
    private String formatCoordinates() {
        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) {
            return "unknown";
        }
        final BlockPos bPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown() || !round) {
            return bPos.getX() + "/" + bPos.getY() + "/" + bPos.getZ();
        } else {
            final int xRounded = Math.round(bPos.getX() / 100.0f);
            final int zRounded = Math.round(bPos.getZ() / 100.0f);
            return xRounded + "/" + bPos.getY() + "/" + zRounded;
        }
    }

    /**
     * The widget configuration screen.
     */
    private final class WidgetConfigGUI extends GuiFactory implements Consumer<ButtonElement> {

        /**
         * The parent screen of the GUI.
         */
        private final GuiScreen parentScreen;

        /**
         * The button for toggling coordinate rounding.
         */
        private ButtonElement roundButton;

        /**
         * The button for finishing configuring the widget.
         */
        private ButtonElement saveButton;

        /**
         * Constructor.
         *
         * @param parent The parent screen.
         */
        private WidgetConfigGUI(final GuiScreen parent) {
            parentScreen = parent;
            createGui();
        }

        @Override
        public void accept(final ButtonElement buttonElement) {
            if (buttonElement == roundButton) {
                round = !round;
                roundButton.getButton().displayString = "Short Coordinates: " + (round ? "Yes" : "No");
            } else if (buttonElement == saveButton) {
                WidgetAPI.saveWidgets();
                closeGui();
            }
        }

        @Override
        protected void createGui() {
            setPadding(0.05, 0.05, 0.1, 0.05);

            addText(new Positioning().center()).setText("Coordinate Widget", 0xA0A0A0);
            addBlank(new Positioning().breakRow().absoluteHeight(25));

            roundButton = addButton(this, new Positioning().center().relativeWidth(30).absoluteHeight(20));
            roundButton.getButton().displayString = "Short Coordinates: " + (round ? "Yes" : "No");
            addBlank(new Positioning().breakRow().absoluteHeight(10));

            addText(new Positioning().center()).setText("Short coordinates are displayed in steps of 100,",
                    0xA0A0A0);
            addText(new Positioning().center()).setText(
                    "e.g. -341 becomes -3. Sneaking shows the full coordinates.", 0xA0A0A0);

            saveButton = addButton(this,
                    new Positioning().alignBottom().center().relativeWidth(30).absoluteHeight(20));
            saveButton.getButton().displayString = "Save & Close";
        }

        @Override
        protected void closeGui() {
            mc.displayGuiScreen(parentScreen);
        }
    }
}
