package org.gradle.samples;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

// r17dame.connecttool
import com.r17dame.connecttool.ConnectTool;
import com.r17dame.connecttool.ConnectToolBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    String TAG = "connectToolPageBack test";
    private ConnectToolBroadcastReceiver connectToolReceiver;
    IntentFilter itFilter;
    ConnectTool _connectTool;
    Button getConnectAuthorizeButton;
    Button getMeButton;

    // page access
    Button Register_pageButton;
    Button Login_pageButton;

    Button rechargeButton;
    Button GetPurchaseOrderListButton;
    Button GetPurchaseOrderButton;
    Button OpenConsumeSPButton;
    Button QueryConsumeSPButton;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Init tool
            _connectTool = new ConnectTool(
                    this,
                    "",
                    "",
                    "",
                    "",
                    "");

            /*
             * Page access
             * */
            String TAG = "connectTool test";
            //頁面註冊
            Register_pageButton = findViewById(com.r17dame.connecttool.R.id.Register_pageButton);
            Register_pageButton.setOnClickListener(view -> _connectTool.OpenRegisterURL());

            //用戶登入 (更新 Acctoken 與 MeInfo)
            getConnectAuthorizeButton = findViewById(com.r17dame.connecttool.R.id.getConnectAuthorizeButton);
            getConnectAuthorizeButton.setOnClickListener(view -> {
                String state = "App-side-State";
                _connectTool.OpenAuthorizeURL(state);
            });

            /*
             * 切換帳號 (功能與 OpenLoginURL 相同)
             * 亦可用: Login_pageButton.setOnClickListener(view -> _connectTool.OpenLoginURL());
             * @see <a href="https://github.com/jianweiCiou/com.17dame.connecttool_android/blob/main/README.md#openregisterurl-openloginurl">Description</a>
             */
            Login_pageButton = findViewById(com.r17dame.connecttool.R.id.Login_pageButton);
            Login_pageButton.setOnClickListener(view -> _connectTool.SwitchAccountURL());
            //或是  Login_pageButton.setOnClickListener(view -> _connectTool.OpenLoginURL());

            //Get MeInfo
            getMeButton = findViewById(com.r17dame.connecttool.R.id.getMeButton);
            getMeButton.setOnClickListener(view -> {
                try {
                    UUID GetMe_RequestNumber = UUID.randomUUID(); // App-side-RequestNumber(UUID), default random
                    _connectTool.GetMe_Coroutine(GetMe_RequestNumber, value -> {
                        /*
                         * App-side add functions.
                         */
                        Log.v(TAG, "GetMe_RequestNumber : " + value.requestNumber);
                        Log.v(TAG, "MeInfo email : " + value.data.email);
                        Log.v(TAG, "MeInfo spCoin : " + value.data.spCoin);
                        Log.v(TAG, "MeInfo userId : " + value.data.userId);
                        Toast.makeText(getApplicationContext(), value.data.email, Toast.LENGTH_SHORT).show();
                    });
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            });

            // 購買 SPCoin
            rechargeButton = findViewById(com.r17dame.connecttool.R.id.rechargeButton);
            rechargeButton.setOnClickListener(view -> {
                String notifyUrl = "";// NotifyUrl is a URL customized by the game developer
                String state = "Custom state";// Custom state ,
                // Step1. Set notifyUrl and state,
                _connectTool.set_purchase_notifyData(notifyUrl, state);

                // Step2. Set currencyCode
                String currencyCode = "2";

                // Step3. Open Recharge Page
                _connectTool.OpenRechargeURL(currencyCode, notifyUrl, state);
            });

            // Open Consume URL
            OpenConsumeSPButton = findViewById(com.r17dame.connecttool.R.id.OpenConsumeSPButton);
            OpenConsumeSPButton.setOnClickListener(view -> {
                String notifyUrl = "http://test";// NotifyUrl is a URL customized by the game developer
                String state = UUID.randomUUID().toString(); // Custom state , default random
                // Step1. Set notifyUrl and state,
                _connectTool.set_purchase_notifyData(notifyUrl, state);

                int consume_spCoin = 65;
                String orderNo = UUID.randomUUID().toString(); // orderNo is customized by the game developer
                String requestNumber = UUID.randomUUID().toString(); // requestNumber is customized by the game developer, default random
                String GameName = "GameName";
                String productName = "productName";
                _connectTool.OpenConsumeSPURL(consume_spCoin, orderNo, GameName, productName, notifyUrl, state, requestNumber);
            });


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        //Assign ConnectToolReceiver
        connectToolReceiver = new ConnectToolBroadcastReceiver();
        itFilter = new IntentFilter();
        itFilter.addAction("com.r17dame.CONNECT_ACTION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // 修正 Android 14+ 的廣播註冊
            this.registerReceiver(connectToolReceiver, itFilter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(connectToolReceiver, itFilter);
        }

        connectToolReceiver.registerCallback(new ConnectToolBroadcastReceiver.ConnectToolReceiverCallback() {
            @Override
            public void connectToolPageBack(Intent intent, String accountBackType) {
                String backType = intent.getStringExtra("accountBackType");
                Log.v(TAG, "connectToolPageBack : " + backType);
                // Open by Account Page (Register, Login) :
                if (backType.equals("Register")) {
                    /*
                     * App-side add functions.
                     */
                    String state = "App-side-State";
                    _connectTool.AccountPageEvent(state, backType);
                }
                // Login
                if (backType.equals("Login")) {
                    /*
                     * App-side add functions.
                     */
                    String state = "App-side-State";
                    _connectTool.AccountPageEvent(state, backType);
                }

                // Complete purchase of SP Coin
                if (backType.equals("CompletePurchase")) {
                    _connectTool.appLinkDataCallBack_CompletePurchase(intent, value -> {
                        Log.v(TAG, "appLinkData CompletePurchase callback : " + value);
                        Toast.makeText(getApplicationContext(), "Purchase tradeNo : " + value.data.tradeNo + "/ spCoin : " + value.data.spCoin, Toast.LENGTH_SHORT).show();
                        /*
                         * App-side add functions.
                         */
                        return value;
                    });
                }

                // Complete consumption of SP Coin
                if (backType.equals("CompleteConsumeSP")) {
                    UUID queryConsumeSP_requestNumber = UUID.randomUUID(); // App-side-RequestNumber(UUID), default random
                    // consume_transactionId
                    _connectTool.appLinkDataCallBack_CompleteConsumeSP(intent, queryConsumeSP_requestNumber, value -> {
                        /*
                         * App-side add functions.
                         */
                        Log.v(TAG, "appLinkData CompleteConsumeSP callback : " + value.data.orderStatus);
                        Toast.makeText(getApplicationContext(), "consumption orderNo : " + value.data.orderNo + "/ spCoin : " + value.data.spCoin + "/ rebate : " + value.data.rebate, Toast.LENGTH_SHORT).show();
                    });
                }

                // get Access token
                if (backType.equals("Authorize")) {
                    UUID GetMe_RequestNumber = UUID.randomUUID(); // App-side-RequestNumber(UUID), default random
                    String state = "App-side-State";
                    _connectTool.appLinkDataCallBack_OpenAuthorize(intent, state, GetMe_RequestNumber, value -> {
                        /*
                         * App-side add functions.
                         */
                        Toast.makeText(getApplicationContext(), value.meInfo.data.email, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // 修正 Android 14+ 的廣播註冊
            this.registerReceiver(connectToolReceiver, itFilter, RECEIVER_EXPORTED);
        } else {
            unregisterReceiver(connectToolReceiver);
        }
    }
}