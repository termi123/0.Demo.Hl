package com.example.demo.hl.fragment;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.demo.hl.MainActivity;
import com.example.demo.hl.R;
import com.example.demo.hl.asyntask.ReadAsyncTask;
import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.bean.URLBean;
import com.example.demo.hl.bean.UserBean;
import com.example.demo.hl.core.DataBaseHandler;
import com.example.demo.hl.core.FakkuConnection;
import com.example.demo.hl.core.FakkuDroidApplication;
import com.example.demo.hl.exception.ExceptionNotLoggedIn;
import com.example.demo.hl.util.Constants;
import com.example.demo.hl.util.Helper;
import com.example.demo.hl.util.ImageQuality;

public class DoujinFragment extends SherlockFragment {

    private MainActivity mMainActivity;
    FakkuDroidApplication app;
    private View mFormView;
    private View mStatusView;
    private View view;
    private DoujinBean currentBean;
    boolean alreadyDownloaded = false;
    private ProgressBar progressBar;

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (FakkuDroidApplication) getActivity().getApplication();
        if (currentBean == null) {
            currentBean = new DoujinBean();
            currentBean.setUrl(getActivity().getIntent().getStringExtra(MainActivity.INTENT_VAR_URL));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

	// ////////////////////////////////////Load Page/////
    @Override
    public void onStart() {
        super.onStart();
//        alreadyDownloaded = DataBaseHandler.verifyAlreadyDownloaded(currentBean, getActivity());
        if (!currentBean.isCompleted()) {
        	Log.i("currentBean", "not completed");
            Helper.executeAsyncTask(new CompleteDoujin(), currentBean);
            Log.i("currentBean", currentBean.toString());
        } else {
        	Log.i("currentBean", "completed");
            setComponents();
            showProgress(false);
        }
//        if (DownloadManagerService.DoujinMap.exists(currentBean)) {
//            Helper.executeAsyncTask(new UpdateStatus());
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater
                .inflate(R.layout.fragment_doujin, container, false);

        mFormView = view.findViewById(R.id.view_form);
        mStatusView = view.findViewById(R.id.view_status);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

	// ////////////////////////////////////Button Event
    public void viewInBrowser(View view) {
        Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
        viewBrowser.setData(Uri.parse(currentBean.getUrl()));
        this.startActivity(viewBrowser);
    }

    public void refresh() {
        currentBean = app.getCurrent();
        Helper.executeAsyncTask(new CompleteDoujin(), currentBean);
    }

    class CompleteDoujin extends AsyncTask<DoujinBean, Float, DoujinBean> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected DoujinBean doInBackground(DoujinBean... beans) {
            if (app.getSettingBean().isChecked())
                try {
                    FakkuConnection.connect(app.getSettingBean());
                    Log.i("connect", "ok");
                } catch (ClientProtocolException e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }
            DoujinBean bean = beans[0];

            try {
                FakkuConnection.parseHTMLDoujin(bean);
                Log.i("parseHTMLDoujin", "ok");
            } catch (Exception e) {
                bean = null;
                Helper.logError(this, e.getMessage(), e);
            }
//            Log.i("Bean size", bean.toString());
            
            try {
                if (bean != null) {
                	Log.i("Bean size", "!= null");
                    File dir = Helper.getCacheDir(getActivity());
                    
                    Log.i("file", bean.getFileImageTitle());
                    Log.i("dir", dir.toString());
                    File myFile = new File(dir, bean.getFileImageTitle());
                    
                    Helper.saveInStorage(myFile, bean.getUrlImageTitle());
                    Log.i("getUrlImageTitle", bean.getUrlImageTitle());
                }
            } catch (Exception e) {
                Helper.logError(this, e.getMessage(), e);
            }
            
            return bean;
        }

        protected void onPostExecute(DoujinBean bean) {
        	
            try {
                if (bean != null) {
//                		&& bean.getTitle() != null) {
                    setComponents();
                    showProgress(false);

                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.no_data),
                            Toast.LENGTH_SHORT).show();
                    mMainActivity.onBackPressed();
                }
            } catch (Exception e) {
                Helper.logError(this, e.getMessage(), e);
            }
        }
    }

    public void addOrRemoveFavorite(View view) {
        if (!app.getSettingBean().isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.login_please)
                    .setPositiveButton(R.string.login,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
//                                    Intent itMain = new Intent(mMainActivity, MainActivity.class);
//                                    itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
//                                    getActivity().startActivityForResult(itMain, 1);
                                }
                            }
                    )
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    return;
                                }
                            }
                    ).create().show();
        }
        app.setSettingBean(null);
        if (app.getSettingBean().isChecked())
            if (!currentBean.isAddedInFavorite()) {
                Helper.executeAsyncTask(new FavoriteDoujin(), true);
            } else {
                Helper.executeAsyncTask(new FavoriteDoujin(), false);
            }
    }
    
    class FavoriteDoujin extends AsyncTask<Boolean, Float, Boolean> {

        protected void onPreExecute() {
            showProgress(true);
        }

        protected Boolean doInBackground(Boolean... bool) {
            boolean isConnected = false;
            if (app.getSettingBean().isChecked())
                try {
                    FakkuConnection.connect(app.getSettingBean());
                    isConnected = app.getSettingBean().isChecked();
                } catch (ClientProtocolException e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }

            if (!isConnected) {
                UserBean s = app.getSettingBean();
                s.setChecked(false);
                new DataBaseHandler(getActivity()).updateSetting(s);
                app.setSettingBean(null);
            } else {
                Boolean b = bool[0];
                try {
                    if (b)
                        FakkuConnection.transaction(currentBean
                                .urlFavorite(Constants.SITEADDFAVORITE));
                    else
                        FakkuConnection.transaction(currentBean
                                .urlFavorite(Constants.SITEREMOVEFAVORITE));
                } catch (ExceptionNotLoggedIn e) {
                    Helper.logError(this, e.getMessage(), e);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }
                currentBean.setAddedInFavorite(b);
            }
            return isConnected;
        }

        protected void onPostExecute(Boolean bytes) {
            showProgress(false);
            if (bytes) {
                setComponents();
                String text = null;

                if (currentBean.isAddedInFavorite())
                    text = getResources().getString(R.string.added_favorite);
                else
                    text = getResources().getString(R.string.removed_favorite);
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT)
                        .show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setMessage(R.string.login_please)
                        .setPositiveButton(R.string.login,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
//                                        Intent itMain = new Intent(mMainActivity, MainActivity.class);
//                                        itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.LOGIN);
//                                        getActivity().startActivityForResult(itMain, 1);
                                    }
                                }
                        )
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        return;
                                    }
                                }
                        ).create().show();
            }
        }
    }
    
    public void read(View view) {
        boolean alreadyDownloaded = DataBaseHandler.verifyAlreadyDownloaded(currentBean, getActivity());
        Helper.executeAsyncTask(new ReadAsyncTask(getActivity(), alreadyDownloaded), currentBean);
    }

    public void download() {
        if (!alreadyDownloaded) {
//            DownloadManagerService.downloadDoujin(currentBean, getActivity());
            Helper.executeAsyncTask(new UpdateStatus());
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.ask_delete)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    String folder = currentBean.getId();
                                    File dir = Helper.getDir(folder, getActivity());
//                                    try {
//                                        FileUtils.deleteDirectory(dir);
//                                    } catch (IOException e) {
//                                        Helper.logError(this, e.getMessage(), e);
//                                    }
                                    DataBaseHandler db = new DataBaseHandler(
                                            getActivity());
                                    db.deleteDoujin(currentBean.getId());

                                    ImageButton btnDownload = (ImageButton) DoujinFragment.this.view.findViewById(R.id.btnDownload);
                                    btnDownload
                                            .setImageResource(R.drawable.av_download);
                                    btnDownload
                                            .setContentDescription(getResources()
                                                    .getString(
                                                            R.string.download));
                                    Toast.makeText(
                                            getActivity(),
                                            getResources().getString(
                                                    R.string.deleted),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    alreadyDownloaded = false;
                                }
                            }
                    ).setNegativeButton(android.R.string.no, null)
                    .create().show();
        }
    }

    class UpdateStatus extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... arg0) {
            try {
                Thread.sleep(1000);
//                while (DownloadManagerService.DoujinMap.exists(currentBean)) {
//                    if (isRemoving()) {
//                        break;
//                    }
//                    if (DownloadManagerService.currentBean != null
//                            && DownloadManagerService.currentBean.getId()
//                            .hashCode() == currentBean.getId()
//                            .hashCode())
//                        publishProgress(DownloadManagerService.percent);
//                    Thread.sleep(1000);
//                }
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
//            progressBar.setProgress(DownloadManagerService.percent);
        }

        protected void onPostExecute(Boolean bytes) {
            if (isRemoving()) {
                return;
            }
            progressBar.setProgress(100);
            alreadyDownloaded = DataBaseHandler.verifyAlreadyDownloaded(currentBean, getActivity());
            if (alreadyDownloaded) {
                ImageButton btnDownload = (ImageButton) view.findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.content_discard);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.delete));
            }

        }

    }
    
    public void setComponents() {

        RelativeLayout rl = (RelativeLayout) view.findViewById(
                R.id.doujinDetail);
        rl.setVisibility(View.VISIBLE);

        TextView tvDescription = (TextView) view.findViewById(
                R.id.tvDescription);
        TextView tvDoujin = (TextView) view.findViewById(R.id.tvDoujin);
//        TextView tvArtist = (TextView) view.findViewById(R.id.tvArtist);
        ImageView ivTitle = (ImageView) view.findViewById(R.id.ivTitle);
//        TextView tvSerie = (TextView) view.findViewById(R.id.tvSerie);
//        TextView tvQtyPages = (TextView) view
//                .findViewById(R.id.tvQtyPages);
//        TextView tvUploader = (TextView) view
//                .findViewById(R.id.tvUploader);
//        TextView tvLanguage = (TextView) view
//                .findViewById(R.id.tvLanguage);
//        TextView tvTranslator = (TextView) view.findViewById(
////                R.id.tvTranslator);
//        LinearLayout llTags = (LinearLayout) view
//                .findViewById(R.id.llTags);

        String s = getResources().getString(R.string.content_pages);
        
//
        s = s.replace("rpc1", "" + currentBean.getQtyPages());
//
//        tvQtyPages.setText(s);
//
//        s = getResources().getString(R.string.content_uploader);
//
//        s = s.replace("rpc1", currentBean.getUploader().getUser());
//        s = s.replace("rpc2", currentBean.getDate());

//        SpannableString content = new SpannableString(s);
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        tvUploader.setText(content);
//
//        s = getResources().getString(R.string.label_description);
//        s = s.replace("?", currentBean.getDescription().replace("<br>", "<br/>"));
        tvDescription.setText(Html.fromHtml(currentBean.getDescription().replace(
                "<div>", "</div>")));
//        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
//
//        content = new SpannableString(currentBean.getTitle());
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvDoujin.setText(currentBean.getTitle());
//
//        s = getResources().getString(R.string.label_artist);
//        s = s.replace("?", currentBean.getArtist().getDescription());
//        tvArtist.setText(Html.fromHtml(s));
//
//        s = getResources().getString(R.string.label_serie);
//        s = s.replace("?", currentBean.getSerie().getDescription());
//        tvSerie.setText(Html.fromHtml(s));
//
//        s = getResources().getString(R.string.label_language);
//        s = s.replace("?", currentBean.getLanguage().getDescription());
//        tvLanguage.setText(Html.fromHtml(s));
//
//        s = getResources().getString(R.string.label_translator);
//        s = s.replace("?", currentBean.getTranslator()==null?"":currentBean.getTranslator().getDescription());
//        tvTranslator.setText(Html.fromHtml(s));

        Bitmap bmpTitle = currentBean.getBitmapImageTitle(Helper.getCacheDir(getActivity()), ImageQuality.HIGH);
        
        Log.i("bmpTitle", currentBean.getBitmapImageTitle(Helper.getCacheDir(getActivity()), ImageQuality.HIGH).toString());
        if (bmpTitle != null)
            ivTitle.setImageBitmap(bmpTitle);

//        tvUploader.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent itMain = new Intent(mMainActivity, MainActivity.class);
//                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.FAVORITES);
//                itMain.putExtra(MainActivity.INTENT_VAR_USER, currentBean.getUploader().getUser());
//                itMain.putExtra(MainActivity.INTENT_VAR_URL, currentBean.getUploader().getUrlUser());
//                getActivity().startActivityForResult(itMain, 1);
//            }
//        });
//        tvArtist.setOnClickListener(new URLListener(currentBean
//                .getArtist(), R.string.tile_artist));
//        tvLanguage.setOnClickListener(new URLListener(currentBean
//                .getLanguage(), R.string.tile_language));
//        tvSerie.setOnClickListener(new URLListener(currentBean.getSerie(),
//                R.string.tile_serie));
//        tvTranslator.setOnClickListener(new URLListener(currentBean
//                .getTranslator(), R.string.tile_translator));

//        llTags.removeAllViews();
//        for (URLBean urlBean : currentBean.getLstTags()) {
//            TextView tv = (TextView) getActivity().getLayoutInflater().inflate(
//                    R.layout.textview_custom, null);
//            content = new SpannableString(urlBean.getDescription());
//            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//            tv.setText(content);
//
//            tv.setOnClickListener(new URLListener(urlBean, R.string.tile_tag));
//            llTags.addView(tv);
//        }

        ImageButton btnAddToFavorite = (ImageButton) view
                .findViewById(R.id.btnAddToFavorite);
        alreadyDownloaded = DataBaseHandler.verifyAlreadyDownloaded(currentBean, getActivity());

        if (currentBean != null) {
            if (currentBean.isAddedInFavorite()) {
                btnAddToFavorite.setImageResource(R.drawable.rating_important);
                btnAddToFavorite.setContentDescription(getResources()
                        .getString(R.string.remove_favorite));
            } else {
                btnAddToFavorite
                        .setImageResource(R.drawable.rating_not_important);
                btnAddToFavorite.setContentDescription(getResources()
                        .getString(R.string.add_favorite));
            }
            if (alreadyDownloaded) {
                ImageButton btnDownload = (ImageButton) view
                        .findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.content_discard);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.delete));
            } else {
                ImageButton btnDownload = (ImageButton) view
                        .findViewById(R.id.btnDownload);
                btnDownload.setImageResource(R.drawable.av_download);
                btnDownload.setContentDescription(getResources().getString(
                        R.string.download));
            }
        }
    }

    class URLListener implements OnClickListener {

        URLBean urlBean;
        int rID;

        public URLListener(URLBean urlBean, int rID) {
            this.urlBean = urlBean;
            this.rID = rID;
        }

        @Override
        public void onClick(View v) {
            if (urlBean != null && urlBean.getDescription() != null && !urlBean.getDescription().equals("")) {
                Intent itMain = new Intent(mMainActivity, MainActivity.class);
                itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN_LIST);
                itMain.putExtra(MainActivity.INTENT_VAR_URL, urlBean.getUrl());
                itMain.putExtra(MainActivity.INTENT_VAR_TITLE, urlBean.getDescription());
                getActivity().startActivityForResult(itMain, 1);
            }
        }
    }
    
	// ////////////////////////////////////Progress
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mFormView.setVisibility(View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public DoujinBean getCurrentBean() {
        return currentBean;
    }
}
