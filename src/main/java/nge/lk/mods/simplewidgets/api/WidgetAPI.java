package nge.lk.mods.simplewidgets.api;

import nge.lk.mods.simplewidgets.WidgetIO;
import nge.lk.mods.simplewidgets.WidgetManager;

/**
 * The public API of the mod.
 */
public final class WidgetAPI {

    /**
     * The widget manager.
     */
    private static WidgetManager manager;

    /**
     * Manages widget IO.
     */
    private static WidgetIO widgetIO;

    /**
     * Initialize the API. Not intended to be called by users of the API.
     *
     * @param mgr The widget manager.
     */
    public static void initialize(final WidgetManager mgr, final WidgetIO io) {
        manager = mgr;
        widgetIO = io;
    }

    /**
     * Registers a widget.
     *
     * @param widget The widget to register.
     */
    public static void registerWidget(final Widget widget) {
        manager.registerWidget(widget);
    }

    /**
     * Get a widget by its save ID.
     *
     * @param saveId The save ID.
     *
     * @return The widget, or null.
     */
    public static Widget getWidget(final String saveId) {
        return manager.getWidget(saveId);
    }

    /**
     * Saves all widgets.
     */
    public static void saveWidgets() {
        widgetIO.saveAll(manager.getAll());
    }

    /**
     * Private constructor to prevent instance creation.
     */
    private WidgetAPI() {
    }
}
