package games.mrlaki5.soundtest.SoundClient.Receiver;

public class ChunkElement {

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
