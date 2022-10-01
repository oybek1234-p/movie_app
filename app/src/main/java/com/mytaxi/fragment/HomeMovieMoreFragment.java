package com.mytaxi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.tuyenmonkey.mkloader.MKLoader;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.adapter.HomeMovieAdapter;
import com.mytaxi.item.ItemMovie;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.NetworkUtils;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.MovieDetailsActivity;
import com.mytaxi.apps.R;
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

public class HomeMovieMoreFragment extends Fragment {

    private ArrayList<ItemMovie> mListItem;
    private RecyclerView recyclerView;
    private HomeMovieAdapter adapter;
    private MKLoader progressBar;
    private LinearLayout lyt_not_found;
    private String Id, movieUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cander_row_recyclerview, container, false);
        if (getArguments() != null) {
            Id = getArguments().getString("Id");
            if (getArguments().containsKey("movieUrl"))
                movieUrl = getArguments().getString("movieUrl");
        }
        mListItem = new ArrayList<>();
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        if (NetworkUtils.isConnected(getActivity())) {
            getMovie();
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void getMovie() {

        if(getContext() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            if (!Id.isEmpty()) {
                jsObj.addProperty("lang_id", Id);
                jsObj.addProperty("filter", Constant.FILTER_NEWEST);
            }
            params.put("data", API.toBase64(jsObj.toString()));

            client.post(getContext(), Id.isEmpty() ? Constant.API_URL + movieUrl : Constant.MOVIE_BY_LANGUAGE_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    showProgress(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    showProgress(false);

                    String result = new String(responseBody);
                    try {
                        JSONObject mainJson = new JSONObject(result);
                        JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                        JSONObject objJson;
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                objJson = jsonArray.getJSONObject(i);
                                if (objJson.has(Constant.STATUS)) {
                                    lyt_not_found.setVisibility(View.VISIBLE);
                                } else {
                                    ItemMovie objItem = new ItemMovie();
                                    objItem.setMovieId(objJson.getString(Constant.MOVIE_ID));
                                    objItem.setMovieName(objJson.getString(Constant.MOVIE_TITLE));
                                    objItem.setMovieImage(objJson.getString(Constant.MOVIE_POSTER));
                                    objItem.setMovieDuration(objJson.getString(Constant.MOVIE_DURATION));
                                    objItem.setPremium(objJson.getString(Constant.MOVIE_ACCESS).equals("Paid"));
                                    mListItem.add(objItem);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    displayData();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    showProgress(false);
                    lyt_not_found.setVisibility(View.VISIBLE);
                }

            });
        }
    }

    private void displayData() {
        if (mListItem.size() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {

            lyt_not_found.setVisibility(View.GONE);
            adapter = new HomeMovieAdapter(getActivity(), mListItem, true);
            recyclerView.setAdapter(adapter);


            adapter.setOnItemClickListener(new RvOnClickListener() {
                @Override
                public void onItemClick(int position) {
                    String movieId = mListItem.get(position).getMovieId();
                    Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                    intent.putExtra("Id", movieId);
                    startActivity(intent);
                }
            });
        }
    }


    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}

