package bin.Pages.Reconnect;

import bin.Data;
import bin.Pages.IPage;
import bin.Pages.PageManager;

import javax.swing.*;

public class Reconnect implements IPage {
    private JPanel mainPanel;
    private JLabel tryLabel;

    public Reconnect() {
        var image = new ImageIcon(Data.Reconnection.pathGif);
        var resizeImage = image.getImage().getScaledInstance(
                (int) (image.getIconWidth() / Data.Reconnection.scaleGif),
                (int) (image.getIconHeight() / Data.Reconnection.scaleGif),
                Data.Reconnection.scaleTypeGif
        );
        tryLabel.setIcon(new ImageIcon(resizeImage));
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @Override
    public boolean update() {
        tryLabel.setText(tryLabel.getText().replaceAll("[.]", ""));
        tryLabel.setText(tryLabel.getText() + ".");

        new Timer(800, e -> {
            if (!PageManager.hasProblem()) {
                ((Timer) e.getSource()).stop();
            } else {
                if (tryLabel.getText().contains("...")) {
                    tryLabel.setText(tryLabel.getText().replaceAll("[.]", ""));
                }
                tryLabel.setText(tryLabel.getText() + ".");
            }
        }).start();

        return true;
    }
}
