package bin;

import bin.Pages.PageManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB {

    private static Connection connection = null;

    private DB() {}

    public static void connect() throws Exception {

        Data.Debug.debugMessage("Try to connect...");

        if (!isDBExist())
            throw new Exception("Connection error");

        if (connection != null) {
            Data.Debug.debugMessage("Old connection is closed");
            connection.close();
        }

        Class.forName(Data.DB.className);
        var url = Data.DB.urlPrefix + Data.DB.path;
        connection = DriverManager.getConnection(url);

        Data.Debug.debugMessage("Connection success");
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException ignore) {}

        connection = null;

        Data.Debug.debugMessage("Connection is closed");
    }

    // tests table

    public static List<String> getTests() throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var result = new ArrayList<String>();

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName
        );

        while (tests.next())
            result.add(tests.getString("name"));

        return result;
    }

    public static void addTest(String testName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (!tests.next()) {
            connection.createStatement().executeUpdate(
                 "insert into " + Data.DB.Table.testsName +
                      " (" + Data.DB.Table.TestsColumns.NAME + ")" +
                      " values ('" + testName + "')"
            );
        }
    }

    public static void renameTest(String oldTestName, String newTestName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + newTestName + "'"
        );

        if (!tests.next()) {

            tests = connection.createStatement().executeQuery(
                    "select * from " + Data.DB.Table.testsName +
                         " where " + Data.DB.Table.TestsColumns.NAME + " = '" + oldTestName + "'"
            );

            if (tests.next()) {
                int id = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
                connection.createStatement().executeUpdate(
                        "update " + Data.DB.Table.testsName +
                             " set " + Data.DB.Table.TestsColumns.NAME + " = '" + newTestName + "'" +
                             " where " + Data.DB.Table.TestsColumns.ID + " = " + id
                );
            }

        }
    }

    public static void removeTest(String testName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int id = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            connection.createStatement().executeUpdate(
                "delete from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.ID + " = " + id
            );
            connection.createStatement().executeUpdate(
                 "delete from " + Data.DB.Table.wordsName +
                      " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + id
            );
            connection.createStatement().executeUpdate(
                 "delete from " + Data.DB.Table.translatesName +
                      " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + id
            );
        }
    }

    public static List<Task> getTest(String testName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        List<Task> result = new ArrayList<>();

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (!tests.next())
            return result;

        int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());

        var words = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.wordsName +
                     " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID
        );

        while (words.next()) {
            var tmpTask = new Task();
            setTask(tmpTask, testID, words);
            result.add(tmpTask);
        }

        return result;
    }

    // task (words and translates table) work

    public static void addTask(String testName, Task task) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var tasks = connection.createStatement().executeQuery(
                 "select * from " + Data.DB.Table.wordsName +
                      " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                      " and " + Data.DB.Table.WordsColumns.NAME + " = '" + task.getWord() + "'"
            );

            if (!tasks.next()) {
                connection.createStatement().executeUpdate(
                  "insert into " + Data.DB.Table.wordsName +
                       " ( " + Data.DB.Table.WordsColumns.TEST_ID + ", " + Data.DB.Table.WordsColumns.NAME + ", " + Data.DB.Table.WordsColumns.IS_COMMENT + " )" +
                       " values ( " + testID + ", '" + task.getWord() + "', " + (task.isComment() ? 1 : 0) + " )"
                );

                var word = connection.createStatement().executeQuery(
                  "select * from " + Data.DB.Table.wordsName +
                       " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                       " and " + Data.DB.Table.WordsColumns.NAME + " = '" + task.getWord() + "'"
                );

                if (word.next()) {
                    int wordID = word.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());

                    var testIDName = Data.DB.Table.TranslatesColumns.TEST_ID.toString();
                    var wordIDName = Data.DB.Table.TranslatesColumns.WORD_ID.toString();
                    var translateName = Data.DB.Table.TranslatesColumns.NAME.toString();

                    for (var translate : task.getTranslates()) {

                        var translates = connection.createStatement().executeQuery(
                             "select * from " + Data.DB.Table.translatesName +
                                  " where " + testIDName + " = " + testID +
                                  " and " + wordIDName + " = " + wordID +
                                  " and " + translateName + " = '" + translate + "'"
                        );

                        if (!translates.next()) {
                            connection.createStatement().executeUpdate(
                                    "insert into " + Data.DB.Table.translatesName +
                                         " (" + testIDName + ", " + wordIDName + ", " + translateName + " )" +
                                         " values (" + testID + ", " + wordID + ", '" + translate + "')"
                            );
                        }

                    }
                }
            }
        }
    }

    public static void renameTask(String testName, String oldWordName, String newWordName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                 "select * from " + Data.DB.Table.wordsName +
                      " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                      " and " + Data.DB.Table.WordsColumns.NAME + " = '" + oldWordName + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());
                connection.createStatement().executeUpdate(
                        "update " + Data.DB.Table.wordsName +
                             " set " + Data.DB.Table.WordsColumns.NAME + " = '" + newWordName + "'" +
                             " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                             " and " + Data.DB.Table.WordsColumns.WORD_ID + " = " + wordID
                );
            }
        }
    }

    public static void setComment(String testName, String taskName, boolean isComment) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                    "select * from " + Data.DB.Table.wordsName +
                         " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                         " and " + Data.DB.Table.WordsColumns.NAME + " = '" + taskName + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());
                connection.createStatement().executeUpdate(
                        "update " + Data.DB.Table.wordsName +
                                " set " + Data.DB.Table.WordsColumns.IS_COMMENT + " = " + (isComment ? 1 : 0) + "" +
                                " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                                " and " + Data.DB.Table.WordsColumns.WORD_ID + " = " + wordID
                );
            }
        }
    }

    public static void removeTask(String testName, String taskName) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                 "select * from " + Data.DB.Table.wordsName +
                      " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                      " and " + Data.DB.Table.WordsColumns.NAME + " = '" + taskName + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());
                connection.createStatement().executeUpdate(
                     "delete from " + Data.DB.Table.wordsName +
                          " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                          " and " + Data.DB.Table.WordsColumns.WORD_ID + " = " + wordID
                );
                connection.createStatement().executeUpdate(
                     "delete from " + Data.DB.Table.translatesName +
                          " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                          " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID
                );
            }
        }
    }

    public static Task getTask(String testName, String taskWord) throws Exception {

        if (!isConnect())
            throw new Exception("Connection error");

        var result = new Task();

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (!tests.next())
            return result;

        int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());

        var words = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.wordsName +
                     " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                     " and " + Data.DB.Table.WordsColumns.NAME + " = '" + taskWord + "'"
        );

        if (words.next())
            setTask(result, testID, words);

        return result;
    }

    // translate table

    public static void addTranslate(String testName, String word, String translate) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                 "select * from " + Data.DB.Table.wordsName +
                      " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                      " and " + Data.DB.Table.WordsColumns.NAME + " = '" + word + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());
                var translates = connection.createStatement().executeQuery(
                     "select * from " + Data.DB.Table.translatesName +
                             " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                             " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID +
                             " and " + Data.DB.Table.TranslatesColumns.NAME + " = '" + translate + "'"
                );

                if (!translates.next()) {

                    var testIDName = Data.DB.Table.TranslatesColumns.TEST_ID.toString();
                    var wordIDName = Data.DB.Table.TranslatesColumns.WORD_ID.toString();
                    var translateName = Data.DB.Table.TranslatesColumns.NAME.toString();

                    connection.createStatement().executeUpdate(
                            "insert into " + Data.DB.Table.translatesName +
                                 " (" + testIDName + ", " + wordIDName + ", " + translateName + " )" +
                                 " values (" + testID + ", " + wordID + ", '" + translate + "')"
                    );
                }
            }
        }

    }

    public static void renameTranslate(String testName, String word, String oldTranslate, String newTranslate) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                    "select * from " + Data.DB.Table.wordsName +
                         " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                         " and " + Data.DB.Table.WordsColumns.NAME + " = '" + word + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());

                var oldTranslates = connection.createStatement().executeQuery(
                        "select * from " + Data.DB.Table.translatesName +
                             " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                             " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID +
                             " and " + Data.DB.Table.TranslatesColumns.NAME + " = '" + oldTranslate + "'"
                );

                var newTranslates = connection.createStatement().executeQuery(
                        "select * from " + Data.DB.Table.translatesName +
                                " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                                " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID +
                                " and " + Data.DB.Table.TranslatesColumns.NAME + " = '" + newTranslate + "'"
                );

                if (oldTranslates.next() && !newTranslates.next()) {
                    connection.createStatement().executeUpdate(
                            "update " + Data.DB.Table.translatesName +
                                 " set " + Data.DB.Table.TranslatesColumns.NAME + " = '" + newTranslate + "'" +
                                 " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                                 " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID +
                                 " and " + Data.DB.Table.TranslatesColumns.NAME + " = '" + oldTranslate + "'"
                    );

                }
            }
        }
    }

    public static void removeTranslate(String testName, String word, String translate) throws Exception {
        if (!isConnect())
            throw new Exception("Connection error");

        var tests = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.testsName +
                     " where " + Data.DB.Table.TestsColumns.NAME + " = '" + testName + "'"
        );

        if (tests.next()) {
            int testID = tests.getInt(Data.DB.Table.TestsColumns.ID.toString());
            var words = connection.createStatement().executeQuery(
                    "select * from " + Data.DB.Table.wordsName +
                         " where " + Data.DB.Table.WordsColumns.TEST_ID + " = " + testID +
                         " and " + Data.DB.Table.WordsColumns.NAME + " = '" + word + "'"
            );

            if (words.next()) {
                int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());
                connection.createStatement().executeUpdate(
                        "delete from " + Data.DB.Table.translatesName +
                             " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                             " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID +
                             " and " + Data.DB.Table.TranslatesColumns.NAME + " = '" + translate + "'"
                );
            }
        }
    }

    private static void setTask(Task result, int testID, ResultSet words) throws Exception {
        result.setWord(words.getString(Data.DB.Table.WordsColumns.NAME.toString()));
        result.setComment(words.getInt(Data.DB.Table.WordsColumns.IS_COMMENT.toString()) != 0);

        int wordID = words.getInt(Data.DB.Table.WordsColumns.WORD_ID.toString());

        var translations = connection.createStatement().executeQuery(
                "select * from " + Data.DB.Table.translatesName +
                     " where " + Data.DB.Table.TranslatesColumns.TEST_ID + " = " + testID +
                     " and " + Data.DB.Table.TranslatesColumns.WORD_ID + " = " + wordID
        );


        while (translations.next()) {
            String translate = translations.getString(Data.DB.Table.TranslatesColumns.NAME.toString());
            result.addTranslate(translate);
        }
    }

    // reconnection

    public static void tryToConnect(Runnable action) {
        EventQueue.invokeLater(() -> {
            if (PageManager.isInitialized())
                PageManager.setPage(PageManager.Page.Reconnect);
        });

        var timer = new Timer(1000, e -> {
            try {
                DB.connect();
                ((Timer) e.getSource()).stop();
            } catch (Exception ignore) {
                int time = ((Timer) e.getSource()).getDelay();
                if (time <= 30_000)
                    ((Timer) e.getSource()).setDelay((int) (time * 1.5));
            }

        });
        timer.start();

        SwingWorker<String, String> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                while (timer.isRunning()) { }
                return null;
            }

            @Override
            protected void done() {
                action.run();
            }

        };
        worker.execute();
    }

    public static boolean isConnect() {
        try {
            if (!isDBExist())
                return false;
            if (connection != null && !connection.isClosed())
                return true;
        } catch (SQLException exception) {
            Data.Debug.debugMessage(exception.getMessage());
        }

        return false;
    }

    private static boolean isDBExist() {
        var file = new File(Data.DB.path);
        return file.exists() && !file.isDirectory();
    }

    public static void main(String[] args) {
        try {
            connect();

            setComment("unitTest2", "task1", true);

            disconnect();
        } catch (Exception exception) {
            Data.Debug.debugMessage(exception.getMessage());
        }
    }

}
