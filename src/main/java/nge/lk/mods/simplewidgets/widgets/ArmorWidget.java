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
 * A widget that shows the current status of the player's equipment.
 */
public class ArmorWidget extends Widget {

    /**
     * The item stacks that need to be rendered.
     */
    private final ItemStack[] renderList = new ItemStack[5];

    /**
     * An example render list for the editor.
     */
    private final ItemStack[] exampleList = new ItemStack[5];

    /**
     * Whether the armor information is aligned to the right.
     */
    private boolean alignRight;

    /**
     * Whether the armor information is aligned to the top.
     */
    private boolean alignTop;

    /**
     * Constructor.
     */
    public ArmorWidget() {
        super("armor", z -> true);
        positionX = 0;
        onResize();
        positionY = 1000 - height;
        updateAlignments();

        exampleList[0] = new ItemStack(Items.leather_helmet, 1, 25);
        exampleList[1] = new ItemStack(Items.chainmail_chestplate, 1, 5);
        exampleList[2] = new ItemStack(Items.diamond_leggings, 1, 132);
        exampleList[3] = new ItemStack(Items.golden_boots, 1, 7);
        exampleList[4] = new ItemStack(Items.iron_sword, 1, 201);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(final ScaledResolution scaledResolution) {
        final ItemStack[] target;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            boolean any = false;
            for (final ItemStack its : renderList) {
                if (its != null && its.getMaxDamage() > 0) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                target = exampleList;
            } else {
                target = renderList;
            }
        } else {
            target = renderList;
        }

        final int baseX;
        if (alignRight) {
            baseX = (int) ((positionX + width) * scaledResolution.getScaledWidth() / 1000.0);
        } else {
            baseX = (int) (positionX * scaledResolution.getScaledWidth() / 1000.0);
        }
        final int baseY;
        if (alignTop) {
            baseY = (int) (positionY * scaledResolution.getScaledHeight() / 1000.0);
        } else {
            baseY = (int) ((positionY + height) * scaledResolution.getScaledHeight() / 1000.0);
        }

        final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        final int start = alignTop ? 0 : 4;
        final int step = alignTop ? 1 : -1;
        final int end = alignTop ? 5 : -1;
        final int renderOffset = alignTop ? 0 : -16;
        int yCur = baseY + 2 * step + renderOffset;
        for (int i = start; i != end; i += step) {
            if (target[i] == null) {
                continue;
            }
            if (i == 4 && target[i].getMaxDamage() == 0) {
                continue;
            }
            renderItem.renderItemAndEffectIntoGUI(target[i], alignRight ? baseX - 2 - 16 : baseX + 2, yCur);
            if (target[i].getMaxDamage() > 0) {
                final ItemStack its = target[i];
                final int dura = its.getMaxDamage() - its.getItemDamage();
                final double pct = 1.0 * dura / its.getMaxDamage();
                final double eps = 1e-7;

                final int color;
                if (1.0 - pct < eps) {
                    color = 0x00AA00;
                } else if (pct > 0.75) {
                    color = 0x55FF55;
                } else if (pct > 0.5) {
                    color = 0xFFFF55;
                } else if (pct > 0.25) {
                    color = 0xFFAA00;
                } else if (pct > 0.05) {
                    color = 0xFF5555;
                } else {
                    color = 0xAA0000;
                }

                final String duraStr = Integer.toString(dura);
                final int textX = alignRight ? baseX - 1 - 16 - 2 - font.getStringWidth(duraStr) : baseX + 1 + 16 + 2;
                font.drawString(duraStr, textX, yCur + 4, color);
            }
            yCur += step * (1 + 16);
        }
    }

    @Override
    public void onResize() {
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        final ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        // 5*16, 4 for padding, 3 for blanks
        height = 1000 * (80 + 4 + 3) / resolution.getScaledHeight();
        // 16 for icon, 2 for gap, 4 for padding
        width = 16 + 2 + 4 + font.getStringWidth("9999");
        width = 1000 * width / resolution.getScaledWidth();
        super.onResize();
    }

    @SubscribeEvent
    public void onTick(final ClientTickEvent event) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            int i = 0;
            for (final ItemStack item : Minecraft.getMinecraft().thePlayer.inventory.armorInventory) {
                renderList[3 - i++] = item;
            }
            renderList[4] = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
        }
    }

    @Override
    public void configure() {
        Minecraft.getMinecraft().displayGuiScreen(new WidgetConfigGUI(Minecraft.getMinecraft().currentScreen));
    }

    @Override
    public String serialize() {
        return super.serialize() + "," + alignRight + "," + alignTop;
    }

    @Override
    public int deserialize(final String line) {
        final int k = super.deserialize(line);
        final String[] data = line.split(",");
        alignRight = Boolean.parseBoolean(data[k + 1]);
        alignTop = Boolean.parseBoolean(data[k + 2]);
        return k + 2;
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
         * The button for the horizontal alignment.
         */
        private ButtonElement alignRightButton;

        /**
         * The button for the vertical alignment.
         */
        private ButtonElement alignTopButton;

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
            } else if (buttonElement == alignTopButton) {
                alignTop = !alignTop;
                alignTopButton.getButton().displayString = "Align Top: " + (alignTop ? "Yes" : "No");
            } else if (buttonElement == saveButton) {
                WidgetAPI.saveWidgets();
                closeGui();
            }
        }

        @Override
        protected void createGui() {
            setPadding(0.05, 0.05, 0.1, 0.05);

            addText(new Positioning().center()).setText("Armor Widget", 0xA0A0A0);
            addBlank(new Positioning().breakRow().absoluteHeight(25));

            alignRightButton = addButton(this,
                    new Positioning().center().relativeWidth(30).absoluteHeight(20));
            alignRightButton.getButton().displayString = "Align Right: " + (alignRight ? "Yes" : "No");
            addBlank(new Positioning().breakRow().absoluteHeight(15));

            alignTopButton = addButton(this,
                    new Positioning().center().relativeWidth(30).absoluteHeight(20));
            alignTopButton.getButton().displayString = "Align Top: " + (alignTop ? "Yes" : "No");

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
