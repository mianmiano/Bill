package com.swufe.bill.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.swufe.bill.AddRecordActivity;
import com.swufe.bill.BillActivity;
import com.swufe.bill.GlobalUtil;
import com.swufe.bill.R;
import com.swufe.bill.adapter.MonthListAdapter;
import com.swufe.bill.adapter.StickyHeaderGridLayoutManager;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.swufe.bill.BillActivity.date2Str;
import static com.swufe.bill.utils.BillUtil.packageChartList;
import static com.swufe.bill.utils.BillUtil.packageDetailList;

public class MonthListFragment extends Fragment{

    private static String TAG = "MonthListFragment";

    private RecyclerView rvList;
    private TextView t_income;
    private TextView t_outcome;
    private TextView t_total;
    private Context mContext;
    private BillActivity billActivity;
    private ImageButton btnDate;

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
    private MonthChartBean monthChartBean;

    private static float income;
    private static float outcome;
    private static float total;

    public MonthListFragment() {}

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
                BmobQuery<Bill> query = getMonthList(
                        user,MonthChartFragment.getYearAndMonthNow()[0],MonthChartFragment.getYearAndMonthNow()[1]);
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
//                    Log.i(TAG, "handleMessage: list.size="+list.get(0).getList().size());

                    mLayoutManager = new StickyHeaderGridLayoutManager(SPAN_SIZE);
                    mLayoutManager.setHeaderBottomOverlapMargin(5);
                    adapter = new MonthListAdapter(mContext,list);
                    rvList.setLayoutManager(mLayoutManager);
                    rvList.setAdapter(adapter);

                    adapter.setOnStickyHeaderClickListener(new MonthListAdapter.OnStickyHeaderClickListener() {
                        @Override
                        public List<MonthListBean.DaylistBean> OnDeleteClick(
                                Bill item, int section, int offset,List<MonthListBean.DaylistBean> data) {
                            //数据库删除对应数据
                            Log.i(TAG, "onClick: bill UUID="+item.getUuid());
                            //获取objectedId
                            BmobQuery<Bill> query = GlobalUtil.getInstance().
                                    billDatabaseHelper.readRecordByUuid(item.getUuid());
                            query.findObjects(new FindListener<Bill>() {
                                @Override
                                public void done(List<Bill> list, BmobException e) {
                                    if(e==null){
                                        Log.i(TAG, "done: 查询成功：共"+list.size()+"条数据。");
                                        item.setObjectedId(list.get(0).getObjectedId());
                                    }else{
                                        Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                                    }
                                }
                            });
                            data.get(section).getList().remove(offset);
                            item.delete(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        Log.i("bmob","成功");
                                    }else{
                                        Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                                    }
                                }
                            });
                            notifyTotalAmountChanged(data);

                            return data;
                        }

                        @Override
                        public void OnEditClick(Bill item, int section, int offset) {
                            Intent intent = new Intent(getContext(), AddRecordActivity.class);
                            intent.putExtra("record",item);
                            startActivityForResult(intent,4);
                        }
                    });

                }else if(msg.what==9){
                    monthListBean = (MonthListBean) msg.obj;
                    Log.i(TAG, "handleMessage: monthListBean="+monthListBean.getDaylist());
                    t_income.setText(monthListBean.getT_income());
                    t_outcome.setText(monthListBean.getT_outcome());
                    t_total.setText(monthListBean.getT_total());
                    list = monthListBean.getDaylist();
//                    Log.i(TAG, "handleMessage: list.size="+list.get(0).getList().size());
                    adapter.setmDatas(list);
                    adapter.notifyAllSectionsDataSetChanged();
                }
                super.handleMessage(msg);
            }
        };

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerBuilder(getActivity(), (Date date, View v) -> {
                    setYear = date2Str(date,"yyyy");
                    setMonth = date2Str(date,"MM");
                    Log.i("TimePickerBuilder", "onClick: year="+setYear+" month="+setMonth);
                    BmobQuery<Bill> query = MonthListFragment.getMonthList(user,setYear,setMonth);
                    query.findObjects(new FindListener<Bill>() {
                        @Override
                        public void done(List<Bill> object, BmobException e) {
                            if(e==null){
                                Log.i("TimePickerBuilder", "done: 共有"+object.size()+"条");
                                MonthListBean mlb = packageDetailList(object);
                                MonthChartBean mcb = packageChartList(object);
                                ((BillActivity)getActivity()).setMonthChartBean(mcb);
                                MonthChartFragment.isChangedDate = true;
                                Message msg = handler.obtainMessage(9);
                                msg.obj = mlb;
                                handler.sendMessage(msg);
                            }else{
                                Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                            }
                        }
                    });
                }).setType(new boolean[]{true, true, false, false, false, false})
                        .setRangDate(null, Calendar.getInstance())
                        .isDialog(true)//是否显示为对话框样式
                        .build().show();
            }
        });
    }

    private void init_views() {
        btnDate = getActivity().findViewById(R.id.btn_date);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4&&resultCode==2){
            BmobQuery<Bill> query = GlobalUtil.getInstance().billDatabaseHelper.readRecrods(getYear2Month());
            query.findObjects(new FindListener<Bill>() {
                @Override
                public void done(List<Bill> list, BmobException e) {
                    MonthChartBean mcb = packageChartList(list);
                    ((BillActivity)getActivity()).setMonthChartBean(mcb);
                    MonthChartFragment.isEdit = true;
                    MonthListBean mlb = packageDetailList(list);
                    List<MonthListBean.DaylistBean> data = mlb.getDaylist();
                    notifyTotalAmountChanged(data);
                    adapter.setmDatas(data);
                    adapter.notifyAllSectionsDataSetChanged();
                }
            });
        }
    }

    //    public void changeDate(String year, String month,MonthListBean monthListBean) {
//        setYear = year;
//        setMonth = month;
//        Log.i(TAG, "changeDate: year="+setYear+" month="+setMonth);
////        t_income.setText(monthListBean.getT_income());
////        t_outcome.setText(monthListBean.getT_outcome());
////        t_total.setText(monthListBean.getT_total());
//        list = monthListBean.getDaylist();
//        adapter.setmDatas(list);
//        adapter.notifyAllSectionsDataSetChanged();
//    }

    private void notifyTotalAmountChanged(List<MonthListBean.DaylistBean> data) {
        float income = 0;
        float outcome = 0;
        float total = 0;
        for(int i =0;i<data.size();i++){
            for (int j=0;j<data.get(i).getList().size();j++){
                Bill bill = data.get(i).getList().get(j);
                if(bill.getType()==1){
                    outcome += Float.parseFloat(bill.getAmount().toString());
                }else{
                    income += Float.parseFloat(bill.getAmount().toString());
                }
            }
        }
        total = income - outcome;
        t_total.setText(String.valueOf(total));
        t_outcome.setText(String.valueOf(outcome));
        t_income.setText(String.valueOf(income));
    }

    public static void setIncome(float tincome) {
        income = tincome;
    }

    public static void setOutcome(float toutcome) {
        outcome = toutcome;
    }

    public static void setTotal(float ttotal) {
        total = ttotal;
    }

    public static BmobQuery<Bill> getMonthList(BmobUser user, String setYear, String setMonth) {
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

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        //do something

        billActivity = (BillActivity) context;
        billActivity.setHandler(handler);
        Log.i(TAG, "onAttach: 已设置handler");
    }
}
