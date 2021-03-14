package bin.Pages.Testing;

import bin.DB;
import bin.Data;
import bin.Pages.IPage;
import bin.Pages.PageManager;
import bin.Pages.Menu.Menu;
import bin.Pages.PageUtil;
import bin.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;

public class Testing implements IPage {

    private JPanel mainPanel;
    private JLabel titleLabel;
    private JButton homeButton;
    private JTextField inputField;
    private JButton enterButton;
    private JLabel messageLabel;
    private JLabel wordLabel;

    List<Task> test;
    private final Random random = new Random();
    private int indexOfWord;

    public Testing() {
        homeButton.setIcon(new ImageIcon(""));
        homeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        inputField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        setActions();
        setKeyBindings();
    }

    private void doNext() {
        if (enterButton.getText().equals(Data.Testing.completeButton)) {
            PageManager.setPage(PageManager.Page.Menu);
        } else if (enterButton.getText().equals(Data.Testing.continueButton)) {
            indexOfWord = random.nextInt(test.size());
            wordLabel.setText(test.get(indexOfWord).getWord());
            enterButton.setText(Data.Testing.checkButton);
            messageLabel.setText("");
            inputField.setText("");
        } else {
            if (check()) {
                StringBuilder message = new StringBuilder(Data.Testing.successMessage);

                int size = test.get(indexOfWord).getTranslates().size();
                for (int i = 0; i <  size - 1; i++)
                    message.append(test.get(indexOfWord).getTranslates().get(i)).append(", ");
                message.append(test.get(indexOfWord).getTranslates().get(size - 1));

                messageLabel.setText(message.toString());
                messageLabel.setForeground(new Color(35, 168, 95));
                test.remove(indexOfWord);
            } else {
                StringBuilder message = new StringBuilder(Data.Testing.wrongMessage);

                int size = test.get(indexOfWord).getTranslates().size();
                for (int i = 0; i <  size - 1; i++)
                    message.append(test.get(indexOfWord).getTranslates().get(i)).append(", ");
                message.append(test.get(indexOfWord).getTranslates().get(size - 1));

                messageLabel.setText(message.toString());
                messageLabel.setForeground(new Color(240, 41, 41));
            }
            enterButton.setText(Data.Testing.continueButton);

            if (test.size() == 0) {
                inputField.setEditable(false);
                enterButton.setText(Data.Testing.completeButton);
            }
        }
    }

    @Override
    public boolean update() {
        String testName = ((Menu) PageManager.getPage(PageManager.Page.Menu)).getTestsList().getSelectedValue();
        try {
            test = DB.getTest(testName.trim());
            test.removeIf(Task::isComment);
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
            DB.tryToConnect(() -> PageManager.setPage(PageManager.Page.Testing));
            return false;
        }

        titleLabel.setText(testName);
        enterButton.setText(Data.Testing.continueButton);

        if (test.size() != 0) {
            doNext();
        } else {
            JOptionPane.showMessageDialog(null, "Не удалось открыть тест", "", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        inputField.setEditable(true);
        EventQueue.invokeLater(() -> inputField.requestFocusInWindow());

        return true;
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    private void setActions() {
        homeButton.addActionListener(e -> PageManager.setPage(PageManager.Page.Menu));
        enterButton.addActionListener(e -> doNext() );
    }

    private void setKeyBindings() {
        PageUtil.setKeyBinding(mainPanel, "exit", KeyEvent.VK_ESCAPE, 0, () -> PageManager.setPage(PageManager.Page.Menu));
        PageUtil.setKeyBinding(mainPanel, "next", KeyEvent.VK_ENTER, 0, this::doNext);
    }

    private boolean check() {
        boolean result = false;
        for (var answer : test.get(indexOfWord).getTranslates()) {
            if (similarity(answer, inputField.getText()) >= Data.Testing.accuracy) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

}
