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

import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.swufe.bill.adapter.MainFragmentPagerAdapter;
import com.swufe.bill.fragment.MonthChartFragment;
import com.swufe.bill.fragment.MonthListFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.bmob.v3.BmobUser;

import static android.media.MediaExtractor.MetricsConstants.FORMAT;

public class BillActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        init_views();

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerBuilder(BillActivity.this, (Date date, View v) -> {
                    monthListFragment.changeDate(date2Str(date, "yyyy"), date2Str(date, "MM"));
                    monthChartFragment.changeDate(date2Str(date, "yyyy"), date2Str(date, "MM"));
                }).setType(new boolean[]{true, true, false, false, false, false})
                        .setRangDate(null, Calendar.getInstance())
                        .isDialog(true)//是否显示为对话框样式
                        .build().show();
            }
        });
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
//
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.toolbar_date:
//                //时间选择器
//                new TimePickerBuilder(BillActivity.this, (Date date, View v) -> {
//                    monthListFragment.changeDate(date2Str(date, "yyyy"), date2Str(date, "MM"));
//                    monthChartFragment.changeDate(date2Str(date, "yyyy"), date2Str(date, "MM"));
//                }).setType(new boolean[]{true, true, false, false, false, false})
//                        .setRangDate(null, Calendar.getInstance())
//                        .isDialog(true)//是否显示为对话框样式
//                        .build().show();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

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
}
