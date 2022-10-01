package com.mytaxi.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.tuyenmonkey.mkloader.MKLoader;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.adapter.EpisodeAdapter;
import com.mytaxi.adapter.RelatedShowsAdapter;
import com.mytaxi.adapter.SeasonAdapter;
import com.mytaxi.cast.Casty;
import com.mytaxi.cast.MediaData;
import com.mytaxi.fragment.ChromecastScreenFragment;
import com.mytaxi.fragment.EmbeddedImageFragment;
import com.mytaxi.fragment.ExoPlayerFragment;
import com.mytaxi.fragment.PremiumContentFragment;
import com.mytaxi.item.ItemEpisode;
import com.mytaxi.item.ItemPlayer;
import com.mytaxi.item.ItemSeason;
import com.mytaxi.item.ItemShow;
import com.mytaxi.item.ItemSubTitle;
import com.mytaxi.util.API;
import com.mytaxi.util.BannerAds;
import com.mytaxi.util.Constant;
import com.mytaxi.util.Events;
import com.mytaxi.util.GlobalBus;
import com.mytaxi.util.IsRTL;
import com.mytaxi.util.NetworkUtils;
import com.mytaxi.util.RvOnClickListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ShowDetailsActivity extends AppCompatActivity {

    MKLoader mMKLoader, mMKLoaderEpisode;
    LinearLayout lyt_not_found;
    RelativeLayout lytParent;
    WebView webView;
    TextView textTitle, textDate, textLanguage, textGenre, textRelViewAll, textDuration, textNoEpisode, textDurationLbl, textRate;
    LinearLayout lytRate;
    RecyclerView rvRelated, rvEpisode;
    ItemShow itemShow;
    ArrayList<ItemShow> mListItemRelated;
    ArrayList<ItemSeason> mListSeason;
    ArrayList<ItemEpisode> mListItemEpisode;
    RelatedShowsAdapter relatedShowsAdapter;
    String Id;
    StringBuilder strGenre = new StringBuilder();
    LinearLayout lytRelated, lytSeason;
    MyApplication myApplication;
    NestedScrollView nestedScrollView;
    Toolbar toolbar;
    AppCompatSpinner spSeason;
    SeasonAdapter seasonAdapter;
    EpisodeAdapter episodeAdapter;
    private FragmentManager fragmentManager;
    private int playerHeight;
    FrameLayout frameLayout;
    boolean isFullScreen = false;
    boolean isFromNotification = false;
    LinearLayout mAdViewLayout;
    boolean isPurchased = false;
    private int selectedEpisode = 0;
    private Casty casty;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);
        IsRTL.ifSupported(this);
        GlobalBus.getBus().register(this);
        mAdViewLayout = findViewById(R.id.adView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        myApplication = MyApplication.getInstance();
        fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        Id = intent.getStringExtra("Id");
        if (intent.hasExtra("isNotification")) {
            isFromNotification = true;
        }
        casty = Casty.create(this)
                .withMiniController();

        frameLayout = findViewById(R.id.playerSection);
        int columnWidth = NetworkUtils.getScreenWidth(this);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
        playerHeight = frameLayout.getLayoutParams().height;

        BannerAds.showBannerAds(this, mAdViewLayout);

        mListItemRelated = new ArrayList<>();
        mListSeason = new ArrayList<>();
        mListItemEpisode = new ArrayList<>();
        itemShow = new ItemShow();
        lytRelated = findViewById(R.id.lytRelated);
        mMKLoader = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        lytParent = findViewById(R.id.lytParent);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        webView = findViewById(R.id.webView);
        textRelViewAll = findViewById(R.id.textRelViewAll);
        textTitle = findViewById(R.id.textTitle);
        textDate = findViewById(R.id.textDate);
        textLanguage = findViewById(R.id.txtLanguage);
        textGenre = findViewById(R.id.txtGenre);
        textDuration = findViewById(R.id.txtDuration);
        textDurationLbl = findViewById(R.id.txtDurationLbl);
        rvRelated = findViewById(R.id.rv_related);
        rvEpisode = findViewById(R.id.rv_episode);
        spSeason = findViewById(R.id.spSeason);
        lytSeason = findViewById(R.id.lytSeason);
        mMKLoaderEpisode = findViewById(R.id.progressBar);
        textNoEpisode = findViewById(R.id.textNoEpisode);
        textRate = findViewById(R.id.txtIMDbRating);
        lytRate = findViewById(R.id.lytIMDB);

        rvRelated.setHasFixedSize(true);
        rvRelated.setLayoutManager(new LinearLayoutManager(ShowDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rvRelated.setFocusable(false);
        rvRelated.setNestedScrollingEnabled(false);

        rvEpisode.setHasFixedSize(true);
        rvEpisode.setLayoutManager(new LinearLayoutManager(ShowDetailsActivity.this, LinearLayoutManager.VERTICAL, false));
        rvEpisode.setFocusable(false);
        rvEpisode.setNestedScrollingEnabled(false);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
            getDetails();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

        LinearLayout lySynopsis = findViewById(R.id.lySynopsis);
        lySynopsis.setOnClickListener(v ->{

            ImageView ivArrow = findViewById(R.id.ivArrow);

            if(webView.getVisibility() == View.GONE) {
                webView.setVisibility(View.VISIBLE);
                ivArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
            }
            else {
                webView.setVisibility(View.GONE);
                ivArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            }
        });
    }

    private void getDetails() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("show_id", Id);
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.SHOW_DETAILS_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mMKLoader.setVisibility(View.VISIBLE);
                lytParent.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoader.setVisibility(View.GONE);
                lytParent.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject objJson = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    if (objJson.length() > 0) {
                        if (objJson.has(Constant.STATUS)) {
                            lyt_not_found.setVisibility(View.VISIBLE);
                        } else {
                            itemShow.setShowId(objJson.getString(Constant.SHOW_ID));
                            itemShow.setShowName(objJson.getString(Constant.SHOW_NAME));
                            itemShow.setShowDescription(objJson.getString(Constant.SHOW_DESC));
                            itemShow.setShowImage(objJson.getString(Constant.SHOW_POSTER));
                            itemShow.setShowLanguage(objJson.getString(Constant.SHOW_LANGUAGE));
                            itemShow.setShowRating(objJson.getString(Constant.IMDB_RATING));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.RELATED_SHOW_ARRAY_NAME);
                            if (jsonArrayChild.length() != 0) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemShow item = new ItemShow();
                                    item.setShowId(objChild.getString(Constant.SHOW_ID));
                                    item.setShowName(objChild.getString(Constant.SHOW_TITLE));
                                    item.setShowImage(objChild.getString(Constant.SHOW_POSTER));
                                    mListItemRelated.add(item);
                                }
                            }


                            JSONArray jsonArrayGenre = objJson.getJSONArray(Constant.GENRE_LIST);
                            if (jsonArrayGenre.length() != 0) {
                                String prefix = "";
                                for (int k = 0; k < jsonArrayGenre.length(); k++) {
                                    JSONObject objChild = jsonArrayGenre.getJSONObject(k);
                                    strGenre.append(prefix);
                                    prefix = " | ";
                                    strGenre.append(objChild.getString(Constant.GENRE_NAME));
                                }
                            } else {
                                textGenre.setVisibility(View.GONE);
                            }

                            JSONArray jsonArraySeason = objJson.getJSONArray(Constant.SEASON_ARRAY_NAME);
                            if (jsonArraySeason.length() != 0) {
                                for (int j = 0; j < jsonArraySeason.length(); j++) {
                                    JSONObject objSeason = jsonArraySeason.getJSONObject(j);
                                    ItemSeason item = new ItemSeason();
                                    item.setSeasonId(objSeason.getString(Constant.SEASON_ID));
                                    item.setSeasonName(objSeason.getString(Constant.SEASON_NAME));
                                    mListSeason.add(item);
                                }
                            }

                        }
                        displayData();

                    } else {
                        mMKLoader.setVisibility(View.GONE);
                        lytParent.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mMKLoader.setVisibility(View.GONE);
                lytParent.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        setTitle(itemShow.getShowName());
        textTitle.setText(itemShow.getShowName());
        textLanguage.setText(itemShow.getShowLanguage());
        textGenre.setText(strGenre.toString());

        if (itemShow.getShowRating().isEmpty() || itemShow.getShowRating().equals("0")) {
            lytRate.setVisibility(View.GONE);
        } else {
            textRate.setText(itemShow.getShowRating());
        }

        String mimeType = "text/html";
        String encoding = "utf-8";
        String htmlText = itemShow.getShowDescription();

        boolean isRTL = Boolean.parseBoolean(getResources().getString(R.string.isRTL));
        String direction = isRTL ? "rtl" : "ltr";

        String text = "<html dir=" + direction + "><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/custom.otf\")}body{font-family: MyFont;color: #9c9c9c;font-size:14px;margin-left:0px;line-height:1.3}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

        if (!mListItemRelated.isEmpty()) {
            rvRelated.setLayoutManager(new GridLayoutManager(this, 3));
            relatedShowsAdapter = new RelatedShowsAdapter(ShowDetailsActivity.this, mListItemRelated, false);
            rvRelated.setAdapter(relatedShowsAdapter);

            relatedShowsAdapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String showId = mListItemRelated.get(position).getShowId();
                    Intent intent = new Intent(ShowDetailsActivity.this, ShowDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("Id", showId);
                    startActivity(intent);
                }
            });

        } else {
            lytRelated.setVisibility(View.GONE);
        }

        if (!mListSeason.isEmpty()) {
            seasonAdapter = new SeasonAdapter(ShowDetailsActivity.this,
                    android.R.layout.simple_list_item_1,
                    mListSeason);
            seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSeason.setAdapter(seasonAdapter);

            spSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.e("seasonId", mListSeason.get(i).getSeasonName());
                    mListItemEpisode.clear();
                    selectedEpisode = 0;
                    changeSeason(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            lytSeason.setVisibility(View.GONE);
            setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
            textDate.setVisibility(View.GONE);
            textDuration.setVisibility(View.GONE);
            textDurationLbl.setVisibility(View.GONE);
        }

        casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
            @Override
            public void onConnected() {
                initCastPlayer(selectedEpisode);
            }

            @Override
            public void onDisconnected() {
                if (!mListItemEpisode.isEmpty()) {
                    playEpisode(selectedEpisode);
                } else {
                    setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
                }

            }
        });

    }

    public void showToast(String msg) {
        Toast.makeText(ShowDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void changeSeason(int seasonId) {
        ItemSeason itemSeason = mListSeason.get(seasonId);
        if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
            getEpisode(itemSeason.getSeasonId());
        } else {
            showToast(getString(R.string.conne_msg1));
        }
    }

    private void getEpisode(String seasonId) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("season_id", seasonId);
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.EPISODE_LIST_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mMKLoaderEpisode.setVisibility(View.VISIBLE);
                rvEpisode.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mMKLoaderEpisode.setVisibility(View.GONE);
                rvEpisode.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    isPurchased = mainJson.getBoolean(Constant.USER_PLAN_STATUS);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    if (jsonArray.length() > 0) {
                        textNoEpisode.setVisibility(View.GONE);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objJson = jsonArray.getJSONObject(i);
                            ItemEpisode itemEpisode = new ItemEpisode();
                            itemEpisode.setEpisodeId(objJson.getString(Constant.EPISODE_ID));
                            itemEpisode.setEpisodeName(objJson.getString(Constant.EPISODE_TITLE));
                            itemEpisode.setEpisodeImage(objJson.getString(Constant.EPISODE_IMAGE));
                            itemEpisode.setEpisodeUrl(objJson.getString(Constant.EPISODE_URL));
                            itemEpisode.setEpisodeType(objJson.getString(Constant.EPISODE_TYPE));
                            itemEpisode.setEpisodeDate(objJson.getString(Constant.EPISODE_DATE));
                            itemEpisode.setEpisodeDuration(objJson.getString(Constant.EPISODE_DURATION));
                            itemEpisode.setPremium(objJson.getString(Constant.EPISODE_ACCESS).equals("Paid"));
                            itemEpisode.setDownload(objJson.getBoolean(Constant.DOWNLOAD_ENABLE));
                            itemEpisode.setDownloadUrl(objJson.getString(Constant.DOWNLOAD_URL));

                            itemEpisode.setQuality(objJson.getBoolean(Constant.IS_QUALITY));
                            itemEpisode.setSubTitle(objJson.getBoolean(Constant.IS_SUBTITLE));
                            itemEpisode.setQuality480(objJson.getString(Constant.QUALITY_480));
                            itemEpisode.setQuality720(objJson.getString(Constant.QUALITY_720));
                            itemEpisode.setQuality1080(objJson.getString(Constant.QUALITY_1080));

                            itemEpisode.setSubTitleLanguage1(objJson.getString(Constant.SUBTITLE_LANGUAGE_1));
                            itemEpisode.setSubTitleUrl1(objJson.getString(Constant.SUBTITLE_URL_1));
                            itemEpisode.setSubTitleLanguage2(objJson.getString(Constant.SUBTITLE_LANGUAGE_2));
                            itemEpisode.setSubTitleUrl2(objJson.getString(Constant.SUBTITLE_URL_2));
                            itemEpisode.setSubTitleLanguage3(objJson.getString(Constant.SUBTITLE_LANGUAGE_3));
                            itemEpisode.setSubTitleUrl3(objJson.getString(Constant.SUBTITLE_URL_3));

                            mListItemEpisode.add(itemEpisode);
                        }
                        displayEpisode();

                    } else {
                        mMKLoaderEpisode.setVisibility(View.GONE);
                        rvEpisode.setVisibility(View.GONE);
                        textNoEpisode.setVisibility(View.VISIBLE);
                        setImageIfSeasonAndEpisodeNone(itemShow.getShowImage());
                        textDate.setVisibility(View.GONE);
                        textDuration.setVisibility(View.GONE);
                        textDurationLbl.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mMKLoaderEpisode.setVisibility(View.GONE);
                rvEpisode.setVisibility(View.GONE);
                textNoEpisode.setVisibility(View.VISIBLE);
                textDate.setVisibility(View.GONE);
                textDuration.setVisibility(View.GONE);
                textDurationLbl.setVisibility(View.GONE);
            }
        });
    }

    private void displayEpisode() {
        episodeAdapter = new EpisodeAdapter(ShowDetailsActivity.this, mListItemEpisode, isPurchased);
        rvEpisode.setAdapter(episodeAdapter);

        if (!mListItemEpisode.isEmpty()) {
            playEpisode(0);
            episodeAdapter.select(0);
        }

        episodeAdapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                episodeAdapter.select(position);
                playEpisode(position);
            }
        });
    }

    private void playEpisode(int playPosition) {
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        textDate.setText(itemEpisode.getEpisodeDate());
        textDuration.setText(itemEpisode.getEpisodeDuration());
        selectedEpisode = playPosition;
        if (itemEpisode.isPremium()) {
            if (isPurchased) {
                setPlayer(playPosition);
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "Shows");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            setPlayer(playPosition);
        }

        if (NetworkUtils.isConnected(ShowDetailsActivity.this)) {
            episodeRecentlyWatched(itemEpisode.getEpisodeId());
        }
    }

    private void initCastPlayer(int playPosition) {
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        if (itemEpisode.isPremium()) {
            if (isPurchased) {
                castScreen();
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(Id, "Shows");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            castScreen();
        }
    }

    private void setPlayer(int playPosition) {
        ItemEpisode itemEpisode = mListItemEpisode.get(playPosition);
        if (itemEpisode.getEpisodeUrl().isEmpty()) {
            showToast(getString(R.string.stream_not_found));
            EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeImage(), false);
            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
        } else {
            switch (itemEpisode.getEpisodeType()) { //URL Embed
                case "Local":
                case "URL":
                case "HLS":
                case "DASH":
                    if (casty.isConnected()) {
                        castScreen();
                    } else {
                        ExoPlayerFragment exoPlayerFragment = ExoPlayerFragment.newInstance(getPlayerData());
                        fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();
                    }
                    break;
                case "Embed":
                    EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeImage(), true);
                    fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
                    break;

            }
        }
    }

    private void episodeRecentlyWatched(String episodeId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("episode_id", episodeId);
        jsObj.addProperty("user_id", myApplication.getIsLogin() ? myApplication.getUserId() : "");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(this, Constant.EPISODE_RECENTLY_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFullScreen(Events.FullScreen fullScreen) {
        isFullScreen = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            gotoFullScreen();
        } else {
            gotoPortraitScreen();
        }
    }

    private void gotoPortraitScreen() {
        nestedScrollView.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        mAdViewLayout.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerHeight));
    }

    private void gotoFullScreen() {
        nestedScrollView.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        mAdViewLayout.setVisibility(View.GONE);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            Events.FullScreen fullScreen = new Events.FullScreen();
            fullScreen.setFullScreen(false);
            GlobalBus.getBus().post(fullScreen);
        } else {
            if (isFromNotification) {
                Intent intent = new Intent(ShowDetailsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setImageIfSeasonAndEpisodeNone(String imageCover) {
        EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance("", imageCover, false);
        fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        casty.addMediaRouteMenuItem(menu);
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    private void playViaCast() {
        if (!mListItemEpisode.isEmpty()) {
            ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
            if (itemEpisode.getEpisodeType().equals("Local") || itemEpisode.getEpisodeType().equals("URL") || itemEpisode.getEpisodeType().equals("HLS")) {
                casty.getPlayer().loadMediaAndPlay(createSampleMediaData(itemEpisode.getEpisodeUrl(), itemEpisode.getEpisodeName(), itemEpisode.getEpisodeImage()));
            } else {
                showToast(getResources().getString(R.string.cast_youtube));
            }
        } else {
            showToast(getString(R.string.stream_not_found));
        }
    }

    private MediaData createSampleMediaData(String videoUrl, String videoTitle, String videoImage) {
        return new MediaData.Builder(videoUrl)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                .setContentType(getType(videoUrl))
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(videoTitle)
                .setSubtitle(getString(R.string.app_name))
                .addPhotoUrl(videoImage)
                .build();
    }

    private String getType(String videoUrl) {
        if (videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }

    private void castScreen() {
        ChromecastScreenFragment chromecastScreenFragment = new ChromecastScreenFragment();
        fragmentManager.beginTransaction().replace(R.id.playerSection, chromecastScreenFragment).commitAllowingStateLoss();
        chromecastScreenFragment.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                playViaCast();
            }
        });
    }

    private ItemPlayer getPlayerData() {
        ItemPlayer itemPlayer = new ItemPlayer();
        ItemEpisode itemEpisode = mListItemEpisode.get(selectedEpisode);
        itemPlayer.setDefaultUrl(itemEpisode.getEpisodeUrl());
        if (itemEpisode.getEpisodeType().equals("Local") || itemEpisode.getEpisodeType().equals("URL")) {
            itemPlayer.setQuality(itemEpisode.isQuality());
            itemPlayer.setSubTitle(itemEpisode.isSubTitle());
            itemPlayer.setQuality480(itemEpisode.getQuality480());
            itemPlayer.setQuality720(itemEpisode.getQuality720());
            itemPlayer.setQuality1080(itemEpisode.getQuality1080());

            ArrayList<ItemSubTitle> itemSubTitles = new ArrayList<>();
            ItemSubTitle subTitleOff = new ItemSubTitle("0", getString(R.string.off_sub_title), "");
            itemSubTitles.add(subTitleOff);
            if (!itemEpisode.getSubTitleLanguage1().isEmpty()) {
                ItemSubTitle subTitle1 = new ItemSubTitle("1", itemEpisode.getSubTitleLanguage1(), itemEpisode.getSubTitleUrl1());
                itemSubTitles.add(subTitle1);
            }
            if (!itemEpisode.getSubTitleLanguage2().isEmpty()) {
                ItemSubTitle subTitle2 = new ItemSubTitle("2", itemEpisode.getSubTitleLanguage2(), itemEpisode.getSubTitleUrl2());
                itemSubTitles.add(subTitle2);
            }
            if (!itemEpisode.getSubTitleLanguage3().isEmpty()) {
                ItemSubTitle subTitle3 = new ItemSubTitle("3", itemEpisode.getSubTitleLanguage3(), itemEpisode.getSubTitleUrl3());
                itemSubTitles.add(subTitle3);
            }
            itemPlayer.setSubTitles(itemSubTitles);

            if (itemEpisode.getQuality480().isEmpty() && itemEpisode.getQuality720().isEmpty() && itemEpisode.getQuality1080().isEmpty()) {
                itemPlayer.setQuality(false);
            }

            if (itemEpisode.getSubTitleLanguage1().isEmpty() && itemEpisode.getSubTitleLanguage2().isEmpty() && itemEpisode.getSubTitleLanguage3().isEmpty()) {
                itemPlayer.setSubTitle(false);
            }
        } else {
            itemPlayer.setQuality(false);
            itemPlayer.setSubTitle(false);
        }
        return itemPlayer;
    }
}
