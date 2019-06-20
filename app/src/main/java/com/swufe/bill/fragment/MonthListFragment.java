package com.swufe.bill.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swufe.bill.GlobalUtil;
import com.swufe.bill.R;
import com.swufe.bill.adapter.MonthListAdapter;
import com.swufe.bill.adapter.StickyHeaderGridLayoutManager;
import com.swufe.bill.base.BaseFragment;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.swufe.bill.utils.BillUtil.packageDetailList;

public class MonthListFragment extends Fragment{

    private static String TAG = "MonthListFragment";

    private RecyclerView rvList;
    private TextView t_income;
    private TextView t_outcome;
    private TextView t_total;
    private Context mContext;

    int part, index;
    private static final int SPAN_SIZE = 1;
    private String setYear = MonthChartFragment.getYearAndMonthNow()[0];
    private String setMonth = MonthChartFragment.getYearAndMonthNow()[1];

    private StickyHeaderGridLayoutManager mLayoutManager;
    private MonthListAdapter adapter;
    private MonthListListener monthListListener;
    private MonthListBean monthListBean;
    private List<MonthListBean.DaylistBean> list;
    private Handler handler;

    private BmobUser user = GlobalUtil.getInstance().getUserId();

    public MonthListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_month_list, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        init_views();

        //开启子线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<Bill> query = getMonthList(user,setYear,setMonth);
                query.findObjects(new FindListener<Bill>() {
                    @Override
                    public void done(List<Bill> object, BmobException e) {
                        if(e==null){
                            Log.i(TAG, "done: 共有"+object.size()+"条");
                            MonthListBean mlb = packageDetailList(object);
                            Message msg = handler.obtainMessage(5);
                            msg.obj = mlb;
                            handler.sendMessage(msg);
                        }else{
                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                        }
                    }
                });
            }
        }); //注意！必须加this
        thread.start(); // 调用run方法
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 5) {
                    monthListBean = (MonthListBean) msg.obj;
                    Log.i(TAG, "handleMessage: monthListBean="+monthListBean.getDaylist());
                    //设置数据
//                    monthListListener.OnDataChanged(monthListBean.getT_outcome()
//                            , monthListBean.getT_income(), monthListBean.getT_total());
                    t_income.setText(monthListBean.getT_income());
                    t_outcome.setText(monthListBean.getT_outcome());
                    t_total.setText(monthListBean.getT_total());
                    list = monthListBean.getDaylist();
                    Log.i(TAG, "handleMessage: list.size="+list.get(0).getList().size());
//                    adapter.setmDatas(nList);
//                    adapter.notifyAllSectionsDataSetChanged();

                    mLayoutManager = new StickyHeaderGridLayoutManager(SPAN_SIZE);
                    mLayoutManager.setHeaderBottomOverlapMargin(5);
                    adapter = new MonthListAdapter(mContext,list);
                    rvList.setLayoutManager(mLayoutManager);
                    rvList.setAdapter(adapter);

                }
                super.handleMessage(msg);
            }
        };
    }

    private void init_views() {
        t_income = getActivity().findViewById(R.id.t_income);
        t_outcome = getActivity().findViewById(R.id.t_outcome);
        t_total = getActivity().findViewById(R.id.t_total);
        rvList = getActivity().findViewById(R.id.rv_list);
        rvList.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean animateRemove(RecyclerView.ViewHolder holder) {
                dispatchRemoveFinished(holder);
                return false;
            }
        });

    }

    private String getYear2Month() {
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf(cal.get(Calendar.YEAR));
        int m = cal.get(Calendar.MONTH)+1;
        String month;
        if(m<10){
            month = "0"+String.valueOf(m);
        }else{
            month = String.valueOf(m);
        }
        String date = year+"-"+month;
        return date;
    }

    public void changeDate(String year, String month) {
        setYear = year;
        setMonth = month;
        Log.i(TAG, "changeDate: year="+setYear+" month="+setMonth);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<Bill> query = getMonthList(user,setYear,setMonth);
                query.findObjects(new FindListener<Bill>() {
                    @Override
                    public void done(List<Bill> object, BmobException e) {
                        if(e==null){
                            Log.i(TAG, "done: 共有"+object.size()+"条");
                            MonthListBean mlb = packageDetailList(object);
                            Message msg = handler.obtainMessage(5);
                            msg.obj = mlb;
                            handler.sendMessage(msg);
                        }else{
                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                        }
                    }
                });
            }
        }); //注意！必须加this
        thread.start(); // 调用run方法
    }

    private BmobQuery<Bill> getMonthList(BmobUser user, String setYear, String setMonth) {
        String y2m = setYear+"-"+setMonth;
        BmobQuery<Bill> eq1 = new BmobQuery<>();
        eq1.addWhereEqualTo("userId",user);
        BmobQuery<Bill> eq2 = new BmobQuery<>();
        eq2.addWhereEqualTo("year2month",y2m);
        List<BmobQuery<Bill>> andQuerys = new ArrayList<BmobQuery<Bill>>();
        andQuerys.add(eq1);
        andQuerys.add(eq2);
        BmobQuery<Bill> query = new BmobQuery<Bill>();
        query.and(andQuerys);
        query.setLimit(50);
        return query;
    }

    public interface MonthListListener {
        void OnDataChanged(String outcome, String income, String total);
    }

}
