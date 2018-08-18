package games.mrlaki5.soundtest.Chat;

public class Message {

    private String message;
    private int user;   //0-current user, 1-other user

    public Message(String message, int user) {
        this.message = message;
        this.user = user;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
