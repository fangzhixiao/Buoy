package edu.neu.madcourse.buoy;/*
Sources:
https://developersbreach.com/merge-multiple-adapters-with-concatadapter-android/
https://medium.com/swlh/expandable-list-in-android-with-mergeadapter-3a7f0cb56166
https://medium.com/androiddevelopers/merge-adapters-sequentially-with-mergeadapter-294d2942127a
https://developer.android.com/guide/topics/ui/controls/checkbox
https://medium.com/techmacademy/how-to-read-and-write-booleans-in-a-parcelable-class-99e5948db58d

 */
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.neu.madcourse.buoy.InnerAdapter;
import edu.neu.madcourse.buoy.InnerItemCard;
import edu.neu.madcourse.buoy.ItemAdapter;
import edu.neu.madcourse.buoy.ItemCard;
import edu.neu.madcourse.buoy.R;

public class userList extends AppCompatActivity {
    private ArrayList<ItemCard> itemCardArrayList; //item card list
    static final String ITEMCARDLIST = "itemCardList";
    private ConcatAdapter concatAdapter; //main adapter merges all adapters together.

    private ArrayList<ItemAdapter> parentAdapters; //list of item card adapters
    static final String PARENTADAPTERLIST = "parentAdapterList";
    private HashMap<ItemCard, InnerAdapter> innerAdapters; //list of inner lists mapped to item card as keys

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        mockList();
        createRecyclerView();

        this.parentAdapters = new ArrayList<>();
        for (int i = 0; i < this.itemCardArrayList.size(); i++){
            ItemAdapter item = new ItemAdapter(this.itemCardArrayList.get(i));
            this.parentAdapters.add(item);
        }
        setAdapters();
    }

    private void setAdapters(){
        for (int i = 0; i < this.parentAdapters.size(); i++){
            ItemAdapter item = this.parentAdapters.get(i);
            this.concatAdapter.addAdapter(item);
            int finalI = i;

            item.setOnItemClickListener(() -> {
                ItemCard itemCard = itemCardArrayList.get(finalI);
                if (itemCard.isExpanded()){ //if inner list is already expanded, tap should close list.
                    concatAdapter.removeAdapter(Objects.requireNonNull(innerAdapters.get(itemCard)));
                    itemCard.setExpanded();
                } else { //else expand list.
                    itemCard.setExpanded(); //set expanded to true
                    concatAdapterSet(); //show concat adapter list
                }
                item.notifyDataSetChanged();
            });
        }
        populateInnerAdapterList();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(ITEMCARDLIST, this.itemCardArrayList);
        savedInstanceState.putSerializable(PARENTADAPTERLIST, this.parentAdapters);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        itemCardArrayList = (ArrayList<ItemCard>)savedInstanceState.getSerializable(ITEMCARDLIST);
        parentAdapters = (ArrayList<ItemAdapter>)savedInstanceState.getSerializable(PARENTADAPTERLIST);
        createRecyclerView();

        setAdapters();
    }

    private void createRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view_nested);
        recyclerView.setHasFixedSize(true);

        this.concatAdapter = new ConcatAdapter();
        recyclerView.setAdapter(this.concatAdapter);

        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);
    }


    //populate nested lists of parent.
    private void populateInnerAdapterList(){
        this.innerAdapters = new HashMap<>();
        for(int i = 0; i < this.itemCardArrayList.size(); i++){
            ArrayList<InnerItemCard> list = this.itemCardArrayList.get(i).getHeaderList();
            InnerAdapter adapter = new InnerAdapter(list);
            this.innerAdapters.put(this.itemCardArrayList.get(i), adapter);
            adapter.setOnInnerClickListener(new InnerAdapter.InnerItemClickListener() {
                @Override
                public void onItemClick(int pos) {
                    //some action happens here.
                }

                @Override
                public void onCheckClick(int pos) {
                    list.get(pos).setChecked();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void mockList(){
        this.itemCardArrayList = new ArrayList<>();
        //Mock out nested list of item cards to show
        ArrayList<InnerItemCard> list1 = new ArrayList<>();
        ArrayList<InnerItemCard> list2 = new ArrayList<>();
        ArrayList<InnerItemCard> list3 = new ArrayList<>();

        list1.add(new InnerItemCard("list 1 item 1"));
        list1.add(new InnerItemCard("list 1 item 2"));
        list1.add(new InnerItemCard("list 1 item 3"));


        list2.add(new InnerItemCard("list 2 item 1"));
        list2.add(new InnerItemCard("list 2 item 2"));

        itemCardArrayList.add(new ItemCard("list1", list1));
        itemCardArrayList.add(new ItemCard("list2", list2));
        itemCardArrayList.add(new ItemCard("list3", list3));
    }

    //concat needs to be sequential.
    private void concatAdapterSet(){
        for(Map.Entry<ItemCard, InnerAdapter> each : this.innerAdapters.entrySet()){
            concatAdapter.removeAdapter(each.getValue());
        }
        int concatIndex = 0;
        for(int i = 0; i < this.parentAdapters.size(); i++){
            ItemCard currentParent = this.itemCardArrayList.get(i);
            concatIndex ++;
            if (currentParent.isExpanded()){
                concatAdapter.addAdapter(concatIndex, Objects.requireNonNull(this.innerAdapters.get(currentParent)));
                concatIndex ++;
            }
        }
    }



}