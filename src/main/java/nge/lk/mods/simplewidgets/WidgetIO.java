package nge.lk.mods.simplewidgets;

import lombok.RequiredArgsConstructor;
import nge.lk.mods.commonlib.util.DebugUtil;
import nge.lk.mods.commonlib.util.FileUtil;
import nge.lk.mods.simplewidgets.api.Widget;
import nge.lk.mods.simplewidgets.api.WidgetManager;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Handles input and output of widgets.
 */
@RequiredArgsConstructor
public class WidgetIO {

    /**
     * The widget save file.
     */
    private final File widgetFile;

    /**
     * The widget manager.
     */
    private final WidgetManager manager;

    /**
     * Loads all widgets from disk.
     */
    public void loadAll() {
        try {
            FileUtil.readLineStorage(widgetFile, (line, lineNo) -> {
                final String[] split = line.split(",");
                final String saveId = split[0];
                manager.provideSerializedWidget(saveId, line);
            }, (version, line) -> line);
        } catch (final IOException e) {
            DebugUtil.recoverableError(e);
        }
    }

    /**
     * Saves all widgets to disk.
     */
    public void saveAll(final Iterator<Widget> widgets) {
        try {
            FileUtil.writeLineStorage(Widget.WIDGET_VERSION, widgetFile, new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return widgets.hasNext();
                }

                @Override
                public String next() {
                    return widgets.next().serialize();
                }
            });
        } catch (final IOException e) {
            DebugUtil.recoverableError(e);
        }
    }
}
