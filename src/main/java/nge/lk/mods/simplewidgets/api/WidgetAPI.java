package nge.lk.mods.simplewidgets.api;

/**
 * The public API of the mod.
 */
public final class WidgetAPI {

    /**
     * The widget manager.
     */
    private static WidgetManager manager;

    /**
     * Initialize the API.
     *
     * @param mgr The widget manager.
     */
    public static void initialize(final WidgetManager mgr) {
        manager = mgr;
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
     * Private constructor to prevent instance creation.
     */
    private WidgetAPI() {
    }
}
