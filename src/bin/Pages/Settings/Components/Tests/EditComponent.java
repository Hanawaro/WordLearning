package bin.Pages.Settings.Components.Tests;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.Settings.Components.IComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class EditComponent implements IComponent {
    @Override
    public void doAction(JList<String> testsList) {
        int id = testsList.getSelectedIndex();
        if (id != -1) {
            final List<String> finalTests = new ArrayList<>();

            var model = (DefaultListModel<String>) testsList.getModel();
            for (int i = 0; i < model.size(); i++)
                if (!testsList.getSelectedValue().equals(model.elementAt(i)))
                    finalTests.add(model.elementAt(i).trim());


            // create panel
            var label = new JLabel("Новое имя теста: ");

            var field = new JTextField(testsList.getSelectedValue().trim());
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
                    // rename from db
                    try {
                        DB.renameTest(testsList.getSelectedValue().trim(), field.getText());
                    } catch (Exception exception) {
                        Data.Debug.debugMessage(exception.getMessage());
                        DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Settings)));
                        return;
                    }
                    PageManager.getPage(PageManager.Page.Menu).update();
                    PageManager.getPage(PageManager.Page.Settings).update();

                    // rename from list
                    listModel.set(id, "  " + field.getText() + "  ");
                }
            }
        }
    }
}
