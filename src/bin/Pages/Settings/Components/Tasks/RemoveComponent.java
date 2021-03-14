package bin.Pages.Settings.Components.Tasks;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.Settings.Components.IComponent;
import bin.Pages.Settings.Settings;

import javax.swing.*;

public class RemoveComponent implements IComponent {
    @Override
    public void doAction(JList<String> list) {
        int id = list.getSelectedIndex();
        if (id != -1) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();

            // remove from table
            try {
                var dbName = ((Settings) PageManager.getPage(PageManager.Page.Settings)).getTaskName();
                DB.removeTask(dbName, list.getSelectedValue().trim());
            } catch (Exception exception) {
                Data.Debug.debugMessage(exception.getMessage());
                DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Menu)));
                return;
            }

            // remove from list
            listModel.remove(list.getSelectedIndex());

            var settings = (Settings) PageManager.getPage(PageManager.Page.Settings);
            if (listModel.size() == 0) {
                list.setSelectedIndex(-1);
                list.setModel(new DefaultListModel<>());

                settings.updateTasksList();
                settings.resetRequest();
            } else {
                if (listModel.size() > id) {
                    list.setSelectedIndex(id);
                } else {
                    list.setSelectedIndex(id - 1);
                }
            }
        }
    }
}
