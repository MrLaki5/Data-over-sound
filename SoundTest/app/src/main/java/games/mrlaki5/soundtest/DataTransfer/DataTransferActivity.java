package games.mrlaki5.soundtest.DataTransfer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.R;
import games.mrlaki5.soundtest.Settings.SettingsActivity;
import games.mrlaki5.soundtest.SoundClient.CallbackSendRec;
import games.mrlaki5.soundtest.SoundClient.Receiver.RecordTask;
import games.mrlaki5.soundtest.SoundClient.Sender.BufferSoundTask;

public class DataTransferActivity extends AppCompatActivity implements CallbackSendRec {
    //File used in file browser for current place
    private File currentFolder;
    //File used in file browser for root folder
    private File rootFolder;
    //List for files and folders in file browser
    private ListView myList;
    //View for file browser dialog
    private View myView;
    //File browser dialog
    private AlertDialog myDialog;
    //File that needs to be send
    private File sendFile=null;
    //Folder where file is going to be received
    private File receiveFolder=null;
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

    //Listener for listView on browsing for file to be sent
    private AdapterView.OnItemClickListener adapSendListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File[] files= currentFolder.listFiles();
            if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
                //On all folders except root on first position is back
                if(position==0){
                    //back is pressed
                    currentFolder=currentFolder.getParentFile();
                    loadAdapter();
                }
                else{
                    //folder is pressed
                    if(files[position-1].isDirectory()) {
                        currentFolder = files[position-1];
                        loadAdapter();
                    }
                    else{
                        //file is pressed, disable dialog and save chosen file
                        sendFile=files[position-1];
                        ((TextView) findViewById(R.id.sendDataText)).setText(sendFile.getName());
                        ImageView iv = (ImageView) findViewById(R.id.sendDataImage);
                        iv.setImageResource(R.drawable.file_image);
                        ((Button) findViewById(R.id.sendDataButt)).setVisibility(View.VISIBLE);
                        if (myDialog != null){
                            myDialog.dismiss();
                            myDialog = null;
                            myView=null;
                        }
                    }
                }
            }
            //If its root folder
            else{
                //folder is pressed
                if(files[position].isDirectory()) {
                    currentFolder = files[position];
                    loadAdapter();
                }
                else{
                    //file is pressed, disable dialog and save chosen file
                    sendFile=files[position];
                    ((TextView) findViewById(R.id.sendDataText)).setText(sendFile.getName());
                    ImageView iv = (ImageView) findViewById(R.id.sendDataImage);
                    iv.setImageResource(R.drawable.file_image);
                    ((Button) findViewById(R.id.sendDataButt)).setVisibility(View.VISIBLE);
                    if (myDialog != null){
                        myDialog.dismiss();
                        myDialog = null;
                        myView=null;
                    }
                }
            }
        }
    };

    //Listener for listView on browsing receive folder
    private AdapterView.OnItemClickListener adapReceiveListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File[] files= currentFolder.listFiles();
            if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
                //On all folders except root on first position is back
                if(position==0){
                    //back is pressed
                    currentFolder=currentFolder.getParentFile();
                    loadAdapter();
                }
                else{
                    //folder is pressed
                    if(files[position-1].isDirectory()) {
                        currentFolder = files[position-1];
                        loadAdapter();
                    }
                }
            }
            //Its root folder
            else{
                //folder is pressed
                if(files[position].isDirectory()) {
                    currentFolder = files[position];
                    loadAdapter();
                }
            }
        }
    };

    //Listener for choosing receive folder in browse folder dialog
    private View.OnClickListener receiveExplorerButtonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Save current folder and disable dialog
            receiveFolder=currentFolder;
            ((TextView) findViewById(R.id.receiveDataText)).setText(receiveFolder.getName());
            ImageView iv = (ImageView) findViewById(R.id.receiveDataImage);
            iv.setImageResource(R.drawable.folder_image);
            ((Button) findViewById(R.id.receiveDataButt)).setVisibility(View.VISIBLE);
            if (myDialog != null){
                myDialog.dismiss();
                myDialog = null;
                myView=null;
            }
        }
    };

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
            ab.setTitle("Data transfer");
        }
        sendingBar=((ProgressBar) findViewById(R.id.sendDataProgressBar));
    }

    //Creates dialog for file explorer
    private void browseFileExplorer(){
        currentFolder= new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        rootFolder=currentFolder;
        //Create dialog
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        //Load dialog view
        myView= getLayoutInflater().inflate(R.layout.dialog_file_explorer, null);
        myList=((ListView) myView.findViewById(R.id.dialogFExFilesList));
        myList.setOnItemClickListener(adapSendListener);
        loadAdapter();
        //Set view of dialog
        mBulder.setView(myView);
        //Create and show dialog
        mBulder.setMessage("Choose file:");
        myDialog=mBulder.create();
        myDialog.show();
    }

    //Check if permission for storage are granted and activates dialog
    public void browseFileExplorer(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            browseFileExplorer();
        }
    }

    //Creates dialog for folder explorer
    private void browseFolderExplorer(){
        currentFolder= new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        rootFolder=currentFolder;
        //Create dialog
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        //Load dialog view
        myView= getLayoutInflater().inflate(R.layout.dialog_folder_explorer, null);
        //Add to buttons on dialog view click listeners
        myList=((ListView) myView.findViewById(R.id.dialogFolderExFilesList));
        myList.setOnItemClickListener(adapReceiveListener);
        ((Button) myView.findViewById(R.id.dialogFolderExButton)).setOnClickListener(receiveExplorerButtonListener);
        loadAdapter();
        //Set view of dialog
        mBulder.setView(myView);
        //Create and show dialog
        mBulder.setMessage("Choose folder:");
        myDialog=mBulder.create();
        myDialog.show();
    }

    //Check if permission for storage are granted and activates dialog
    public void browseFolderExplorer(View view){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            browseFolderExplorer();
        }
    }

    //Fills file browser view with files and folders from current folder
    private void loadAdapter(){
        File[] files= currentFolder.listFiles();
        ArrayList<FileExplorerElement> folders=new ArrayList<FileExplorerElement>();
        //if its not root folder add back option
        if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
            folders.add(new FileExplorerElement("Back", "", false, true));
        }
        for(int i=0; i<files.length; i++){
            String fileNameTmp=files[i].getName();
            String fileSizeTmp=""+files[i].length()+"B";
            boolean isFolder=files[i].isDirectory();
            folders.add(new FileExplorerElement(fileNameTmp, fileSizeTmp, !isFolder, false));
        }
        FileExplorerAdapter adapter=new FileExplorerAdapter(DataTransferActivity.this, folders);
        myList.setAdapter(adapter);
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
            //Load file and start sending it in send task, update GUI to send state
            try {
                byte bytes[] = new byte[(int) sendFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sendFile));
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(bytes);
                sendingData=true;
                ((ProgressBar) findViewById(R.id.sendDataProgressBar)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.sendDataField)).setClickable(false);
                ((Button) view).setText("STOP");
                //Send only extension of file from file name, faster sending
                String fileName=sendFile.getName();
                String tempStr[]=fileName.split("\\.");
                fileName=tempStr[tempStr.length-1];
                byte[] nameBytes=fileName.getBytes("UTF-8");
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
            //Start listening task and refresh GUI
            try {
                listeningData=true;
                ((LinearLayout) findViewById(R.id.receiveDataField)).setClickable(false);
                ((Button) view).setText("STOP");
                Integer[] sendArguments=getSettingsArguments();
                listeningTask=new RecordTask();
                listeningTask.setCallbackRet(this);
                listeningTask.setFileName(receiveFolder.getAbsolutePath());
                listeningTask.execute(sendArguments);
            }
            catch(Exception e){
                e.printStackTrace();
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
        ((LinearLayout) findViewById(R.id.sendDataField)).setClickable(true);
        ((Button) findViewById(R.id.sendDataButt)).setText("SEND");
    }

    //Update GUI and flag to initial state from listening state
    private void stopListen(){
        listeningData=false;
        ((TextView) findViewById(R.id.receiveDataTextReceive)).setVisibility(View.INVISIBLE);
        ((LinearLayout) findViewById(R.id.receiveDataField)).setClickable(true);
        ((Button) findViewById(R.id.receiveDataButt)).setText("Listen");
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
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                //Permission to storage granted on file browser
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    browseFileExplorer();
                }
                return;
            }
            case 1: {
                //Permission to storage granted on folder browser
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    browseFolderExplorer();
                }
                return;
            }
        }
    }

    //Called when sending task or receiving task have finished work
    @Override
    public void actionDone(int srFlag, String message) {
        //If its sending task and activity is still in sending mode
        if(CallbackSendRec.SEND_ACTION==srFlag && sendingData){
            stopSend();
            ((Button) findViewById(R.id.sendDataButt)).setVisibility(View.INVISIBLE);
            sendFile=null;
            ((TextView) findViewById(R.id.sendDataText)).setText("No file selected");
            ImageView iv = (ImageView) findViewById(R.id.sendDataImage);
            iv.setImageResource(R.drawable.file_image_grey);
            Toast toast=Toast.makeText(this, "Data was sent", Toast.LENGTH_LONG);
            toast.show();
        }
        //If its receiving task and activity is still in receiving mode
        else{
            if(CallbackSendRec.RECEIVE_ACTION==srFlag && listeningData){
                stopListen();
                ((Button) findViewById(R.id.receiveDataButt)).setVisibility(View.INVISIBLE);
                receiveFolder=null;
                ((TextView) findViewById(R.id.receiveDataText)).setText("Folder not selected");
                ImageView iv = (ImageView) findViewById(R.id.receiveDataImage);
                iv.setImageResource(R.drawable.folder_image_grey);
                Toast toast=Toast.makeText(this, "Data "+message+" received", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    //Called when receiving task starts receiving message
    @Override
    public void receivingSomething() {
        ((TextView) findViewById(R.id.receiveDataTextReceive)).setVisibility(View.VISIBLE);
    }
}
