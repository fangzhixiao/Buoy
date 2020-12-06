package edu.neu.madcourse.buoy;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.buoy.listobjects.TaskList;

public class Profile extends AppCompatActivity {
    private DatabaseReference mdataBase;
    private FirebaseAuth mFirebaseAuth;
    private List<TaskList> userTaskList;
    private String uid;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mTextView = (TextView) findViewById(R.id.text);

        //get User's Task List
        mdataBase = FirebaseDatabase.getInstance().getReference();
        uid = mFirebaseAuth.getInstance().getCurrentUser().getUid();
        mdataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mdataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userTaskList = user.getTaskLists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Reading from DB error", error.getMessage());
            }
        });

    }

    //TODO: Display User's first/last name, username, top x number of tasks due soon.
    //TODO: When adding achievement badges, display those in horizontal scrolling recycler view as well.
}