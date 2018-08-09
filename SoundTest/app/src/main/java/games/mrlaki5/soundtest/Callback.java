package games.mrlaki5.soundtest;

public interface Callback {
    void onBufferAvailable(byte[] buffer);

    void setBufferSize(int size);
}
