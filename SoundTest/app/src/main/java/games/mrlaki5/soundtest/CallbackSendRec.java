package games.mrlaki5.soundtest;

public interface CallbackSendRec {
    public void actionDone(int srFlag, String message);
    public static int SEND_ACTION=0;
    public static int RECEIVE_ACTION=1;
}
