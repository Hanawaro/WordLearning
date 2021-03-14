package bin.Pages.Settings.Components.Tasks;

import bin.DB;
import bin.Data;
import bin.Pages.PageManager;
import bin.Pages.Settings.Components.IComponent;
import bin.Pages.Settings.Settings;
import bin.Task;

import javax.swing.*;

public class CommentComponent implements IComponent {
    @Override
    public void doAction(JList<String> list) {
        int id = list.getSelectedIndex();
        if (id != -1) {
            var dbName = ((Settings) PageManager.getPage(PageManager.Page.Settings)).getTaskName();
            try {
                var task = DB.getTask(dbName, list.getSelectedValue().trim());
                DB.setComment(dbName, task.getWord(), !task.isComment());
            } catch (Exception exception) {
                Data.Debug.debugMessage(exception.getMessage());
                DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Menu)));
            }
        }
    }

    public void doActionAll(JList<String> list) {
        int id = list.getSelectedIndex();
        if (id != -1) {
            var dbName = ((Settings) PageManager.getPage(PageManager.Page.Settings)).getTaskName();
            try {
                var test = DB.getTest(dbName);

                if (test.stream().anyMatch(Task::isComment)) {
                    for (var task : test)
                        if (task.isComment())
                            DB.setComment(dbName, task.getWord(), false);
                } else {
                    for (var task : test)
                        DB.setComment(dbName, task.getWord(), true);
                }
            } catch (Exception exception) {
                Data.Debug.debugMessage(exception.getMessage());
                DB.tryToConnect(() -> PageManager.setPage(() -> PageManager.setCurrentPage(PageManager.Page.Menu)));
            }
        }
    }
}
