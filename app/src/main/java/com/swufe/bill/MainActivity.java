package com.swufe.bill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.Conversation;
import com.swufe.bill.utils.InputUtils;
import com.swufe.bill.widget.PieChartUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.commons.ImageLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.messages.MessageList;
import cn.jiguang.imui.messages.MsgListAdapter;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener,
        EasyPermissions.PermissionCallbacks, SensorEventListener,Runnable {

    private final static String TAG = "MessageListActivity";
    private final int RC_RECORD_VOICE = 0x0001;
    private final int RC_CAMERA = 0x0002;
    private final int RC_PHOTO = 0x0003;

    private MessageList messageList;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ImageButton btnChart;
    private ChatInputView chatInputView;
    private Context mContext;

    private MsgListAdapter<MyMessage> mAdapter;
    private List<MyMessage> mData;
    private Handler handler;

    private InputMethodManager mImm;
    private Window mWindow;
    private HeadsetDetectReceiver mReceiver;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private BmobUser user;
    private DefaultUser robot = GlobalUtil.getInstance().getRobot();

    private String tmpMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        Bmob.initialize(this,"95131e35bd99e7e316cee6797a581bf8","Bmob");
//        login();

        init_views();
        user = (BmobUser) BmobUser.getCurrentUser(BmobUser.class);
        Log.i(TAG, "getMessages: user"+user);

        //开启子线程
        Thread thread = new Thread(this); //注意！必须加this
        thread.start(); // 调用run方法

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 2) {
                    mData = (List<MyMessage>) msg.obj;
                    Log.i(TAG, "handleMessage: mData="+mData);
                    initMsgAdapter();
                }
                else if (msg.what == 4) {
                    String reply = (String) msg.obj;
                    Log.i(TAG, "handleMessage: reply="+reply);
                    Log.i(TAG, "onActivityResult: reply="+reply);
                    MyMessage messageReply = new MyMessage(reply,IMessage.MessageType.RECEIVE_TEXT.ordinal());
                    messageReply.setUserInfo(new DefaultUser("0", "面面", "R.drawable.robot"));
                    mAdapter.addToStart(messageReply,true);

                    Conversation conReply = new Conversation(Conversation.RECEIVE_TEXT,reply);
                    conReply.setUserId(user);
                    GlobalUtil.getInstance().conDatabaseHelper.addRecord(conReply);
                }
                else if(msg.what==7){
                    Bill bBill = (Bill) msg.obj;
                    Log.i(TAG, "handleMessage: bill="+bBill.getUuid());
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(bBill.getCategory())
                            .content("\t\t" + bBill.getAmount().toString() + "元\n\t\t" + bBill.getRemark()
                                    +"\n\n\t\t"+bBill.getDate())
//                            +"\n\t\t"+DateUtils.long2Str(bBill.getCrdate(), FORMAT_HMS_CN))
                            .positiveText("朕知道了")
                            .icon(PieChartUtils.getDrawable(bBill.getCategory()))
                            .limitIconToDefaultSize()
                            .show();
                }
                super.handleMessage(msg);
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转界面
                Intent intent = new Intent(MainActivity.this,AddRecordActivity.class);
                //跳转界面并返回
                startActivityForResult(intent,1);
            }
        });
        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BillActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1&&resultCode == 2){
            Bundle bundle = data.getExtras();
            Bill bill = (Bill) bundle.get("record");
            String category = bill.getCategory();
            Double amount = (Double) bill.getAmount();
            String msgStr = category + amount.toString()+"元";

            //添加到数据库
            Conversation conversation = new Conversation(Conversation.SEND_TEXT,msgStr);
            conversation.setBillUuid(bill.getUuid());
            conversation.setUserId(user);
            GlobalUtil.getInstance().conDatabaseHelper.addRecord(conversation);

            MyMessage message = new MyMessage(msgStr,IMessage.MessageType.SEND_TEXT.ordinal());
            message.setBillId(bill.getUuid());
            message.setConversationId(conversation.getUuid());
            message.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
            message.setUserInfo(new DefaultUser(user.getObjectId(), user.getUsername(), "R.drawable.me"));
            //添加到消息列表
            mAdapter.addToStart(message,true);

            Thread threadReply = new Thread() {
                @Override
                public void run() {
                    String reply = InputUtils.getString(message.getText());
                    Message message = handler.obtainMessage(4);
                    message.obj = reply;
                    handler.sendMessage(message);
                }
            };
            threadReply.start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init_views() {
        GlobalUtil.getInstance().setContext(getApplicationContext());
        GlobalUtil.getInstance().mainActivity = this;

        mContext = getBaseContext();
        messageList = findViewById(R.id.msg_list);
        chatInputView = findViewById(R.id.chat_input);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("XixiBill");
        btnChart = findViewById(R.id.btn_chart);

        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindow = getWindow();

//        mReceiver = new HeadsetDetectReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
//        registerReceiver(mReceiver, intentFilter);
    }

    private void initMsgAdapter() {
        final float density = getResources().getDisplayMetrics().density;
        final float MIN_WIDTH = 60 * density;
        final float MAX_WIDTH = 200 * density;
        final float MIN_HEIGHT = 60 * density;
        final float MAX_HEIGHT = 200 * density;
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadAvatarImage(ImageView avatarImageView, String string) {
                // You can use other image load libraries.
                if (string.contains("R.drawable")) {
                    Integer resId = getResources().getIdentifier(string.replace("R.drawable.", ""),
                            "drawable", getPackageName());

                    avatarImageView.setImageResource(resId);
                } else {
                    Glide.with(MainActivity.this)
                            .load(string)
                            .apply(new RequestOptions().placeholder(R.drawable.aurora_headicon_default))
                            .into(avatarImageView);
                }
            }


            @Override
            public void loadImage(final ImageView imageView, String string) {
                // You can use other image load libraries.
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(string)
                        .apply(new RequestOptions().fitCenter().placeholder(R.drawable.aurora_picture_not_found))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int imageWidth = resource.getWidth();
                                int imageHeight = resource.getHeight();
                                Log.d(TAG, "Image width " + imageWidth + " height: " + imageHeight);

                                // 裁剪 bitmap
                                float width, height;
                                if (imageWidth > imageHeight) {
                                    if (imageWidth > MAX_WIDTH) {
                                        float temp = MAX_WIDTH / imageWidth * imageHeight;
                                        height = temp > MIN_HEIGHT ? temp : MIN_HEIGHT;
                                        width = MAX_WIDTH;
                                    } else if (imageWidth < MIN_WIDTH) {
                                        float temp = MIN_WIDTH / imageWidth * imageHeight;
                                        height = temp < MAX_HEIGHT ? temp : MAX_HEIGHT;
                                        width = MIN_WIDTH;
                                    } else {
                                        float ratio = imageWidth / imageHeight;
                                        if (ratio > 3) {
                                            ratio = 3;
                                        }
                                        height = imageHeight * ratio;
                                        width = imageWidth;
                                    }
                                } else {
                                    if (imageHeight > MAX_HEIGHT) {
                                        float temp = MAX_HEIGHT / imageHeight * imageWidth;
                                        width = temp > MIN_WIDTH ? temp : MIN_WIDTH;
                                        height = MAX_HEIGHT;
                                    } else if (imageHeight < MIN_HEIGHT) {
                                        float temp = MIN_HEIGHT / imageHeight * imageWidth;
                                        width = temp < MAX_WIDTH ? temp : MAX_WIDTH;
                                        height = MIN_HEIGHT;
                                    } else {
                                        float ratio = imageHeight / imageWidth;
                                        if (ratio > 3) {
                                            ratio = 3;
                                        }
                                        width = imageWidth * ratio;
                                        height = imageHeight;
                                    }
                                }
                                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                params.width = (int) width;
                                params.height = (int) height;
                                imageView.setLayoutParams(params);
                                Matrix matrix = new Matrix();
                                float scaleWidth = width / imageWidth;
                                float scaleHeight = height / imageHeight;
                                matrix.postScale(scaleWidth, scaleHeight);
                                imageView.setImageBitmap(Bitmap.createBitmap(resource, 0, 0, imageWidth, imageHeight, matrix, true));
                            }
                        });
            }

            @Override
            public void loadVideo(ImageView imageCover, String uri) {

            }

            /**
             * Load video message
             * @param imageCover Video message's image cover
             * @param uri Local path or url.
             */
//            public void loadVideo(ImageView imageCover, String uri) {
//                long interval = 5000 * 1000;
//                Glide.with(MainActivity.this)
//                        .asBitmap()
//                        .load(uri)
//                        // Resize image view by change override size.
//                        .apply(new RequestOptions().frame(interval).override(200, 400))
//                        .into(imageCover);
//            }
        };

        // Use default layout
        MsgListAdapter.HoldersConfig holdersConfig = new MsgListAdapter.HoldersConfig();
        mAdapter = new MsgListAdapter<>("0", holdersConfig, imageLoader);

        mAdapter.setOnMsgClickListener(new MsgListAdapter.OnMsgClickListener<MyMessage>() {
            @Override
            public void onMessageClick(MyMessage myMessage) {
                // do something
                if(myMessage.getType() == IMessage.MessageType.SEND_TEXT.ordinal()){
                    //获取对应bill
                    if(myMessage.getBillId()!=null){
                        Log.i(TAG, "onMessageClick: uuid="+myMessage.getBillId());

                        Thread threadId = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BmobQuery<Bill> query = GlobalUtil.getInstance().billDatabaseHelper
                                        .readRecordByUuid(myMessage.getBillId());
                                query.findObjects(new FindListener<Bill>() {
                                    @Override
                                    public void done(List<Bill> object, BmobException e) {
                                        if(e==null){
                                            int i = 0;
                                            Log.i(TAG, "done: 查询成功：共"+object.size()+"条数据。");
                                            for (Bill bbill : object) {
                                                if(i==0){
                                                    Message msg2 = handler.obtainMessage(7);
                                                    msg2.obj = bbill;
                                                    handler.sendMessage(msg2);
                                                }

                                            }
                                        }else{
                                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                                        }
                                    }
                                });
                            }
                        });
                        threadId.start();
                    }
                }
//                if (message.getType() == IMessage.MessageType.RECEIVE_VIDEO.ordinal()
//                        || message.getType() == IMessage.MessageType.SEND_VIDEO.ordinal()) {
//                    if (!TextUtils.isEmpty(message.getMediaFilePath())) {
//                        Intent intent = new Intent(MessageListActivity.this, VideoActivity.class);
//                        intent.putExtra(VideoActivity.VIDEO_PATH, message.getMediaFilePath());
//                        startActivity(intent);
//                    }
//                } else if (message.getType() == IMessage.MessageType.RECEIVE_IMAGE.ordinal()
//                        || message.getType() == IMessage.MessageType.SEND_IMAGE.ordinal()) {
//                    Intent intent = new Intent(MessageListActivity.this, BrowserImageActivity.class);
//                    intent.putExtra("msgId", message.getMsgId());
//                    intent.putStringArrayListExtra("pathList", mPathList);
//                    intent.putStringArrayListExtra("idList", mMsgIdList);
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            getApplicationContext().getString(R.string.message_click_hint),
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });

        mAdapter.setMsgLongClickListener(new MsgListAdapter.OnMsgLongClickListener<MyMessage>() {
            @Override
            public void onMessageLongClick(View view, MyMessage message) {
//                Toast.makeText(getApplicationContext(),
//                        getApplicationContext().getString(R.string.message_long_click_hint),
//                        Toast.LENGTH_SHORT).show();
                // do something
            }
        });

        mAdapter.setOnAvatarClickListener(new MsgListAdapter.OnAvatarClickListener<MyMessage>() {
            @Override
            public void onAvatarClick(MyMessage message) {
                DefaultUser userInfo = (DefaultUser) message.getFromUser();
//                Toast.makeText(getApplicationContext(),
//                        getApplicationContext().getString(R.string.avatar_click_hint),
//                        Toast.LENGTH_SHORT).show();
                // do something
            }
        });

        mAdapter.setMsgStatusViewClickListener(new MsgListAdapter.OnMsgStatusViewClickListener<MyMessage>() {
            @Override
            public void onStatusViewClick(MyMessage message) {
                // message status view click, resend or download here
            }
        });

//        MyMessage message = new MyMessage("Hello World", IMessage.MessageType.RECEIVE_TEXT.ordinal());
//        message.setUserInfo(new DefaultUser("0", "Deadpool", "R.drawable.deadpool"));
//        mAdapter.addToStart(message, true);
        mAdapter.addToEndChronologically(mData);
        // Deprecated, should use onRefreshBegin to load next page
        mAdapter.setOnLoadMoreListener(new MsgListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalCount) {
//                Log.i("MessageListActivity", "Loading next page");
//                loadNextPage();
            }
        });

        messageList.setAdapter(mAdapter);
        mAdapter.getLayoutManager().scrollToPosition(0);

        mReceiver = new HeadsetDetectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, intentFilter);
    }

    public void run(){
        final List<MyMessage> msglist = new ArrayList<>();

        BmobQuery<Conversation> bmobQuery = new BmobQuery<Conversation>();
        Log.i(TAG, "getMessages: userId="+user.getObjectId());
        bmobQuery.addWhereEqualTo("userId", user.getObjectId());
        bmobQuery.findObjects(new FindListener<Conversation>() {
            @Override
            public void done(List<Conversation> object, BmobException e) {
                if(e==null){
                    Log.i(TAG, "查询成功：共"+object.size()+"条数据。");
                    if(object.size()==0){
                        String content = "欢迎来到xixi记账~:)\n点击下方按钮，可以添加账目。" +
                                "\n生成账目后，点击对话框可以查看账目信息。" +
                                "\n点击右上角图标可以查看统计数据。";
                        String time = DateUtil.getFormattedTime(System.currentTimeMillis());
                        MyMessage message = new MyMessage(content,IMessage.MessageType.RECEIVE_TEXT.ordinal());
//                            message.setUserInfo(GlobalUtil.getInstance().getRobot());
                        message.setTimeString(time);
                        message.setUserInfo(new DefaultUser("0", "面面", "R.drawable.robot"));
                        msglist.add(message);
                        Conversation conversation = new Conversation(2,content);
                        conversation.setUserId(user);
                        GlobalUtil.getInstance().conDatabaseHelper.addRecord(conversation);
                    }else{
                        for (Conversation con : object) {
                            int type = con.getType();
                            String content = con.getContent();
                            String uuid = con.getBillUuid();
                            String date = con.getCreatedAt();
                            String time = getTime(date);
                            Log.i(TAG, "done: time="+time);
                            MyMessage message;
                            if(type==1){
                                message = new MyMessage(content,IMessage.MessageType.SEND_TEXT.ordinal());
                                message.setBillId(uuid);
                                message.setTimeString(time);
                                message.setUserInfo(new DefaultUser(user.getObjectId(), user.getUsername(), "R.drawable.me"));
                                message.setMessageStatus(IMessage.MessageStatus.SEND_SUCCEED);
                            }else{
                                message = new MyMessage(content,IMessage.MessageType.RECEIVE_TEXT.ordinal());
//                            message.setUserInfo(GlobalUtil.getInstance().getRobot());
                                message.setTimeString(time);
                                message.setUserInfo(new DefaultUser("0", "面面", "R.drawable.robot"));
                            }
                            msglist.add(message);
                        }
                    }

                    Message msg2 = handler.obtainMessage(2);
                    msg2.obj = msglist;
                    handler.sendMessage(msg2);
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });


    }

    private String getTime(String date) {
        String dn = DateUtil.getFormattedDate();
        String ymd = date.substring(0,10);
        String hour = date.substring(11,16);
        Log.i(TAG, "getTime: dn="+dn+" ymd="+ymd+" hour="+hour);
        if (dn.compareTo(ymd)>0){
            return date.substring(0,16);
        }else{
            return date.substring(11,16);
        }
    }

    private void loadNextPage() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<MyMessage> list = new ArrayList<>();
                Resources res = getResources();
                String[] messages = res.getStringArray(R.array.conversation);
                for (int i = 0; i < messages.length; i++) {
                    MyMessage message;
                    if (i % 2 == 0) {
                        message = new MyMessage(messages[i], IMessage.MessageType.RECEIVE_TEXT.ordinal());
                        message.setUserInfo(new DefaultUser("0", "DeadPool", "R.drawable.deadpool"));
                    } else {
                        message = new MyMessage(messages[i], IMessage.MessageType.SEND_TEXT.ordinal());
                        message.setUserInfo(new DefaultUser("1", "IronMan", "R.drawable.ironman"));
                    }
                    message.setTimeString(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                    list.add(message);
                }
//                Collections.reverse(list);
                // MessageList 0.7.2 add this method, add messages chronologically.
                mAdapter.addToEndChronologically(list);
//                messageList.ref
//                chatInputView.refreshComplete();
            }
        }, 1500);
    }

    private void scrollToBottom() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                messageList.smoothScrollToPosition(0);
            }
        }, 200);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private class HeadsetDetectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.hasExtra("state")) {
                    int state = intent.getIntExtra("state", 0);
                    mAdapter.setAudioPlayByEarPhone(state);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                ChatInputView chatInputView = mChatView.getChatInputView();
//                if (chatInputView.getMenuState() == View.VISIBLE) {
//                    chatInputView.dismissMenuLayout();
//                }
                try {
                    View v = getCurrentFocus();
                    if (mImm != null && v != null) {
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        view.clearFocus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
//        mSensorManager.unregisterListener(this);
    }
}