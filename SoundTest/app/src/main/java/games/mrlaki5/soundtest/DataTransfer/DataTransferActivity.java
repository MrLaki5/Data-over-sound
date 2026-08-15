package games.mrlaki5.soundtest.DataTransfer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import games.mrlaki5.soundtest.R;
import games.mrlaki5.soundtest.Settings.SettingsActivity;
import games.mrlaki5.soundtest.SoundClient.CallbackSendRec;
import games.mrlaki5.soundtest.SoundClient.DocumentUtils;
import games.mrlaki5.soundtest.SoundClient.Receiver.RecordTask;
import games.mrlaki5.soundtest.SoundClient.Sender.BufferSoundTask;

public class DataTransferActivity extends AppCompatActivity implements CallbackSendRec {
    //Request code of system picker for file that needs to be sent
    private static final int REQUEST_PICK_FILE=0;
    //Request code of system picker for folder where data is received
    private static final int REQUEST_PICK_FOLDER=1;
    //Request code of microphone permission, needed for receiving
    private static final int REQUEST_RECORD_AUDIO=2;
    //Extension sent for files that don't have one
    private static final String DEFAULT_EXTENSION="bin";
    //Size of chunk used while reading chosen file
    private static final int READ_BUFFER_SIZE=4096;

    //Document of file that needs to be send
    private Uri sendFile=null;
    //Document tree of folder where file is going to be received
    private Uri receiveFolder=null;
    //Is data being send flag
    boolean sendingData=false;
    //Is activity listening for data flag
    boolean listeningData=false;
    //Task for sending data
    private BufferSoundTask sendTask=null;
    //Progress bar for sending data task
    private ProgressBar sendingBar=null;
    //Task for receiving data
    private RecordTask listeningTask=null;

    //Called on stopping activity
    @Override
    protected void onStop() {
        super.onStop();
        //If listener or send task are still active, turn them off
        if(listeningTask!=null){
            stopListen();
            listeningTask.setWorkFalse();
        }
        if(sendTask!=null){
            stopSend();
            sendTask.setWorkFalse();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);
        //set title of action bar
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle(R.string.data_transfer);
        }
        sendingBar=(findViewById(R.id.sendDataProgressBar));
    }

    //Opens system file picker for choosing file that needs to be sent. System picker is used
    //because from Android 10 app can't browse storage of user on its own.
    public void browseFileExplorer(View view) {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startPicker(intent, REQUEST_PICK_FILE);
    }

    //Opens system folder picker for choosing folder where received data is saved
    public void browseFolderExplorer(View view){
        startPicker(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_PICK_FOLDER);
    }

    //Starts system picker, informs user if there is no app for choosing documents on device
    private void startPicker(Intent intent, int requestCode){
        try {
            startActivityForResult(intent, requestCode);
        }
        catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, R.string.no_file_manager, Toast.LENGTH_LONG).show();
        }
    }

    //Called when user chooses file or folder in system picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK || data==null || data.getData()==null){
            return;
        }
        Uri chosenUri=data.getData();
        if(requestCode==REQUEST_PICK_FILE){
            //Save chosen file and update GUI
            keepAccessToDocument(chosenUri, false);
            sendFile=chosenUri;
            String fileName=DocumentUtils.getDisplayName(getContentResolver(), chosenUri,
                    getString(R.string.chosen_file));
            ((TextView) findViewById(R.id.sendDataText)).setText(fileName);
            ImageView iv = findViewById(R.id.sendDataImage);
            iv.setImageResource(R.drawable.file_image);
            (findViewById(R.id.sendDataButt)).setVisibility(View.VISIBLE);
        }
        else{
            if(requestCode==REQUEST_PICK_FOLDER){
                //Save chosen folder and update GUI
                keepAccessToDocument(chosenUri, true);
                receiveFolder=chosenUri;
                ((TextView) findViewById(R.id.receiveDataText)).setText(getFolderName(chosenUri));
                ImageView iv = findViewById(R.id.receiveDataImage);
                iv.setImageResource(R.drawable.folder_image);
                (findViewById(R.id.receiveDataButt)).setVisibility(View.VISIBLE);
            }
        }
    }

    //Returns name that system shows for folder chosen in picker
    private String getFolderName(Uri treeUri){
        try {
            //Chosen folder is document tree, its name is kept on root document of that tree
            Uri folderUri=DocumentsContract.buildDocumentUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri));
            return DocumentUtils.getDisplayName(getContentResolver(), folderUri,
                    getString(R.string.chosen_folder));
        }
        catch (Exception e){
            e.printStackTrace();
            return getString(R.string.chosen_folder);
        }
    }

    //Called to keep access to chosen document, so it can be used after device is rotated.
    //File that is sent is only read, folder where data is received is also written in.
    private void keepAccessToDocument(Uri documentUri, boolean needsWrite){
        try {
            if(needsWrite){
                getContentResolver().takePersistableUriPermission(documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            else{
                getContentResolver().takePersistableUriPermission(documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        catch (SecurityException e){
            //Access was given only until activity is alive, that is enough for one transfer
            e.printStackTrace();
        }
    }

    //Called when data is send
    public void sendData(View view) {
        if(sendFile==null){
            return;
        }
        //If listening task is active turn it off
        if(listeningData){
            stopListen();
            if(listeningTask!=null){
                listeningTask.setWorkFalse();
            }
        }
        if(!sendingData) {
            //Load chosen file, if it can't be read inform user and stay in initial state
            byte[] bytes=readDocument(sendFile);
            if(bytes==null){
                Toast toast=Toast.makeText(this, R.string.file_read_error, Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            //Start sending file in send task, update GUI to send state
            try {
                sendingData=true;
                (findViewById(R.id.sendDataProgressBar)).setVisibility(View.VISIBLE);
                (findViewById(R.id.sendDataField)).setClickable(false);
                ((Button) view).setText(R.string.stop);
                //Send only extension of file from file name, faster sending
                String fileName=DocumentUtils.getDisplayName(getContentResolver(), sendFile, "");
                byte[] nameBytes=getExtension(fileName).getBytes("UTF-8");
                Integer[] sendArguments=getSettingsArguments();
                sendTask= new BufferSoundTask();
                sendTask.setProgressBar(sendingBar);
                sendTask.setCallbackSR(this);
                sendTask.setBuffer(nameBytes);
                sendTask.setFileBuffer(bytes);
                sendTask.execute(sendArguments);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        //Stop is pressed, turn off task and update GUI
        else{
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
            stopSend();
        }
    }

    //Reads whole content of chosen document, returns null if document can't be read
    private byte[] readDocument(Uri documentUri){
        InputStream in=null;
        try {
            in=getContentResolver().openInputStream(documentUri);
            if(in==null){
                return null;
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buffer=new byte[READ_BUFFER_SIZE];
            int readNum;
            while((readNum=in.read(buffer))!=-1){
                out.write(buffer, 0, readNum);
            }
            return out.toByteArray();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        finally {
            if(in!=null){
                try {
                    in.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //Returns extension of given file name, if file name doesn't have one default is returned
    private String getExtension(String fileName){
        int dotIndex=fileName.lastIndexOf('.');
        if(dotIndex>=0 && dotIndex<(fileName.length()-1)){
            return fileName.substring(dotIndex+1);
        }
        return DEFAULT_EXTENSION;
    }

    //Called to start listening for data
    public void listenData(View view) {
        if(receiveFolder==null){
            return;
        }
        //If sending task is active, turn it off
        if(sendingData){
            stopSend();
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
        }
        if(!listeningData) {
            //Recording needs microphone permission, listening starts after user grants it
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            }
            else {
                listen();
            }
        }
        else{
            //If listening task is active, turn it off (stop is pressed)
            if(listeningTask!=null){
                listeningTask.setWorkFalse();
            }
            stopListen();
        }
    }

    //Called to start listening task and refresh GUI
    private void listen(){
        if(receiveFolder==null){
            return;
        }
        try {
            listeningData=true;
            (findViewById(R.id.receiveDataField)).setClickable(false);
            ((Button) findViewById(R.id.receiveDataButt)).setText(R.string.stop);
            Integer[] sendArguments=getSettingsArguments();
            listeningTask=new RecordTask();
            listeningTask.setCallbackRet(this);
            listeningTask.setDestinationFolder(getApplicationContext().getContentResolver(),
                    receiveFolder);
            listeningTask.execute(sendArguments);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //Update GUI and flag to initial state from sending state
    private void stopSend(){
        sendingData=false;
        sendingBar.setVisibility(View.INVISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sendingBar.setProgress(1, true);
        }
        else{
            sendingBar.setProgress(1);
        }
        (findViewById(R.id.sendDataField)).setClickable(true);
        ((Button) findViewById(R.id.sendDataButt)).setText(R.string.send);
    }

    //Update GUI and flag to initial state from listening state
    private void stopListen(){
        listeningData=false;
        (findViewById(R.id.receiveDataTextReceive)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.receiveDataField)).setClickable(true);
        ((Button) findViewById(R.id.receiveDataButt)).setText(R.string.listen);
    }

    //Called to get parameters from settings preferences
    private Integer[] getSettingsArguments(){
        Integer[] tempArr = new Integer[6];
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tempArr[0] = Integer.parseInt(preferences.getString(SettingsActivity.KEY_START_FREQUENCY,
                SettingsActivity.DEF_START_FREQUENCY));
        tempArr[1] = Integer.parseInt(preferences.getString(SettingsActivity.KEY_END_FREQUENCY,
                SettingsActivity.DEF_END_FREQUENCY));
        tempArr[2] = Integer.parseInt(preferences.getString(SettingsActivity.KEY_BIT_PER_TONE,
                SettingsActivity.DEF_BIT_PER_TONE));
        if (preferences.getBoolean(SettingsActivity.KEY_ENCODING,
                SettingsActivity.DEF_ENCODING)) {
            tempArr[3] = 1;
        } else {
            tempArr[3] = 0;
        }
        if (preferences.getBoolean(SettingsActivity.KEY_ERROR_DETECTION,
                SettingsActivity.DEF_ERROR_DETECTION)) {
            tempArr[4] = 1;
        } else {
            tempArr[4] = 0;
        }
        tempArr[5] = Integer.parseInt(preferences.getString(SettingsActivity.KEY_ERROR_BYTE_NUM,
                SettingsActivity.DEF_ERROR_BYTE_NUM));
        return tempArr;
    }

    //Called when user answers on permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                //If user granted permission on mic, continue with listening
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listen();
                }
                else{
                    Toast toast=Toast.makeText(this, R.string.mic_permission_needed,
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            }
        }
    }

    //Called when sending task or receiving task have finished work
    @Override
    public void actionDone(int srFlag, String message) {
        //If its sending task and activity is still in sending mode
        if(CallbackSendRec.SEND_ACTION==srFlag && sendingData){
            stopSend();
            (findViewById(R.id.sendDataButt)).setVisibility(View.INVISIBLE);
            sendFile=null;
            ((TextView) findViewById(R.id.sendDataText)).setText(R.string.no_file_selected);
            ImageView iv = findViewById(R.id.sendDataImage);
            iv.setImageResource(R.drawable.file_image_grey);
            Toast toast=Toast.makeText(this, R.string.data_was_sent, Toast.LENGTH_LONG);
            toast.show();
        }
        //If its receiving task and activity is still in receiving mode
        else{
            if(CallbackSendRec.RECEIVE_ACTION==srFlag && listeningData){
                stopListen();
                //Data was received, but file couldn't be created in chosen folder
                if(message==null){
                    Toast toast=Toast.makeText(this, R.string.data_receive_error,
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                (findViewById(R.id.receiveDataButt)).setVisibility(View.INVISIBLE);
                receiveFolder=null;
                ((TextView) findViewById(R.id.receiveDataText)).setText(R.string.folder_not_selected);
                ImageView iv = findViewById(R.id.receiveDataImage);
                iv.setImageResource(R.drawable.folder_image_grey);
                String partRetStr=getResources().getString(R.string.data_received);
                Toast toast=Toast.makeText(this, partRetStr + " " + message, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    //Called when receiving task starts receiving message
    @Override
    public void receivingSomething() {
        (findViewById(R.id.receiveDataTextReceive)).setVisibility(View.VISIBLE);
    }
}
