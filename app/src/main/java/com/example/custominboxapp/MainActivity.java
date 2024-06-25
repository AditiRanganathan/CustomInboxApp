package com.example.custominboxapp;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.inbox.CTInboxMessage;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CustomInboxAdapter adapter;
    private List<CustomInboxMessage> inboxMessages = new ArrayList<>(); // Initialize with an empty list

    private CleverTapAPI cleverTapDefaultInstance;
    private Handler handler;
    private Runnable secondTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fetch_messages_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleverTapDefaultInstance.pushEvent("CT custom demo App Inbox pushed");
                fetchCampaignMessages();
            }
        });

        // Initialize CleverTap
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (cleverTapDefaultInstance != null) {
            cleverTapDefaultInstance.initializeInbox();
        } else {
            Log.e("CustomInboxApp", "CleverTap instance is null");
            showToast("CleverTap instance initialization failed");
            return;
        }

        // Initialize the handler and second task
        handler = new Handler();
        secondTask = new Runnable() {
            @Override
            public void run() {
                checkAndRemoveExpiredMessages();
                handler.postDelayed(this, 1000); // Check every second
            }
        };

        // Start the second task
        handler.post(secondTask);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRemoveExpiredMessages();
    }

    private void fetchCampaignMessages() {
        if (cleverTapDefaultInstance == null) {
            Log.e("CustomInboxApp", "CleverTap instance is null");
            return;
        }

        // Fetch inbox messages from CleverTap campaign
        List<CTInboxMessage> ctInboxMessages = cleverTapDefaultInstance.getAllInboxMessages();
        Log.d("CustomInboxApp", "Fetched " + ctInboxMessages.size() + " messages");

        // Convert CleverTap inbox messages to custom inbox messages
        inboxMessages = convertToCustomInboxMessages(ctInboxMessages);

        // Display custom inbox messages in RecyclerView
        displayInboxMessages();

        // Check & remove expired messages
        checkAndRemoveExpiredMessages();
    }

    private List<CustomInboxMessage> convertToCustomInboxMessages(List<CTInboxMessage> ctInboxMessages) {
        List<CustomInboxMessage> customInboxMessages = new ArrayList<>();
        for (CTInboxMessage ctInboxMessage : ctInboxMessages) {
            Log.d("CustomInboxApp", "Payload: " + ctInboxMessage.getData().toString());
            try {
                String titleText = ctInboxMessage.getData().getJSONObject("msg").getJSONArray("content").getJSONObject(0).getJSONObject("title").getString("text");
                String messageText = ctInboxMessage.getData().getJSONObject("msg").getJSONArray("content").getJSONObject(0).getJSONObject("message").getString("text");
                long ttl = ctInboxMessage.getData().optLong("wzrk_ttl", -1);
                boolean isRead = ctInboxMessage.isRead();

                customInboxMessages.add(new CustomInboxMessage(
                        titleText,
                        messageText,
                        isRead,
                        ttl
                ));
            } catch (JSONException e) {
                Log.e("CustomInboxApp", "JSON parsing error", e);
            }
        }
        Log.d("CustomInboxApp", "Converted " + customInboxMessages.size() + " messages");
        return customInboxMessages;
    }

    private void displayInboxMessages() {
        if (inboxMessages == null || inboxMessages.isEmpty()) {
            Log.d("CustomInboxApp", "No messages to display");
            showToast("No messages to display");
            return;
        }

        adapter = new CustomInboxAdapter(inboxMessages, new CustomInboxAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CustomInboxMessage message) {
                showToast("Clicked: " + message.getTitle());
            }
        });
        recyclerView.setAdapter(adapter);
        Log.d("CustomInboxApp", "Messages displayed in RecyclerView");
    }

    private void checkAndRemoveExpiredMessages() {
        long currentTime = new Date().getTime(); // Get the current time

        Log.d("CustomInboxApp", "Current time: " + currentTime);
        Date date = new Date(currentTime);
        System.out.println(date);

        // Define date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Format date into a string
        String formattedDate = sdf.format(date);

        // Print in console
        System.out.println("Current Date and Time: " + formattedDate);

        Iterator<CustomInboxMessage> iterator = inboxMessages.iterator();

        while (iterator.hasNext()) {

            CustomInboxMessage message = iterator.next();
            long ttl = message.getTtl(); // Get the TTL of the message
            long ttlInMillis = ttl * 1000;
            Log.d("CustomInboxApp", "Message TTL in millis: " + ttlInMillis);
            Date ttlDate = new Date(ttlInMillis);
            System.out.println(ttlInMillis);

            // Define the desired date format
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Format the date object into a string
            String formattedDate1 = sdf1.format(ttlDate);

            System.out.println("TTL Date and Time: " + formattedDate1);
            //Log.d("CustomInboxApp", "Message TTL: " + ttl);

            if(currentTime>=ttlInMillis){
                iterator.remove();
                Log.d("CustomInboxApp", "Removed expired message: " + message.getTitle());
            }
            //for days
//            long timeToLive = ttlDate.getTime() - date.getTime(); // Calculate the remaining TTL
//            long timeToLiveInDays = TimeUnit.MILLISECONDS.toDays(timeToLive);
//            Log.d("CustomInboxApp", "Time to live: " + timeToLiveInDays);
//            if (timeToLiveInDays <= 0) { // Check if the TTL has expired
//                iterator.remove(); // Remove the expired message
//                Log.d("CustomInboxApp", "Removed expired message: " + message.getTitle());
//            }
        }

        // Notify the adapter about the changes
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e("CustomInboxApp", "Adapter is null");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(secondTask);
    }
}
