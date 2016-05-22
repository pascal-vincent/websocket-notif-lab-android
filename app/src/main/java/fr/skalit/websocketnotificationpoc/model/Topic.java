package fr.skalit.websocketnotificationpoc.model;

import java.util.List;

/**
 * Created by pascalvincent on 17/05/2016.
 */
public class Topic {
    private String name;
    private String id;

    private TopicMessage message;
    private List<TopicMessage> messages;
    private String createdAt;
    private String updatedAt;

    public TopicMessage getMessage() {
        return message;
    }

    public void setMessage(TopicMessage message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TopicMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TopicMessage> messages) {
        this.messages = messages;
    }

    public Topic(String name, String id, String createdAt, String updatedAt, List<TopicMessage> messages) {
        this.name = name;
        this.id = id;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name : " + this.name);
        stringBuilder.append(", id : " + this.id);
        stringBuilder.append(", createdAt : " + this.createdAt);

        if(this.messages != null) {
            stringBuilder.append(", messages : [ ");
            for(TopicMessage topicMessage : this.messages) {
                stringBuilder.append(topicMessage.toString());
                stringBuilder.append(", ");
            }
            stringBuilder.append("]");
        }

        return stringBuilder.toString();
    }
}
