package android.example.com.squawker.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkDatabase;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by powerman23rus on 14.11.17.
 * Enjoy ;)
 */

public class SquawkerFirebaseMessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data != null) {
            String author = data.get("author");
            String authorKey = data.get("authorKey");
            String message = data.get("message");
            Long date = Long.parseLong(data.get("date"));

            ContentValues cv = new ContentValues();
            cv.put(SquawkContract.COLUMN_AUTHOR, author);
            cv.put(SquawkContract.COLUMN_AUTHOR_KEY, authorKey);
            cv.put(SquawkContract.COLUMN_MESSAGE, message);
            cv.put(SquawkContract.COLUMN_DATE, date);

            getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, cv);

            Context context = getApplicationContext();
            Intent intent = new Intent(context, MainActivity.class);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("New message from " + author)
                    .setSmallIcon(R.drawable.ic_duck)
                    .setSound(defaultSoundUri)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);

        }
    }
}
