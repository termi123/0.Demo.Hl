package com.example.demo.hl.fragment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.example.demo.hl.MainActivity;
import com.example.demo.hl.R;
import com.example.demo.hl.R.color;
import com.example.demo.hl.adapter.DoujinListAdapter;
import com.example.demo.hl.asyntask.ReadAsyncTask;
import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.core.DataBaseHandler;
import com.example.demo.hl.core.FakkuConnection;
import com.example.demo.hl.core.FakkuDroidApplication;
import com.example.demo.hl.util.Constants;
import com.example.demo.hl.util.Helper;
import com.example.demo.hl.util.ImageQuality;

public class DoujinListFragment extends SherlockListFragment {

	int index = -1;
	FakkuDroidApplication app;
	LinkedList<DoujinBean> llDoujin;
	DoujinListAdapter da;
	String url = Constants.SITEROOT;
	int numPage = 1;
	private View mFormView;
	private View mStatusView;
	private View view;
	boolean related;
	private MainActivity mMainActivity;
	private boolean refresh;

	public void setMainActivity(MainActivity mainActivity) {
		mMainActivity = mainActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (llDoujin == null || refresh)
			loadPage();
	}

	@Override
	public void onPause() {
		super.onPause();
		ListView list = (ListView) view.findViewById(android.R.id.list);
		index = list.getFirstVisiblePosition();
	}

	@Override
	public void onResume() {
		super.onResume();
		ListView list = (ListView) view.findViewById(android.R.id.list);
		if (index > -1)
			list.setSelectionFromTop(index, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater
				.inflate(R.layout.fragment_doujin_list, container, false);

		mFormView = view.findViewById(R.id.view_form);
		mStatusView = view.findViewById(R.id.view_status);

		return view;
	}

	// ////////////////////////////////////Button Event
	public void nextPage(View view) {
		index = -1;
		numPage++;
		loadPage();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this.getActivity(), text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		index = -1;
		if (numPage - 1 == 0) {
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		} else {
			numPage--;
			loadPage();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		}
	}

	public void changePage(int page) {
		index = -1;
		numPage = page;
		loadPage();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this.getActivity(), text, duration);
		toast.show();
	}

	// ////////////////////////////////////Load Page/////
	@SuppressLint("NewApi")
	public void loadPage() {
		TextView tvPage = (TextView) view.findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		if (related) {
			Helper.executeAsyncTask(new DownloadCatalog(),
					app.getRelatedUrl(numPage, url));
			// Log.i("Get RealateURL", app.getRelatedUrl(numPage, url));
		} else {
			Helper.executeAsyncTask(new DownloadCatalog(),
					app.getUrl(numPage, url));
			// Log.i("Get URL", app.getUrl(numPage, url));
		}

	}

	// ////////////////////////////////////Download Catalog
	class DownloadCatalog extends AsyncTask<String, String, Integer> {

		private ProgressBar progressBar;

		protected void onPreExecute() {
			progressBar = (ProgressBar) DoujinListFragment.this.getActivity()
					.findViewById(R.id.progressBarImages);
			progressBar.setProgress(0);
			showProgress(true);
		}

		protected Integer doInBackground(String... urls) {

			publishProgress(getResources().getString(R.string.downloading_data));
			try {
				// Log.i("URL Catalog: ", urls[0]);
				llDoujin = FakkuConnection.parseHTMLCatalog(urls[0]);
				// Log.i("Doujin List: ", llDoujin.toString());
			} catch (ClientProtocolException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (IOException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (URISyntaxException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (Exception e) {
				Helper.logError(this, e.getMessage(), e);
			}
			if (llDoujin == null)
				llDoujin = new LinkedList<DoujinBean>();
			return llDoujin.size();
		}

		@SuppressWarnings("unchecked")
		protected void onPostExecute(Integer size) {
			if (DoujinListFragment.this.getActivity() != null) {
				if (llDoujin.size() == 0) {
					progressBar.setMax(100);
					progressBar.setProgress(100);
				} else {
					progressBar.setMax(llDoujin.size());
					List<List<DoujinBean>> list = Helper.splitArrayList(
							llDoujin, 3);
					// Log.i("Doujin List cata: ", list.toString());
					for (List<DoujinBean> l : list) {
						Helper.executeAsyncTask(new DownloadImage(), l);
					}
				}
				setData();
				showProgress(false);
			}
		}
	}

	// ////////////////////////////////////Download Image
	class DownloadImage extends AsyncTask<List<DoujinBean>, String, Boolean> {

		private ProgressBar progressBar;

		protected void onPreExecute() {
			progressBar = (ProgressBar) DoujinListFragment.this.getActivity()
					.findViewById(R.id.progressBarImages);
		}

		@Override
		protected Boolean doInBackground(List<DoujinBean>... params) {
			boolean success = false;
			List<DoujinBean> list = params[0];
			// Log.i("Doujin Bean: ", list.toString());
			for (DoujinBean bean : list) {
				if (DoujinListFragment.this.getActivity() != null) {
					try {
						String a = bean.getUrl();
						// Log.i("Get Url a", a);

						int c = bean.getUrl().lastIndexOf("/");
						String d = bean.getUrl().substring(69, c);
						// .replaceAll("'", "");
						// Log.i("Get new Url a", d);
						//
						File dir = Helper.getCacheDir(getActivity());
						// Log.i("dir", dir.toString());

						File myFile = new File(dir, bean.getFileImagePage());
						Helper.saveInStorage(myFile, bean.getUrlImagePage());
						
						// myFile = new File(dir, bean.getFileImageTitle());
						// Helper.saveInStorage(myFile,
						// bean.getUrlImageTitle());

						bean.loadImages(dir, ImageQuality.HIGH);
					} catch (Exception e) {
						Helper.logError(this, e.getMessage(), e);
					}
				}
				publishProgress();
			}

			return success;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			progressBar.setProgress(progressBar.getProgress() + 1);
			da.notifyDataSetChanged();
		}
	}

	// ////////////////////////////////////Menu Item event
	public void refresh(View view) {
		loadPage();
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrl(numPage, url)));
		this.startActivity(viewBrowser);
	}

	public void setRelated(boolean related) {
		numPage = 1;
		this.related = related;
	}

	public void setUrl(String url) {
		refresh = true;
		numPage = 1;
		this.url = url;
	}

	private void setData() {
		// Bundle bundle = getArguments();
		// String test = Integer.toString(bundle.getInt("a"));
		// Log.i("Test", test);
		// if(bundle.getInt("a")==1){
		// da = new DoujinListAdapter(this.getActivity(),
		// R.layout.row_doujin_main, 0,
		// llDoujin, related);
		// }else{
		da = new DoujinListAdapter(this.getActivity(), R.layout.row_doujin, 0,
				llDoujin, related);
		// }

		da.doujinListFragment = this;
		this.setListAdapter(da);
		refresh = false;
	}

	public void quickDownload(int position) {
		Toast.makeText(getActivity(), "More", Toast.LENGTH_LONG).show();
		// DoujinBean bean = llDoujin.get(position);
		// DownloadManagerService.downloadDoujin(bean, getActivity());
	}

	public void quickRead(int position) {
		Toast.makeText(getActivity(), "Map", Toast.LENGTH_LONG).show();
		// DoujinBean bean = llDoujin.get(position);
		// boolean alreadyDownload =
		// DataBaseHandler.verifyAlreadyDownloaded(bean,
		// getActivity());
		// Helper.executeAsyncTask(new ReadAsyncTask(getActivity(),
		// alreadyDownload), bean);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	// ////////////////////////////////////Progress
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
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

	// ////////////////////////////////////Menu Item event > Doujin
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		DoujinBean data = llDoujin.get(position);
		Intent itMain = intentForDoujin(data);
		// Log.i("Data", data.toString());
		getActivity().startActivityForResult(itMain, 1);
	}

	private Intent intentForDoujin(DoujinBean data) {

		Intent itMain = new Intent(mMainActivity, MainActivity.class);
		itMain.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT,
				MainActivity.DOUJIN);
		itMain.putExtra(MainActivity.INTENT_VAR_URL, data.getUrl());
		itMain.putExtra(MainActivity.INTENT_VAR_TITLE, data.getTitle());

		return itMain;
	}

}
