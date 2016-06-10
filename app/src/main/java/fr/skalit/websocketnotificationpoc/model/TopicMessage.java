package fr.skalit.websocketnotificationpoc.model;

/**
 * Created by pascalvincent on 21/05/2016.
 */
class TopicMessage {
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    private String content;
    private String date;
    private String priority;

    public TopicMessage(String content, String date, String priority) {
        this.content = content;
        this.date = date;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return ("content : " + this.content) +
                ", date : " + this.date +
                ", priority" + this.priority;
    }

}
