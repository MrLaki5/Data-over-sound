package games.mrlaki5.soundtest.DataTransfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.R;


//Adapter used for printing score ArrayList to ListView
public class FileExplorerAdapter extends ArrayAdapter<FileExplorerElement>{

    //ScoresActivity context
    private Context mContext;
    //ArrayList with scores
    private List<FileExplorerElement> myList;

    //Constructor
    public FileExplorerAdapter(@NonNull Context context, ArrayList<FileExplorerElement> list) {
        //Constructor of ArrayAdapter. Resource is set in getView so now is passed 0
        super(context, 0, list);
        this.mContext=context;
        this.myList=list;
    }

    //Method called for showing data from score ArrayList to view
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Load layout for score list view element
        View listItem=convertView;
        if(listItem==null){
            listItem= LayoutInflater.from(mContext).inflate(R.layout.storage_file_list_element, parent,
                    false);
        }
        //Get current element from arrayList
        FileExplorerElement Elem= myList.get(position);
        //Set data to newly created view
        ((TextView) listItem.findViewById(R.id.fileName)).setText(Elem.getFileName());
        if(Elem.isFile()){
            ImageView iv = (ImageView) listItem.findViewById(R.id.fileImage);
            iv.setImageResource(R.drawable.file_image);
            ((TextView) listItem.findViewById(R.id.fileSize)).setText(Elem.getFileSize());
        }
        else{
            ImageView iv = (ImageView) listItem.findViewById(R.id.fileImage);
            iv.setImageResource(R.drawable.folder_image);
            ((TextView) listItem.findViewById(R.id.fileSize)).setText("");
        }

        return listItem;
    }
}
