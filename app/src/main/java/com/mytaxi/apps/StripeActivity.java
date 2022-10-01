package com.mytaxi.apps;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardMultilineWidget;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class StripeActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, stripePublisherKey;
    Button btnPay;
    MyApplication myApplication;
    CardMultilineWidget cardMultilineWidget;
    private PaymentResultCallback paymentResultCallback;
    private Stripe stripe;
    RelativeLayout progressBar;
    private String paymentIntentClientSecret = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment_cander);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.payment));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        progressBar = findViewById(R.id.loadingLayout);
        cardMultilineWidget = findViewById(R.id.cardInputWidget_checkout);
        myApplication = MyApplication.getInstance();
        paymentResultCallback = new PaymentResultCallback(StripeActivity.this);

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        stripePublisherKey = intent.getStringExtra("stripePublisherKey");

        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isConnected(StripeActivity.this)) {
                    getToken();
                } else {
                    Toast.makeText(StripeActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void startCheckout() {
        showProgressDialog();
        PaymentMethodCreateParams params = cardMultilineWidget.getPaymentMethodCreateParams();
        if (params != null) {
            ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
            stripe = new Stripe(StripeActivity.this, stripePublisherKey);
            stripe.confirmPayment(this, confirmParams);
        } else {
            dismissProgressDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, paymentResultCallback);
    }


    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {

        StripeActivity activity;

        @NonNull
        private final WeakReference<StripeActivity> activityRef;

        PaymentResultCallback(@NonNull StripeActivity activity) {
            activityRef = new WeakReference<>(activity);
            this.activity = activityRef.get();
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(paymentIntent));
                    String paymentId = jsonObject.getString("id");
                    if (NetworkUtils.isConnected(activity)) {
                        new Transaction(activity)
                                .purchasedItem(activity.planId, paymentId, activity.planGateway);
                    } else {
                        activity.showError(activity.getString(R.string.conne_msg1));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                activity.showError(paymentIntent.getLastPaymentError().getMessage());
            }

            activity.dismissProgressDialog();
        }

        @Override
        public void onError(@NonNull Exception e) {
            final StripeActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.showError(e.getMessage());
            activity.dismissProgressDialog();
        }
    }

    public void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgressDialog() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String Title) {
        new AlertDialog.Builder(StripeActivity.this)
                .setTitle(getString(R.string.stripe_payment_error_1))
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

    private void getToken() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("amount", planPrice);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.STRIPE_TOKEN_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);
                        paymentIntentClientSecret = objJson.getString("stripe_payment_token");
                        startCheckout();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(getString(R.string.stripe_token_error));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                dismissProgressDialog();
                showError(getString(R.string.stripe_token_error));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
