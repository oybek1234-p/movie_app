package com.mytaxi.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mytaxi.apps.MainActivity;
import com.mytaxi.apps.MyApplication;
import com.mytaxi.apps.R;
import com.mytaxi.apps.SignInActivity;
import com.onesignal.OneSignal;


public class SettingFragment extends Fragment {

    private MyApplication myApplication;
    private Context context;
    public Runnable runnable;

    public void setContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    public String getStringX(int id) {
        return context.getString(id);
    }

    public void onClick() {
        if (runnable!=null) {
            runnable.run();
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        myApplication = MyApplication.getInstance();

        LinearLayout lytRate = rootView.findViewById(R.id.lytRateApp);
        LinearLayout lytMore = rootView.findViewById(R.id.lytMoreApp);
        LinearLayout lytShare = rootView.findViewById(R.id.lytShareApp);
        LinearLayout lytPrivacy = rootView.findViewById(R.id.lytPrivacy);
        LinearLayout lytAbout = rootView.findViewById(R.id.lytAbout);
        LinearLayout lytLogout = rootView.findViewById(R.id.lytLogout);
        SwitchCompat notificationSwitch = rootView.findViewById(R.id.switch_notification);

        notificationSwitch.setChecked(myApplication.getNotification());

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            myApplication.saveIsNotification(isChecked);
            OneSignal.setSubscription(isChecked);
        });

        lytRate.setOnClickListener(v -> rateApp());

        lytMore.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getStringX(R.string.play_more_apps)))));

        lytShare.setOnClickListener(v -> shareApp());

        lytAbout.setOnClickListener(v -> {
            String about = getStringX(R.string.about);
            AboutFragment aboutFragment = new AboutFragment();
            changeFragment(aboutFragment, about);
        });

        lytPrivacy.setOnClickListener(v -> {
            String privacy = getStringX(R.string.privacy_policy);
            PrivacyFragment privacyFragment = new PrivacyFragment();
            changeFragment(privacyFragment, privacy);
        });

        if(myApplication.getIsLogin()) {
            onClick();
            lytLogout.setOnClickListener(v -> {
                AlertDialog alertDialog = new AlertDialog.Builder(requireActivity(), R.style.AlertDialogStyle).create();
                alertDialog.setTitle(getStringX(R.string.menu_logout));
                alertDialog.setMessage(getStringX(R.string.logout_msg));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        myApplication.saveIsLogin(false);
                        Intent intent = new Intent(getContext(), SignInActivity.class);
                        intent.putExtra("isLogout", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        if(getActivity() != null)
                            getActivity().finish();
                    }
                });
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.highlight));
            });
        } else {
            lytLogout.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void changeFragment(Fragment fragment, String Name) {
        onClick();
        FragmentManager fm = getFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.Container, fragment, Name);
        ft.addToBackStack(Name);
        ft.commit();
        if(getActivity() != null)
            ((MainActivity) getActivity()).setToolbarTitle(Name);
    }

    private void shareApp() {
        onClick();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getStringX(R.string.share_msg) + requireActivity().getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void rateApp() {
        onClick();
        final String appName = requireActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appName)));
        }
    }
}
