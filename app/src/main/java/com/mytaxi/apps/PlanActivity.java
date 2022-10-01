package com.mytaxi.apps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.tuyenmonkey.mkloader.MKLoader;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.adapter.PlanAdapter;
import com.mytaxi.item.ItemPlan;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.mytaxi.util.RvOnClickListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PlanActivity extends AppCompatActivity {

    MKLoader mMKLoader;
    LinearLayout lyt_not_found;
    RecyclerView rvPlan;
    NestedScrollView nestedScrollView;
    ArrayList<ItemPlan> mListItem;
    PlanAdapter adapter;
    int selectedPlan = 0;
    ImageView imageClose;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_plan_cander);
        IsRTL.ifSupported(this);

        mListItem = new ArrayList<>();
        mMKLoader = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        imageClose = findViewById(R.id.imageClose);

        rvPlan = findViewById(R.id.rv_plan);
        rvPlan.setHasFixedSize(true);
        rvPlan.setLayoutManager(new LinearLayoutManager(PlanActivity.this, LinearLayoutManager.VERTICAL, false));
        rvPlan.setFocusable(false);
        rvPlan.setNestedScrollingEnabled(false);

        if (NetworkUtils.isConnected(PlanActivity.this)) {
            getPlan();
        } else {
            Toast.makeText(PlanActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getPlan() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.PLAN_LIST_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mMKLoader.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoader.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);

                            ItemPlan objItem = new ItemPlan();
                            objItem.setPlanId(objJson.getString(Constant.PLAN_ID));
                            objItem.setPlanName(objJson.getString(Constant.PLAN_NAME));
                            objItem.setPlanPrice(objJson.getString(Constant.PLAN_PRICE));
                            objItem.setPlanDuration(objJson.getString(Constant.PLAN_DURATION));
                            objItem.setPlanCurrencyCode(objJson.getString(Constant.CURRENCY_CODE));
                            mListItem.add(objItem);

                        }
                        displayData();
                    } else {
                        mMKLoader.setVisibility(View.GONE);
                        nestedScrollView.setVisibility(View.GONE);
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
                nestedScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        adapter = new PlanAdapter(PlanActivity.this, mListItem);
        rvPlan.setAdapter(adapter);

        adapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                selectedPlan = position;

                ItemPlan itemPlan = mListItem.get(selectedPlan);
                String isFreePlan = itemPlan.getPlanPrice();
                if (isFreePlan.equals("0.00")) {
                    if (NetworkUtils.isConnected(PlanActivity.this)) {
                        new Transaction(PlanActivity.this).purchasedItem(itemPlan.getPlanId(), "-", "N/A");
                    } else {
                        Toast.makeText(PlanActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(PlanActivity.this, SelectPlanActivity.class);
                    intent.putExtra("planId", itemPlan.getPlanId());
                    intent.putExtra("planName", itemPlan.getPlanName());
                    intent.putExtra("planPrice", itemPlan.getPlanPrice());
                    intent.putExtra("planDuration", itemPlan.getPlanDuration());
                    startActivity(intent);
                }
            }
        });
    }
}
