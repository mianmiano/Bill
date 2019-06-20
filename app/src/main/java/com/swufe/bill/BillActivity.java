package com.swufe.bill;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.swufe.bill.adapter.MainFragmentPagerAdapter;
import com.swufe.bill.fragment.MonthChartFragment;
import com.swufe.bill.fragment.MonthListFragment;

import cn.bmob.v3.BmobUser;

public class BillActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView tOutcome;
    private TextView tIncome;
    private TextView tTotal;

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
    }

    private void init_views() {
        toolbar = findViewById(R.id.toolbar_bill);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.main_viewpager);
        tOutcome = findViewById(R.id.t_outcome);
        tIncome = findViewById(R.id.t_income);
        tTotal = findViewById(R.id.t_total);

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


}
