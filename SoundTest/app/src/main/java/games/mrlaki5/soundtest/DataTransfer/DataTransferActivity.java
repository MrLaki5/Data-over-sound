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
import games.mrlaki5.soundtest.SoundClient.Sender.BufferSoundTask;

public class DataTransferActivity extends AppCompatActivity implements CallbackSendRec {

    private File currentFolder;
    private File rootFolder;
    private ListView myList;
    private View myView;
    private AlertDialog myDialog;

    private File sendFile=null;
    private File receiveFolder=null;

    boolean sendingData=false;

    private BufferSoundTask sendTask=null;
    private ProgressBar sendingBar=null;

    private AdapterView.OnItemClickListener adapSendListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File[] files= currentFolder.listFiles();
            if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
                if(position==0){
                    currentFolder=currentFolder.getParentFile();
                    loadAdapter();
                }
                else{
                    if(files[position-1].isDirectory()) {
                        currentFolder = files[position-1];
                        loadAdapter();
                    }
                    else{
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
            else{
                if(files[position].isDirectory()) {
                    currentFolder = files[position];
                    loadAdapter();
                }
                else{
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

    private AdapterView.OnItemClickListener adapReceiveListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File[] files= currentFolder.listFiles();
            if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
                if(position==0){
                    currentFolder=currentFolder.getParentFile();
                    loadAdapter();
                }
                else{
                    if(files[position-1].isDirectory()) {
                        currentFolder = files[position-1];
                        loadAdapter();
                    }
                }
            }
            else{
                if(files[position].isDirectory()) {
                    currentFolder = files[position];
                    loadAdapter();
                }
            }
        }
    };

    private View.OnClickListener receiveExplorerButtonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle("Data transfer");
        }

        sendingBar=((ProgressBar) findViewById(R.id.sendDataProgressBar));
    }

    private void browseFileExplorer(){
        currentFolder= new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        rootFolder=currentFolder;
        //Create dialog
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        //Load dialog view
        myView= getLayoutInflater().inflate(R.layout.dialog_file_explorer, null);
        //Add to buttons on dialog view click listeners
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

    private void loadAdapter(){

        File[] files= currentFolder.listFiles();
        ArrayList<FileExplorerElement> folders=new ArrayList<FileExplorerElement>();
        if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
            folders.add(new FileExplorerElement("Back", "", false, true));
        }
        for(int i=0; i<files.length; i++){
            String fileNameTmp=files[i].getName();
            String fileSizeTmp=""+files[i].length()+"B";
            boolean isFolder=files[i].isDirectory();
            folders.add(new FileExplorerElement(fileNameTmp, fileSizeTmp, !isFolder, false));
        }


        //Create score adapter with data score list
        FileExplorerAdapter adapter=new FileExplorerAdapter(DataTransferActivity.this, folders);
        //Add score adapter to view list
        myList.setAdapter(adapter);
    }

    public void sendData(View view) {
        if(sendFile==null){
            return;
        }
        if(!sendingData) {
            try {
                byte bytes[] = new byte[(int) sendFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sendFile));
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(bytes);

                sendingData=true;
                ((ProgressBar) findViewById(R.id.sendDataProgressBar)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.sendDataField)).setClickable(false);
                ((Button) view).setText("STOP");

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
        else{
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
            stopSend();
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    browseFileExplorer();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    browseFolderExplorer();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void actionDone(int srFlag, String message) {
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
    }

    @Override
    public void receivingSomething() {

    }
}
