package bin;

import bin.Pages.PageManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        var entryPoint = new Main();
        entryPoint.start();
    }

    JFrame frame;

    public Main() {
        frame = new JFrame();
        frame.setVisible(Data.Window.visible);
        frame.setSize(Data.Window.width, Data.Window.height);
        frame.setTitle(Data.Window.title);
        frame.setResizable(Data.Window.resizable);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                DB.disconnect();
                frame.dispose();
                System.exit(0);
            }
        });
    }

    private void start() {
        try {
            PageManager.init(frame);
            DB.connect();
            PageManager.reset(PageManager.Page.Menu);
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
            DB.tryToConnect(() -> PageManager.reset(PageManager.Page.Menu));
        }
    }

}
