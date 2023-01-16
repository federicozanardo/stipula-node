package models.dto.requests;

public class Message {
    private final String className;

    public Message(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "Message{" +
                "className='" + className + '\'' +
                '}';
    }
}
