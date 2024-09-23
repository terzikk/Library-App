package com.example.libraryapp.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DueDateCheckService extends Service {
    private ScheduledExecutorService executor;
    private static final String CHANNEL_ID = "LibraryAppNotifications";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLoanScanning();
        return START_STICKY;
    }

    private void startLoanScanning() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::startDueDateCheck, 0, 2, TimeUnit.HOURS);
    }

    private void startDueDateCheck() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Loan")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming dueDate is stored as a string in Firestore
                            String dueDateString = document.getString("dueDate");
                            Integer memberId = document.getLong("memberId").intValue();
                            Integer bookId = document.getLong("bookId").intValue();

                            // Retrieve book title and member name from local database
                            String bookTitle = Drawer.database.bookDao().getBookTitle(bookId);
                            String memberName = Drawer.database.memberDao().getMemberName(memberId);

                            if (dueDateString != null) {
                                try {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date dueDate = dateFormat.parse(dueDateString);
                                    long currentTimeMillis = System.currentTimeMillis();
                                    long dueDateTimeMillis = dueDate.getTime();
                                    long timeDiffMillis = dueDateTimeMillis - currentTimeMillis;
                                    int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(timeDiffMillis);
                                    if (daysRemaining <= 2) {
                                        // Send notification with different contexts
                                        sendNotification(getApplicationContext(), bookTitle, memberName);
                                        // You can call sendNotification() multiple times with different contexts
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

    private void sendNotification(Context context, String bookTitle, String memberName) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.book_icon)
                .setContentTitle("Loan Due Soon")
                .setContentText("The book with title " + bookTitle + " should be returned within the next 2 days by " + memberName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Use unique notification IDs for each notification to ensure they are displayed separately
            int notificationId = generateNotificationId();
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private int generateNotificationId() {
        // Generate unique notification IDs using a random number generator or other method
        return (int) System.currentTimeMillis();
    }


    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LibraryApp Notifications";
            String description = "Notification channel for LibraryApp";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
