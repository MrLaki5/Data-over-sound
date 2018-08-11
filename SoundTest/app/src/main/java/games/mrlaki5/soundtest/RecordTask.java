package games.mrlaki5.soundtest;

import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

public class RecordTask extends AsyncTask<Void, Double, Void> implements Callback{


    int bufferSizeInBytes = 0;       //JEDAN SHORT JE DVA BYTE 1024 short je 2048 byte
    //short[] buffer = new short[bufferSizeInBytes];

    private boolean work=true;

    private ArrayList<ChunkElement> recordedArray;
    private ArrayList<ChunkElement> forTransformationArray;
    private String forTransformationArraySem="Semaphore";
    private String recordedArraySem="Semaphore";

    private Recorder recorder=null;

    private TextView refreshTW;

    private String myString="";

    @Override
    protected Void doInBackground(Void... voids) {
        Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND + THREAD_PRIORITY_MORE_FAVORABLE);
        recordedArray=new ArrayList<ChunkElement>();

        recorder=new Recorder();
        recorder.setCallback(this);
        recorder.start();


        int listeningStarted=0;
        int startCounter=0;
        int endCounter=0;

        int currShift=0;
        byte currInfo=0;

        int lastInfo=2; //0 zero, 1 one, 2 none
        int lastCount=0;

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
                if((currNum>19900) && (currNum<20250)){
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
                if(currNum>=19900 && currNum<20250){
                    lastInfo=2;
                    lastCount=0;
                    Log.i(Recorder.class.getSimpleName(), "Synchronization bit");
                }
                else{
                    if(currNum>20900){
                        endCounter++;
                        if(endCounter>=3){
                            setWorkFalse();
                        }
                    }
                    else{
                        endCounter=0;
                        if(currNum>17900 && currNum<18250){
                            Log.i(Recorder.class.getSimpleName(), " freq: "+currNum + " putting 0");
                            if ((lastInfo!=0)){
                                Log.i(Recorder.class.getSimpleName(), " 0 put in a");
                                lastCount=0;
                                lastInfo=0;
                                currInfo<<=1;
                                currShift++;
                            }
                            /*if((lastInfo==0) && (lastCount==2)){
                                Log.i(Recorder.class.getSimpleName(), " 0 put in b");
                                lastCount=0;
                                currInfo<<=1;
                                currShift++;
                            }*/
                            lastCount++;
                        }
                        else{
                            if(currNum>18800 && currNum<19250){
                                Log.i(Recorder.class.getSimpleName(), " freq: "+currNum + " putting 1");
                                if ((lastInfo!=1)){
                                    Log.i(Recorder.class.getSimpleName(), " 1 put in a");
                                    lastCount=0;
                                    lastInfo=1;
                                    currInfo<<=1;
                                    currInfo|=0x01;
                                    currShift++;
                                }
                                /*if((lastInfo==1) && (lastCount==2)){
                                    Log.i(Recorder.class.getSimpleName(), " 1 put in b");
                                    lastCount=0;
                                    currInfo<<=1;
                                    currInfo|=0x01;
                                    currShift++;
                                }*/
                                lastCount++;
                            }
                        }
                        if(currShift==8){
                            byte[] tempArr= new byte[1];
                            tempArr[0]=currInfo;
                            currShift=0;
                            try {
                                String tempStr= new String(tempArr, "UTF-8");
                                Log.i(Recorder.class.getSimpleName(), " Transfered to: "+ tempStr);
                                myString+=tempStr;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }

        return null;
    }

    public double calculate(byte[] buffer) {

        //double[] magnitude = new double[bufferSizeInBytes / 4];

        //Create Complex array for use in FFT
        //Complex[] fftTempArray = new Complex[bufferSizeInBytes/ 2];

        Complex[] fftTempArray1= new Complex[1024];
        Complex[] fftTempArray2= new Complex[bufferSizeInBytes/4];

        double[] magnitude1 = new double[bufferSizeInBytes / 8];
        double[] magnitude2 = new double[bufferSizeInBytes / 8];

        int tempII=-1;

        int tempI=-1;
        for (int i = 0; i < bufferSizeInBytes; i+=2) {

            short buff = buffer[i + 1];
            short buff2 = buffer[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            short tempShort= (short) (buff | buff2);

            tempI++;

            //fftTempArray[tempI] = new Complex(tempShort, 0);

            if(tempI<1024){
                fftTempArray1[tempI]= new Complex(tempShort, 0);
            }
            else{
                i=bufferSizeInBytes;
                break;
              //  tempII++;
                //fftTempArray2[tempII]= new Complex(tempShort, 0);
            }
        }

        final  Complex[] fftArray1= FFT.fft(fftTempArray1);
        //final Complex[] fftArray2 = FFT.fft(fftTempArray2);

        int startIndex1=(15000*(1024))/44100;

        // calculate power spectrum (magnitude) values from fft[]
        for (int i = startIndex1; i < (512) - 1; ++i) {
            magnitude1[i] = fftArray1[i].abs();
            //magnitude2[i] = fftArray2[i].abs();
        }

        double max_magnitude1 = magnitude1[0];
        int max_index1 = 0;

        //double max_magnitude2 = magnitude2[0];
        //int max_index2 = 0;


        max_index1=startIndex1;
        max_magnitude1=(int)magnitude1[max_index1];
        //max_index2=startIndex1;
        //max_magnitude2=(int)magnitude2[max_index2];
        for (int i = startIndex1; i < magnitude1.length; ++i) {
            if (magnitude1[i] > max_magnitude1) {
                max_magnitude1 = (int) magnitude1[i];
                max_index1 = i;
            }
            //if (magnitude2[i] > max_magnitude2) {
              //  max_magnitude2 = (int) magnitude2[i];
                //max_index2 = i;
            //}
        }

        double freq1 = 44100 * max_index1 / (1024);//here will get frequency in hz like(17000,18000..etc)
        //double freq2 = 44100 * max_index2 / (bufferSizeInBytes/4);//here will get frequency in hz like(17000,18000..etc)

/*
        //Obtain array of FFT data
        final Complex[] fftArray = FFT.fft(fftTempArray);

        int startIndex=(15000*(bufferSizeInBytes/2))/44100;

        // calculate power spectrum (magnitude) values from fft[]
        for (int i = startIndex; i < (bufferSizeInBytes / 4) - 1; ++i) {
            magnitude[i] = fftArray[i].abs();
        }

        // find largest peak in power spectrum
        double max_magnitude = magnitude[0];
        int max_index = 0;



        max_index=startIndex;
        max_magnitude=(int)magnitude[max_index];
        for (int i = startIndex; i < magnitude.length; ++i) {
            if (magnitude[i] > max_magnitude) {
                max_magnitude = (int) magnitude[i];
                max_index = i;
            }
        }

        //for (int i = 0; i < magnitude.length; ++i) {
            //if (magnitude[i] > max_magnitude) {
              //  max_magnitude = (int) magnitude[i];
            //    max_index = i;
          //  }
        //}
        double freq = 44100 * max_index / (bufferSizeInBytes/2);//here will get frequency in hz like(17000,18000..etc)*/
        return freq1;
    }


    @Override
    public void onBufferAvailable(byte[] buffer) {
        synchronized (recordedArraySem){
            recordedArray.add(new ChunkElement(buffer));
            recordedArraySem.notifyAll();
            //Log.i(Recorder.class.getSimpleName(), " in queue: "+recordedArray.size());
            while(recordedArray.size()>100){
                try {
                    recordedArraySem.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ChunkElement getElementForCalculation(){
        ChunkElement temp=null;
        synchronized (forTransformationArraySem){
            while(forTransformationArray.isEmpty()){
                try {
                    forTransformationArraySem.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            temp= forTransformationArray.remove(0);
        }
        return temp;
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