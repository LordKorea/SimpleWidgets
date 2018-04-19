package nge.lk.mods.simplewidgets.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import nge.lk.mods.commonlib.gui.factory.GuiFactory;
import nge.lk.mods.commonlib.gui.factory.Positioning;
import nge.lk.mods.commonlib.gui.factory.element.ButtonElement;
import nge.lk.mods.simplewidgets.GuiHudEditor;
import nge.lk.mods.simplewidgets.api.Widget;
import nge.lk.mods.simplewidgets.api.WidgetAPI;

import java.util.function.Consumer;

/**
 * A widget that shows the number of arrows a player has.
 */
public class ArrowWidget extends Widget {

    /**
     * The number of arrows the player has.
     */
    private int arrowCount;

    /**
     * Whether the armor information is aligned to the right.
     */
    private boolean alignRight;

    /**
     * Constructor.
     */
    public ArrowWidget() {
        super("arrow", z -> true);
        alignRight = true;
        onResize();
        positionX = 1000 - width;
        positionY = 1000 - height;
        updateAlignments();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final int baseX;
        if (alignRight) {
            baseX = (int) ((positionX + width) * scaledResolution.getScaledWidth() / 1000.0);
        } else {
            baseX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        }
        final int baseY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);

        final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        if (arrowCount > 0 || Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            final int step = alignRight ? -1 : 1;
            renderItem.renderItemAndEffectIntoGUI(new ItemStack(Items.arrow), baseX + (alignRight ? -17 : 1),
                    baseY + 2);
            final String arrowStr = Integer.toString(arrowCount);
            final int textX = baseX + step * (1 + 16 + 2) - (alignRight ? font.getStringWidth(arrowStr) : 0);
            font.drawString(arrowStr, textX, baseY + 6, 0xFFFFFF);
        }
    }

    @Override
    public void onResize() {
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        // 16 for icon, 2 for padding
        height = 1000 * 18 / resolution.getScaledHeight();
        // 16 for icon, 2 for gap, 4 for padding
        width = 16 + 2 + 4 + font.getStringWidth("9999");
        width = 1000 * width / resolution.getScaledWidth();
        super.onResize();
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            arrowCount = 0;
            for (final ItemStack its : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
                if (its == null) {
                    continue;
                }
                if (its.getItem() == Items.arrow) {
                    arrowCount += its.stackSize;
                }
            }
        }
    }

    @Override
    public void configure() {
        Minecraft.getMinecraft().displayGuiScreen(new WidgetConfigGUI(Minecraft.getMinecraft().currentScreen));
    }

    @Override
    public String serialize() {
        return super.serialize() + "," + alignRight;
    }

    @Override
    public int deserialize(final String line) {
        final int k = super.deserialize(line);
        final String[] data = line.split(",");
        alignRight = Boolean.parseBoolean(data[k + 1]);
        return k + 1;
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
         * The button for the horizontal alignment
         */
        private ButtonElement alignRightButton;

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
            if (buttonElement == alignRightButton) {
                alignRight = !alignRight;
                alignRightButton.getButton().displayString = "Align Right: " + (alignRight ? "Yes" : "No");
            } else if (buttonElement == saveButton) {
                WidgetAPI.saveWidgets();
                closeGui();
            }
        }

        @Override
        protected void createGui() {
            setPadding(0.05, 0.05, 0.1, 0.05);

            addText(new Positioning().center()).setText("Arrow Widget", 0xA0A0A0);
            addBlank(new Positioning().breakRow().absoluteHeight(25));

            alignRightButton = addButton(this, new Positioning().center().relativeWidth(30).absoluteHeight(20));
            alignRightButton.getButton().displayString = "Align Right: " + (alignRight ? "Yes" : "No");

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
