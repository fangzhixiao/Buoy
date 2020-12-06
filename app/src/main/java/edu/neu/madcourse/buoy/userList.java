package edu.neu.madcourse.buoy;
/*
Sources:
https://developersbreach.com/merge-multiple-adapters-with-concatadapter-android/
https://medium.com/swlh/expandable-list-in-android-with-mergeadapter-3a7f0cb56166
https://medium.com/androiddevelopers/merge-adapters-sequentially-with-mergeadapter-294d2942127a
https://developer.android.com/guide/topics/ui/controls/checkbox
https://medium.com/techmacademy/how-to-read-and-write-booleans-in-a-parcelable-class-99e5948db58d
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.neu.madcourse.buoy.InnerAdapter;
import edu.neu.madcourse.buoy.InnerItemCard;
import edu.neu.madcourse.buoy.ItemAdapter;
import edu.neu.madcourse.buoy.ItemCard;
import edu.neu.madcourse.buoy.R;
import edu.neu.madcourse.buoy.listobjects.Task;
import edu.neu.madcourse.buoy.listobjects.TaskList;
import edu.neu.madcourse.buoy.newtask.AddTaskDialogFragment;
// TODO: If we add achievements, add category spinners to dialog box.
// TODO: Be able to delete task, see task's buoy's, complete a list button --> new activity or fragment to get location etc..
public class userList extends AppCompatActivity implements AddTaskDialogFragment.AddTaskDialogListener {
    static final String PLACEHOLDERITEMCARD = "itemCard placeHolder";

    private ArrayList<ItemCard> itemCardArrayList = new ArrayList<>(); //item card list
    static final String ITEMCARDLIST = "itemCardList";
    private ConcatAdapter concatAdapter; //main adapter merges all adapters together.

    private ArrayList<ItemAdapter> parentAdapters; //list of item card adapters
    private HashMap<ItemCard, InnerAdapter> innerAdapters; //list of inner lists mapped to item card as keys

    private RecyclerView recyclerView;
    private Button btnSubmit;
    private Button btnAddTodo;
    private EditText newTodoInput;
    private EditText newListInput;
    private String uid;//this user's id

    private DatabaseReference mdataBase;
    private FirebaseAuth mFirebaseAuth;
    private List<TaskList> userTaskList; //arrayList

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        btnSubmit = findViewById(R.id.userListSubmit);
        btnAddTodo = findViewById(R.id.checkBoxAdd);
        newTodoInput = findViewById(R.id.checkBoxInput);
        newListInput = findViewById(R.id.userListInput);

        createRecyclerView();

        mdataBase = FirebaseDatabase.getInstance().getReference();
        uid = mFirebaseAuth.getInstance().getCurrentUser().getUid();
        mdataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //populate user's list
        mdataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                //itemCardArrayList = user.getItemCardArrayList();
                userTaskList = user.getTaskLists();
                taskListTranslateToItemCardLists(); //translate user's task lists to item card lists
                parentAdapters = new ArrayList<>();
                //set itemCardArrayList into parent Adapters list
                for (int i = 0; i < itemCardArrayList.size(); i++) {
                    ItemAdapter item = new ItemAdapter(itemCardArrayList.get(i));
                    parentAdapters.add(item);
                }
                setAdapters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Reading from DB error", error.getMessage());
            }
        });

        //make new list
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = newListInput.getText().toString();
                if (name.isEmpty()) {
                    newListInput.setError("List must have a name");
                } else if (name.length() > 15) {
                    newListInput.setError("List name is too long");
                } else {
                    newList(name); //add new list to DB
                    newListInput.setText("");

                    //close the keyboard
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        // fine nevermind then I didn't want you to have fun anyway
                    }
                }
            }
        });
    }

    /**
     * Sets Listeners for each Parent Adapter and Inner Adapter.
     */
    private void setAdapters() {

        for (int i = 0; i < this.parentAdapters.size(); i++) {
            ItemAdapter item = this.parentAdapters.get(i);
            this.concatAdapter.addAdapter(item);
            int finalI = i;

            item.setOnItemClickListener(new ItemAdapter.ItemClickListener() {
                @Override
                public synchronized void onItemClick() {
                    ItemCard itemCard = itemCardArrayList.get(finalI);
                    if (itemCard.isExpanded()) { //if inner list is already expanded, tap should close list.
                        concatAdapter.removeAdapter(Objects.requireNonNull(innerAdapters.get(itemCard)));
                        itemCard.setExpanded();
                    } else { //else expand list.
                        itemCard.setExpanded(); //set expanded to true
                        concatAdapterSet(); //show concat adapter list
                    }
                    item.notifyDataSetChanged();
                }

                @Override
                public synchronized void onDeletePressed() {
                    ItemCard card = itemCardArrayList.get(finalI);
                    parentAdapters.remove(item);
                    innerAdapters.remove(card);
                    concatAdapter.removeAdapter(item);
                    itemCardArrayList.remove(card);
                    concatAdapterSet();

                    userTaskList.remove(finalI);
                    if (userTaskList.size() == 0) {
                        TaskList defaultList = new TaskList(PLACEHOLDERITEMCARD);
                        userTaskList.add(defaultList);
                    }
                    mdataBase.child("taskLists").setValue(userTaskList);
                }

                @Override
                public synchronized void onTodoAddPressed() {
                    ItemCard card = itemCardArrayList.get(finalI);
                    launchNewTaskDialogBox(item, finalI, card);
                }

            });
        }
        populateInnerAdapterList();
    }

    public void newList(String name) {
        //putting new TaskList into DB
        TaskList newTaskList = new TaskList(name);
        this.userTaskList.add(newTaskList);
        mdataBase.child("taskLists").setValue(userTaskList).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //populating recyclerview with new list
                ArrayList<InnerItemCard> newList = new ArrayList<>(); //new inner arraylist
                ItemCard newCard = new ItemCard(name, newList);
                itemCardArrayList.add(newCard);

                ItemAdapter item = new ItemAdapter(newCard);
                parentAdapters.add(item);

                setAdapters();
                Toast.makeText(userList.this, "New List added Successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                userTaskList.remove(newTaskList);
                Log.e("New List Creation Failure", e.getMessage());
                Toast.makeText(userList.this, "Failed to Add new List", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(ITEMCARDLIST, this.itemCardArrayList);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void createRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_nested);
        recyclerView.setHasFixedSize(true);

        this.concatAdapter = new ConcatAdapter();
        recyclerView.setAdapter(this.concatAdapter);

        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);
    }


    //populate nested lists and associated inner adapters of parent.
    private void populateInnerAdapterList() {
        this.innerAdapters = new HashMap<>();
        for (int i = 0; i < this.itemCardArrayList.size(); i++) {
            TaskList taskList = this.userTaskList.get(i);
            List<Task> tasks = taskList.getTaskList();
            ArrayList<InnerItemCard> list = this.itemCardArrayList.get(i).getHeaderList();
            InnerAdapter adapter = new InnerAdapter(list);
            this.innerAdapters.put(this.itemCardArrayList.get(i), adapter);
            adapter.setOnInnerClickListener(new InnerAdapter.InnerItemClickListener() {
                @Override
                public void onItemClick(int pos) {
                    //some action happens here, probably go to buoy's?.
                }

                @Override
                public void onCheckClick(int pos) {
                    list.get(pos).setChecked();
                    tasks.get(pos).setCompleted(!tasks.get(pos).isCompleted());
                    mdataBase.child("taskLists").setValue(userTaskList);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    //concat needs to be sequential.
    private void concatAdapterSet() {
        for (Map.Entry<ItemCard, InnerAdapter> each : this.innerAdapters.entrySet()) {
            concatAdapter.removeAdapter(each.getValue());
        }
        int concatIndex = 0;
        for (int i = 0; i < this.parentAdapters.size(); i++) {
            ItemCard currentParent = this.itemCardArrayList.get(i);
            concatIndex++;
            if (currentParent.isExpanded()) {
                concatAdapter.addAdapter(concatIndex, Objects.requireNonNull(this.innerAdapters.get(currentParent)));
                concatIndex++;
            }
        }
    }

    /**
     * Translates a List of TaskList Objects into a List of ItemCard objects. TaskList Objects have
     * a List of Task Objects which is translated into InnerItemCard Objects for each Item Card.
     */
    private void taskListTranslateToItemCardLists() {
        for (int k = 0; k < this.userTaskList.size(); k++) {
            if (this.userTaskList.get(k).getListTitle().equals(PLACEHOLDERITEMCARD)) {
                this.userTaskList.remove(k);
                break;
            }
        }

        if (this.userTaskList.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.userTaskList.size(); i++) {
            ArrayList<InnerItemCard> currentInnerList = new ArrayList<>();
            TaskList eachList = this.userTaskList.get(i);
            List<Task> tasks = eachList.getTaskList();
            if (!eachList.getListTitle().equals(PLACEHOLDERITEMCARD)) {
                for (int j = 0; j < tasks.size(); j++) {
                    Task eachTask = tasks.get(j);
                    if (!eachTask.getTaskTitle().equals(PLACEHOLDERITEMCARD)) {
                        currentInnerList.add(new InnerItemCard(eachTask));
                    }
                }
                this.itemCardArrayList.add(new ItemCard(eachList.getListTitle(), currentInnerList));
            }
        }
    }

    //Launch dialog fragment for adding a new task to given parent adapter and index.
    private void launchNewTaskDialogBox(ItemAdapter parentAdapter, int parentIndex, ItemCard parentCard) {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment newDialog = AddTaskDialogFragment.newInstance(parentAdapter, parentIndex, parentCard);
        newDialog.show(fm, "New Task");
    }


    //Positive Click Method for Dialog Box: creates new task object, writes to firebase and displays in app.
    @Override
    public void onPositiveClick(AddTaskDialogFragment dialog) {
        String todo = dialog.getToDo();
        int finalI = dialog.getParentIndex();
        ItemAdapter item = dialog.getParentAdapter();
        ItemCard card = dialog.getParentCard();
        String date = dialog.getDate();
        String time = dialog.getTime();
        String dateAndTime = date + " " + time;

        InnerItemCard newToDo = new InnerItemCard(todo, dateAndTime); //new inner card
        //Parses string of date and time into LocalDateTime Object
        LocalDateTime thisDate = LocalDateTime.parse(dateAndTime, formatter);
        Task cardTask = new Task(todo, null, null,
                thisDate.getYear(), thisDate.getMonthValue(),
                thisDate.getDayOfMonth(), thisDate.getHour(), thisDate.getMinute());

        dialog.dismiss();

        if (card == null || cardTask == null) {
            Toast.makeText(userList.this, "Error making new task.", Toast.LENGTH_SHORT).show();
        } else {
            userTaskList.get(finalI).getTaskList().add(cardTask); //add new task to user task list
            //listeners to make sure task is added successfully before adding to adapters.
            mdataBase.child("taskLists").setValue(userTaskList)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            InnerAdapter innerListAdapter = innerAdapters.get(card);
                            card.addToHeaderList(newToDo);
                            innerListAdapter.notifyDataSetChanged();
                            item.notifyDataSetChanged();
                            Toast.makeText(userList.this, "Task added Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Write to DB new Task Failure", e.getMessage());
                            Toast.makeText(userList.this, "Error making new task", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}