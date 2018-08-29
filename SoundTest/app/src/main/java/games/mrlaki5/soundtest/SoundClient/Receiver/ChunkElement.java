package games.mrlaki5.soundtest.SoundClient.Receiver;

//Bean representation of one recorded data
public class ChunkElement {

    //Recorded data
    private byte[] buffer;

    public ChunkElement(byte[] buffer){
        this.buffer=buffer;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
