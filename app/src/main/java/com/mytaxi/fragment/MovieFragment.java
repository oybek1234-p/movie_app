package com.mytaxi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mytaxi.adapter.MovieAdapter;
import com.mytaxi.item.ItemMovie;
import com.mytaxi.util.API;
import com.mytaxi.util.Constant;
import com.mytaxi.util.EndlessRecyclerViewScrollListener;
import com.mytaxi.util.NetworkUtils;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.MovieDetailsActivity;
import com.mytaxi.apps.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuyenmonkey.mkloader.MKLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cz.msebera.android.httpclient.Header;

public class MovieFragment extends Fragment {

    private ArrayList<ItemMovie> mListItem;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private MKLoader progressBar;
    private LinearLayout lyt_not_found;
    private String Id;
    private boolean isFirst = true, isOver = false;
    private boolean isLanguage = true;
    private int pageIndex = 1;
    private String mFilter = Constant.FILTER_NEWEST;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cander_row_recyclerview, container, false);
        if (getArguments() != null) {
            Id = getArguments().getString("Id");
            isLanguage = getArguments().getBoolean("isLanguage", true);
        }

        mListItem = new ArrayList<>();
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 0) {
                    return 3;
                }
                return 1;
            }
        });

        if (NetworkUtils.isConnected(getActivity())) {
            getMovie(mFilter);
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }


        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        pageIndex++;
                        getMovie(mFilter);
                    }, 1000);
                } else {
                    adapter.hideHeader();
                }
            }
        };

        recyclerView.addOnScrollListener(endlessRecyclerViewScrollListener);

        return rootView;
    }

    private void getMovie(String mFilter) {

        if(getContext() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty(isLanguage ? "lang_id" : "genre_id", Id);
            jsObj.addProperty("filter", mFilter);
            params.put("data", API.toBase64(jsObj.toString()));
            params.put("page", pageIndex);
            client.post(getContext(), isLanguage ? Constant.MOVIE_BY_LANGUAGE_URL : Constant.MOVIE_BY_GENRE_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    if (isFirst)
                        showProgress(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (isFirst)
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
                        } else {
                            isOver = true;
                            if (adapter != null) {
                                adapter.hideHeader();
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
            if (isFirst) {
                isFirst = false;
                adapter = new MovieAdapter(getActivity(), mListItem);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

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

    public void selectFilter(String filterTag) {
        endlessRecyclerViewScrollListener.resetState();
        mListItem.clear();
        isFirst = true;
        isOver = false;
        mFilter = filterTag;
        pageIndex = 1;

        if (NetworkUtils.isConnected(getActivity())) {
            getMovie(mFilter);
        } else {
            Toast.makeText(getActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }
    }
}

