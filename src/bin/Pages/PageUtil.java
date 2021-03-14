package bin.Pages;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PageUtil {
    public static void setKeyBinding(JComponent component, String bindName, int keyBind, int modifiers, Runnable runnable) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyBind, modifiers), bindName);
        component.getActionMap().put(bindName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    public static void setLocalKeyBinding(JComponent component, String bindName, int keyBind, int modifiers, Runnable runnable) {
        component.getInputMap().put(KeyStroke.getKeyStroke(keyBind, modifiers), bindName);
        component.getActionMap().put(bindName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }
}
