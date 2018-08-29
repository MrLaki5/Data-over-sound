package games.mrlaki5.soundtest.Chat;

//Message bean
public class Message {

    //Message content
    private String message;
    //User who sent message, 0-current user, 1-other user
    private int user;

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
