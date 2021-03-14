package bin.Pages.Settings;

import bin.DB;
import bin.Data;
import bin.Pages.IPage;
import bin.Pages.PageManager;
import bin.Pages.Menu.Menu;
import bin.Pages.PageUtil;
import bin.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Settings implements IPage {
    private JPanel mainPanel;
    private JButton homeButton;

    private JButton addTestButton;
    private JButton removeTestButton;
    private JButton editTestButton;

    private JButton addTaskButton;
    private JButton removeTaskButton;
    private JButton editTaskButton;
    private JButton commentTaskButton;

    private JList<String> testsList;
    private JList<String> tasksList;

    public Settings() {
        homeButton.setIcon(new ImageIcon(""));
        homeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addTestButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editTestButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeTestButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        commentTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setActions();
        setKeyBindings();
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @Override
    public boolean update() {
        var taskMenuList = ((Menu) PageManager.getPage(PageManager.Page.Menu)).getTestsList();
        testsList.setModel(taskMenuList.getModel());
        testsList.setSelectedIndex(taskMenuList.getSelectedIndex());

        if (testsList.getSelectedIndex() == -1) {
            updateTasksList();
        } else {
            return updateTasksList(taskMenuList.getSelectedValue().trim());
        }

        return true;
    }

    public boolean updateTasksList(String dbName) {

        var model = new DefaultListModel<String>();

        if (dbName == null) {
            tasksList.setModel(model);
            return true;
        }

        int id = tasksList.getSelectedIndex();

        final java.util.List<Task> test;
        try {
            test = DB.getTest(dbName);
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
            DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Settings)));
            return false;
        }

        tasksList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            var taskWord = new JLabel();
            if (value != null) {

                taskWord.setOpaque(true);

                taskWord.setText(value);

                if (test.stream().anyMatch(task -> task.getWord().equals(value.trim()) && task.isComment())) {
                    if (isSelected)
                        taskWord.setBackground(Color.blue);
                    else
                        taskWord.setBackground(Color.white);
                    taskWord.setForeground(Color.lightGray);
                } else {
                    if (isSelected) {
                        taskWord.setBackground(Color.blue);
                        taskWord.setForeground(Color.white);
                    } else {
                        taskWord.setBackground(Color.white);
                        taskWord.setForeground(Color.black);
                    }
                }
            }
            return taskWord;
        });

        if (test.size() > 0)
            for (var task : test)
                model.add(0, " " + task.getWord() + " ");

        tasksList.setModel(model);

        if (test.size() > 0 && id == -1)
            tasksList.setSelectedIndex(0);
        else
            tasksList.setSelectedIndex(id);

        updateTasksList();

        return true;
    }

    public void setAddTestEnabled(boolean enabled) { addTestButton.setEnabled(enabled); }
    public void setRemoveTestEnabled(boolean enabled) { removeTestButton.setEnabled(enabled); }
    public void setEditTestEnabled(boolean enabled) { editTestButton.setEnabled(enabled); }

    public void setAddTaskEnabled(boolean enabled) { addTaskButton.setEnabled(enabled); }
    public void setRemoveTaskEnabled(boolean enabled) { removeTaskButton.setEnabled(enabled); }
    public void setEditTaskEnabled(boolean enabled) { editTaskButton.setEnabled(enabled); }
    public void setCommentTaskEnabled(boolean enabled) { commentTaskButton.setEnabled(enabled); }

    public void updateTasksList() {
        if (testsList.getSelectedIndex() == -1) {
            setAddTestEnabled(true);
            setEditTestEnabled(false);
            setRemoveTestEnabled(false);

            setAddTaskEnabled(false);
            setCommentTaskEnabled(false);
            setEditTaskEnabled(false);
            setRemoveTaskEnabled(false);
        } else if (tasksList.getSelectedIndex() == -1) {
            setAddTestEnabled(true);
            setEditTestEnabled(true);
            setRemoveTestEnabled(true);

            setAddTaskEnabled(true);
            setCommentTaskEnabled(false);
            setEditTaskEnabled(false);
            setRemoveTaskEnabled(false);
        } else {
            setAddTestEnabled(true);
            setEditTestEnabled(true);
            setRemoveTestEnabled(true);

            setAddTaskEnabled(true);
            setCommentTaskEnabled(true);
            setEditTaskEnabled(true);
            setRemoveTaskEnabled(true);
        }
    }

    public void resetRequest() { EventQueue.invokeLater(() -> homeButton.requestFocusInWindow()); }

    public String getTaskName() { return testsList.getSelectedValue().trim(); }

    private void setActions() {
        homeButton.addActionListener(e -> {
            PageManager.setPage(PageManager.Page.Menu);

            var testsMenuList = ((Menu) PageManager.getPage(PageManager.Page.Menu)).getTestsList();
            testsMenuList.setModel(testsList.getModel());
            testsMenuList.setSelectedIndex(testsList.getSelectedIndex());
        });

        testsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (testsList.getSelectedIndex() != -1) {
                    var selected = (String) testsList.getSelectedValue();
                    updateTasksList(selected.trim());

                    updateTasksList();
                    if (tasksList.getSelectedIndex() != -1)
                        tasksList.setSelectedIndex(0);
                }
            }
        });

        tasksList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    new bin.Pages.Settings.Components.Tasks.CommentComponent().doAction(tasksList);
                    updateTasksList(testsList.getSelectedValue().trim());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateTasksList();
            }
        });

        // tests

        addTestButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tests.AddComponent().doAction(testsList);
            updateTasksList();
        });

        removeTestButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tests.RemoveComponent().doAction(testsList);
            updateTasksList();
        });

        editTestButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tests.EditComponent().doAction(testsList);
            updateTasksList();
        });

        // tasks

        addTaskButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tasks.AddComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        commentTaskButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tasks.CommentComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        removeTaskButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tasks.RemoveComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        editTaskButton.addActionListener(e -> {
            new bin.Pages.Settings.Components.Tasks.EditComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });
    }

    private void setKeyBindings() {

        PageUtil.setKeyBinding(mainPanel, "exit", KeyEvent.VK_ESCAPE, 0, () -> {
            PageManager.setPage(PageManager.Page.Menu);

            var taskMenuList = ((Menu) PageManager.getPage(PageManager.Page.Menu)).getTestsList();
            taskMenuList.setModel(testsList.getModel());
            taskMenuList.setSelectedIndex(testsList.getSelectedIndex());
        });

        PageUtil.setKeyBinding(testsList, "add", KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, () -> {
            new bin.Pages.Settings.Components.Tests.AddComponent().doAction(testsList);
            updateTasksList();
        });

        PageUtil.setKeyBinding(testsList, "edit", KeyEvent.VK_SPACE, InputEvent.SHIFT_DOWN_MASK, () -> {
            new bin.Pages.Settings.Components.Tests.EditComponent().doAction(testsList);
            updateTasksList();
        });

        PageUtil.setKeyBinding(testsList, "remove", KeyEvent.VK_MINUS, InputEvent.SHIFT_DOWN_MASK, () -> {
            new bin.Pages.Settings.Components.Tests.RemoveComponent().doAction(testsList);
            updateTasksList();
        });

        PageUtil.setKeyBinding(testsList, "up", KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK, () -> {
            var model = (DefaultListModel<String>) testsList.getModel();
            if (model.size() != 0 && testsList.getSelectedIndex() != 0) {
                testsList.setSelectedIndex(testsList.getSelectedIndex() - 1);

                var selected = (String) testsList.getSelectedValue();
                updateTasksList(selected.trim());
                updateTasksList();

                if (tasksList.getSelectedIndex() != -1)
                    tasksList.setSelectedIndex(0);
            }
        });

        PageUtil.setKeyBinding(testsList, "down", KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK, () -> {
            var model = (DefaultListModel<String>) testsList.getModel();
            if (testsList.getSelectedIndex() < model.size()) {
                testsList.setSelectedIndex(testsList.getSelectedIndex() + 1);

                var selected = (String) testsList.getSelectedValue();
                updateTasksList(selected.trim());
                updateTasksList();

                if (tasksList.getSelectedIndex() != -1)
                    tasksList.setSelectedIndex(0);
            }
        });


        PageUtil.setKeyBinding(tasksList, "add", KeyEvent.VK_ENTER, 0, () -> {
            new bin.Pages.Settings.Components.Tasks.AddComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        PageUtil.setKeyBinding(tasksList, "edit", KeyEvent.VK_SPACE, 0, () -> {
            new bin.Pages.Settings.Components.Tasks.EditComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        PageUtil.setKeyBinding(tasksList, "remove", KeyEvent.VK_MINUS, 0, () -> {
            new bin.Pages.Settings.Components.Tasks.RemoveComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        PageUtil.setKeyBinding(tasksList, "comment", KeyEvent.VK_SLASH, 0, () -> {
            new bin.Pages.Settings.Components.Tasks.CommentComponent().doAction(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());
        });

        PageUtil.setKeyBinding(tasksList, "commentAll", KeyEvent.VK_SLASH, InputEvent.SHIFT_DOWN_MASK, () -> {
            int index = tasksList.getSelectedIndex();

            new bin.Pages.Settings.Components.Tasks.CommentComponent().doActionAll(tasksList);
            updateTasksList(testsList.getSelectedValue().trim());

            if (index != -1)
                tasksList.setSelectedIndex(index);
        });

        PageUtil.setKeyBinding(tasksList, "up", KeyEvent.VK_UP, 0, () -> {
            var model = (DefaultListModel<String>) tasksList.getModel();
            if (model.size() != 0 && tasksList.getSelectedIndex() != 0)
                tasksList.setSelectedIndex(tasksList.getSelectedIndex() - 1);
        });

        PageUtil.setKeyBinding(tasksList, "down", KeyEvent.VK_DOWN, 0, () -> {
            var model = (DefaultListModel<String>) tasksList.getModel();
            if (tasksList.getSelectedIndex() < model.size())
                tasksList.setSelectedIndex(tasksList.getSelectedIndex() + 1);
        });
    }
}
