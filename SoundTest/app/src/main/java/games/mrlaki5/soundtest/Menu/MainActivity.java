package games.mrlaki5.soundtest.Menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import games.mrlaki5.soundtest.Chat.ChatActivity;
import games.mrlaki5.soundtest.DataTransfer.DataTransferActivity;
import games.mrlaki5.soundtest.R;
import games.mrlaki5.soundtest.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
        //If values in preferences don't exist (on first start), create them
        if(!preferences.contains(SettingsActivity.KEY_START_FREQUENCY)){
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString(SettingsActivity.KEY_START_FREQUENCY,
                    SettingsActivity.DEF_START_FREQUENCY);
            editor.putString(SettingsActivity.KEY_END_FREQUENCY,
                    SettingsActivity.DEF_END_FREQUENCY);
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

    //Start settings activity
    public void settings(View view) {
        Intent intent= new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    //Start chat activity
    public void chatStart(View view) {
        Intent intent= new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    //Start data transfer activity
    public void dataStart(View view) {
        Intent intent= new Intent(MainActivity.this, DataTransferActivity.class);
        startActivity(intent);
    }
}
