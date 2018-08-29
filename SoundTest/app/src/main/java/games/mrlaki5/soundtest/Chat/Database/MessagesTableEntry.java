package games.mrlaki5.soundtest.Chat.Database;

import android.provider.BaseColumns;

public class MessagesTableEntry implements BaseColumns {

    //Name of table in database
    public static final String TABLE_NAME="messagesChat";
    //Name of user column, 0-this user, 1-outside user
    public static final String COLUMN_USER="user";
    //Name of message column
    public static final String COLUMN_MESSAGE="message";
}
