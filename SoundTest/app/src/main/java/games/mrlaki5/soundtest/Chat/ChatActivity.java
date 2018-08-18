package games.mrlaki5.soundtest.Chat;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.BufferSoundTask;
import games.mrlaki5.soundtest.CallbackSendRec;
import games.mrlaki5.soundtest.Chat.Database.DbHelper;
import games.mrlaki5.soundtest.Chat.Database.MessagesTableEntry;
import games.mrlaki5.soundtest.R;
import games.mrlaki5.soundtest.SettingsActivity;

public class ChatActivity extends AppCompatActivity implements CallbackSendRec{

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private ProgressBar sendingBar;

    private boolean isSending=false;
    private BufferSoundTask sendTask=null;
    private String sendText;

    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendingBar=((ProgressBar) findViewById(R.id.progressBar));

        messageList=new ArrayList<Message>();

        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        String[] columnsRet={MessagesTableEntry.COLUMN_USER,
                MessagesTableEntry.COLUMN_MESSAGE,
                MessagesTableEntry._ID};
        //Execute query and get cursor
        Cursor cursor=db.query(MessagesTableEntry.TABLE_NAME, columnsRet,
                null, null,
                null,null,null);
        //Go through cursor (database scores) and add every different player
        // combination and their scores to score data list
        while(cursor.moveToNext()){
            //Get player names and scores
            int user=cursor.getInt(cursor.getColumnIndex(
                    MessagesTableEntry.COLUMN_USER));
            String message=cursor.getString(cursor.getColumnIndex(
                    MessagesTableEntry.COLUMN_MESSAGE));
            messageList.add(new Message(message, user));
        }
        //Close cursor
        cursor.close();

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(manager);
    }

    public void sendMessage(View view) {
        if(!isSending) {
            sendText = ((TextView) findViewById(R.id.edittext_chatbox)).getText().toString();
            if (!sendText.isEmpty() && !sendText.equals(" ")) {
                isSending=true;
                sendingBar.setVisibility(View.VISIBLE);
                sendTask = new BufferSoundTask();
                sendTask.setProgressBar(sendingBar);
                sendTask.setCallbackSR(this);
                ((Button) view).setText("STOP");
                try {
                    byte[] byteText = sendText.getBytes("UTF-8");
                    sendTask.setBuffer(byteText);
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
                    sendTask.execute(tempArr);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            ((Button) view).setText("START");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sendingBar.setProgress(1, true);
            }
            else{
                sendingBar.setProgress(1);
            }
            sendingBar.setVisibility(View.INVISIBLE);
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
            isSending=false;
        }
    }

    @Override
    public void actionDone(int srFlag) {
        if(CallbackSendRec.SEND_ACTION==srFlag && isSending){
            ((Button) findViewById(R.id.button_chatbox_send)).setText("START");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sendingBar.setProgress(1, true);
            }
            else{
                sendingBar.setProgress(1);
            }
            sendingBar.setVisibility(View.INVISIBLE);
            isSending=false;
            String text=((TextView) findViewById(R.id.edittext_chatbox)).getText().toString();
            if(sendText.equals(text)){
                ((TextView) findViewById(R.id.edittext_chatbox)).setText("");
            }

            DbHelper helper = new DbHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MessagesTableEntry.COLUMN_USER, 0);
            values.put(MessagesTableEntry.COLUMN_MESSAGE, sendText);
            db.insert(MessagesTableEntry.TABLE_NAME, null, values);

            messageList.add(new Message(sendText, 0));
            mMessageAdapter.notifyDataSetChanged();
        }
    }
}
