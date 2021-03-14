package bin.Pages.Settings.Components.Tasks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TaskForm {
    JPanel mainPanel;

    JList<String> translatesList;
    JButton addButton;
    JButton editButton;
    JButton removeButton;
    JTextField wordField;

    public TaskForm() {
        ((DefaultListCellRenderer) translatesList.getCellRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        EventQueue.invokeLater(() -> wordField.requestFocusInWindow());

        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        translatesList.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(() -> mainPanel.requestFocusInWindow());
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }
}
