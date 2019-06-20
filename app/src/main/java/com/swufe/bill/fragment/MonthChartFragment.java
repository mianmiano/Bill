package com.swufe.bill.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.swufe.bill.GlobalUtil;
import com.swufe.bill.R;
import com.swufe.bill.adapter.MonthChartBillViewBinder;
import com.swufe.bill.base.BaseFragment;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;
import com.swufe.bill.widget.PieChartUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.jiguang.imui.view.CircleImageView;
import me.drakeet.multitype.MultiTypeAdapter;

import static com.swufe.bill.utils.BillUtil.packageChartList;

public class MonthChartFragment extends Fragment implements Runnable,
        OnChartValueSelectedListener, View.OnClickListener {

    private static String TAG = "MonthChartFragment";

    private PieChart mChart;
    private TextView centerTitle;
    private TextView centerMoney;
    private LinearLayout layoutCenter;
    private ImageView centerImg;
    private CircleImageView circleBg;
    private ImageView circleImg;
    private RelativeLayout layoutCircle;
    private TextView title;
    private TextView money;
    private TextView rankTitle;
    private RelativeLayout layoutOther;
    private TextView otherMoney;
    private SwipeRefreshLayout swipe;
    private RelativeLayout itemType;
    private RelativeLayout itemOther;
    private RecyclerView rvList;
    private LinearLayout layoutTypedata;

    private boolean TYPE = true;//默认总支出true
    private List<MonthChartBean.SortTypeList> tMoneyBeanList;
    private String sort_image;//饼状图与之相对应的分类图片地址
    private String sort_name;
    private String back_color;

    private MonthChartBean monthChartBean;
    private MultiTypeAdapter adapter;
    private Handler handler;
    private BmobUser user = GlobalUtil.getInstance().getUserId();

    private String setYear = MonthChartFragment.getYearAndMonthNow()[0];
    private String setMonth = MonthChartFragment.getYearAndMonthNow()[1];

    public MonthChartFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_chart, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init_views();

        //开启子线程
        Thread thread = new Thread(this); //注意！必须加this
        thread.start(); // 调用run方法

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 3) {
                    monthChartBean = (MonthChartBean) msg.obj;
                    Log.i(TAG, "handleMessage: monthChartBean="+monthChartBean.getTotalIn());
                    setReportData();
                }
                super.handleMessage(msg);
            }
        };
    }

    private void init_views() {
        mChart = Objects.requireNonNull(getActivity()).findViewById(R.id.chart);
        centerTitle = getActivity().findViewById(R.id.center_title);
        centerMoney = getActivity().findViewById(R.id.center_money);
        layoutCenter = getActivity().findViewById(R.id.layout_center);
        centerImg = getActivity().findViewById(R.id.center_img);
        circleBg = getActivity().findViewById(R.id.circle_bg);
        circleImg = getActivity().findViewById(R.id.circle_img);
        layoutCircle = getActivity().findViewById(R.id.layout_circle);
        title = getActivity().findViewById(R.id.title);
        money = getActivity().findViewById(R.id.money);
        rankTitle = getActivity().findViewById(R.id.rank_title);
        layoutOther = getActivity().findViewById(R.id.layout_other);
        otherMoney = getActivity().findViewById(R.id.other_money);
        swipe = getActivity().findViewById(R.id.swipe);
        itemType = getActivity().findViewById(R.id.item_type);
        itemOther = getActivity().findViewById(R.id.item_other);
        rvList = getActivity().findViewById(R.id.rv_list_chart);
        layoutTypedata = getActivity().findViewById(R.id.layout_typedata);


        //初始化饼状图
        PieChartUtils.initPieChart(mChart);
        //设置圆盘是否转动，默认转动
        mChart.setRotationEnabled(true);
        mChart.setOnChartValueSelectedListener(this);
        //改变加载显示的颜色
        swipe.setColorSchemeColors(getResources().getColor(R.color.text_red), getResources().getColor(R.color.text_red));
        //设置向下拉多少出现刷新
        swipe.setDistanceToTriggerSync(200);
        //设置刷新出现的位置
        swipe.setProgressViewEndTarget(false, 200);
        swipe.setOnRefreshListener(()->{
            swipe.setRefreshing(false);
            changeDate(setYear,setMonth);
//            getMonthChart(GlobalUtil.getInstance().getUserId(), setYear, setMonth);
        });

        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MultiTypeAdapter();
        adapter.register(Bill.class, new MonthChartBillViewBinder(getContext()));
        rvList.setAdapter(adapter);

        layoutCenter.setOnClickListener(this);
        itemType.setOnClickListener(this);
        itemOther.setOnClickListener(this);
    }

    @Override
    public void run() {
        BmobQuery<Bill> query = getMonthChart(GlobalUtil.getInstance().getUserId(),setYear,setMonth);
        query.findObjects(new FindListener<Bill>() {
            @Override
            public void done(List<Bill> object, BmobException e) {
                if(e==null){
                    Log.i(TAG, "done: 共有"+object.size()+"条");
                    MonthChartBean mChartBean = packageChartList(object);
                    Log.i(TAG, "done: monthChartBean="+mChartBean.getTotalIn());
                    Message msg = handler.obtainMessage(3);
                    msg.obj = mChartBean;
                    handler.sendMessage(msg);
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    private BmobQuery<Bill> getMonthChart(BmobUser user,String setYear, String setMonth) {
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

    public static String[] getYearAndMonthNow() {
        String[] date = new String[2];
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf(cal.get(Calendar.YEAR));
        int m = cal.get(Calendar.MONTH)+1;
        String month;
        if(m<10){
            month = "0"+String.valueOf(m);
        }else{
            month = String.valueOf(m);
        }
        date[0]=year;
        date[1]=month;
        return date;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_center:  //图表中心键
                TYPE = !TYPE;
                setReportData();
                break;
            case R.id.item_type:
                break;
            case R.id.item_other:
                break;
        }
    }

    /**
     * 报表数据
     */
    private void setReportData() {

        if (monthChartBean == null) {
            Log.i(TAG, "setReportData: monthChartBean为空！");
            return;
        }

        float totalMoney;
        if (TYPE) {
            centerTitle.setText("总支出");
            centerImg.setImageResource(R.mipmap.tallybook_output);
            tMoneyBeanList = monthChartBean.getOutSortlist();
            totalMoney = monthChartBean.getTotalOut();
        } else {
            centerTitle.setText("总收入");
            centerImg.setImageResource(R.mipmap.tallybook_input);
            tMoneyBeanList = monthChartBean.getInSortlist();
            totalMoney = monthChartBean.getTotalIn();
        }

        centerMoney.setText("" + totalMoney);

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (tMoneyBeanList != null && tMoneyBeanList.size() > 0) {
            layoutTypedata.setVisibility(View.VISIBLE);
            for (int i = 0; i < tMoneyBeanList.size(); i++) {
                float scale =tMoneyBeanList.get(i).getMoney() / totalMoney;
                float value = (scale < 0.06f) ? 0.06f : scale;
                entries.add(new PieEntry(value, PieChartUtils.getDrawable(tMoneyBeanList.get(i).getSortImg())));
                colors.add(Color.parseColor(tMoneyBeanList.get(i).getBack_color()));
            }
            setNoteData(0,entries.get(0).getValue());
        } else {//无数据时的显示
            layoutTypedata.setVisibility(View.GONE);
            entries.add(new PieEntry(1f));
            colors.add(0xffAAAAAA);
        }

        PieChartUtils.setPieChartData(mChart, entries, colors);
    }

    /**
     * 点击饼状图上区域后相应的数据设置
     *
     * @param index
     */
    private void setNoteData(int index, float value) {
        if (null==tMoneyBeanList||tMoneyBeanList.size()==0)
            return;
        sort_image = tMoneyBeanList.get(index).getSortImg();
        sort_name = tMoneyBeanList.get(index).getSortName();
        back_color = tMoneyBeanList.get(index).getBack_color();
        if (TYPE) {
            money.setText("-" + tMoneyBeanList.get(index).getMoney());
        } else {
            money.setText("+" + tMoneyBeanList.get(index).getMoney());
        }
        DecimalFormat df = new DecimalFormat("0.00%");
        title.setText(sort_name+" : "+df.format(value));
        rankTitle.setText(sort_name + "排行榜");
        circleBg.setImageDrawable(new ColorDrawable(Color.parseColor(back_color)));
        circleImg.setImageDrawable(PieChartUtils.getDrawable(tMoneyBeanList.get(index).getSortImg()));

//        adapter.setSortName(sort_name);
        adapter.setItems(tMoneyBeanList.get(index).getList());
        adapter.notifyDataSetChanged();
    }

    public void changeDate(String year, String month) {
        setYear = year;
        setMonth = month;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<Bill> query = getMonthChart(user,setYear,setMonth);
                query.findObjects(new FindListener<Bill>() {
                    @Override
                    public void done(List<Bill> object, BmobException e) {
                        if(e==null){
                            Log.i(TAG, "done: 共有"+object.size()+"条");
                            MonthChartBean mcb = packageChartList(object);
                            Message msg = handler.obtainMessage(5);
                            msg.obj = mcb;
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
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        int entryIndex = (int) h.getX();
        PieChartUtils.setRotationAngle(mChart, entryIndex);
        setNoteData(entryIndex,e.getY());
    }

    @Override
    public void onNothingSelected() {

    }
}
