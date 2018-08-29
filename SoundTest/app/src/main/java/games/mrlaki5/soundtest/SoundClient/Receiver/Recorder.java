package games.mrlaki5.soundtest.SoundClient.Receiver;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

public class Recorder {

    //Recorder parameters
    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    //Mono=8b, Stereo=16b
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    //16b or 8b per sample
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    //Number of samples in 1sec
    private int sampleRate = 44100;
    //Recording thread
    private Thread thread;
    //Callback used to set up filled buffer
    private Callback callback;

    public Recorder() {}

    public Recorder(Callback callback) {
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() {
        if (thread != null) return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                //Gets minimum buffer size (can be larger)
                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
                int optimalBufSize=12000;
                if(optimalBufSize<minBufferSize){
                    optimalBufSize=minBufferSize;
                }
                //Sets the chosen buffer size in analyzing (father) activity
                callback.setBufferSize(optimalBufSize);
                //Create recorder
                AudioRecord recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, optimalBufSize);
                if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    Thread.currentThread().interrupt();
                    return;
                } else {
                    Log.i(Recorder.class.getSimpleName(), "Started.");
                }
                byte[] buffer = new byte[optimalBufSize];
                //Start recording
                recorder.startRecording();
                while (thread != null && !thread.isInterrupted() && (recorder.read(buffer, 0, optimalBufSize)) > 0) {
                    callback.onBufferAvailable(buffer);
                }
                recorder.stop();
                recorder.release();
            }
        }, Recorder.class.getName());
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}