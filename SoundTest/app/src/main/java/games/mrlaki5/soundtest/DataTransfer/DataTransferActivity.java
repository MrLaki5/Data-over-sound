package games.mrlaki5.soundtest.DataTransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import games.mrlaki5.soundtest.R;

public class DataTransferActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle("Data transfer");
        }
    }
}
