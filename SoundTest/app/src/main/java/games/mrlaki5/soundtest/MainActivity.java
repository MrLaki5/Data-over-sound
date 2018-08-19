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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void settings(View view) {
        Intent intent= new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void chatStart(View view) {
        Intent intent= new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    public void dataStart(View view) {
    }
}
