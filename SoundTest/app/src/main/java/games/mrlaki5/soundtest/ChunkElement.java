package games.mrlaki5.soundtest;

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
