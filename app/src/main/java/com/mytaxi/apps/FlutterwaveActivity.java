package com.mytaxi.apps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.mytaxi.item.ItemPaymentSetting;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.razorpay.Checkout;
import java.util.UUID;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class FlutterwaveActivity extends AppCompatActivity {

    String planId, planPrice, planCurrency, planGateway, planGateWayText, flutterwavePublicKey, flutterwaveEncKey, planName,
    email, phone, name;
    String txRef;
    Button btnPay;
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
        flutterwavePublicKey = intent.getStringExtra("flutterwavePublicKey");
        flutterwaveEncKey = intent.getStringExtra("flutterwaveEncKey");
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        email = intent.getStringExtra("email");

        logo = findViewById(R.id.paymentLogo);
        logo.setImageResource(R.drawable.flutterwave);
        btnPay = findViewById(R.id.btn_pay);
        String payString = getString(R.string.pay_via, planPrice, planCurrency, planGateWayText);
        btnPay.setText(payString);

        Checkout.preload(getApplicationContext());

        btnPay.setOnClickListener(view -> startPayment());

    }

    private void startPayment() {

        txRef = email +"-"+  UUID.randomUUID().toString();
        ItemPaymentSetting paymentSetting = new ItemPaymentSetting();

        new RaveUiManager(FlutterwaveActivity.this).setAmount(Double.parseDouble(planPrice))
                .setCurrency(planCurrency)
                .setEmail(email)
                .setfName(name)
                .setlName("")
                .setNarration("Plan")
                .setPublicKey(flutterwavePublicKey)
                .setEncryptionKey(flutterwaveEncKey)
                .setTxRef(txRef)
                .setPhoneNumber(phone, true)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(true)
                .acceptAchPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptUgMobileMoneyPayments(true)
                .acceptZmMobileMoneyPayments(true)
                .acceptRwfMobileMoneyPayments(true)
                .acceptSaBankPayments(true)
                .acceptUkPayments(true)
                .acceptBankTransferPayments(true)
                .acceptUssdPayments(true)
                .acceptBarterPayments(true)
                .allowSaveCardFeature(true)
                .onStagingEnv(paymentSetting.isFlutterwaveLive())
                .withTheme(R.style.FlutterwaveTheme)
                .isPreAuth(true)
                .shouldDisplayFee(true)
                .showStagingLabel(true)
                .initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
         *  We advise you to do a further verification of transaction's details on your server to be
         *  sure everything checks out before providing service or goods.
         */
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();

                try {
                    if (NetworkUtils.isConnected(FlutterwaveActivity.this)) {
                        new Transaction(FlutterwaveActivity.this)
                                .purchasedItem(planId, txRef, "Flutterwave");
                    } else {
                        showError(getString(R.string.conne_msg1));
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Exception in payment", e);
                }
            }
            else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showError(String Title) {
        new AlertDialog.Builder(FlutterwaveActivity.this)
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
