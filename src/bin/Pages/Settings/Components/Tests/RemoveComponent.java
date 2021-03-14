package bin.Pages.Settings.Components.Tests;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.Settings.Components.IComponent;
import bin.Pages.Settings.Settings;

import javax.swing.*;

public class RemoveComponent implements IComponent {
    @Override
    public void doAction(JList<String> testsList) {
        int id = testsList.getSelectedIndex();
        if (id != -1) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) testsList.getModel();

            // remove from db
            try {
                DB.removeTest(testsList.getSelectedValue().trim());
            } catch (Exception exception) {
                Data.Debug.debugMessage(exception.getMessage());
                DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Settings)));
                return;
            }
            PageManager.getPage(PageManager.Page.Menu).update();
            PageManager.getPage(PageManager.Page.Settings).update();

            // remove from list
            listModel.remove(id);

            var settings = (Settings) PageManager.getPage(PageManager.Page.Settings);
            if (listModel.size() == 0) {
                testsList.setSelectedIndex(-1);

                settings.updateTasksList(null);
                settings.updateTasksList();
                settings.resetRequest();
            } else {
                if (listModel.size() > id)
                    testsList.setSelectedIndex(id);
                else
                    testsList.setSelectedIndex(id - 1);

                settings.updateTasksList(testsList.getSelectedValue().trim());
                settings.updateTasksList();
            }
        }
    }
}
