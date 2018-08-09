package games.mrlaki5.soundtest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private boolean isPlaing=false;
    private boolean isListening=false;
    private MediaPlayer mPlayer;
    private SoundTask task=null;
    private RecordTask taskList=null;
    private BufferSoundTask buffSoundTask=null;
    private int freq=10;

    private SeekBar.OnSeekBarChangeListener soundSeekListener =
            new SeekBar.OnSeekBarChangeListener() {

                //Method called when user moves slider
                // updated continuously as the user slides the thumb
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Set new value to text view
                    ((TextView) findViewById(R.id.soundFrequency)).setText("" + progress + "kHz");
                    //Set new value to current field
                    freq=progress;
                }

                //Method called when the user first touches the SeekBar
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Blank
                }

                //Method called after the user finishes moving the SeekBar
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(task!=null){
                        task.setFreq(freq*1000);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((SeekBar) findViewById(R.id.soundSlider)).setOnSeekBarChangeListener(soundSeekListener);

    }

    public void soundPlayStop(View view) {
        if(isListening){
            soundListen(findViewById(R.id.listenButt));
        }
        if(isPlaing){
            isPlaing=false;
            ((TextView) view).setText("Play");
            if(task!=null){
                task.setWorkFalse();
                task=null;
            }
        }
        else{
            isPlaing=true;
            ((TextView) view).setText("Stop");
            if(task==null){
                task=new SoundTask();
                task.setFreq(freq*1000);
                task.execute();
            }
        }
    }


    public void soundListen(View view) {
        if(isPlaing){
            soundPlayStop(findViewById(R.id.playButt));
        }
        if(isListening){
            isListening=false;
            ((TextView) view).setText("Listen");
            if(taskList!=null){
                taskList.setWorkFalse();
                taskList=null;
            }
        }
        else{
            isListening=true;
            ((TextView) view).setText("Stop");
            if(taskList==null){
                TextView tw=findViewById(R.id.currFreq);
                taskList=new RecordTask();
                taskList.setTW(tw);
                //taskList.execute();
                if (Build.VERSION.SDK_INT >= 11) {
                    taskList.executeOnExecutor(taskList.THREAD_POOL_EXECUTOR);
                } else {
                    taskList.execute();
                }
            }
        }
    }

    public void soundMessage(View view) {
        if(isListening){
            soundListen(findViewById(R.id.listenButt));
        }
        if(isPlaing){
            soundPlayStop(findViewById(R.id.playButt));
        }
        String messageStr=((EditText) findViewById(R.id.messageText)).getText().toString();
        if(!messageStr.isEmpty()){
            try {
                byte[] message=messageStr.getBytes("UTF-8");
                buffSoundTask=new BufferSoundTask();
                buffSoundTask.setBuffer(message);
                buffSoundTask.execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
