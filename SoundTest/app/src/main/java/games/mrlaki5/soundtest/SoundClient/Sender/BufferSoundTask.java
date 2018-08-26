package games.mrlaki5.soundtest.SoundClient.Sender;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ProgressBar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.mrlaki5.soundtest.AdaptiveHuffman.AdaptiveHuffmanCompress;
import games.mrlaki5.soundtest.AdaptiveHuffman.BitOutputStream;
import games.mrlaki5.soundtest.ReedSolomon.EncoderDecoder;
import games.mrlaki5.soundtest.SoundClient.BitFrequencyConverter;
import games.mrlaki5.soundtest.SoundClient.ByteArrayParser;
import games.mrlaki5.soundtest.SoundClient.CallbackSendRec;

public class BufferSoundTask extends AsyncTask<Integer, Integer, Void> {

    private boolean work=true;

    private double durationSec=0.270;//0.270;  //CAN PLAY ON 1.8 BUT NOT IN HAND, OPTIMAL 1.9

    private int sampleRate = 44100;
    private int bufferSize=0;

    private AudioTrack myTone=null;

    private byte[] message;
    private byte[] messageFile;
    private ProgressBar progressBar=null;
    private CallbackSendRec callbackSR;

    public void setBuffer(byte[] message){
        this.message=message;
    }

    public void setFileBuffer(byte[] messageFile){
        this.messageFile=messageFile;
    }

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar=progressBar;
    }

    public CallbackSendRec getCallbackSR() {
        return callbackSR;
    }

    public void setCallbackSR(CallbackSendRec callbackSR) {
        this.callbackSR = callbackSR;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        int startFreq=integers[0];
        int endFreq=integers[1];
        int bitsPerTone=integers[2];
        int encoding=integers[3];
        int errorDet=integers[4];
        int errorDetBNum=integers[5];
        BitFrequencyConverter bitConverter=new BitFrequencyConverter(startFreq, endFreq, bitsPerTone);



        byte[] encodedMessage=message;
        byte[] encodedMessageFile=messageFile;

        if(encoding==1) {
            InputStream in = new ByteArrayInputStream(encodedMessage);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BitOutputStream bitOut = new BitOutputStream(out);
            try {
                AdaptiveHuffmanCompress.compress(in, bitOut);
                bitOut.close();
                encodedMessage = out.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            finally {
                try {
                    in.close();
                    out.close();
                    bitOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(encodedMessageFile!=null) {
                in = new ByteArrayInputStream(encodedMessageFile);
                out = new ByteArrayOutputStream();
                bitOut = new BitOutputStream(out);
                try {
                    AdaptiveHuffmanCompress.compress(in, bitOut);
                    bitOut.close();
                    encodedMessageFile = out.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        in.close();
                        out.close();
                        bitOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if(errorDet==1){

            ByteArrayParser bParser=new ByteArrayParser();
            List<byte[]> tempList= bParser.devideInto256Chunks(encodedMessage, errorDetBNum);
            EncoderDecoder encoder = new EncoderDecoder();

            for(int i=0; i<tempList.size(); i++){
                try {
                    byte[] tempArr = encoder.encodeData(tempList.get(i), errorDetBNum);
                    bParser.mergeArray(tempArr);
                } catch (EncoderDecoder.DataTooLargeException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            encodedMessage=bParser.getAndResetOutputByteArray();

            if(encodedMessageFile!=null){
                tempList= bParser.devideInto256Chunks(encodedMessageFile, errorDetBNum);
                encoder = new EncoderDecoder();

                for(int i=0; i<tempList.size(); i++){
                    try {
                        byte[] tempArr = encoder.encodeData(tempList.get(i), errorDetBNum);
                        bParser.mergeArray(tempArr);
                    } catch (EncoderDecoder.DataTooLargeException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                encodedMessageFile=bParser.getAndResetOutputByteArray();
            }
        }
        if(encodedMessage==null){
            return null;
        }
        ArrayList<Integer> freqs=bitConverter.calculateFrequency(encodedMessage);
        ArrayList<Integer> freqsFile=null;
        if(encodedMessageFile!=null){
            freqsFile=bitConverter.calculateFrequency(encodedMessageFile);
        }
        if(!work){
            return null;
        }
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        myTone = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                AudioTrack.MODE_STREAM);
        myTone.play();
        int currProgress=0;
        int allLength=freqs.size()*2+4;
        if(freqsFile!=null){
            allLength+=freqsFile.size()*2+4;
        }
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        for (int freq: freqs) {
            //playTone((double)freq,durationSec);
            playTone((double)freq,durationSec/2);
            publishProgress(((++currProgress)*100)/allLength);
            playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
            publishProgress(((++currProgress)*100)/allLength);
            if(!work){
                myTone.release();
                return null;
            }
        }
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);

        if(freqsFile!=null){
            playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
            publishProgress(((++currProgress)*100)/allLength);
            playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
            publishProgress(((++currProgress)*100)/allLength);
            for (int freq: freqsFile) {
                //playTone((double)freq,durationSec);
                playTone((double)freq,durationSec/2);
                publishProgress(((++currProgress)*100)/allLength);
                playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
                publishProgress(((++currProgress)*100)/allLength);
                if(!work){
                    myTone.release();
                    return null;
                }
            }
            playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
            publishProgress(((++currProgress)*100)/allLength);
            playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
            publishProgress(((++currProgress)*100)/allLength);
        }
        myTone.release();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        super.onProgressUpdate(integers);
        if(progressBar!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(integers[0], true);
            } else {
                progressBar.setProgress(integers[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callbackSR.actionDone(CallbackSendRec.SEND_ACTION, null);
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
