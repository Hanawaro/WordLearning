package bin;

import java.util.ArrayList;
import java.util.List;

public class Task {

    private String word;
    private java.util.List<String> translates;
    private boolean isComment;

    public Task() {
        this.word = null;
        this.translates = new ArrayList<>();
        this.isComment = false;
    }

    public Task(String word, List<String> translates) {
        this.word = word;
        this.translates = translates;
        this.isComment = false;
    }

    public Task(String word, List<String> translates, boolean isComment) {
        this(word, translates);
        this.isComment = isComment;
    }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public List<String> getTranslates() { return translates; }
    public void addTranslate(String translate) { this.translates.add(translate); }
    public void setTranslates(List<String> translates) { this.translates = translates; }

    public boolean isComment() { return isComment; }
    public void setComment(boolean comment) { isComment = comment; }

    @Override
    public String toString() {
        return "Task {" +
                "word = '" + word + '\'' +
                ", translates = " + translates +
                " is " + (isComment ? "comment" : "uncomment") +
                '}';
    }
}