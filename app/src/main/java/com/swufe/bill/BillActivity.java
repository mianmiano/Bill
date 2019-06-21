package com.swufe.bill;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.swufe.bill.adapter.MainFragmentPagerAdapter;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;
import com.swufe.bill.fragment.MonthChartFragment;
import com.swufe.bill.fragment.MonthListFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.media.MediaExtractor.MetricsConstants.FORMAT;
import static com.swufe.bill.utils.BillUtil.packageChartList;
import static com.swufe.bill.utils.BillUtil.packageDetailList;

public class BillActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView tOutcome;
    private TextView tIncome;
    private TextView tTotal;
    private ImageButton btnDate;

    private View drawerHeader;
    private ImageView drawerIv;
    private TextView drawerTvAccount, drawerTvMail;

    protected static final int USERINFOACTIVITY_CODE = 0;
    protected static final int LOGINACTIVITY_CODE = 1;

    private BmobUser user;

    private FragmentManager mFragmentManager;
    private MainFragmentPagerAdapter mFragmentPagerAdapter;
    private MonthListFragment monthListFragment;
    private MonthChartFragment monthChartFragment;
    private Handler mHandler;
    private MonthChartBean monthChartBean;

    private String year;
    private String month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        init_views();
        monthListFragment = new MonthListFragment();
        monthChartFragment = new MonthChartFragment();

//        mHandler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                if(msg.what==9){
//                    List<Bill> list = (List<Bill>) msg.obj;
//                    MonthListBean mlb = packageDetailList(list);
//                    MonthChartBean mcb = packageChartList(list);
//                    monthListFragment.changeDate(year, month,mlb);
//                    monthChartFragment.changeDate(year, month,mcb);
//                }
//            }
//        };
        Log.i("BillActivity", "onCreate: handler="+mHandler);

//        btnDate.setOnClickListener(this);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    private void init_views() {
        toolbar = findViewById(R.id.toolbar_bill);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.main_viewpager);
        tOutcome = findViewById(R.id.t_outcome);
        tIncome = findViewById(R.id.t_income);
        tTotal = findViewById(R.id.t_total);
        btnDate = findViewById(R.id.btn_date);

        //初始化Toolbar
        toolbar.setTitle("XixiBill");

        //设置头部账户
        user = GlobalUtil.getInstance().getUserId();

        //初始化ViewPager
        mFragmentManager = getSupportFragmentManager();
        mFragmentPagerAdapter = new MainFragmentPagerAdapter(mFragmentManager);
        viewPager.setAdapter(mFragmentPagerAdapter);
        viewPager.setCurrentItem(0);

        tabLayout.setupWithViewPager(viewPager);
    }

    public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
        if (d == null) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String s = sdf.format(d);
        return s;
    }

    @Override
    public void onClick(View view) {
//        new TimePickerBuilder(BillActivity.this, (Date date, View v) -> {
//            year = date2Str(date,"yyyy");
//            month = date2Str(date,"MM");
//            Log.i("TimePickerBuilder", "onClick: year="+year+" month="+month);
//            BmobQuery<Bill> query = MonthListFragment.getMonthList(user,year,month);
//            query.findObjects(new FindListener<Bill>() {
//                @Override
//                public void done(List<Bill> object, BmobException e) {
//                    if(e==null){
//                        Log.i("TimePickerBuilder", "done: 共有"+object.size()+"条");
////                                        MonthListBean mlb = packageDetailList(object);
////                                Message msg = mHandler.obtainMessage(9);
////                                msg.obj = object;
////                                mHandler.sendMessage(msg);
//                        MonthListBean mlb = packageDetailList(object);
//                        MonthChartBean mcb = packageChartList(object);
//                        monthListFragment.changeDate(year, month,mlb);
//                        monthChartFragment.changeDate(year, month,mcb);
//
//                    }else{
//                        Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
//                    }
//                }
//            });
////                    Thread thread = new Thread(new Runnable() {
////                        @Override
////                        public void run() {
////
////                        }
////                    }); //注意！必须加this
////                    thread.start(); // 调用run方法
//        }).setType(new boolean[]{true, true, false, false, false, false})
//                .setRangDate(null, Calendar.getInstance())
//                .isDialog(true)//是否显示为对话框样式
//                .build().show();
    }

    public void setMonthChartBean(MonthChartBean monthChartBean){
        Log.i("BillActivity", "setMonthChartBean: monthChartBean"+monthChartBean);
        this.monthChartBean = monthChartBean;
    }
    public MonthChartBean getMonthChartBean(){
        return monthChartBean;
    }
}
