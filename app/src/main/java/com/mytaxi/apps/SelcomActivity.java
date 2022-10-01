package com.mytaxi.apps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.razorpay.Checkout;
import com.tuyenmonkey.mkloader.MKLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SelcomActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, selcomApiKey, selcomSecretKey, planName,
            email, phone, name;
    String address, order_id;
    Button btnPay;
    MKLoader mMKLoader;
    ImageView logo;
    MyApplication myApplication;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_cander);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.payment));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        myApplication = MyApplication.getInstance();

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        selcomApiKey = intent.getStringExtra("selcomApiKey");
        selcomSecretKey = intent.getStringExtra("selcomSecretKey");
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");
        address = intent.getStringExtra("address");

        logo = findViewById(R.id.paymentLogo);
        logo.setImageResource(R.drawable.selcom);
        mMKLoader = findViewById(R.id.progressBar1);
        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        Checkout.preload(getApplicationContext());

        btnPay.setOnClickListener(view -> startPayment());

    }

    private void startPayment() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("name", name);
        jsObj.addProperty("email", email);
        jsObj.addProperty("address", address);
        jsObj.addProperty("phone", phone);
        jsObj.addProperty("amount", planPrice);
        jsObj.addProperty("currency", planCurrency);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.GET_SELCOM_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mMKLoader.setVisibility(View.VISIBLE);
                logo.setVisibility(View.GONE);
                btnPay.setVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoader.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                btnPay.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        String success = objJson.getString(Constant.SUCCESS);

                        if(success.equals("1")) {
                            String selcom_url = objJson.getString(Constant.SELCOM_URL);
                            order_id = objJson.getString(Constant.ORDER_ID);
                            Intent intent = new Intent(SelcomActivity.this, SelcomPayment.class);
                            intent.putExtra("selcom_url", selcom_url);
                            startActivity(intent);
                            Log.v("SELCOMURL", order_id);
                        } else {
                            Toast.makeText(SelcomActivity.this, "Error: " + objJson.getString("error"), Toast.LENGTH_SHORT).show();
                            Log.e("SELCOMURL", objJson.getString("error"));
                        }
                    } else {
                        mMKLoader.setVisibility(View.GONE);
                        logo.setVisibility(View.VISIBLE);
                        btnPay.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                mMKLoader.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                btnPay.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!TextUtils.isEmpty(order_id)) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty(Constant.ORDER_ID, order_id);
            params.put("data", API.toBase64(jsObj.toString()));
            client.post(this, Constant.SELCOM_PAYMENT_STATUS, params, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    mMKLoader.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.GONE);
                    btnPay.setVisibility(View.GONE);

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    mMKLoader.setVisibility(View.GONE);
                    logo.setVisibility(View.VISIBLE);
                    btnPay.setVisibility(View.VISIBLE);


                    String result = new String(responseBody);
                    try {
                        JSONObject mainJson = new JSONObject(result);
                        JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                        JSONObject objJson;
                        if (jsonArray.length() > 0) {
                            objJson = jsonArray.getJSONObject(0);
                            String success = objJson.getString(Constant.SUCCESS);

                            if (success.equals("1")) {
                                String payment_status = objJson.getString(Constant.PAYMENT_STATUS);

                                switch (payment_status) {
                                    case "COMPLETED":
                                        try {
                                            if (NetworkUtils.isConnected(SelcomActivity.this)) {
                                                new Transaction(SelcomActivity.this)
                                                        .purchasedItem(planId, order_id, "Selcom");
                                            } else {
                                                showError(getString(R.string.conne_msg1));
                                            }
                                        } catch (Exception e) {
                                            Log.e("TAG", "Exception in payment", e);
                                        }
                                        break;
                                    case "PENDING":
                                        Toast.makeText(SelcomActivity.this, "Selcom Payment Pending", Toast.LENGTH_SHORT).show();
                                        break;
                                    case "CANCELLED":
                                    case "USERCANCELLED":
                                        Toast.makeText(SelcomActivity.this, "Selcom Payment Canceled", Toast.LENGTH_SHORT).show();
                                        break;
                                    case "REJECTED":
                                        Toast.makeText(SelcomActivity.this, "Selcom Payment Rejected", Toast.LENGTH_SHORT).show();
                                        break;
                                }

                                Log.v("SELCOM", payment_status);
                            } else {
                                Toast.makeText(SelcomActivity.this, "Error: " + objJson.getString("error"), Toast.LENGTH_SHORT).show();
                                Log.e("SELCOM", objJson.getString("error"));
                            }
                        } else {
                            mMKLoader.setVisibility(View.GONE);
                            logo.setVisibility(View.VISIBLE);
                            btnPay.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                        error) {
                    mMKLoader.setVisibility(View.GONE);
                    logo.setVisibility(View.VISIBLE);
                    btnPay.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void showError(String Title) {
        new AlertDialog.Builder(SelcomActivity.this)
                .setTitle("Selcom Payment Error")
                .setMessage(Title)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
