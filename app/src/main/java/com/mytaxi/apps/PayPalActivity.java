package com.mytaxi.apps;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PayPalActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, payPalClientId;
    Button btnPay;
    boolean isSandbox = false;
    private static final int REQUEST_PAYPAL_PAYMENT = 1;
    String CONFIG_ENVIRONMENT;
    PayPalConfiguration config;
    ImageView logo;

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

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planPrice = intent.getStringExtra("planPrice");
        planCurrency = intent.getStringExtra("planCurrency");
        planGateway = intent.getStringExtra("planGateway");
        planGateWayText = intent.getStringExtra("planGatewayText");
        payPalClientId = intent.getStringExtra("payPalClientId");
        isSandbox = intent.getBooleanExtra("isSandbox", false);

        if (isSandbox) {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
        } else {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
        }

        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(payPalClientId);

        Intent intentService = new Intent(this, PayPalService.class);
        intentService.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intentService);

        logo = findViewById(R.id.paymentLogo);
        logo.setImageResource(R.drawable.ic_paypal);
        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        buyPayPal();

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyPayPal();
            }
        });
    }

    private void buyPayPal() {
        PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(planPrice), planCurrency, getString(R.string.paypal_display_website),
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intentPay = new Intent(PayPalActivity.this, PaymentActivity.class);
        intentPay.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intentPay, REQUEST_PAYPAL_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAYPAL_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        String paymentId = confirm.toJSONObject()
                                .getJSONObject("response").getString("id");
                        if (NetworkUtils.isConnected(PayPalActivity.this)) {
                            new Transaction(PayPalActivity.this)
                                    .purchasedItem(planId, paymentId, planGateway);
                        } else {
                            showError(getString(R.string.conne_msg1));
                        }

                    } catch (JSONException e) {
                        showError(getString(R.string.paypal_payment_error_1));
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showError(getString(R.string.paypal_payment_error_2));
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                showError(getString(R.string.paypal_payment_error_3));
            }
        }
    }

    private void showError(String Title) {
        new AlertDialog.Builder(PayPalActivity.this)
                .setTitle(getString(R.string.paypal_payment_error_4))
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
