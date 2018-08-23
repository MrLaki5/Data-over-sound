package games.mrlaki5.soundtest.DataTransfer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.R;

public class DataTransferActivity extends AppCompatActivity {

    File currentFolder;
    File rootFolder;
    ListView myList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle("Data transfer");
        }
        currentFolder= new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        rootFolder=currentFolder;
        myList=((ListView) findViewById(R.id.tempListView));
        loadAdapter();
        myList.setOnItemClickListener(adapListener);
    }

    public void someFun(View view) {
        File[] files= currentFolder.listFiles();
        if(files.length>=1) {
            int num=Integer.parseInt(((EditText) findViewById(R.id.tempEditT)).getText().toString());
            currentFolder = files[num];
            files= currentFolder.listFiles();
            String names="";
            for(int i=0; i<files.length; i++){
                if(files[i].isDirectory()){
                    names+=i+": "+files[i].getName()+" is folder\n";
                }
                else{
                    names+=i+": "+files[i].getName()+" is file, size:" + files[i].length() + "\n";
                }
            }
        }
    }

    public void loadAdapter(){

        File[] files= currentFolder.listFiles();
        ArrayList<FileExplorerElement> folders=new ArrayList<FileExplorerElement>();
        if(!currentFolder.getAbsolutePath().equals(rootFolder.getAbsolutePath())){
            folders.add(new FileExplorerElement("Back", "", false));
        }
        for(int i=0; i<files.length; i++){
            String fileNameTmp=files[i].getName();
            String fileSizeTmp=""+files[i].length()+"B";
            boolean isFolder=files[i].isDirectory();
            folders.add(new FileExplorerElement(fileNameTmp, fileSizeTmp, !isFolder));
        }


        //Create score adapter with data score list
        FileExplorerAdapter adapter=new FileExplorerAdapter(DataTransferActivity.this, folders);
        //Add score adapter to view list
        myList.setAdapter(adapter);
    }
}
