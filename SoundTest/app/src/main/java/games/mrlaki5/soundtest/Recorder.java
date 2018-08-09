package games.mrlaki5.soundtest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

public class Recorder {

    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;
    private Thread thread;
    private Callback callback;

    public Recorder() {
    }

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

                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);

                int optimalBufSize=1;

                while(optimalBufSize<minBufferSize){
                    optimalBufSize<<=1;
                }

                optimalBufSize=16384;

                callback.setBufferSize(optimalBufSize);

                AudioRecord recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, optimalBufSize);

                if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    Thread.currentThread().interrupt();
                    return;
                } else {
                    Log.i(Recorder.class.getSimpleName(), "Started.");
                    //callback.onStart();
                }
                byte[] buffer = new byte[optimalBufSize];
                recorder.startRecording();

                int k=1;
                while (thread != null && !thread.isInterrupted() && (k=recorder.read(buffer, 0, optimalBufSize)) > 0) {
                    Log.i(Recorder.class.getSimpleName(), "Recorderd bits= "+k);
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