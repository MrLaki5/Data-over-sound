package games.mrlaki5.soundtest.SoundClient.Receiver;

//Interface for father activity of recorder
public interface Callback {

    //Called when recorder finishes recording one byte array
    void onBufferAvailable(byte[] buffer);

    //Set size of byte arrays that recorder will produce
    void setBufferSize(int size);
}
