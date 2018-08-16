package games.mrlaki5.soundtest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import games.mrlaki5.soundtest.AdaptiveHuffman.AdaptiveHuffmanCompress;
import games.mrlaki5.soundtest.AdaptiveHuffman.BitOutputStream;

public class BufferSoundTask extends AsyncTask<Integer, Void, Void> {

    private boolean work=true;

    private double durationSec=0.270;//0.270;  //CAN PLAY ON 1.8 BUT NOT IN HAND, OPTIMAL 1.9

    private int sampleRate = 44100;
    private int bufferSize=0;

    private AudioTrack myTone=null;

    private byte[] message;

    public void setBuffer(byte[] message){
        this.message=message;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        int startFreq=integers[0];
        int endFreq=integers[1];
        int bitsPerTone=integers[2];
        int encoding=integers[3];
        BitFrequencyConverter bitConverter=new BitFrequencyConverter(startFreq, endFreq, bitsPerTone);

        byte[] encodedMessage=message;

        if(encoding==1) {
            encodedMessage = null;
            InputStream in = new ByteArrayInputStream(message);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BitOutputStream bitOut = new BitOutputStream(out);

            try {
                AdaptiveHuffmanCompress.compress(in, bitOut);
                bitOut.close();
                encodedMessage = out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (encodedMessage == null) {
                return null;
            }
        }
        ArrayList<Integer> freqs=bitConverter.calculateFrequency(encodedMessage);

        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        myTone = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                AudioTrack.MODE_STREAM);
        myTone.play();
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        for (int freq: freqs) {
            //playTone((double)freq,durationSec);
            playTone((double)freq,durationSec/2);
            playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        }
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        myTone.release();
        return null;
    }

    public boolean isWork() {
        return work;
    }

    public void setWorkFalse() {
        this.work = false;
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


        double anglePadding = (freqOfTone * 2 * Math.PI) / (sampleRate);
        double angleCurrent = 0;
        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = Math.sin(angleCurrent);
            angleCurrent += anglePadding;
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
            // Play the track
            myTone.write(generatedSnd, 0, generatedSnd.length);     // Load the track
        }
        catch (Exception e){

        }
    }

}
