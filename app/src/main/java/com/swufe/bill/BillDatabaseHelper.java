package com.swufe.bill;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.Conversation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.jiguang.imui.commons.models.IMessage;

public class BillDatabaseHelper {
    private static String TAG = "BmobDatabaseHelper";
    public static final String DB_NAME = "Bill";

    public void addRecord(Bill bill){
        bill.setUserId(GlobalUtil.getInstance().getUserId());
        Log.i(TAG, "addRecord: date="+bill.getDate());
        bill.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    Log.i(TAG, "done: 创建数据成功");
                }else{
                    Log.i(TAG+" bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    public void removeRecord(String objectId){
        Bill bill = new Bill();
        bill.setObjectId(objectId);
        bill.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.i(TAG+" bmob","记录删除成功");
                }else{
                    Log.i(TAG+" bmob","记录删除失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    public void editRecord(String objectId,Bill bill){
        bill.update(objectId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.i(TAG+" bmob","更新成功");
                }else{
                    Log.i(TAG+" bmob","更新失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    public BmobQuery<Bill> readRecordByUuid(String uuid){
//        final Bill[] bill = new Bill[1];
//        Bill bill = new Bill();
        BmobQuery<Bill> eq1 = new BmobQuery<Bill>();
        eq1.addWhereEqualTo("userId",GlobalUtil.getInstance().getUserId());
        BmobQuery<Bill> eq2 = new BmobQuery<Bill>();
        eq2.addWhereEqualTo("uuid",uuid);
        List<BmobQuery<Bill>> andQuerys = new ArrayList<BmobQuery<Bill>>();
        andQuerys.add(eq1);
        andQuerys.add(eq2);
        BmobQuery<Bill> query = new BmobQuery<Bill>();
        query.and(andQuerys);
        return query;
    }

    public BmobQuery<Bill> readRecrods(String dateStr){
        //查询账户下对应时间的账
        BmobQuery<Bill> eq1 = new BmobQuery<Bill>();
        eq1.addWhereEqualTo("userId",GlobalUtil.getInstance().getUserId());
        BmobQuery<Bill> eq2 = new BmobQuery<Bill>();
        eq2.addWhereEqualTo("year2month",dateStr);
        List<BmobQuery<Bill>> andQuerys = new ArrayList<BmobQuery<Bill>>();
        andQuerys.add(eq1);
        andQuerys.add(eq2);
        BmobQuery<Bill> query = new BmobQuery<Bill>();
        query.and(andQuerys);
        query.setLimit(50);
        return query;
    }

    public LinkedList<String> getAvaliableDate(){

        final LinkedList<String> dates = new LinkedList<>();

        BmobQuery<Bill> query = new BmobQuery<Bill>();
        query.addWhereEqualTo("userId", GlobalUtil.getInstance().getUserId());
        query.setLimit(100);
        query.findObjects(new FindListener<Bill>() {
            @Override
            public void done(List<Bill> object, BmobException e) {
                if(e==null){
                    Log.i(TAG, "done: 查询成功：共"+object.size()+"条数据。");
                    for (Bill bill : object) {
                        String date = bill.getDate();
                        if(!dates.contains(date)){
                            dates.add(date);
                        }
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
        return dates;
    }
}
