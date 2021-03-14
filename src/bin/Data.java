package bin;

import java.awt.*;

public class Data {

    public static class Window {
        public static final int width  = 800;
        public static final int height = 600;
        public static final boolean visible = true;
        public static final boolean resizable = false;
        public static final String title = "Words learning";
    }

    public static class Reconnection {
        public static final String pathGif = "/Users/hanawarokato/Documents/Development/2 course/java/SAOD Lab/src/Studying/src/loading3.gif";
        public static final double scaleGif = 2.2;
        public static final int scaleTypeGif = Image.SCALE_DEFAULT;
    }

    public static class DB {
        public static final String path =  "/Users/hanawarokato/Documents/Development/java/WordLearning/src/sqlite/info.db";
        public static final String className = "org.sqlite.JDBC";
        public static final String urlPrefix = "jdbc:sqlite:/";

        public static class Table {
            public static final String testsName = "tests";
            public enum TestsColumns {
                ID("id"), NAME("name");

                String name;
                TestsColumns(String name) { this.name = name; }
                @Override public String toString() { return name; }
            }

            public static final String wordsName = "words";
            public enum WordsColumns {
                TEST_ID("testID"), WORD_ID("wordID"), NAME("name"), IS_COMMENT("isComment");

                String name;
                WordsColumns(String name) { this.name = name; }
                @Override public String toString() { return name; }
            }

            public static final String translatesName = "translates";
            public enum TranslatesColumns {
                TEST_ID("testID"), WORD_ID("wordID"), NAME("name");

                String name;
                TranslatesColumns(String name) { this.name = name; }
                @Override public String toString() { return name; }
            }
        }
    }

    public static class Testing {
        public static final String checkButton    = "Проверить";
        public static final String continueButton = "Следующий";
        public static final String completeButton = "Завершить тест";
        public static final String successMessage = "Верно! - ";
        public static final String wrongMessage   = "Неправильно - ";

        public static final double accuracy = 0.8;
    }

    public static class Debug {
        private static final boolean isDebug = true;

        public static void debugMessage(String message) {
            if (isDebug)
                System.out.println(message);
        }
    }

}
