package com.swufe.bill;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthListBean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.swufe.bill.utils.BillUtil.packageDetailList;

public class GlobalUtil {
    private static final String TAG = "GlobalUtil";

    private static GlobalUtil instance;

    public BillDatabaseHelper billDatabaseHelper;
    public ConDatabaseHelper conDatabaseHelper;
    private Context context;
    public MainActivity mainActivity;
    public LoginActivity loginActivity;

    private BmobUser user;
    private DefaultUser robot;

    public LinkedList<CategoryResBean> costRes = new LinkedList<>();
    public LinkedList<CategoryResBean> earnRes = new LinkedList<>();

    private static int[] costIconRes = {R.drawable.type_changhuanfeiyong,
            R.drawable.type_shouxufei,R.drawable.type_weiyuejin, R.drawable.type_zhufang,R.drawable.type_bangong,
            R.drawable.type_canyin,R.drawable.type_yiliao,R.drawable.type_yundong,R.drawable.type_yule,
            R.drawable.type_jujia,R.drawable.type_chongwu,R.drawable.type_shuma,
            R.drawable.type_juanzeng, R.drawable.type_lingshi,R.drawable.type_haizi,
            R.drawable.type_zhangbei,R.drawable.type_liwu, R.drawable.type_xuexi,
            R.drawable.type_shuiguo,R.drawable.type_meirong, R.drawable.type_weixiu,
            R.drawable.type_lvxing,R.drawable.type_jiaotong,R.drawable.type_yiliao,
            R.drawable.type_lijin};
    private static int[] costIconResBlack = {R.drawable.sort_huankuan,
            R.drawable.sort_shouxufei,R.drawable.sort_weiyuejin,
            R.drawable.sort_zhufang, R.drawable.sort_bangong,
            R.drawable.sort_canyin,R.drawable.sort_yiliao,R.drawable.sort_yundong,
            R.drawable.sort_yule,
            R.drawable.sort_jujia,R.drawable.sort_chongwu,R.drawable.sort_shuma,
            R.drawable.sort_juanzeng, R.drawable.sort_lingshi,R.drawable.sort_haizi,
            R.drawable.sort_zhangbei,R.drawable.sort_liwu, R.drawable.sort_xuexi,
            R.drawable.sort_shuiguo,R.drawable.sort_meirong, R.drawable.sort_weixiu,
            R.drawable.sort_lvxing,R.drawable.sort_jiaotong,R.drawable.sort_yiliao,
            R.drawable.sort_lijin};
    private static int[] costIconResBlack_mp = {R.mipmap.sort_huankuan,
            R.mipmap.sort_shouxufei,R.mipmap.sort_weiyuejin,
            R.mipmap.sort_zhufang, R.mipmap.sort_bangong,
            R.mipmap.sort_canyin,R.mipmap.sort_yiliao,R.mipmap.sort_yundong,
            R.mipmap.sort_yule,
            R.mipmap.sort_jujia,R.mipmap.sort_chongwu,R.mipmap.sort_shuma,
            R.mipmap.sort_juanzeng, R.mipmap.sort_lingshi,R.mipmap.sort_haizi,
            R.mipmap.sort_zhangbei,R.mipmap.sort_liwu, R.mipmap.sort_xuexi,
            R.mipmap.sort_shuiguo,R.mipmap.sort_meirong, R.mipmap.sort_weixiu,
            R.mipmap.sort_lvxing,R.mipmap.sort_jiaotong,R.mipmap.sort_yiliao,
            R.mipmap.sort_lijin};

    private static String[] costTitle = {"还款","手续费","违约金","住房","办公",
            "餐饮","医疗","运动","娱乐","居家","宠物",
            "数码","捐赠","零食","孩子","长辈","礼物","学习","水果","美容",
            "维修","旅行","交通","饮料","礼金"};

    private static int[] earnIconRes = {R.drawable.type_lijin,
            R.drawable.type_yongjinjiangli,R.drawable.type_lixi,R.drawable.type_lixi,
            R.drawable.type_jianzhi};

    private static int[] earnIconResBlack = {R.drawable.sort_lijin,
            R.drawable.sort_jiangjin,R.drawable.sort_lixi,R.drawable.sort_fanxian,
            R.drawable.sort_jianzhi};

    private static String[] earnTitle = {"工资","礼金","利息","理财","兼职"};

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        billDatabaseHelper = new BillDatabaseHelper();
        conDatabaseHelper = new ConDatabaseHelper();

        Bmob.initialize(context,"95131e35bd99e7e316cee6797a581bf8","Bmob");
        login();

        robot = new DefaultUser("0","面面","R.id.drawable.robot");

        for(int i = 0;i<costTitle.length;i++){
            CategoryResBean res = new CategoryResBean();
            res.title = costTitle[i];
            res.resBlack = costIconResBlack[i];
            res.resWhite = costIconResBlack_mp[i];
            costRes.add(res);
        }

        for(int i = 0;i<earnTitle.length;i++){
            CategoryResBean res = new CategoryResBean();
            res.title = earnTitle[i];
            res.resBlack = earnIconResBlack[i];
            res.resWhite = costIconResBlack_mp[i];
            earnRes.add(res);
        }


    }

    public static GlobalUtil getInstance(){

        if (instance == null){
            instance = new GlobalUtil();
        }

        return instance;
    }

    public int getResourceIcon(String category){

        for(CategoryResBean res:costRes){
            if (res.title.equals(category)){
                return res.resBlack;
            }
        }

        for(CategoryResBean res:earnRes){
            if (res.title.equals(category)){
                return res.resBlack;
            }
        }

        return costRes.get(0).resBlack;
    }

    public int getResourceIconMp(String category){

        for(CategoryResBean res:costRes){
            if (res.title.equals(category)){
                return res.resWhite;
            }
        }

        for(CategoryResBean res:earnRes){
            if (res.title.equals(category)){
                return res.resWhite;
            }
        }

        return costRes.get(0).resWhite;
    }

    private void login(){
        user = new BmobUser();
        user.setUsername("AA");
        user.setPassword("111111");
        user.login(new SaveListener<Object>() {
            @Override
            public void done(Object o, BmobException e) {
                if(e==null){
                    Log.i(TAG, "登录成功");
                }else{
                    Log.i(TAG, "登录失败：" + e.getMessage());
                }
            }
        });
    }

    public void setUserId(BmobUser user){this.user = user;}
    public BmobUser getUserId(){
        return user;
    }

    public DefaultUser getRobot() {
        return robot;
    }

}
