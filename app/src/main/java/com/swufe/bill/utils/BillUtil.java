package com.swufe.bill.utils;

import android.util.Log;

import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthChartBean;
import com.swufe.bill.bean.MonthListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 包装账单展示列表工具类
 */
public class BillUtil {

    /**
     * 账单按天分类
     * @param list
     * @return
     */
    public static MonthListBean packageDetailList(List<Bill> list) {
        MonthListBean bean = new MonthListBean();
        if(list.size()==0){
            bean.setT_income(String.valueOf(0.00));
            bean.setT_outcome(String.valueOf(0.00));
            bean.setT_total(String.valueOf(0.00));
            bean.setDaylist(null);
        }else {
            float t_income = 0;
            float t_outcome = 0;
            List<MonthListBean.DaylistBean> daylist = new ArrayList<>();
            List<Bill> beanList = new ArrayList<>();
            float income = 0;
            float outcome = 0;

            String preDay = "";  //记录前一天的时间
            for (int i = 0; i < list.size(); i++) {
                Bill bBill = list.get(i);
                Log.i("BillUtils", "packageDetailList: objectedId="+bBill.getObjectedId());
                bBill.setObjectedId(list.get(i).getObjectedId());
                Log.i("BillUtils", "packageDetailList: objectedId2="+list.get(i).getObjectedId());

                //计算总收入支出
                if (bBill.getType() == 2)
                    t_income += Float.parseFloat(bBill.getAmount().toString());
                else
                    t_outcome += Float.parseFloat(bBill.getAmount().toString());

                //判断后一个账单是否于前者为同一天
                if (i == 0 || preDay.equals(bBill.getDate())) {

                    if (bBill.getType() == 2)
                        income += Float.parseFloat(bBill.getAmount().toString());
                    else
                        outcome += Float.parseFloat(bBill.getAmount().toString());
                    beanList.add(bBill);
                    Log.i("BillUtils", "packageDetailList: list.add");

                    if (i==0)
                        preDay = bBill.getDate();
                } else {
                    //局部变量防止引用冲突
                    List<Bill> tmpList = new ArrayList<>();
                    tmpList.addAll(beanList);
                    Log.i("BillUtils", "packageDetailList: tmpList.add");
                    MonthListBean.DaylistBean tmpDay = new MonthListBean.DaylistBean();
                    tmpDay.setList(tmpList);
                    Log.i("BillUtils", "packageDetailList: tmpDay.add");
                    tmpDay.setMoney("支出：" + outcome + " 收入：" + income);
                    tmpDay.setTime(preDay);
                    daylist.add(tmpDay);

                    //清空前一天的数据
                    beanList.clear();
                    income = 0;
                    outcome = 0;

                    //添加数据
                    if (bBill.getType() == 2)
                        income += Float.parseFloat(bBill.getAmount().toString());
                    else
                        outcome += Float.parseFloat(bBill.getAmount().toString());
                    beanList.add(bBill);
                    Log.i("BillUtils", "packageDetailList: list.add");
                    preDay = bBill.getDate();
                }
            }

            if (beanList.size() > 0) {
                //局部变量防止引用冲突
                List<Bill> tmpList = new ArrayList<>();
                tmpList.addAll(beanList);
                Log.i("tmpList", "packageDetailList: tmpList.addall");
                MonthListBean.DaylistBean tmpDay = new MonthListBean.DaylistBean();
                tmpDay.setList(tmpList);
                tmpDay.setMoney("支出：" + outcome + " 收入：" + income);
                tmpDay.setTime(beanList.get(0).getDate());
                daylist.add(tmpDay);
            }

            bean.setT_income(String.valueOf(t_income));
            bean.setT_outcome(String.valueOf(t_outcome));
            bean.setT_total(String.valueOf(t_income-t_outcome));
            bean.setDaylist(daylist);
        }
        return bean;
    }

    /**
     * 账单按类型分类
     * @param list
     * @return
     */
    public static MonthChartBean packageChartList(List<Bill> list) {

        MonthChartBean bean = new MonthChartBean();
        float t_income = 0;
        float t_outcome = 0;

        Map<String, List<Bill>> mapIn = new HashMap<>();
        Map<String, Float> moneyIn = new HashMap<>();
        Map<String, List<Bill>> mapOut = new HashMap<>();
        Map<String, Float> moneyOut = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Bill bBill = list.get(i);
            //计算总收入支出
            if (bBill.getType() ==2){
                t_income += Float.parseFloat(bBill.getAmount().toString());
            }
            else{
                t_outcome += Float.parseFloat(bBill.getAmount().toString());
            }

            //账单分类
            String sort = bBill.getCategory();
            List<Bill> listBill;
            if (bBill.getType() ==2) {
                if (mapIn.containsKey(sort)) {
                    listBill = mapIn.get(sort);
                } else {
                    listBill = new ArrayList<>();
                }
                if (moneyIn.containsKey(sort))
                    moneyIn.put(sort, moneyIn.get(sort) + Float.parseFloat(bBill.getAmount().toString()));
                else
                    moneyIn.put(sort, Float.parseFloat(bBill.getAmount().toString()));
                listBill.add(bBill);
                mapIn.put(sort, listBill);
            } else {
                if (mapOut.containsKey(sort)) {
                    listBill = mapOut.get(sort);
                } else {
                    listBill = new ArrayList<>();
                }
                if (moneyOut.containsKey(sort))
                    moneyOut.put(sort, moneyOut.get(sort) + Float.parseFloat(bBill.getAmount().toString()));
                else
                    moneyOut.put(sort, Float.parseFloat(bBill.getAmount().toString()));
                listBill.add(bBill);
                mapOut.put(sort, listBill);
            }
        }

        List<MonthChartBean.SortTypeList> outSortlist = new ArrayList<>();    //账单分类统计支出
        List<MonthChartBean.SortTypeList> inSortlist = new ArrayList<>();    //账单分类统计收入

        for (Map.Entry<String, List<Bill>> entry : mapOut.entrySet()) {
            MonthChartBean.SortTypeList sortTypeList = new MonthChartBean.SortTypeList();
            sortTypeList.setList(entry.getValue());
            sortTypeList.setSortName(entry.getKey());
            sortTypeList.setSortImg(entry.getValue().get(0).getCategory());
            sortTypeList.setMoney(moneyOut.get(entry.getKey()));
            sortTypeList.setBack_color(randomColor());
            outSortlist.add(sortTypeList);
        }
        for (Map.Entry<String, List<Bill>> entry : mapIn.entrySet()) {
            MonthChartBean.SortTypeList sortTypeList = new MonthChartBean.SortTypeList();
            sortTypeList.setList(entry.getValue());
            sortTypeList.setSortName(entry.getKey());
            sortTypeList.setSortImg(entry.getValue().get(0).getCategory());
            sortTypeList.setMoney(moneyIn.get(entry.getKey()));
            sortTypeList.setBack_color(randomColor());
            inSortlist.add(sortTypeList);
        }

        bean.setOutSortlist(outSortlist);
        bean.setInSortlist(inSortlist);
        bean.setTotalIn(t_income);
        bean.setTotalOut(t_outcome);
        return bean;
    }

    public static String randomColor(){
        //红色
        String red;
        //绿色
        String green;
        //蓝色
        String blue;
        //生成随机对象
        Random random = new Random();
        //生成红色颜色代码
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成绿色颜色代码
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成蓝色颜色代码
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();

        //判断红色代码的位数
        red = red.length()==1 ? "0" + red : red ;
        //判断绿色代码的位数
        green = green.length()==1 ? "0" + green : green ;
        //判断蓝色代码的位数
        blue = blue.length()==1 ? "0" + blue : blue ;
        //生成十六进制颜色值
        return "#"+red+green+blue;
    }

    /**
     * 账单按支付方式分类
     * @param list
     * @return
     */
//    public static MonthAccountBean packageAccountList(List<Bill> list) {
//
//        MonthAccountBean bean = new MonthAccountBean();
//        float t_income = 0;
//        float t_outcome = 0;
//
//        Map<String, List<Bill>> mapAccount = new HashMap<>();
//        Map<String, Float> mapMoneyIn = new HashMap<>();
//        Map<String, Float> mapMoneyOut = new HashMap<>();
//        for (int i = 0; i < list.size(); i++) {
//            Bill bBill = list.get(i);
//            //计算总收入支出
//            if (bBill.isIncome()) t_income += bBill.getCost();
//            else t_outcome += bBill.getCost();
//
//            String pay = bBill.getPayName();
//
//            if (mapAccount.containsKey(pay)) {
//                List<Bill> bBills = mapAccount.get(pay);
//                bBills.add(bBill);
//                mapAccount.put(pay, bBills);
//            } else {
//                List<Bill> bBills = new ArrayList<>();
//                bBills.add(bBill);
//                mapAccount.put(pay, bBills);
//            }
//
//            if (bBill.isIncome()) {
//                if (mapMoneyIn.containsKey(pay)) {
//                    mapMoneyIn.put(pay, mapMoneyIn.get(pay) + bBill.getCost());
//                } else {
//                    mapMoneyIn.put(pay, bBill.getCost());
//                }
//            } else {
//                if (mapMoneyOut.containsKey(pay)) {
//                    mapMoneyOut.put(pay, mapMoneyOut.get(pay) + bBill.getCost());
//                } else {
//                    mapMoneyOut.put(pay, bBill.getCost());
//                }
//            }
//        }
//
//        List<MonthAccountBean.PayTypeListBean> payTypeListBeans = new ArrayList<>();    //账单分类统计支出
//        for (Map.Entry<String, List<Bill>> entry : mapAccount.entrySet()) {
//            MonthAccountBean.PayTypeListBean payTypeListBean = new MonthAccountBean.PayTypeListBean();
//            payTypeListBean.setBills(entry.getValue());
//            //先判断当前支付方式是否有输入或支出
//            //因为有可能只有支出或收入
//            if (mapMoneyIn.containsKey(entry.getKey()))
//                payTypeListBean.setIncome(mapMoneyIn.get(entry.getKey()));
//            if (mapMoneyOut.containsKey(entry.getKey()))
//                payTypeListBean.setOutcome(mapMoneyOut.get(entry.getKey()));
//            payTypeListBean.setPayImg(entry.getValue().get(0).getPayImg());
//            payTypeListBean.setPayName(entry.getValue().get(0).getPayName());
//            payTypeListBeans.add(payTypeListBean);
//        }
//
//        bean.setTotalIn(t_income);
//        bean.setTotalOut(t_outcome);
//        bean.setList(payTypeListBeans);
//        return bean;
//    }

    /**
     * CoBill=>Bill
     *
     * @param coBill
     * @return
     */
//    public static Bill toBill(CoBill coBill) {
//        Bill bBill = new Bill();
//        bBill.setObjectedId(coBill.getObjectId());
////        bBill.setVersion(coBill.getVersion());
//        bBill.setType(coBill.getIncome());
//        bBill.setDate(coBill.getCrdate());
//        bBill.setCrDate(coBill.getCrdate());
////        bBill.setCategory(coBill.getSortImg());
//        bBill.setCategory(coBill.getSortName());
////        bBill.setPayImg(coBill.getPayImg());
////        bBill.setPayName(coBill.getPayName());
//        bBill.setUserId(coBill.getUserid());
//        bBill.setRemark(coBill.getContent());
//        bBill.setAmount(coBill.getCost());
//
//        return bBill;
//    }
}
