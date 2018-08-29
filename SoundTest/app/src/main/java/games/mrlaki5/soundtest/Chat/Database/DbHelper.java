package games.mrlaki5.soundtest.Chat.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    //String for database initialization
    public static final String SQL_CREATE_ENTRIES="CREATE TABLE " + MessagesTableEntry.TABLE_NAME +
            " ( " + MessagesTableEntry._ID + " INTEGER PRIMARY KEY, " +
            MessagesTableEntry.COLUMN_USER + " INTEGER, " +
            MessagesTableEntry.COLUMN_MESSAGE + " TEXT );";
    //String for deleting database
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + MessagesTableEntry.TABLE_NAME;
    //Version of database
    public static final int DATABASE_VERSION = 1;
    //Physical name of database
    public static final String DATABASE_NAME = "MessagesDataOverSound.db";

    //Constructor for database helper
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Method for database initialization
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    //After version change drop db and initialize new one
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}