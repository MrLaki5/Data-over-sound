package games.mrlaki5.soundtest.DataTransfer;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.R;

public class DataTransferActivity extends AppCompatActivity {

    private File currentFolder;
    private File rootFolder;
    private ListView myList;
    private View myView;
    private AlertDialog myDialog;

    private File sendFile=null;

    AdapterView.OnItemClickListener adapListener=new AdapterView.OnItemClickListener() {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle("Data transfer");
        }
        //myList=((ListView) findViewById(R.id.tempListView));
        //loadAdapter();
        //myList.setOnItemClickListener(adapListener);
    }

    public void browseFileExplorer(View view) {
        currentFolder= new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        rootFolder=currentFolder;
        //Create dialog
        AlertDialog.Builder mBulder= new AlertDialog.Builder(this);
        //Load dialog view
        myView= getLayoutInflater().inflate(R.layout.dialog_file_explorer, null);
        //Add to buttons on dialog view click listeners
        myList=((ListView) myView.findViewById(R.id.dialogFExFilesList));
        myList.setOnItemClickListener(adapListener);
        loadAdapter();
        //Set view of dialog
        mBulder.setView(myView);
        //Create and show dialog
        mBulder.setMessage("Choose file:");
        myDialog=mBulder.create();
        myDialog.show();
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

    private void loadSendFile(){

    }

    public void sendData(View view) {
        ((ProgressBar) findViewById(R.id.sendDataProgressBar)).setVisibility(View.VISIBLE);
        ((Button) view).setText("STOP");
    }
}
