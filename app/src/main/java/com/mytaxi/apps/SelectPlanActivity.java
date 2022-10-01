package com.mytaxi.apps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.tuyenmonkey.mkloader.MKLoader;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.mytaxi.item.ItemPaymentSetting;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SelectPlanActivity extends AppCompatActivity {

    String planId, planName, planPrice, planDuration;
    TextView textPlanName, textPlanPrice, textPlanDuration, textChoosePlanName, textEmail, textChangePlan, textLogout, textPlanCurrency;
    LinearLayout lyPaymentOptions;
    CardView btnPaypal, btnStripe, btnRazorpay, btnPaystack, btnSelcom, btnFlutterwave;
    MyApplication myApplication;
    MKLoader mMKLoader;
    LinearLayout lyt_not_found, textNoPaymentGateway;
    RelativeLayout lytDetails;
    ItemPaymentSetting paymentSetting;
    ImageView imageClose;
    String name, phone, email, address, order_id;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cander_activity_select_plan);
        IsRTL.ifSupported(this);

        myApplication = MyApplication.getInstance();
        paymentSetting = new ItemPaymentSetting();

        final Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planDuration = intent.getStringExtra("planDuration");

        btnPaypal = findViewById(R.id.btnPaypal);
        btnStripe = findViewById(R.id.btnStripe);
        btnRazorpay = findViewById(R.id.btnRazorpay);
        btnPaystack = findViewById(R.id.btnPaystack);
        btnSelcom = findViewById(R.id.btnSelcom);
        btnFlutterwave = findViewById(R.id.btnFlutterwave);

        mMKLoader = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        lytDetails = findViewById(R.id.lytDetails);
        textPlanName = findViewById(R.id.textPackName);
        textPlanPrice = findViewById(R.id.textPrice);
        textPlanCurrency = findViewById(R.id.textCurrency);
        textPlanDuration = findViewById(R.id.textDay);
        textChoosePlanName = findViewById(R.id.choosePlanName);
        textEmail = findViewById(R.id.planEmail);
        textLogout = findViewById(R.id.textLogout);
        textChangePlan = findViewById(R.id.changePlan);
        lyPaymentOptions = findViewById(R.id.lyPaymentOptions);
        textNoPaymentGateway = findViewById(R.id.textNoPaymentGateway);
        imageClose = findViewById(R.id.imageClose);

        textPlanName.setText(planName);
        textPlanPrice.setText(planPrice);
        textPlanDuration.setText(getString(R.string.plan_day_for, planDuration));
        textChoosePlanName.setText(planName);
        textEmail.setText(myApplication.getUserEmail());

        //Fetch user info for payment
        getUserProfile();

        textChangePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnPaypal.setOnClickListener(view -> {

            Intent intentPayPal = new Intent(SelectPlanActivity.this, PayPalActivity.class);
            intentPayPal.putExtra("planId", planId);
            intentPayPal.putExtra("planPrice", planPrice);
            intentPayPal.putExtra("planCurrency", paymentSetting.getCurrencyCode());
            intentPayPal.putExtra("planGateway", "Paypal");
            intentPayPal.putExtra("planGatewayText", getString(R.string.paypal));
            intentPayPal.putExtra("isSandbox", paymentSetting.isPayPalSandbox());
            intentPayPal.putExtra("payPalClientId", paymentSetting.getPayPalClientId());
            startActivity(intentPayPal);
        });

        btnStripe.setOnClickListener(view -> {

            Intent intentStripe = new Intent(SelectPlanActivity.this, StripeActivity.class);
            intentStripe.putExtra("planId", planId);
            intentStripe.putExtra("planPrice", planPrice);
            intentStripe.putExtra("planCurrency", paymentSetting.getCurrencyCode());
            intentStripe.putExtra("planGateway", "Stripe");
            intentStripe.putExtra("planGatewayText", getString(R.string.stripe));
            intentStripe.putExtra("stripePublisherKey", paymentSetting.getStripePublisherKey());
            startActivity(intentStripe);
        });

        btnRazorpay.setOnClickListener(view -> {

            Intent intentRazor = new Intent(SelectPlanActivity.this, RazorPayActivity.class);
            intentRazor.putExtra("planId", planId);
            intentRazor.putExtra("planName", planName);
            intentRazor.putExtra("planPrice", planPrice);
            intentRazor.putExtra("planCurrency", paymentSetting.getCurrencyCode());
            intentRazor.putExtra("planGateway", "Razorpay");
            intentRazor.putExtra("planGatewayText", getString(R.string.razor_pay));
            intentRazor.putExtra("razorPayKey", paymentSetting.getRazorPayKey());
            startActivity(intentRazor);
        });

        btnPaystack.setOnClickListener(view -> {

            Intent intentPayStack = new Intent(SelectPlanActivity.this, PayStackActivity.class);
            intentPayStack.putExtra("planId", planId);
            intentPayStack.putExtra("planPrice", planPrice);
            intentPayStack.putExtra("planCurrency", paymentSetting.getCurrencyCode());
            intentPayStack.putExtra("planGateway", "Paystack");
            intentPayStack.putExtra("planGatewayText", getString(R.string.pay_stack));
            intentPayStack.putExtra("payStackPublicKey", paymentSetting.getPayStackPublicKey());
            startActivity(intentPayStack);
        });

        btnFlutterwave.setOnClickListener(view -> {

            Intent intentPayStack = new Intent(SelectPlanActivity.this, FlutterwaveActivity.class);
            intentPayStack.putExtra("planId", planId);
            intentPayStack.putExtra("planPrice", planPrice);
            intentPayStack.putExtra("planCurrency", paymentSetting.getCurrencyCode());
            intentPayStack.putExtra("planGateway", "Flutterwave");
            intentPayStack.putExtra("planGatewayText", getString(R.string.flutterwave));
            intentPayStack.putExtra("flutterwavePublicKey", paymentSetting.getFlutterwavePublicKey());
            intentPayStack.putExtra("flutterwaveEncKey", paymentSetting.getFlutterwaveEncKey());
            intentPayStack.putExtra("name", name);
            intentPayStack.putExtra("phone", phone);
            intentPayStack.putExtra("email", email);
            startActivity(intentPayStack);
        });

        if (NetworkUtils.isConnected(SelectPlanActivity.this)) {
            getPaymentSetting();
        } else {
            Toast.makeText(SelectPlanActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getPaymentSetting() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.PAYMENT_SETTING_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mMKLoader.setVisibility(View.VISIBLE);
                lytDetails.setVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoader.setVisibility(View.GONE);
                lytDetails.setVisibility(View.VISIBLE);


                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        paymentSetting.setCurrencyCode(objJson.getString(Constant.CURRENCY_CODE));
                        paymentSetting.setPayPal(objJson.getBoolean(Constant.PAY_PAL_ON));
                        paymentSetting.setPayPalSandbox(objJson.getString(Constant.PAY_PAL_SANDBOX).equals("sandbox"));
                        paymentSetting.setPayPalClientId(objJson.getString(Constant.PAY_PAL_CLIENT));
                        paymentSetting.setStripe(objJson.getBoolean(Constant.STRIPE_ON));
                        paymentSetting.setStripePublisherKey(objJson.getString(Constant.STRIPE_PUBLISHER));
                        paymentSetting.setRazorPay(objJson.getBoolean(Constant.RAZOR_PAY_ON));
                        paymentSetting.setRazorPayKey(objJson.getString(Constant.RAZOR_PAY_KEY));
                        paymentSetting.setPayStackPublicKey(objJson.getString(Constant.PAY_STACK_KEY));
                        paymentSetting.setPayStack(objJson.getBoolean(Constant.PAY_STACK_ON));
                        paymentSetting.setSelcom(objJson.getBoolean(Constant.SELCOM_ON));
                        paymentSetting.setSelcomApiKey(objJson.getString(Constant.SELCOM_API_KEY));
                        paymentSetting.setSelcomSecretKey(objJson.getString(Constant.SELCOM_SECRET_KEY));
                        paymentSetting.setFlutterwave(objJson.getBoolean(Constant.FLUTTERWAVE_ON));
                        paymentSetting.setFlutterwavePublicKey(objJson.getString(Constant.FLUTTERWAVE_PUBLIC_KEY));
                        paymentSetting.setFlutterwaveEncKey(objJson.getString(Constant.FLUTTERWAVE_ENC_KEY));
                        paymentSetting.setFlutterwaveLive(objJson.getBoolean(Constant.FLUTTERWAVE_MODE));
                        displayData();
                    } else {
                        mMKLoader.setVisibility(View.GONE);
                        lytDetails.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                mMKLoader.setVisibility(View.GONE);
                lytDetails.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        textPlanCurrency.setText(paymentSetting.getCurrencyCode());
        if (!paymentSetting.isPayPal()) {
            btnPaypal.setVisibility(View.GONE);
        }
        if (!paymentSetting.isStripe()) {
            btnStripe.setVisibility(View.GONE);
        }
        if (!paymentSetting.isRazorPay()) {
            btnRazorpay.setVisibility(View.GONE);
        }
        if (!paymentSetting.isPayStack()) {
            btnPaystack.setVisibility(View.GONE);
        }
        if (!paymentSetting.isSelcom() || !paymentSetting.getCurrencyCode().equals("TZS")) {
            btnSelcom.setVisibility(View.GONE);
        }
        if (!paymentSetting.isFlutterwave()) {
            btnFlutterwave.setVisibility(View.GONE);
        }
        if (!paymentSetting.isPayPal() &&
            !paymentSetting.isStripe() &&
            !paymentSetting.isRazorPay() &&
            !paymentSetting.isPayStack() &&
            (!paymentSetting.isSelcom() || !paymentSetting.getCurrencyCode().equals("TZS")) &&
            !paymentSetting.isFlutterwave()){

                textNoPaymentGateway.setVisibility(View.VISIBLE);
                lyPaymentOptions.setVisibility(View.GONE);
        }
    }

    private void getUserProfile() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getUserId());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.PROFILE_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                mMKLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoader.setVisibility(View.GONE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    if (jsonArray.length() > 0) {
                        JSONObject objJson;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            name = objJson.getString(Constant.USER_NAME);
                            email = objJson.getString(Constant.USER_EMAIL);
                            phone = objJson.getString(Constant.USER_PHONE);
                            address = objJson.getString(Constant.USER_ADDRESS);

                            if(TextUtils.isEmpty(phone))
                                phone = "09000000000";

                            if(TextUtils.isEmpty(address))
                                address = "123 Address";

                        }

                        btnSelcom.setOnClickListener(view -> {

                            Intent intentPayStack = new Intent(SelectPlanActivity.this, SelcomActivity.class);
                            intentPayStack.putExtra("planId", planId);
                            intentPayStack.putExtra("planPrice", planPrice);
                            intentPayStack.putExtra("planCurrency", paymentSetting.getCurrencyCode());
                            intentPayStack.putExtra("planGateway", "Selcom");
                            intentPayStack.putExtra("planGatewayText", getString(R.string.selcom));
                            intentPayStack.putExtra("selcomApiKey", paymentSetting.getSelcomApiKey());
                            intentPayStack.putExtra("selcomSecretKey", paymentSetting.getSelcomSecretKey());
                            intentPayStack.putExtra("name", name);
                            intentPayStack.putExtra("phone", phone);
                            intentPayStack.putExtra("email", email);
                            intentPayStack.putExtra("address", address);
                            startActivity(intentPayStack);
                        });

                    } else {
                        mMKLoader.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mMKLoader.setVisibility(View.GONE);
            }
        });
    }
}
