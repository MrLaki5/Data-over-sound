package games.mrlaki5.soundtest.SoundClient.Receiver;

public interface Callback {
    void onBufferAvailable(byte[] buffer);

    void setBufferSize(int size);
}
