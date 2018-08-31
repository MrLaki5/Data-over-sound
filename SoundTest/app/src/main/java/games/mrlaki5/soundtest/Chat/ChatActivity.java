package games.mrlaki5.soundtest.Chat;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import games.mrlaki5.soundtest.SoundClient.Sender.BufferSoundTask;
import games.mrlaki5.soundtest.SoundClient.CallbackSendRec;
import games.mrlaki5.soundtest.Chat.Database.DbHelper;
import games.mrlaki5.soundtest.Chat.Database.MessagesTableEntry;
import games.mrlaki5.soundtest.R;
import games.mrlaki5.soundtest.SoundClient.Receiver.RecordTask;
import games.mrlaki5.soundtest.Settings.SettingsActivity;

public class ChatActivity extends AppCompatActivity implements CallbackSendRec{
    //View for showing all messages
    private RecyclerView mMessageRecycler;
    //Adapter for messages view
    private MessageListAdapter mMessageAdapter;
    //Layout manager for messages view
    private LinearLayoutManager mManager;
    //Progress bar for sending message progress
    private ProgressBar sendingBar;
    //Is sending flag
    private boolean isSending=false;
    //Is listening flag
    private boolean isListening=false;
    //Is listening and receiving flag
    private boolean isReceiving=false;
    //Task for sending message
    private BufferSoundTask sendTask=null;
    //Task for receiving message
    private RecordTask listenTask=null;
    //Text to be send
    private String sendText;
    //List of all messages
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Put name on action bar of activity
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle(R.string.chat);
        }
        sendingBar=findViewById(R.id.progressBar);

        //Create list of messages and import it from database
        messageList=new ArrayList<>();
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        String[] columnsRet={MessagesTableEntry.COLUMN_USER,
                MessagesTableEntry.COLUMN_MESSAGE,
                MessagesTableEntry._ID};
        Cursor cursor=db.query(MessagesTableEntry.TABLE_NAME, columnsRet,
                null, null,
                null,null,null);
        while(cursor.moveToNext()){
            int user=cursor.getInt(cursor.getColumnIndex(
                    MessagesTableEntry.COLUMN_USER));
            String message=cursor.getString(cursor.getColumnIndex(
                    MessagesTableEntry.COLUMN_MESSAGE));
            messageList.add(new Message(message, user));
        }
        cursor.close();

        //initialize view for displaying messages
        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mManager=new LinearLayoutManager(this);
        mManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(mManager);
    }

    //Create menu on chat activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    //Listener for chat menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Check if clear of chat is selected
        if(item.getItemId() == R.id.chat_menu_clear_chat){
            //Clear messages database and messages view
            SQLiteDatabase db=new DbHelper(this).getWritableDatabase();
            db.delete(MessagesTableEntry.TABLE_NAME, null, null);
            messageList.clear();
            //Refresh messages view
            mMessageAdapter.notifyDataSetChanged();
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //Called when activity is moved from screen
    @Override
    protected void onStop() {
        super.onStop();
        //if listening task or sending task are active turn them off and return gui to start state
        if(listenTask!=null){
            stopListening();
            listenTask.setWorkFalse();
        }
        if(sendTask!=null){
            stopSending();
            sendTask.setWorkFalse();
        }
    }

    //Called when message is sending
    public void sendMessage(View view) {
        //If listening task is active, turn it of and return gui to start state
        if(isListening){
            stopListening();
            if(listenTask!=null){
                listenTask.setWorkFalse();
            }
        }

        //Check if activity is already sending
        if(!isSending) {
            //If its not sending, check if message exists and send (prepare GUI and execute task)
            sendText = ((TextView) findViewById(R.id.edittext_chatbox)).getText().toString();
            if (!sendText.isEmpty() && !sendText.equals(" ")) {
                isSending=true;
                sendingBar.setVisibility(View.VISIBLE);
                sendTask = new BufferSoundTask();
                sendTask.setProgressBar(sendingBar);
                sendTask.setCallbackSR(this);
                ((Button) view).setText(R.string.stop);
                try {
                    byte[] byteText = sendText.getBytes("UTF-8");
                    sendTask.setBuffer(byteText);
                    Integer[] tempArr=getSettingsArguments();
                    sendTask.execute(tempArr);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            //If its already sending, stop it
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
            stopSending();
        }
    }

    //Called when sending task or receiving task have finished work
    @Override
    public void actionDone(int srFlag, String message) {
        //If its sending task and activity is still in sending mode
        if(CallbackSendRec.SEND_ACTION==srFlag && isSending){
            //Update GUI to initial state
            stopSending();
            String text=((TextView) findViewById(R.id.edittext_chatbox)).getText().toString();
            //IF text was not changed while sending, clear it
            if(sendText.equals(text)){
                ((TextView) findViewById(R.id.edittext_chatbox)).setText("");
            }
            //Update messages database
            DbHelper helper = new DbHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MessagesTableEntry.COLUMN_USER, 0);
            values.put(MessagesTableEntry.COLUMN_MESSAGE, sendText);
            db.insert(MessagesTableEntry.TABLE_NAME, null, values);
            //Update messages view and refresh it
            messageList.add(new Message(sendText, 0));
            mMessageAdapter.notifyDataSetChanged();
            mManager.smoothScrollToPosition(mMessageRecycler, null, mMessageAdapter.getItemCount());
        }
        else{
            //If its receiving task and activity is still in receiving mode
            if(CallbackSendRec.RECEIVE_ACTION==srFlag && isListening){
                //Update GUI to initial state
                stopListening();
                //If received message exists put it in database and show it on view
                if(!message.equals("")){
                    DbHelper helper = new DbHelper(this);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(MessagesTableEntry.COLUMN_USER, 1);
                    values.put(MessagesTableEntry.COLUMN_MESSAGE, message);
                    db.insert(MessagesTableEntry.TABLE_NAME, null, values);
                    messageList.add(new Message(message, 1));
                    mMessageAdapter.notifyDataSetChanged();
                    mManager.smoothScrollToPosition(mMessageRecycler, null, mMessageAdapter.getItemCount());
                }
            }
        }
    }

    //Called when receiving task starts receiving message
    @Override
    public void receivingSomething() {
        //Update view and flag to show that something is receiving
        messageList.add(new Message("Receiving message...", 2));
        mMessageAdapter.notifyDataSetChanged();
        mManager.smoothScrollToPosition(mMessageRecycler, null, mMessageAdapter.getItemCount());
        isReceiving=true;
    }

    //Called to reset view and flag to initial state from sending state
    private void stopSending(){
        ((Button) findViewById(R.id.button_chatbox_send)).setText(R.string.send);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sendingBar.setProgress(1, true);
        }
        else{
            sendingBar.setProgress(1);
        }
        sendingBar.setVisibility(View.GONE);
        isSending=false;
    }

    //Called to reset view and flag to initial state from listening state
    private void stopListening(){
        if(isReceiving){
            messageList.remove(messageList.size()-1);
            mMessageAdapter.notifyDataSetChanged();
            isReceiving=false;
        }
        ((Button) findViewById(R.id.button_chatbox_listen)).setText(R.string.listen);
        isListening=false;
    }

    //Called to start listening task and update GUI to listening
    private void listen(){
        isListening=true;
        ((Button)findViewById(R.id.button_chatbox_listen)).setText(R.string.stop);
        Integer[] tempArr=getSettingsArguments();
        listenTask=new RecordTask();
        listenTask.setCallbackRet(this);
        listenTask.execute(tempArr);
    }

    //Called on listen button click
    public void listenMessage(View view) {
        //If sending task is active, stop it and update GUI
        if(isSending){
            stopSending();
            if(sendTask!=null){
                sendTask.setWorkFalse();
            }
        }
        //If its not listening check for mic permission and start listening
        if(!isListening) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            } else {
                listen();
            }
        }
        //If its already listening, stop listening and update GUI
        else{
            if(listenTask!=null){
                listenTask.setWorkFalse();
            }
            stopListening();
        }
    }

    //Called when user answers on permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                //If user granted permission on mic, continue with listening
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listen();
                }
                break;
            }
        }
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
}
