package games.mrlaki5.soundtest.SoundClient;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class DocumentUtils {

    //Returns name that system shows for given document (file or folder chosen by user).
    //If name can't be read, given fallback is returned.
    public static String getDisplayName(ContentResolver resolver, Uri documentUri, String fallback){
        Cursor cursor=null;
        try {
            cursor=resolver.query(documentUri, new String[]{OpenableColumns.DISPLAY_NAME},
                    null, null, null);
            if(cursor!=null && cursor.moveToFirst()){
                //Some providers ignore asked columns, so column is searched by name
                int nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(nameIndex>=0){
                    String name=cursor.getString(nameIndex);
                    if(name!=null && !name.isEmpty()){
                        return name;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(cursor!=null){
                cursor.close();
            }
        }
        return fallback;
    }
}
