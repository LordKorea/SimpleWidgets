package nge.lk.mods.simplewidgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import nge.lk.mods.commonlib.util.DebugUtil;
import nge.lk.mods.simplewidgets.api.Widget;
import nge.lk.mods.simplewidgets.api.WidgetAPI;
import nge.lk.mods.simplewidgets.api.WidgetManager;
import nge.lk.mods.simplewidgets.widgets.FPSWidget;
import nge.lk.mods.simplewidgets.widgets.FacingWidget;
import org.lwjgl.input.Keyboard;

import java.io.File;

import static nge.lk.mods.simplewidgets.SimpleWidgetsMod.MODID;
import static nge.lk.mods.simplewidgets.SimpleWidgetsMod.VERSION;

/**
 * Main mod class.
 */
@Mod(modid = MODID, version = VERSION, clientSideOnly = true)
public class SimpleWidgetsMod {

    /**
     * The ID of the mod.
     */
    public static final String MODID = "simplewidgets";

    /**
     * The version of the mod.
     */
    public static final String VERSION = "@VERSION@";

    /**
     * The manager for widget IO.
     */
    private WidgetIO widgetIO;

    /**
     * The widget manager.
     */
    private WidgetManager widgetManager;

    /**
     * The key binding for the editor.
     */
    private KeyBinding editorKey;

    /**
     * The last height that was stored.
     */
    private int lastHeight;

    /**
     * The last width that was stored.
     */
    private int lastWidth;

    @EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        DebugUtil.initializeLogger(MODID);
        widgetManager = new WidgetManager();
        widgetIO = new WidgetIO(new File(event.getModConfigurationDirectory(), "widgets.dat"), widgetManager);
        WidgetAPI.initialize(widgetManager);
    }

    @EventHandler
    public void onInit(final FMLInitializationEvent event) {
        widgetIO.loadAll();
        registerDefaultWidgets();
        editorKey = new KeyBinding("Widget Editor", Keyboard.KEY_F9, "Simple Widgets");
        ClientRegistry.registerKeyBinding(editorKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyPress(final KeyInputEvent event) {
        if (editorKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiHudEditor(widgetIO, widgetManager));
        }
    }

    @SubscribeEvent
    public void overlayRenderHook(final RenderGameOverlayEvent event) {
        // Hook directly after experience rendering
        if (event.isCancelable() || event.type != ElementType.EXPERIENCE) {
            return;
        }

        // Only render for GuiIngame (currentScreen is null)
        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }

        // Create a new matrix to draw on
        GlStateManager.pushMatrix();
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution resolution = new ScaledResolution(mc);
        if (mc.displayHeight != lastHeight || mc.displayWidth != lastWidth) {
            lastHeight = mc.displayHeight;
            lastWidth = mc.displayWidth;
            widgetManager.onResize();
        }
        widgetManager.renderAll(resolution);
        GlStateManager.popMatrix();
    }

    /**
     * Registers all default widgets.
     */
    private void registerDefaultWidgets() {
        final Widget fps = widgetManager.registerWidget(new FPSWidget(0, 0));
        final Widget facing = widgetManager.registerWidget(new FacingWidget(fps.getWidth(), 0));
    }
}
