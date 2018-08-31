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

    //Work flag
    private boolean work=true;
    //Play time of one tone (can play on 0.18, optimal on 0.20, best on 0.27)
    private double durationSec=0.270;
    //Number of samples in 1s
    private int sampleRate = 44100;
    //Object for playing tones
    private AudioTrack myTone=null;
    //If chat text of message, if data name of file
    private byte[] message;
    //If chat null, if data context of file
    private byte[] messageFile;
    //Progress bar for publishing sent progress
    private ProgressBar progressBar=null;
    //Callback for activity to know sending is done
    private CallbackSendRec callbackSR;

    @Override
    protected Void doInBackground(Integer... integers) {
        //Load settings parameters
        int startFreq=integers[0];
        int endFreq=integers[1];
        int bitsPerTone=integers[2];
        int encoding=integers[3];
        int errorDet=integers[4];
        int errorDetBNum=integers[5];
        //Create bit to frequency converter
        BitFrequencyConverter bitConverter=new BitFrequencyConverter(startFreq, endFreq, bitsPerTone);
        byte[] encodedMessage=message;
        byte[] encodedMessageFile=messageFile;
        //If encoding is on
        if(encoding==1) {
            //Encode message with adaptive huffman
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
            //If its data transfer, encode data of file
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
        //If error detection is on
        if(errorDet==1){
            //Cut byte arrays to size of 256-NumberOfErrorBytes because ReedSolomons works only
            //in chunks of 256B
            ByteArrayParser bParser=new ByteArrayParser();
            List<byte[]> tempList= bParser.divideInto256Chunks(encodedMessage, errorDetBNum);
            EncoderDecoder encoder = new EncoderDecoder();
            //Encode byte arrays with Reed Solomon
            for(int i=0; i<tempList.size(); i++){
                try {
                    byte[] tempArr = encoder.encodeData(tempList.get(i), errorDetBNum);
                    bParser.mergeArray(tempArr);
                } catch (EncoderDecoder.DataTooLargeException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            //Merge encoded chunks
            encodedMessage=bParser.getAndResetOutputByteArray();
            //If file is send, do same for data of file
            if(encodedMessageFile!=null){
                tempList= bParser.divideInto256Chunks(encodedMessageFile, errorDetBNum);
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
        //Create object for playing tones
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        myTone = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                AudioTrack.MODE_STREAM);
        myTone.play();
        //Calculate number of tones to be played
        int currProgress=0;
        int allLength=freqs.size()*2+4;
        if(freqsFile!=null){
            allLength+=freqsFile.size()*2+4;
        }
        //Start communication with start handshake
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        playTone((double)bitConverter.getHandshakeStartFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        //Transfer message if chat and file extension if data
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
        //End communication with end handshake
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        playTone((double)bitConverter.getHandshakeEndFreq(), durationSec);
        publishProgress(((++currProgress)*100)/allLength);
        //If file is being send, send file data too
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


    //On update refresh progress bar to current state
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

    //After task finishes to inform callback activity
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(callbackSR!=null) {
            callbackSR.actionDone(CallbackSendRec.SEND_ACTION, null);
        }
    }

    //Called to play tone of specific frequency for specific duration
    public void playTone(double freqOfTone, double duration) {
        //Calculate number of samples in given duration
        double dnumSamples = duration * sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        //Every sample 16bit
        byte generatedSnd[] = new byte[2 * numSamples];
        //Fill the sample array with sin of given frequency
        double anglePadding = (freqOfTone * 2 * Math.PI) / (sampleRate);
        double angleCurrent = 0;
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(angleCurrent);
            angleCurrent += anglePadding;
        }
        //Convert to 16 bit pcm (pulse code modulation) sound array
        //assumes the sample buffer is normalized.
        int idx = 0;
        int i = 0 ;
        //Amplitude ramp as a percent of sample count
        int ramp = numSamples / 20 ;
        //Ramp amplitude up (to avoid clicks)
        for (i = 0; i< ramp; ++i) {
            double dVal = sample[i];
            //Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i/ramp));
            //In 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        // Max amplitude for most of the samples
        for (i = i; i< numSamples - ramp; ++i) {
            double dVal = sample[i];
            //Scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            //In 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        //Ramp amplitude down
        for (i = i; i< numSamples; ++i) {
            double dVal = sample[i];
            //Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples-i)/ramp ));
            //In 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        try {
            // Play the track
            myTone.write(generatedSnd, 0, generatedSnd.length);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

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

    public boolean isWork() {
        return work;
    }

    public void setWorkFalse() {
        this.work = false;
    }
}
