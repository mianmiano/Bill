package com.swufe.bill;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swufe.bill.bean.MyUser;
import com.swufe.bill.widget.OwlView;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnFocusChangeListener {

    private static String TAG = "LoginActivity";

    private OwlView mOwlView;
    private EditText emailET;
    private EditText usernameET;
    private EditText passwordET;
    private EditText rpasswordET;
    private TextView signTV;
    private TextView forgetTV;
    private Button loginBtn;

    public BmobUser user;

    //是否是登陆操作
    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GlobalUtil.getInstance().setContext(getApplicationContext());
        GlobalUtil.getInstance().loginActivity = this;

        init_views();

        passwordET.setOnFocusChangeListener(this);
        rpasswordET.setOnFocusChangeListener(this);
        signTV.setOnClickListener(this);
        forgetTV.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    private void init_views() {
        mOwlView=findViewById(R.id.land_owl_view);
        emailET=findViewById(R.id.login_et_email);
        usernameET=findViewById(R.id.login_et_username);
        passwordET=findViewById(R.id.login_et_password);
        rpasswordET=findViewById(R.id.login_et_rpassword);
        signTV=findViewById(R.id.login_tv_sign);
        forgetTV=findViewById(R.id.login_tv_forget);
        loginBtn=findViewById(R.id.login_btn_login);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn_login:  //button
                if (isLogin) {
                    login();  //登陆
                } else {
                    sign();  //注册
                }
                break;
            case R.id.login_tv_sign:  //sign
                if (isLogin) {
                    //置换注册界面
                    signTV.setText("Login");
                    loginBtn.setText("Sign Up");
                    rpasswordET.setVisibility(View.VISIBLE);
                    emailET.setVisibility(View.VISIBLE);
                } else {
                    //置换登陆界面
                    signTV.setText("Sign Up");
                    loginBtn.setText("Login");
                    rpasswordET.setVisibility(View.GONE);
                    emailET.setVisibility(View.GONE);
                }
                isLogin = !isLogin;
                break;
            case R.id.login_tv_forget:  //忘记密码
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mOwlView.open();
        } else {
            mOwlView.close();
        }
    }

    public void login() {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        if (username.length() == 0 || password.length() == 0) {
            Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        user = new BmobUser();
        user.setUsername("AA");
        user.setPassword("111111");
        user.login(new SaveListener<Object>() {
            @Override
            public void done(Object o, BmobException e) {
                if(e==null){
                    Log.i(TAG, "登录成功");
                    GlobalUtil.getInstance().setUserId(user);
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                }else{
                    Log.i(TAG, "登录失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 执行注册动作
     */
    public void sign() {
        String email = emailET.getText().toString();
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String rpassword = rpasswordET.getText().toString();
//        if (email.length() == 0 || username.length() == 0 || password.length() == 0 || rpassword.length() == 0) {
//            Toast.makeText(this, "请填写必要信息", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!StringUtils.checkEmail(email)) {
//            Toast.makeText(this, "请输入正确的邮箱格式", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!password.equals(rpassword)) {
//            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
//            return;
//        }

        BmobUser bu = new BmobUser();
        bu.setUsername(username);
        bu.setPassword(password);
//        bu.setEmail(email);
        bu.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser s, BmobException e) {
                if(e==null){
                    Toast.makeText(LoginActivity.this, "注册成功"+s.toString(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.i(TAG, "done: 注册失败"+e.toString());
                }
            }
        });
    }
}
