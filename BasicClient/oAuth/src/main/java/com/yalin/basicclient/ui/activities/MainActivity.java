package com.idsmanager.basicclient.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idsmanager.basicclient.R;
import com.idsmanager.basicclient.module.User;
import com.idsmanager.basicclient.net.IDsManagerGetRequest;
import com.idsmanager.basicclient.net.NetService;
import com.idsmanager.basicclient.net.RequestQueueHelper;
import com.idsmanager.basicclient.net.response.UserResponse;
import com.idsmanager.basicclient.utils.SnackBarUtil;

public class MainActivity extends BaseLoadActivity
        implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    TextView tvLoginStat;

    private void initData() {
        if (User.isLogin(getApplicationContext())) {
//            TextView localTextView1 = this.tvLoginStat;
//            String str1 = getString(R.string.welcome1);
//            Object[] arrayOfObject1 = new Object[1];
//            arrayOfObject1[0] = User.getUserAccount(getApplicationContext());
//            localTextView1.setText(String.format(str1, arrayOfObject1));
//            TextView localTextView2 = this.tvLoginStat;
//            StringBuilder localStringBuilder = new StringBuilder().append("\n");
//            String str2 = getString(R.string.welcome2);
//            Object[] arrayOfObject2 = new Object[1];
//            arrayOfObject2[0] = Integer.valueOf(User.getLoginTimes(getApplicationContext()));
//            localTextView2.append(String.format(str2, arrayOfObject2));
            int loginTimes = User.getLoginTimes(this);
            String username = User.getUserAccount(this);
            tvLoginStat.setText("欢迎您："+username+". 您已经登录"+loginTimes+"次");
            return;
        }
        this.tvLoginStat.setText(getString(R.string.no_login));
    }

    private void initView() {
        this.tvLoginStat = ((TextView) findViewById(R.id.tv_user_info));
    }

    private void loginFailed(String msg) {
        dismissLoading();
        SnackBarUtil.showSnack(this, msg);
    }

    private void loginSuccess(User paramUser) {
        if (paramUser == null) {
            SnackBarUtil.showSnack(this, getString(R.string.login_success));
            return;
        }
        User.storeUserInfo(getApplicationContext(), paramUser);
        dismissLoading();
        SnackBarUtil.showSnack(this, R.string.login_success);
    }

    public static void open(Activity paramActivity) {
        paramActivity.startActivity(new Intent(paramActivity, MainActivity.class));
    }

    public static void open(Context context, String token) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("token", token);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void rpLogin(String token) {
        RequestQueue localRequestQueue = RequestQueueHelper.getInstance(this);
        IDsManagerGetRequest IDsManagerGetRequest = new IDsManagerGetRequest(NetService.RP_TOKEN_LOGIN_URL, UserResponse.class, NetService.getHostAuthHeader(token),
                new Response.Listener() {
                    public void onResponse(Object paramObject) {
                        User user = ((UserResponse) paramObject).detail;
                      loginSuccess(user);
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError paramVolleyError) {
                       loginFailed("Sorry,Login Failed");
                    }
                });
        IDsManagerGetRequest.setTag(TAG);
        localRequestQueue.add(IDsManagerGetRequest);
    }

    protected int getDrawerCheckId() {
        return R.id.navigation_home_page;
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        initView();
    }

    protected void onLoginStateChanged() {
        super.onLoginStateChanged();
        int loginTimes = User.getLoginTimes(this);
        String username = User.getUserAccount(this);
        if (!TextUtils.isEmpty(username)) {
            initData();
            tvLoginStat.setText("欢迎您："+username+". 您已经登录"+loginTimes+"次");
//            tvLoginStat.setText(String.format(getString(R.string.welcome1), new Object[]{str1}));
//            TextView tvLoginStat = this.tvLoginStat;
//            StringBuilder localStringBuilder = new StringBuilder().append("\n");
//            String str2 = getString(R.string.welcome2);
//            tvLoginStat.append(String.format(str2, loginTimes));
            return;
        }
        this.tvLoginStat.setText(getString(R.string.no_login));
    }

    protected void onNewIntent(Intent paramIntent) {
        super.onNewIntent(paramIntent);
        String str = paramIntent.getStringExtra("token");
        if (!TextUtils.isEmpty(str)) {
            User.deleteUserInfo(getApplicationContext());
            rpLogin(str);
        }
    }

    protected void onPostCreate(Bundle paramBundle) {
        super.onPostCreate(paramBundle);
        initData();
        if (paramBundle == null) {
            String token = getIntent().getStringExtra("token");
            if (!TextUtils.isEmpty(token)) {
                User.deleteUserInfo(getApplicationContext());
                rpLogin(token);
            }
        }
    }
}