package games.mrlaki5.soundtest;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

//Activity for showing settings
public class SettingsActivity extends AppCompatActivity {

    //Volume key preference value
    public static String KEY_START_FREQUENCY="StFrequency";
    //Shake treshold key preference value
    public static String KEY_END_FREQUENCY="EndFrequency";
    //Time between two shake sensor events key preference value
    public static String KEY_BIT_PER_TONE="BitPerTone";


    //Volume default preference value
    public static int DEF_START_FREQUENCY=17500;
    //Shake treshold default preference value
    public static int DEF_END_FREQUENCY=20000;
    //Time between two shake sensor events default preference value
    public static int DEF_BIT_PER_TONE=4;


    //Shake treshold current preference value
    private int CurrentBitPerTone=0;


    //Shared preferences where settings are stored
    private SharedPreferences preferences;
    //Shared preferences editor used for editing preferences
    private SharedPreferences.Editor editor;

    //Shake treshold slider
    private SeekBar bitPerToneSlider;

    //Shake treshold Text View
    private TextView bitPerToneTW;

    private EditText startFreqET;
    private EditText endFreqET;

    //Slider listener for shake treshol
    private SeekBar.OnSeekBarChangeListener bitPerToneListener =
            new SeekBar.OnSeekBarChangeListener() {

                //Method called when user moves slider
                // updated continuously as the user slides the thumb
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Set new value to text view
                    //Set new value to current field
                    CurrentBitPerTone=progress+1;
                    bitPerToneTW.setText("Bits per tone: " + CurrentBitPerTone);
                }

                //Method called when the user first touches the SeekBar
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Blank
                }

                //Method called after the user finishes moving the SeekBar
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //Blank
                }
            };


    //Method called on creation of SettingsActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Part for removing status bar from screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        //Get values of shared preferences (game settings parameters)
        preferences = getSharedPreferences("Settings", 0);
        //Get shake treshold value
        CurrentBitPerTone=preferences.getInt(KEY_BIT_PER_TONE, DEF_BIT_PER_TONE);
        //Get time value between two shake sensor events
        int CurrStartFreq=preferences.getInt(KEY_START_FREQUENCY, DEF_START_FREQUENCY);
        //Get value of sound volume
        int CurrEndFreq=preferences.getInt(KEY_END_FREQUENCY, DEF_END_FREQUENCY);
        //Create shared preferences editor
        editor=preferences.edit();
        //Find shake treshold TextView on view and set text
        bitPerToneTW=findViewById(R.id.bitPerToneTW);
        int tempBPT=CurrentBitPerTone-1;
        bitPerToneTW.setText("Bits per tone: " + CurrentBitPerTone);

        //Find shake treshold slider on view and set listener and progress on slider
        bitPerToneSlider = findViewById(R.id.bitPerToneSeek);
        bitPerToneSlider.setOnSeekBarChangeListener(bitPerToneListener);
        bitPerToneSlider.setProgress(tempBPT);

        startFreqET=((EditText) findViewById(R.id.startFrequencyEdit));
        endFreqET=((EditText) findViewById(R.id.endFrequencyEdit));
        startFreqET.setText(""+CurrStartFreq);
        endFreqET.setText(""+CurrEndFreq);
    }

    //Method used for restoring default settings values
    public void restoreDef(View view) {
        editor.putInt(KEY_BIT_PER_TONE, DEF_BIT_PER_TONE);
        editor.putInt(KEY_START_FREQUENCY, DEF_START_FREQUENCY);
        editor.putInt(KEY_END_FREQUENCY, DEF_END_FREQUENCY);
        editor.commit();
        //Update current activity values to default ones
        CurrentBitPerTone=DEF_BIT_PER_TONE;
        //Update text views to default values
        int tempBPT=CurrentBitPerTone-1;
        bitPerToneTW.setText("Bits per tone: " + CurrentBitPerTone);
        //Update sliders to default values
        bitPerToneSlider.setProgress(tempBPT);
        startFreqET.setText(""+DEF_START_FREQUENCY);
        endFreqET.setText(""+DEF_END_FREQUENCY);
    }

    public void save(View view) {
        String stF=startFreqET.getText().toString();
        String endF=endFreqET.getText().toString();
        int stFI=Integer.parseInt(stF);
        int endFI=Integer.parseInt(endF);
        editor.putInt(KEY_START_FREQUENCY, stFI);
        editor.putInt(KEY_END_FREQUENCY, endFI);
        editor.putInt(KEY_BIT_PER_TONE, CurrentBitPerTone);
        editor.commit();
    }
}
