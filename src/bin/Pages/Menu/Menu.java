package bin.Pages.Menu;

import bin.DB;
import bin.Data;
import bin.Pages.IPage;
import bin.Pages.PageManager;
import bin.Pages.PageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class Menu implements IPage {
    private JPanel mainPanel;
    private JButton settingsButton;
    private JButton startButton;

    private JList<String> testsList;

    public Menu() {
        settingsButton.setIcon(new ImageIcon(""));
        settingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setActions();
        setKeyBindings();

        updateList();
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    public boolean updateList() {
        List<String> tests;
        try {
            tests = DB.getTests();
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
            DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Menu)));
            return false;
        }

        int currentIndex = testsList.getSelectedIndex();

        var model = new DefaultListModel<String>();
        if (tests.size() > 0)
            for (var test : tests)
                model.add(0, "  " + test + "  ");

        testsList.setModel(model);

        if (currentIndex >= 0 && currentIndex < model.size())
            testsList.setSelectedIndex(currentIndex);

        return true;
    }

    public JList<String> getTestsList() {
        return testsList;
    }

    @Override
    public boolean update() {

        if (!updateList())
            return false;

        DefaultListModel<String> listModel = (DefaultListModel<String>) testsList.getModel();
        startButton.setEnabled(listModel.size() != 0);

        return true;
    }

    private void setActions() {
        startButton.addActionListener(e -> PageManager.setPage(PageManager.Page.Testing));
        settingsButton.addActionListener(e -> PageManager.setPage(PageManager.Page.Settings));
    }

    private void setKeyBindings() {
        PageUtil.setKeyBinding(mainPanel, "exit", KeyEvent.VK_ESCAPE, 0, () -> PageManager.setPage(PageManager.Page.Settings));
        PageUtil.setKeyBinding(mainPanel, "start", KeyEvent.VK_ENTER, 0, () -> PageManager.setPage(PageManager.Page.Testing));

        PageUtil.setKeyBinding(testsList, "up", KeyEvent.VK_UP, 0, () -> {
            var model = (DefaultListModel<String>) testsList.getModel();
            if (model.size() != 0 && testsList.getSelectedIndex() != 0)
                testsList.setSelectedIndex(testsList.getSelectedIndex() - 1);
        });
        PageUtil.setKeyBinding(testsList, "down", KeyEvent.VK_DOWN, 0, () -> {
            var model = (DefaultListModel<String>) testsList.getModel();
            if (testsList.getSelectedIndex() < model.size())
                testsList.setSelectedIndex(testsList.getSelectedIndex() + 1);
        });
    }
}
