package com.swufe.bill;

import android.util.Log;

import com.swufe.bill.bean.Conversation;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class ConDatabaseHelper {
    private static String TAG = "ConDatabaseHelper";
    public static final String DB_NAME = "Conversation";

    public void addRecord(Conversation conversation){
        conversation.setUserId(GlobalUtil.getInstance().getUserId());
        conversation.save(new SaveListener<String>() {
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

}
