package com.swufe.bill;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;
import com.swufe.bill.fragment.MonthChartFragment;
import com.swufe.bill.fragment.MonthListFragment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.swufe.bill.BillActivity.date2Str;

public class AddRecordActivity extends AppCompatActivity implements View.OnClickListener, CategoryRecyclerAdapter.OnCategoryClickListener {
    private static String TAG = "AddRecordActivity";

    private EditText editText;
    private TextView amountText;
    private String userInput = "";
    private ImageButton btnDate;
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private CategoryRecyclerAdapter adapter;


    private String category = "还款";
    private int type = Bill.RECORD_TYPE_EXPENSE;
    private String remark = category;
    private String setYear;
    private String setMonth;
    private String setDate;
    private String ddate = DateUtil.getFormattedDate();

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

        toolbar = findViewById(R.id.toolbar_bill_add);
        toolbar.setTitle("XixiBill");
        btnDate = findViewById(R.id.btn_date_add);
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
            editText.setText(record.getRemark());
            userInput = record.getAmount().toString();
            amountText.setText(userInput);
            type = record.getType();
            adapter.setSelected(record.getCategory());
            ddate = record.getDate();
        }

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerBuilder(AddRecordActivity.this, (Date date, View v) -> {
                    setYear = date2Str(date,"yyyy");
                    setMonth = date2Str(date,"MM");
                    setDate = date2Str(date,"dd");
                    Log.i("TimePickerBuilder", "onClick: year="+setYear+" month="+setMonth+" date="+setDate);
                    ddate = setYear+"-"+setMonth+"-"+setDate;
                }).setType(new boolean[]{true, true, true, false, false, false})
                        .setRangDate(null, Calendar.getInstance())
                        .isDialog(true)//是否显示为对话框样式
                        .build().show();
            }
        });

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
                ImageButton imageButton = findViewById(R.id.keyboard_type);
                if (type == Bill.RECORD_TYPE_EXPENSE){
                    type = Bill.RECORD_TYPE_INCOME;
                    imageButton.setImageResource(R.drawable.earn);
                }else {
                    type = Bill.RECORD_TYPE_EXPENSE;
                    imageButton.setImageResource(R.drawable.cost);
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
                    record.setDate(ddate);
                    try {
                        record.setYear2month(record.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "onClick: uuid="+record.getUuid());
                    if(inEdit){
                        //获取bill objectedId
                        BmobQuery<Bill> query = GlobalUtil.getInstance().billDatabaseHelper.
                                readRecordByUuid(record.getUuid());
                        query.findObjects(new FindListener<Bill>() {
                            @Override
                            public void done(List<Bill> list, BmobException e) {
                                if(e==null){
                                    Log.i(TAG, "done: 查询成功：共"+list.size()+"条数据。");
                                    record.setObjectedId(list.get(0).getObjectedId());
                                }else{
                                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                                }
                            }
                        });
                        GlobalUtil.getInstance().billDatabaseHelper.editRecord(record.getObjectId(),record);
                        //跳转回MonthListFragment
                        Intent intent = new Intent(AddRecordActivity.this, MonthListFragment.class);
                        intent.putExtra("record",record);
                        setResult(2,intent);
                        finish();
                    }else{
                        GlobalUtil.getInstance().billDatabaseHelper.addRecord(record);
                        //跳转回mainActivity
                        Intent intent = new Intent(AddRecordActivity.this,MainActivity.class);
                        intent.putExtra("record",record);
                        setResult(2,intent);
                        finish();
                    }
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
