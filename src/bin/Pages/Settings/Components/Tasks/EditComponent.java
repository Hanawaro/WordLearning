package bin.Pages.Settings.Components.Tasks;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.PageUtil;
import bin.Pages.Settings.Components.IComponent;
import bin.Pages.Settings.Settings;
import bin.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditComponent implements IComponent {
    private TaskForm form = null;
    private JOptionPane pane = null;
    private final List<String> words = new ArrayList<>();
    private final Map<String, String> renameTranslates = new HashMap<>();

    @Override
    public void doAction(JList<String> list) {
        if (list.getSelectedIndex() == -1)
            return;

        words.clear();
        renameTranslates.clear();
        var model = (DefaultListModel<String>) list.getModel();
        for (int i = 0; i < model.size(); i++) {
            if (!list.getSelectedValue().trim().equals(model.elementAt(i).trim()))
                words.add(model.elementAt(i).trim());
        }

        form = new TaskForm();

        form.wordField.setText(list.getSelectedValue().trim());

        Task task;
        try {
            var dbName = ((Settings) PageManager.getPage(PageManager.Page.Settings)).getTaskName();
            task = DB.getTask(dbName, list.getSelectedValue().trim());
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
            DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Menu)));
            return;
        }

        final var newTranslatesModel = new DefaultListModel<String>();
        task.getTranslates().forEach(e -> newTranslatesModel.add(0, e));
        form.translatesList.setModel(newTranslatesModel);
        if (newTranslatesModel.size() > 0) {
            form.addButton.setEnabled(true);
            form.editButton.setEnabled(false);
            form.removeButton.setEnabled(false);
        } else {
            form.addButton.setEnabled(true);
            form.editButton.setEnabled(true);
            form.removeButton.setEnabled(true);
        }

        setActions();
        setKeyBindings();

        // set panel, request and show
        pane = new JOptionPane(form.mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                form.wordField.requestFocusInWindow();
            }
        };

        var dialog = pane.createDialog("");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);

        if (pane.getValue() == Integer.valueOf(0) && resetOkButton()) {

            var newTask = new Task();

            newTask.setWord(form.wordField.getText().trim());

            var translatesModel = (DefaultListModel<String>) form.translatesList.getModel();
            for (int i = 0; i < translatesModel.size(); i++)
                newTask.addTranslate(translatesModel.elementAt(i).trim());

            var dbName = ((Settings) PageManager.getPage(PageManager.Page.Settings)).getTaskName();

            try {
                DB.renameTask(dbName, task.getWord(), newTask.getWord());

                for (var translate : renameTranslates.entrySet()) {
                    DB.renameTranslate(dbName, newTask.getWord(), translate.getKey(), translate.getValue());
                }

                for (var translate : newTask.getTranslates()) {
                    if (!task.getTranslates().contains(translate)) {
                        DB.addTranslate(dbName, newTask.getWord(), translate);
                    }
                }
                for (var translate : task.getTranslates()) {
                    if (!newTask.getTranslates().contains(translate)) {
                        DB.removeTranslate(dbName, newTask.getWord(), translate);
                    }
                }
            } catch (Exception exception) {
                Data.Debug.debugMessage(exception.getMessage());
                DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Settings)));
            }
        }
    }

    private void add(JList<String> list) {
        final List<String> finalTests = new ArrayList<>();

        var model = (DefaultListModel<String>) list.getModel();
        for (int i = 0; i < model.size(); i++)
            finalTests.add(model.elementAt(i).trim());

        // create panel
        var label = new JLabel("Новый перевод: ");

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
                ((JPanel) pane.getComponent(1)).getComponent(0).setEnabled(!finalTests.contains(field.getText()));
            }
        });

        JDialog dialog = pane.createDialog("");
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);

        if (pane.getValue() == Integer.valueOf(0)) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();

            if (!finalTests.contains(field.getText())) {
                // add to list
                listModel.add(0, "  " + field.getText().trim() + "  ");
                list.setSelectedIndex(0);
                EventQueue.invokeLater(() -> form.mainPanel.requestFocusInWindow());

                form.addButton.setEnabled(true);
                form.editButton.setEnabled(true);
                form.removeButton.setEnabled(true);

                resetOkButton();
            }
        }
    }

    private void edit(JList<String> list) {
        int id = list.getSelectedIndex();
        if (id != -1) {
            final List<String> finalTests = new ArrayList<>();

            var model = (DefaultListModel<String>) list.getModel();
            for (int i = 0; i < model.size(); i++)
                if (!list.getSelectedValue().equals(model.elementAt(i)))
                    finalTests.add(model.elementAt(i).trim());

            // create panel
            var label = new JLabel("Перевод: ");

            var field = new JTextField(list.getSelectedValue().trim());
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
                    ((JPanel) pane.getComponent(1)).getComponent(0).setEnabled(!finalTests.contains(field.getText()));
                }
            });

            JDialog dialog = pane.createDialog("");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);

            if (pane.getValue() == Integer.valueOf(0)) {
                DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();

                if (!finalTests.contains(field.getText())) {
                    // edit from list
                    renameTranslates.put(list.getSelectedValue(), field.getText().trim());
                    listModel.set(list.getSelectedIndex(), "  " + field.getText().trim() + "  ");
                    EventQueue.invokeLater(() -> form.mainPanel.requestFocusInWindow());
                }
            }
        }
    }

    private void remove(JList<String> list) {
        int id = list.getSelectedIndex();
        if (id != -1) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();

            // remove from list
            listModel.remove(list.getSelectedIndex());
            EventQueue.invokeLater(() -> form.mainPanel.requestFocusInWindow());

            if (listModel.size() == 0) {
                list.setSelectedIndex(-1);

                form.addButton.setEnabled(true);
                form.editButton.setEnabled(false);
                form.removeButton.setEnabled(false);
            } else {
                if (listModel.size() > id)
                    list.setSelectedIndex(id);
                else
                    list.setSelectedIndex(id - 1);

                form.addButton.setEnabled(true);
                form.editButton.setEnabled(true);
                form.removeButton.setEnabled(true);
            }

            resetOkButton();
        }
    }

    private boolean resetOkButton() {
        if (form.wordField.getText().isEmpty() ||
                words.contains(form.wordField.getText().trim()) ||
                ((DefaultListModel<String>) form.translatesList.getModel()).size() == 0
        ) {
            ((JPanel) pane.getComponent(1)).getComponent(0).setEnabled(false);
            return false;
        } else {
            ((JPanel) pane.getComponent(1)).getComponent(0).setEnabled(true);
            return true;
        }
    }

    private void setActions() {
        form.wordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                resetOkButton();
            }
        });

        form.translatesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (form.translatesList.getSelectedIndex() != -1) {
                    form.addButton.setEnabled(true);
                    form.editButton.setEnabled(true);
                    form.removeButton.setEnabled(true);
                } else {
                    form.addButton.setEnabled(true);
                    form.editButton.setEnabled(false);
                    form.removeButton.setEnabled(false);
                }
            }
        });

        form.addButton.addActionListener(e -> add(form.translatesList));
        form.removeButton.addActionListener(e -> remove(form.translatesList));
        form.editButton.addActionListener(e -> edit(form.translatesList));
    }

    private void setKeyBindings() {
        PageUtil.setLocalKeyBinding(form.wordField, "exit", KeyEvent.VK_ENTER, 0, () -> {
            EventQueue.invokeLater(() -> form.mainPanel.requestFocusInWindow());
            var model = (DefaultListModel<String>) form.translatesList.getModel();
            if (model.size() > 0) {
                form.translatesList.setSelectedIndex(0);
                form.addButton.setEnabled(true);
                form.editButton.setEnabled(true);
                form.removeButton.setEnabled(true);
            } else {
                add(form.translatesList);
            }
        });

        PageUtil.setKeyBinding(form.translatesList, "add", KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, () -> add(form.translatesList));

        PageUtil.setKeyBinding(form.translatesList, "edit", KeyEvent.VK_SPACE, InputEvent.SHIFT_DOWN_MASK, () -> edit(form.translatesList));

        PageUtil.setKeyBinding(form.translatesList, "remove", KeyEvent.VK_MINUS, InputEvent.SHIFT_DOWN_MASK, () -> {
            remove(form.translatesList);
            if (((DefaultListModel<String>) form.translatesList.getModel()).size() == 0) {
                EventQueue.invokeLater(() -> form.wordField.requestFocusInWindow());
                form.translatesList.clearSelection();
                form.addButton.setEnabled(true);
                form.editButton.setEnabled(false);
                form.removeButton.setEnabled(false);
            }
        });

        PageUtil.setKeyBinding(form.translatesList, "up", KeyEvent.VK_UP, 0, () -> {
            var model = (DefaultListModel<String>) form.translatesList.getModel();
            if (model.size() > 0) {
                if (form.translatesList.getSelectedIndex() == 0) {
                    EventQueue.invokeLater(() -> form.wordField.requestFocusInWindow());
                    form.translatesList.clearSelection();
                    form.addButton.setEnabled(true);
                    form.editButton.setEnabled(false);
                    form.removeButton.setEnabled(false);
                } else
                    form.translatesList.setSelectedIndex(form.translatesList.getSelectedIndex() - 1);
            }
        });

        PageUtil.setKeyBinding(form.translatesList, "down", KeyEvent.VK_DOWN, 0, () -> {
            var model = (DefaultListModel<String>) form.translatesList.getModel();
            if (form.translatesList.getSelectedIndex() < model.size())
                form.translatesList.setSelectedIndex(form.translatesList.getSelectedIndex() + 1);
        });
    }
}
