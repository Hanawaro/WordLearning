package bin.Pages;

import bin.Pages.Reconnect.Reconnect;
import bin.Pages.Menu.Menu;
import bin.Pages.Settings.Settings;
import bin.Pages.Testing.Testing;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class PageManager {

    private PageManager() {}

    private static JFrame frame = null;
    private static Map<Page, IPage> panels = null;
    private static boolean hasConnectionProblem = false;

    public enum Page {
        Reconnect, Menu, Settings, Testing
    }

    public static void init(JFrame frame) {
        PageManager.frame = frame;
        panels = new EnumMap<>(Page.class);

        panels.put(Page.Reconnect, new Reconnect());

        panels.put(Page.Menu, null);
        panels.put(Page.Settings, null);
        panels.put(Page.Testing, null);
    }

    public static void reset(Page pageName) {
        panels.put(Page.Menu, new Menu());
        panels.put(Page.Settings, new Settings());
        panels.put(Page.Testing, new Testing());

        setPage(pageName);
    }

    public static IPage getPage(Page pageName) {
        return panels.get(pageName);
    }

    public static void setPage(Page pageName) {
        if (panels.get(pageName).update())
            setCurrentPage(pageName);

        frame.revalidate();

    }

    public static void setPage(Runnable func) {
        func.run();
        frame.validate();
    }

    public static void setCurrentPage(Page pageName) {
        frame.setContentPane(panels.get(pageName).getPanel());
        hasConnectionProblem = (pageName == Page.Reconnect);
    }

    public static boolean hasProblem() {
        return hasConnectionProblem;
    }

    public static boolean isInitialized() {
        return frame != null && panels != null;
    }

}
