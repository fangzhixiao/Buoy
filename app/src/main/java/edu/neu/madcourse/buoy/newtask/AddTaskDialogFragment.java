
package edu.neu.madcourse.buoy.newtask;
/*
Sources
https://guides.codepath.com/android/using-dialogfragment#passing-data-to-activity
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.neu.madcourse.buoy.ItemAdapter;
import edu.neu.madcourse.buoy.ItemCard;
import edu.neu.madcourse.buoy.R;


public class AddTaskDialogFragment extends DialogFragment {
    private String toDo;
    private String date;
    private String time;
    private ItemAdapter parentAdapter;
    private int parentIndex;
    private ItemCard parentCard;
    AddTaskDialogListener listener;

    static final int REQUEST_CODE = 121;
    static final String PARENTADAPTER = "Parent Adapter";
    static final String PARENTCARD = "Parent Card";
    static final String PARENTINDEX = "Parent Index";

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    public AddTaskDialogFragment(){

    }

    public static AddTaskDialogFragment newInstance(ItemAdapter parentAdapter, int parentIndex, ItemCard parentCard){
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARENTADAPTER, parentAdapter);
        args.putParcelable(PARENTCARD, parentCard);
        args.putInt(PARENTINDEX, parentIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public interface AddTaskDialogListener{
        void onPositiveClick(AddTaskDialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        this.parentAdapter = getArguments().getParcelable(PARENTADAPTER);
        this.parentCard = getArguments().getParcelable(PARENTCARD);
        this.parentIndex = getArguments().getInt(PARENTINDEX);

        LocalDateTime localDateTime = LocalDateTime.now().plusDays(14);
        this.date = localDateTime.format(dateFormatter);
        this.time = localDateTime.format(timeFormatter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_task_dialog_layout, null);
        final EditText todoText = (EditText)dialogView.findViewById(R.id.to_do_task_name);
        final Button setDate = (Button)dialogView.findViewById(R.id.set_date);
        final Button setTime = (Button)dialogView.findViewById(R.id.set_time);

        final FragmentManager fm = ((AppCompatActivity)getActivity()).getSupportFragmentManager();
        setDate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.setTargetFragment(AddTaskDialogFragment.this, REQUEST_CODE);
                dateFragment.show(fm, "datePicker");
            }
        });
        setTime.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.setTargetFragment(AddTaskDialogFragment.this, REQUEST_CODE);
                timeFragment.show(fm, "timePicker");
            }
        });

        builder.setView(dialogView)
                .setTitle("Add a new Task to List" + this.parentCard.getHeader())
                .setPositiveButton("Add Task", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = todoText.getText().toString();
                        if(!input.isEmpty()){
                            toDo = todoText.getText().toString();
                            listener.onPositiveClick(AddTaskDialogFragment.this);
                        }else {
                            todoText.setError("Task cannot be empty, please try again.");
                        }
                    }
                })
                .setNegativeButton("Cancel Add Task", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddTaskDialogFragment.this.getDialog().dismiss();
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            listener = (AddTaskDialogListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity must implement AddTaskDialogListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data.getStringExtra("selectedDate") != null){
            this.date = data.getStringExtra("selectedDate");
        }

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data.getStringExtra("selectedTime") != null){
            this.time = data.getStringExtra("selectedTime");
        }
    }

    public String getToDo() {
        return toDo;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public ItemAdapter getParentAdapter() {
        return parentAdapter;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public ItemCard getParentCard() {
        return parentCard;
    }
}