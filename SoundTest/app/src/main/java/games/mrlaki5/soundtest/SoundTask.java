package games.mrlaki5.soundtest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class SoundTask extends AsyncTask<Void, Void, Void> {

    private boolean work=true;

    private int freq=10000;
    private int durationSec=1;

    private int sampleRate = 44100;
    private int bufferSize=0;

    private boolean changeNeeded=true;

    private AudioTrack myTone=null;

    @Override
    protected Void doInBackground(Void... voids) {
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        while(work){
            int tempFreq=0;
            synchronized (this){
                tempFreq=this.freq;
                if(changeNeeded){
                    myTone= new AudioTrack(AudioManager.STREAM_MUSIC,
                            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                            AudioTrack.MODE_STREAM);
                    myTone.play();
                    changeNeeded=false;
                }
            }
            playTone(tempFreq, durationSec);
        }
        return null;
    }

    public boolean isWork() {
        return work;
    }

    public void setWorkFalse() {
        this.work = false;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        synchronized (this){
            this.freq = freq;
            changeNeeded=true;
        }
    }

    public void playTone(double freqOfTone, double duration) {
        //double duration = 1000;                // seconds
        //   double freqOfTone = 1000;           // hz
                   // a number

        double dnumSamples = duration * sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];


        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i = 0 ;

        int ramp = numSamples / 20 ;                                    // Amplitude ramp as a percent of sample count


        for (i = 0; i< ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
            // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i/ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }


        for (i = i; i< numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = i; i< numSamples; ++i) {                               // Ramp amplitude down
            double dVal = sample[i];
            // Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples-i)/ramp ));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        //AudioTrack audioTrack = null;                                   // Get audio track
        try {

            //audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                   // sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                   // AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                   // AudioTrack.MODE_STREAM);
            //audioTrack.play();                                          // Play the track
            myTone.write(generatedSnd, 0, generatedSnd.length);     // Load the track
            //myTone.setLoopPoints(0, generatedSnd.length/4, -1);
        }
        catch (Exception e){
        }
        //if (audioTrack != null) audioTrack.release();           // Track play done. Release track.
    }

}
