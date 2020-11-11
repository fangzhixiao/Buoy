package edu.neu.madcourse.buoy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class StickerSendActivity extends AppCompatActivity {
    private DatabaseReference mdataBase;
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<FriendItemCard> friendList;
    private RecyclerView recyclerView;
    private StickerAdapter stickerAdapter;
    private String thisUsername;
    static final String FRIEND_CARD_LIST = "friendCardList";
    private String friendToken;

    private String SERVER_KEY = "key=AAAAhIS5lRU:APA91bHS8Kx0LjSRHt-O7zX4KxDsYX2yMFf0daJn3Z6g_fIxM81-h9GDSxNt2WNB22fwOfQiM_27R02nzggKOFaOKpmjGJJnAKo7U-3hOzq1qQf7NdL6TQZGRWrD1IGSsJzQbolP3qNH";
    private String SENDER_ID = "569162437909";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_send);
        setTitle("Friends");

        friendList = new ArrayList<>();
        createRecyclerView();

        //friendsTesting = findViewById(R.id.textView3);
        final String uid = mFirebaseAuth.getInstance().getCurrentUser().getUid();
        mdataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mdataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                thisUsername = user.userName;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        getUsers();

        stickerAdapter.setOnItemClickListener(new StickerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                //something happens when item card is clicked
            }

            @Override
            public void onVibesClick(int pos) {
                setTitle("Good Vibes!");
                //sendSticker(pos);
            }

            @Override
            public void onKeepClick(int pos) {
                setTitle("Keep it Up!");
               // sendSticker(pos);
            }

            @Override
            public void onDoItClick(int pos) {
                setTitle("You can Do it!");
               // sendSticker(pos);
            }
        });

        //consider on swipe to delete

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(FRIEND_CARD_LIST, this.friendList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        friendList = savedInstanceState.getParcelableArrayList(FRIEND_CARD_LIST);
        createRecyclerView();
    }

    private void createRecyclerView(){
        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        stickerAdapter = new StickerAdapter(this.friendList);
        recyclerView.setAdapter(stickerAdapter);
        recyclerView.setLayoutManager(rLayoutManager);
    }


    public void getUsers(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot child: snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if(!user.getUserName().equals(thisUsername)) {
                        friendList.add(new FriendItemCard(user.userName, user.firstName, user.lastName, child.getKey()));
                    }
                }
                stickerAdapter.notifyDataSetChanged();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //On send: update friends' sticker list and current users sticker sent count.
    private void sendSticker(int pos){
        FriendItemCard friend = this.friendList.get(pos);
        String id = friend.getUserID();
        //get token of friend
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                friendToken = user.email;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                sendToDevice(friendToken);
            }
        });

        sendMessage.start();

    }

    private void sendToDevice(String friendToken){
        JSONObject payload = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            notification.put("title", "Sticker");
            notification.put("body", "Someone sent a sticker!");
            notification.put("sound", "default");
            notification.put("badge", "1");

            data.put("title", "Sticker");
            data.put("content", "sticker");

            payload.put("to", friendToken);
            payload.put("priority", "high");
            payload.put("notification", notification);
            payload.put("data", data);

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(payload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("TAG", "run: " + resp);
                    Toast.makeText(StickerSendActivity.this,resp,Toast.LENGTH_LONG).show();
                }
            });

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

}