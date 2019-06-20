package com.swufe.bill.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.swufe.bill.fragment.MonthChartFragment;
import com.swufe.bill.fragment.MonthListFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private String[] title = new String[]{"明细","图标"};

    public MainFragmentPagerAdapter(FragmentManager manager){
        super(manager);
    }
    @Override
    public Fragment getItem(int position) {
        if(position==0){
            return new MonthListFragment();
        }else{
            return new MonthChartFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
