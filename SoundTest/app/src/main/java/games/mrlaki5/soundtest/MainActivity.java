package games.mrlaki5.soundtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

import games.mrlaki5.soundtest.Chat.ChatActivity;

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
        //Load preferences
        SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
        //If values in preferences dont exist (on first start), create them
        if(!preferences.contains(SettingsActivity.KEY_START_FREQUENCY)){
            SharedPreferences.Editor editor=preferences.edit();
            //Set shake treshold value
            editor.putString(SettingsActivity.KEY_START_FREQUENCY,
                    SettingsActivity.DEF_START_FREQUENCY);
            //Set time value between two shake sensor events
            editor.putString(SettingsActivity.KEY_END_FREQUENCY,
                    SettingsActivity.DEF_END_FREQUENCY);
            //Set value of sound volume
            editor.putString(SettingsActivity.KEY_BIT_PER_TONE,
                    SettingsActivity.DEF_BIT_PER_TONE);
            editor.putBoolean(SettingsActivity.KEY_ENCODING,
                    SettingsActivity.DEF_ENCODING);
            editor.putBoolean(SettingsActivity.KEY_ERROR_DETECTION,
                    SettingsActivity.DEF_ERROR_DETECTION);
            editor.putString(SettingsActivity.KEY_ERROR_BYTE_NUM,
                    SettingsActivity.DEF_ERROR_BYTE_NUM);
            editor.commit();
        }
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
                //SharedPreferences preferences = getSharedPreferences("Settings", 0);
                Integer[] tempArr= new Integer[6];
                SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
                tempArr[0]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_START_FREQUENCY,
                        SettingsActivity.DEF_START_FREQUENCY));
                tempArr[1]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_END_FREQUENCY,
                        SettingsActivity.DEF_END_FREQUENCY));
                tempArr[2]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_BIT_PER_TONE,
                        SettingsActivity.DEF_BIT_PER_TONE));
                if(preferences.getBoolean(SettingsActivity.KEY_ENCODING,
                        SettingsActivity.DEF_ENCODING)){
                    tempArr[3]=1;
                }
                else{
                    tempArr[3]=0;
                }
                if(preferences.getBoolean(SettingsActivity.KEY_ERROR_DETECTION,
                        SettingsActivity.DEF_ERROR_DETECTION)){
                    tempArr[4]=1;
                }
                else{
                    tempArr[4]=0;
                }
                tempArr[5]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_ERROR_BYTE_NUM,
                        SettingsActivity.DEF_ERROR_BYTE_NUM));
                taskList=new RecordTask();
                taskList.setTW(tw);
                //taskList.execute();
                if (Build.VERSION.SDK_INT >= 11) {
                    taskList.executeOnExecutor(taskList.THREAD_POOL_EXECUTOR, tempArr);
                } else {
                    taskList.execute(tempArr);
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
                Integer[] tempArr= new Integer[6];
                //SharedPreferences preferences = getSharedPreferences("Settings", 0);
                SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
                tempArr[0]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_START_FREQUENCY,
                        SettingsActivity.DEF_START_FREQUENCY));
                tempArr[1]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_END_FREQUENCY,
                        SettingsActivity.DEF_END_FREQUENCY));
                tempArr[2]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_BIT_PER_TONE,
                        SettingsActivity.DEF_BIT_PER_TONE));
                if(preferences.getBoolean(SettingsActivity.KEY_ENCODING,
                        SettingsActivity.DEF_ENCODING)){
                    tempArr[3]=1;
                }
                else{
                    tempArr[3]=0;
                }
                if(preferences.getBoolean(SettingsActivity.KEY_ERROR_DETECTION,
                        SettingsActivity.DEF_ERROR_DETECTION)){
                    tempArr[4]=1;
                }
                else{
                    tempArr[4]=0;
                }
                tempArr[5]=Integer.parseInt(preferences.getString(SettingsActivity.KEY_ERROR_BYTE_NUM,
                        SettingsActivity.DEF_ERROR_BYTE_NUM));
                buffSoundTask.execute(tempArr);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void settings(View view) {
        Intent intent= new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void chatStart(View view) {
        Intent intent= new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }
}
