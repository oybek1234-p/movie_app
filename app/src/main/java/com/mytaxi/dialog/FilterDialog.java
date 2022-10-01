package com.mytaxi.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytaxi.util.Constant;
import com.mytaxi.apps.R;

public class FilterDialog extends BaseDialog {

    private Activity activity;
    private FilterDialogListener filterDialogListener;
    private int selectedFilter = 1;
    private RelativeLayout lytNew, lytOld, lytAtoZ, lytRandom;
    private TextView txtNew, txtOld, txtAtoZ, txtRandom;
    private ImageView imgNewTick, imgOldTick, imgAtoZTick, imgRandomTick;
    private LinearLayout lytRoot;

    public FilterDialog(Activity activity, int selectedFilter) {
        super(activity, R.style.Theme_AppCompat_Translucent);
        this.activity = activity;
        this.selectedFilter = selectedFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dailog_filter);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        lytRoot = findViewById(R.id.lytDialogRoot);
        lytNew = findViewById(R.id.lytNew);
        lytOld = findViewById(R.id.lytOld);
        lytAtoZ = findViewById(R.id.lytAtoZ);
        lytRandom = findViewById(R.id.lytRandom);

        txtNew = findViewById(R.id.txtNew);
        txtOld = findViewById(R.id.txtOld);
        txtAtoZ = findViewById(R.id.txtAtoZ);
        txtRandom = findViewById(R.id.txtRandom);

        imgNewTick = findViewById(R.id.imgNewTick);
        imgOldTick = findViewById(R.id.imgOldTick);
        imgAtoZTick = findViewById(R.id.imgAtoZTick);
        imgRandomTick = findViewById(R.id.imgRandomTick);

        lytNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialogListener.confirm(Constant.FILTER_NEWEST, 1);
                dismiss();
            }
        });

        lytOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialogListener.confirm(Constant.FILTER_OLDEST, 2);
                dismiss();
            }
        });

        lytAtoZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialogListener.confirm(Constant.FILTER_ALPHA, 3);
                dismiss();
            }
        });

        lytRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialogListener.confirm(Constant.FILTER_RANDOM, 4);
                dismiss();
            }
        });

        lytRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        selectedFilter();
    }

    public interface FilterDialogListener {
        void confirm(String filterTag, int filterPosition);
    }

    public void setFilterDialogListener(FilterDialogListener filterDialogListener) {
        this.filterDialogListener = filterDialogListener;
    }

    private void selectedFilter() {
        switch (selectedFilter) {
            case 1:
                lytNew.setBackgroundColor(activity.getResources().getColor(R.color.highlight));
                txtNew.setTextColor(activity.getResources().getColor(R.color.real_white));
                imgNewTick.setVisibility(View.VISIBLE);
                break;
            case 2:
                lytOld.setBackgroundColor(activity.getResources().getColor(R.color.highlight));
                txtOld.setTextColor(activity.getResources().getColor(R.color.real_white));
                imgOldTick.setVisibility(View.VISIBLE);
                break;
            case 3:
                lytAtoZ.setBackgroundColor(activity.getResources().getColor(R.color.highlight));
                txtAtoZ.setTextColor(activity.getResources().getColor(R.color.real_white));
                imgAtoZTick.setVisibility(View.VISIBLE);
                break;
            case 4:
                lytRandom.setBackgroundColor(activity.getResources().getColor(R.color.highlight));
                txtRandom.setTextColor(activity.getResources().getColor(R.color.real_white));
                imgRandomTick.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
