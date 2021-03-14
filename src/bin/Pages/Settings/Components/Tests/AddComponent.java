package bin.Pages.Settings.Components.Tests;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.Settings.Components.IComponent;
import bin.Pages.Settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class AddComponent implements IComponent {
    @Override
    public void doAction(JList<String> testsList) {
        final List<String> finalTests = new ArrayList<>();

        var model = (DefaultListModel<String>) testsList.getModel();
        for (int i = 0; i < model.size(); i++)
            finalTests.add(model.elementAt(i).trim());

        // create panel
        var label = new JLabel("Имя нового теста: ");

        var field = new JTextField();
        field.setPreferredSize(new Dimension(200, 30));

        var panel = new JPanel();
        panel.add(label);
        panel.add(field);

        // set panel, request and show
        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                field.requestFocusInWindow();
            }
        };

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // если имя зарезервировано
                (((JPanel) pane.getComponent(1)).getComponent(0)).setEnabled(!finalTests.contains(field.getText()));
            }
        });

        JDialog dialog = pane.createDialog("");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);

        if (pane.getValue() == Integer.valueOf(0)) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) testsList.getModel();

            if (!finalTests.contains(field.getText())) {
                // add to db
                try {
                    DB.addTest(field.getText());
                } catch (Exception exception) {
                    Data.Debug.debugMessage(exception.getMessage());
                    DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Settings)));
                    return;
                }

                // add to list
                listModel.add(0, "  " + field.getText() + "  ");
                testsList.setSelectedIndex(0);
                ((Settings) PageManager.getPage(PageManager.Page.Settings)).updateTasksList(field.getText());
            }
        }
    }
}
