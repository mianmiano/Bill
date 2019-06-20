package com.swufe.bill;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swufe.bill.bean.Bill;

public class AddRecordActivity extends AppCompatActivity implements View.OnClickListener, CategoryRecyclerAdapter.OnCategoryClickListener {
    private static String TAG = "AddRecordActivity";

    private EditText editText;
    private TextView amountText;
    private String userInput = "";

    private RecyclerView recyclerView;
    private CategoryRecyclerAdapter adapter;


    private String category = "General";
    private int type = Bill.RECORD_TYPE_EXPENSE;
    private String remark = category;

    Bill record = new Bill();

    private boolean inEdit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        findViewById(R.id.keyboard_one).setOnClickListener(this);
        findViewById(R.id.keyboard_two).setOnClickListener(this);
        findViewById(R.id.keyboard_three).setOnClickListener(this);
        findViewById(R.id.keyboard_four).setOnClickListener(this);
        findViewById(R.id.keyboard_five).setOnClickListener(this);
        findViewById(R.id.keyboard_six).setOnClickListener(this);
        findViewById(R.id.keyboard_seven).setOnClickListener(this);
        findViewById(R.id.keyboard_eight).setOnClickListener(this);
        findViewById(R.id.keyboard_nine).setOnClickListener(this);
        findViewById(R.id.keyboard_zero).setOnClickListener(this);


        amountText = (TextView) findViewById(R.id.textView_amount);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(remark);

        handleDot();
        handleTypeChange();
        handleBackspace();
        handleDone();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new CategoryRecyclerAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),4);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.notifyDataSetChanged();

        adapter.setOnCategoryClickListener(this);

        Bill record = (Bill) getIntent().getSerializableExtra("record");

        if (record != null){
            Log.d(TAG,"getIntent " + record.getRemark());
            inEdit = true;
            this.record = record;
        }


    }

    private void handleDot(){
        findViewById(R.id.keyboard_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userInput.contains(".")){
                    userInput += ".";
                }
            }
        });
    }

    private void handleTypeChange(){
        findViewById(R.id.keyboard_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == Bill.RECORD_TYPE_EXPENSE){
                    type = Bill.RECORD_TYPE_INCOME;
                }else {
                    type = Bill.RECORD_TYPE_EXPENSE;
                }
                adapter.changeType(type);
                category = adapter.getSelected();
            }
        });
    }

    private void handleBackspace(){
        findViewById(R.id.keyboard_backspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userInput.length() > 0){
                    userInput = userInput.substring(0,userInput.length() - 1);
                }
                if (userInput.length() > 0 && userInput.charAt(userInput.length() - 1) == '.'){
                    userInput = userInput.substring(0,userInput.length() - 1);
                }
                updateAmountText();
            }
        });
    }

    private void handleDone(){
        findViewById(R.id.keyboard_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userInput.equals("")){
                    Number amount = Double.valueOf(userInput);
                    record.setAmount(amount);
                    record.setType(type);
                    record.setCategory(adapter.getSelected());
                    record.setRemark(editText.getText().toString());
                    record.setDate(DateUtil.getFormattedDate());
                    record.setYear2month();
                    GlobalUtil.getInstance().billDatabaseHelper.addRecord(record);

                    //跳转回mainActivity
                    Intent intent = new Intent(AddRecordActivity.this,MainActivity.class);
                    intent.putExtra("record",record);
                    setResult(2,intent);
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(),"Amount is 0!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String input = button.getText().toString();
        if (userInput.contains(".")){
            if (userInput.split("\\.").length == 1 || userInput.split("\\.")[1].length() < 2){
                userInput += input;
            }
        }else {
            userInput += input;
        }
        updateAmountText();
    }

    private void updateAmountText(){
        if (userInput.contains(".")){
            if (userInput.split("\\.").length == 1){
                amountText.setText(userInput + "00");
            }else if (userInput.split("\\.")[1].length() == 1){
                amountText.setText(userInput + "0");
            }else if (userInput.split("\\.")[1].length() == 2){
                amountText.setText(userInput);
            }
        }else {
            if (userInput.equals("")){
                amountText.setText("0.00");
            }else {
                amountText.setText(userInput + ".00");
            }
        }
    }

    @Override
    public void onClick(String category) {
        this.category = category;
        editText.setText(category);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
