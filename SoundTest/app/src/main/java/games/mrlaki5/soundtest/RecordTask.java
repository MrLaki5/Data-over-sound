package games.mrlaki5.soundtest;

import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import games.mrlaki5.soundtest.AdaptiveHuffman.AdaptiveHuffmanDecompress;
import games.mrlaki5.soundtest.AdaptiveHuffman.BitInputStream;
import games.mrlaki5.soundtest.FFT.Complex;
import games.mrlaki5.soundtest.FFT.FFT;
import games.mrlaki5.soundtest.ReedSolomon.EncoderDecoder;
import games.mrlaki5.soundtest.ReedSolomon.ReedSolomonException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

public class RecordTask extends AsyncTask<Integer, Double, Void> implements Callback{

    int StartFrequency;
    int EndFrequency;
    int BitPerTone;
    int Encoding;

    int bufferSizeInBytes = 0;       //JEDAN SHORT JE DVA BYTE 1024 short je 2048 byte

    private boolean work=true;

    private ArrayList<ChunkElement> recordedArray;
    private String recordedArraySem="Semaphore";

    private Recorder recorder=null;

    private TextView refreshTW;

    private String myString="";

    private BitFrequencyConverter bitConverter;

    @Override
    protected Void doInBackground(Integer... integers) {
        Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND + THREAD_PRIORITY_MORE_FAVORABLE);

        StartFrequency=integers[0];
        EndFrequency=integers[1];
        BitPerTone=integers[2];
        Encoding=integers[3];

        recordedArray=new ArrayList<ChunkElement>();

        bitConverter=new BitFrequencyConverter(StartFrequency, EndFrequency, BitPerTone);

        int HalfPadd=bitConverter.getPadding()/2;
        int HandshakeStart=bitConverter.getHandshakeStartFreq();
        int HandshakeEnd=bitConverter.getHandshakeEndFreq();
        //int HandshakePadd=bitConverter.getHandshakePadding();

        recorder=new Recorder();
        recorder.setCallback(this);
        recorder.start();


        int listeningStarted=0;
        int startCounter=0;
        int endCounter=0;


        int lastInfo=2;

        myString="";

        while (work) {
            ChunkElement tempElem;
            synchronized (recordedArraySem){
                while (recordedArray.isEmpty()){
                    try {
                        recordedArraySem.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tempElem=recordedArray.remove(0);
                recordedArraySem.notifyAll();
            }
            double currNum=calculate(tempElem.getBuffer());
            publishProgress(currNum);

            if(listeningStarted==0){
                if((currNum>(HandshakeStart-HalfPadd)) && (currNum<(HandshakeStart+HalfPadd))){
                    startCounter++;
                    if(startCounter>=2){
                        listeningStarted=1;
                    }
                }
                else{
                    startCounter=0;
                }
            }
            else{
                if((currNum>(HandshakeStart-HalfPadd)) && (currNum<(HandshakeStart+HalfPadd))){
                    lastInfo=2;
                    Log.i(Recorder.class.getSimpleName(), "Synchronization bit");
                    endCounter=0;
                }
                else{
                    if(currNum>(HandshakeEnd-HalfPadd)){
                        endCounter++;
                        if(endCounter>=2){
                            setWorkFalse();
                        }
                    }
                    else{
                        //if(currNum>=16900 && currNum<=20250){
                        Log.i(Recorder.class.getSimpleName(), "income data: "+ currNum);
                        endCounter=0;
                        if(lastInfo!=0){
                            lastInfo=0;
                            Log.i(Recorder.class.getSimpleName(), "calculating for: "+ currNum);
                            bitConverter.calculateBits(currNum);
                        }
                        //}
                    }
                }
            }
        }
        byte[] readBytes=bitConverter.getReadBytes();
        try {
            if(Encoding==1) {
                /*
                InputStream in = new ByteArrayInputStream(readBytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    AdaptiveHuffmanDecompress.decompress(new BitInputStream(in), out);
                    readBytes = out.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                EncoderDecoder encoder = new EncoderDecoder();
                //final byte[] fec_payload;
                try {
                    readBytes = encoder.decodeData(readBytes, 4);
                } catch (Exception e) {

                }
            }
            myString= new String(readBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        publishProgress(10.0);
        return null;
    }

    //Method for calculating frequency with highest amplitude from sound sample
    public double calculate(byte[] buffer) {
        Complex[] fftTempArray1= new Complex[1024];
        int tempI=-1;
        //Convert sound sample from byte to Complex array
        for (int i = 0; i < 2048; i+=2) {
            short buff = buffer[i + 1];
            short buff2 = buffer[i];
            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);
            short tempShort= (short) (buff | buff2);
            tempI++;
            fftTempArray1[tempI]= new Complex(tempShort, 0);
        }
        //Do fast fourier transform
        final  Complex[] fftArray1= FFT.fft(fftTempArray1);
        //Calculate position in array where analyzing should start (high frequency filter)
        int startIndex1=((StartFrequency-100)*(1024))/44100;
        int max_index1 = startIndex1;
        double max_magnitude1 = (int)fftArray1[max_index1].abs();
        double tempMagnitude;
        //Find position of frequency with highest amplitude
        for (int i = startIndex1; i < (512) - 1; ++i) {
            tempMagnitude=fftArray1[i].abs();
            if(tempMagnitude > max_magnitude1){
                max_magnitude1 = (int) tempMagnitude;
                max_index1 = i;
            }
        }
        //Calculate frequency from position
        return 44100 * max_index1 / (1024);
    }


    @Override
    public void onBufferAvailable(byte[] buffer) {
        synchronized (recordedArraySem){
            recordedArray.add(new ChunkElement(buffer));
            recordedArraySem.notifyAll();
            Log.i(Recorder.class.getSimpleName(), " in queue: "+recordedArray.size());
            while(recordedArray.size()>100){
                try {
                    recordedArraySem.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setBufferSize(int size) {
        bufferSizeInBytes=size;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
        if(work){
            refreshTW.setText(""+values[0]+"Hz");
        }
        else{
            refreshTW.setText(myString);
        }
    }

    public void setTW(TextView tw){
        this.refreshTW=tw;
    }

    public void setWorkFalse(){
        if(recorder!=null){
            recorder.stop();
            recorder=null;
        }
        this.work=false;
    }

}