package com.example.demo.hl.asyntask;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.demo.hl.GallerySwipeActivity;
import com.example.demo.hl.R;
import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.core.DataBaseHandler;
import com.example.demo.hl.core.FakkuConnection;
import com.example.demo.hl.core.FakkuDroidApplication;
import com.example.demo.hl.util.Helper;

public class ReadAsyncTask extends AsyncTask<DoujinBean, Integer, DoujinBean> {

    ProgressDialog dialog;

    Activity activity;
    boolean alreadyDownloaded;

    public ReadAsyncTask(Activity activity, boolean alreadyDownloaded) {
        this.activity = activity;
        this.alreadyDownloaded = alreadyDownloaded;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(activity.getString(R.string.quickRead));
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    @Override
    protected DoujinBean doInBackground(DoujinBean... beans) {
        DoujinBean bean = beans[0];
        try {
            if (!alreadyDownloaded) {
                if (bean.getQtyPages() <= 0) {
                    FakkuConnection.parseHTMLDoujin(bean);
                }
                if (bean.getImageServer() == null) {
                    String urlServer = FakkuConnection.imageServerUrl(bean.getUrl());
                    bean.setImageServer(urlServer);
                }
            } else {
                bean = new DataBaseHandler(activity).getDoujinBean(bean.getId());
            }
        } catch (IOException e) {
            return null;
        }
        return bean;
    }

    @Override
    protected void onPostExecute(DoujinBean result) {
        dialog.dismiss();
        if (result == null) {
            Toast.makeText(activity, "Error opening reader", Toast.LENGTH_SHORT).show();
            return;
        }
        ((FakkuDroidApplication) activity.getApplication()).setCurrent(result);
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(activity);
        if (preferenceManager.getBoolean("perfect_viewer_checkbox", false) && alreadyDownloaded) {
            List<String> lstFiles = result.getImagesFiles();
            File dir = Helper.getDir(result.getId(), activity);
            File myFile = new File(dir, lstFiles.get(0));
            Helper.openPerfectViewer(myFile.getAbsolutePath(), activity);
        } else {
            Intent it = new Intent(activity, GallerySwipeActivity.class);
            activity.startActivity(it);
        }
    }
}