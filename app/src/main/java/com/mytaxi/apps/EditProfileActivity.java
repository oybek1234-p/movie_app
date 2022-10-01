package com.mytaxi.apps;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.tuyenmonkey.mkloader.MKLoader;

import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class EditProfileActivity extends AppCompatActivity implements Validator.ValidationListener {

    private MKLoader mMKLoader;
    private LinearLayout lyt_not_found;
    private NestedScrollView nestedScrollView;

    @NotEmpty
    private EditText edtName;
    @Email
    private EditText edtEmail;
    private EditText edtPassword;
    @Length(max = 14, min = 6, message = "Enter valid Phone Number")
    private EditText edtPhone;
    @NotEmpty
    private EditText edtAddress;

    private String strName, strEmail, strPassword, strMobi, strMessage, strAddress;

    private Button btnSubmit;
    private CircularImageView imageAvtar;
    private MyApplication myApplication;

    private RelativeLayout progressBar;

    private Validator validator;

    private ArrayList<Image> userImage = new ArrayList<>();
    private boolean isImage = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cander_activity_edit_profile);
        IsRTL.ifSupported(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.menu_profile));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mMKLoader = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        imageAvtar = findViewById(R.id.imageAvtar);
        btnSubmit = findViewById(R.id.button_submit);
        myApplication = MyApplication.getInstance();


        progressBar = findViewById(R.id.loadingLayout);
        nestedScrollView.setVisibility(View.GONE);

        if (NetworkUtils.isConnected(EditProfileActivity.this)) {
            getUserProfile();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

        imageAvtar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAvtarImage();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        if (NetworkUtils.isConnected(EditProfileActivity.this)) {
            strPassword = edtPassword.getText().toString();
            if (strPassword.length() >= 1 && strPassword.length() <= 5) {
                edtPassword.setError("Invalid Password");
            } else {
                putEditProfile();
            }

        } else {
            showToast(getString(R.string.conne_msg1));
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(EditProfileActivity.this);
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                showToast(message);
            }
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
                    if (jsonArray.length() > 0) {
                        JSONObject objJson;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);

                            edtName.setText(objJson.getString(Constant.USER_NAME));
                            edtEmail.setText(objJson.getString(Constant.USER_EMAIL));
                            edtPhone.setText(objJson.getString(Constant.USER_PHONE));
                            edtAddress.setText(objJson.getString(Constant.USER_ADDRESS));
                            String userImage = objJson.getString(Constant.USER_IMAGE);
                            if (!userImage.isEmpty()) {
                                Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(imageAvtar);
                            }
                        }
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
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mMKLoader.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void putEditProfile() {
        strName = edtName.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strMobi = edtPhone.getText().toString();
        strAddress = edtAddress.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getUserId());
        jsObj.addProperty("name", strName);
        jsObj.addProperty("email", strEmail);
        if (strPassword.isEmpty()) {
            jsObj.addProperty("password", "");
        } else {
            jsObj.addProperty("password", strPassword);
        }
        jsObj.addProperty("phone", strMobi);
        jsObj.addProperty("user_address", strAddress);
        params.put("data", API.toBase64(jsObj.toString()));

        if (isImage) {
            try {
                params.put("user_image", new File(userImage.get(0).getPath()));
                photo = userImage.get(0).getPath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            params.put("user_image", "");
        }

        client.post(this, Constant.EDIT_PROFILE_URL, params, new AsyncHttpResponseHandler() {

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
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
    }

    private String photo = "";

    private void setResult() {
        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(strMessage);
        } else {
            myApplication.saveLogin(myApplication.getUserId(), strName, strEmail,photo);
            showToast(strMessage);
        }
    }


    private void showToast(String msg) {
        Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dismissProgressDialog() {
        progressBar.setVisibility(View.GONE);
    }

    private void chooseAvtarImage() {
        ImagePicker.with(EditProfileActivity.this)
                .setFolderMode(true)
                .setFolderTitle("Folder")
                .setImageTitle("Tap to select")
                .setMaxSize(1)
                .setCameraOnly(false)
                .setShowCamera(false)
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            userImage = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            Uri uri = Uri.fromFile(new File(userImage.get(0).getPath()));
            Picasso.get().load(uri).into(imageAvtar);
            isImage = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
