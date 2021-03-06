package com.example.demo.hl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.example.demo.hl.adapter.MenuListAdapter;
import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.bean.URLBean;
import com.example.demo.hl.bean.VersionBean;
import com.example.demo.hl.core.FakkuConnection;
import com.example.demo.hl.core.FakkuDroidApplication;
import com.example.demo.hl.fragment.DoujinFragment;
import com.example.demo.hl.fragment.DoujinListFragment;
import com.example.demo.hl.fragment.MainListFragment;
import com.example.demo.hl.fragment.MenuListFragment;
import com.example.demo.hl.util.Constants;
import com.example.demo.hl.util.Helper;

public class MainActivity extends SherlockFragmentActivity implements
		SearchView.OnQueryTextListener {

	// ////////////////////////////////////StringExtra
	public final static String INTENT_VAR_URL = "intentVarUrl";
	public final static String INTENT_VAR_TITLE = "intentVarTitle";
	public final static String INTENT_VAR_USER = "intentVarUser";
	public final static String INTENT_VAR_CURRENT_CONTENT = "intentVarCurrentContent";
	public final static String INTENT_VAR_IS_RELATED = "intentVarIsRelated";

	// ////////////////////////////////////Content Page
	public static final int MAIN_LIST = 0;
	public static final int DOUJIN_LIST = 1;
	public static final int DOUJIN = 2;
	// public static final int FAVORITES = 3;

	private DrawerLayout mDrawerLayout;
	private MenuListFragment frmMenu;
	private ActionBarDrawerToggle mDrawerToggle;
	private DoujinListFragment frmDoujinList;
	private DoujinFragment frmDoujinFragment;
	private MainListFragment frmMainList;

	private int currentContent = MAIN_LIST;
	// DOUJIN_LIST;
	FakkuDroidApplication app;
	int test = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (FakkuDroidApplication) getApplication();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		frmMenu = new MenuListFragment();
		frmMenu.setMainActivity(this);

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.menu_frame, frmMenu)
				.commit();

		int tempCurrentContent = getIntent().getIntExtra(
				INTENT_VAR_CURRENT_CONTENT, -1);
		if (tempCurrentContent != -1)
			currentContent = tempCurrentContent;

		if (currentContent == MAIN_LIST) {
			mainlistload();
		}

		else if (currentContent == DOUJIN_LIST) {
			String url = getIntent().getStringExtra(INTENT_VAR_URL);
			url = url == null ? Constants.SITEROOT : url;
			Log.i("url", url);

			String title = getIntent().getStringExtra(INTENT_VAR_TITLE);
			title = title == null ? getResources().getString(R.string.app_name)
					: title;
			Log.i("title", title);
			doujinlistload(url, title);

		} else if (currentContent == DOUJIN) {
			frmDoujinFragment = new DoujinFragment();
			frmDoujinFragment.setMainActivity(this);
			String title = getIntent().getStringExtra(INTENT_VAR_TITLE);
			title = title == null ? getResources().getString(R.string.random)
					: title;

			setTitle(title);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, frmDoujinFragment).commit();

			// FrameLayout f = (FrameLayout) findViewById(R.id.comments_frame);
			// f.setVisibility(View.VISIBLE);

			// frmCommentListFragment = new CommentListFragment();
			// frmCommentListFragment.setMainActivity(this);
			// getSupportFragmentManager().beginTransaction()
			// .replace(R.id.comments_frame, frmCommentListFragment).commit();

		}
		// else if (currentContent == FAVORITES) {
		// String user = getIntent().getStringExtra(INTENT_VAR_USER);
		// String url_user = getIntent().getStringExtra(INTENT_VAR_URL);
		// frmFavorite = new FavoriteFragment();
		// frmFavorite.setMainActivity(this);
		// frmFavorite.setUser(user, url_user);
		// String title = getResources().getString(R.string.favorite);
		// title = title.replace("usr", user);
		// setTitle(title);
		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.content_frame, frmFavorite).commit();

		// }else if (currentContent == Restaurants) {
		// }else if (currentContent == Hotels) {
		// }else if (currentContent == Attractions) {
		// }else if (currentContent == Nightlife) {
		// }else if (currentContent == Shopping) {
		// }else if (currentContent == AirTicket) {
		// }

		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View view) {
				supportInvalidateOptionsMenu();
				// if(currentContent==DOUJIN){
				// //
				// if(frmCommentListFragment!=null&&frmCommentListFragment.isListCharged())
				// // frmCommentListFragment.loadComments();
				// }else{
				// mDrawerLayout.closeDrawer(Gravity.END);
				// }
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void mainlistload() {
		frmMainList = new MainListFragment();
		frmMainList.setMainActivity(this);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmMainList).commit();
		setTitle("Ha Long Bay - Travel Helpers");
	}

	private void doujinlistload(String url, String title) {
		boolean isRelated = getIntent().getBooleanExtra(INTENT_VAR_IS_RELATED,
				false);
		frmDoujinList = new DoujinListFragment();
		frmDoujinList.setMainActivity(this);
		frmDoujinList.setRelated(isRelated);
		url = url == null ? Constants.SITEROOT : url;
		frmDoujinList.setUrl(url);
		Log.i("url", url);

		title = title == null ? getResources().getString(R.string.app_name)
				: title;
		setTitle(title);
		Log.i("title", title);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frmDoujinList)
				.addToBackStack(null).commit();
	}

	// ////////////////////////////////////Bottom Menu - Image
	// Event/////////////////////////////////////

	public void btnres(View view) {
		doujinlistload(Constants.SITERESTAURANT, Constants.TITLERESTAURANT);
	}

	public void btnhotel(View view) {
		doujinlistload(Constants.SITEHOTEL, Constants.TITLEHOTEL);
	}

	public void btnatt(View view) {
		doujinlistload(null, null);
	}

	public void btnnight(View view) {
		doujinlistload(Constants.SITEBAR, Constants.TITLEBAR);
	}

	public void btnshop(View view) {
		doujinlistload(null, null);
	}

	public void btnticket(View view) {
		doujinlistload(null, null);
	}

	// ////////////////////////////////////LeftMenuList/////////////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		frmMenu.createMainMenu();
	}

	// ////////////////////////////////////OptionMenu/////////////////////////////////////

	// ////////////////////////////////////CreateOptionMenu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (currentContent == DOUJIN_LIST || currentContent == MAIN_LIST) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				// Create the search view
				SearchView searchView = new SearchView(getSupportActionBar()
						.getThemedContext());
				searchView
						.setQueryHint(getResources().getText(R.string.search));
				searchView.setOnQueryTextListener(this);
				String hint = "";
				if (currentContent == DOUJIN_LIST)
					getResources().getText(R.string.search);
				else
					getResources().getText(R.string.search_by);
				menu.add(hint)
						.setIcon(R.drawable.action_search)
						.setActionView(searchView)
						.setShowAsAction(
								MenuItem.SHOW_AS_ACTION_IF_ROOM
										| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			} else {
				menu.add(Menu.NONE, R.string.search, 1, R.string.search)
						.setIcon(R.drawable.action_search)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			}
		}
		// if (currentContent == DOWNLOADS && frmDownloadListFragment != null) {
		// if (frmDownloadListFragment.isOrderDate()) {
		// menu.add(Menu.NONE, R.string.order_date, 2, R.string.order_date)
		// .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// } else {
		// menu.add(Menu.NONE, R.string.order_a_to_z, 2, R.string.order_a_to_z)
		// .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// }
		// }
		// if (currentContent == DOWNLOADS_QUEUE || currentContent == DOWNLOADS)
		// {
		// menu.add(Menu.NONE, R.string.go_other_list, 3,
		// R.string.go_other_list)
		// .setIcon(R.drawable.content_import_export)
		// .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// }
		// if (currentContent == FAVORITES) {
		// String user =
		// getIntent().getStringExtra(INTENT_VAR_USER).toLowerCase();
		// if (user.equals(app.getSettingBean().getUser().toLowerCase())) {
		// menu.add(Menu.NONE, R.string.change_user_url, 1,
		// R.string.change_user_url)
		// .setIcon(R.drawable.social_cc_bcc)
		// .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// }
		// }
		return true;
	}

	// ////////////////////////////////////Menu Item
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		} else if (item.getItemId() == R.string.go_other_list) {
			// if (currentContent == DOWNLOADS_QUEUE) {
			// setTitle(R.string.download_title);
			// if (frmDownloadListFragment == null) {
			// frmDownloadListFragment = new DownloadListFragment();
			// frmDownloadListFragment.setMainActivity(this);
			// }
			// getSupportFragmentManager().beginTransaction()
			// .replace(R.id.content_frame, frmDownloadListFragment).commit();
			// currentContent = DOWNLOADS;
			// } else if (currentContent == DOWNLOADS) {
			// setTitle(R.string.download_queue_title);
			// if (frmDownloadQueueListFragment == null) {
			// frmDownloadQueueListFragment = new DownloadQueueListFragment();
			// frmDownloadQueueListFragment.setMainActivity(this);
			// }
			// getSupportFragmentManager().beginTransaction()
			// .replace(R.id.content_frame,
			// frmDownloadQueueListFragment).commit();
			// currentContent = DOWNLOADS_QUEUE;
			// }
			supportInvalidateOptionsMenu();
		} else if (item.getItemId() == R.string.order_a_to_z
				|| item.getItemId() == R.string.order_date) {
			// frmDownloadListFragment.changeOrder();
			supportInvalidateOptionsMenu();
		} else if (item.getItemId() == R.string.search) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle(R.string.abs__searchview_description_query);
			alert.setMessage(R.string.search);

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setNegativeButton(android.R.string.cancel, null);
			// if (currentContent == DOWNLOADS) {
			// alert.setPositiveButton(android.R.string.ok,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// String query = input.getText().toString().trim();
			// // frmDownloadListFragment.search(query);
			// }
			// });
			// } else
			if (currentContent == DOUJIN_LIST) {
				alert.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String query = input.getText().toString();
								String url, title;
								if (!query.equals("")) {
									String strSearch = getResources()
											.getString(R.string.search);
									url = Constants.SITESEARCH + query.trim();
									title = strSearch + ": " + query.trim();
								} else {
									title = getResources().getString(
											R.string.app_name);
									url = Constants.SITEROOT;
								}
								Intent itMain = new Intent(MainActivity.this,
										MainActivity.class);
								itMain.putExtra(INTENT_VAR_CURRENT_CONTENT,
										DOUJIN_LIST);
								itMain.putExtra(INTENT_VAR_TITLE, title);
								itMain.putExtra(INTENT_VAR_URL, url);
								startActivityForResult(itMain, 1);
							}
						});
			}
			alert.show();
		} else if (item.getItemId() == R.string.change_user_url) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Change User URL");
			alert.setMessage("User URL : ");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							SharedPreferences prefs = PreferenceManager
									.getDefaultSharedPreferences(MainActivity.this);
							prefs.edit().putString("url_user", value).commit();
							// frmFavorite.refreshChanginUrlUser(value);
						}
					});

			alert.setNegativeButton(android.R.string.cancel, null);

			alert.show();
		}
		return true;
	}

	// ////////////////////////////////////Get Menu Item
	private android.view.MenuItem getMenuItem(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean expandActionView() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public View getActionView() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getGroupId() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Drawable getIcon() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Intent getIntent() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public char getNumericShortcut() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getOrder() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCheckable() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isChecked() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {
				// TODO Auto-generated method stub

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	// ////////////////////////////////////Query submit
	@Override
	public boolean onQueryTextSubmit(String arg0) {
		if (currentContent == DOUJIN_LIST) {
			String query = arg0.trim();
			String url, title;
			if (!query.equals("")) {
				String strSearch = getResources().getString(R.string.search);
				url = Constants.SITESEARCH + query.trim();
				title = strSearch + ": " + query.trim();
			} else {
				title = getResources().getString(R.string.app_name);
				url = Constants.SITEROOT;
			}
			Intent itMain = new Intent(this, MainActivity.class);
			itMain.putExtra(INTENT_VAR_CURRENT_CONTENT, DOUJIN_LIST);
			itMain.putExtra(INTENT_VAR_TITLE, title);
			itMain.putExtra(INTENT_VAR_URL, url);
			startActivityForResult(itMain, 1);
		}
		InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		return true;
	}

	// ////////////////////////////////////Query Text change
	@Override
	public boolean onQueryTextChange(String arg0) {
		// String query = arg0.trim();
		// if (currentContent != DOUJIN_LIST) {
		// // frmDownloadListFragment.search(query);
		// }
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	// ////////////////////////////////////DOUJIN_LIST/////////////////////////////////////

	// ////////////////////////////////////Next
	public void nextPage(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.nextPage(view);
		// else if (currentContent == DOWNLOADS)
		// frmDownloadListFragment.nextPage(view);
		// else if (currentContent == DOUJIN)
		// frmCommentListFragment.nextPage(view);
		// else
		// frmFavorite.nextPage(view);
	}

	// ////////////////////////////////////Previous
	public void previousPage(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.previousPage(view);
		// else if (currentContent == DOWNLOADS)
		// frmDownloadListFragment.previousPage(view);
		// else if (currentContent == DOUJIN)
		// frmCommentListFragment.previousPage(view);
		// else
		// frmFavorite.previousPage(view);
	}

	// ////////////////////////////////////Change Page
	public void changePage(View view) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Go to...");
		alert.setMessage("Page");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						if (!value.equals("")) {
							int page = Integer.parseInt(value);
							if (page <= 0) {
								Toast.makeText(MainActivity.this,
										R.string.error_page_out,
										Toast.LENGTH_SHORT).show();
							} else {
								if (currentContent == DOUJIN_LIST) {
									frmDoujinList.changePage(page);
								}
								// else if (currentContent == FAVORITES) {
								// // frmFavorite.changePage(page);
								// }
								// else if (currentContent == DOWNLOADS) {
								// frmDownloadListFragment.changePage(page);
								// }
							}
						}
					}
				});

		alert.setNegativeButton(android.R.string.cancel, null);

		alert.show();
	}

	// ////////////////////////////////////Open Browser
	public void viewInBrowser(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.viewInBrowser(view);
		// else if (currentContent == DOUJIN)
		// frmDoujinFragment.viewInBrowser(view);
		// else
		// frmFavorite.viewInBrowser(view);
	}

	// ////////////////////////////////////Refresh
	public void refresh(View view) {
		if (currentContent == DOUJIN_LIST)
			frmDoujinList.refresh(view);
		// else if (currentContent == FAVORITES)
		// frmFavorite.refresh(view);
		// else if (currentContent == DOUJIN)
		// frmDoujinFragment.refresh();
	}

	// ////////////////////////////////////DOUJIN/////////////////////////////////////

	// ////////////////////////////////////Relate Content
	public void relatedContent(View view) {
		String title = getResources().getString(R.string.related_content);
		String url = frmDoujinFragment.getCurrentBean().getRelatedUrl();

		Intent itMain = new Intent(MainActivity.this, MainActivity.class);
		itMain.putExtra(INTENT_VAR_CURRENT_CONTENT, DOUJIN_LIST);
		itMain.putExtra(INTENT_VAR_TITLE, title);
		itMain.putExtra(INTENT_VAR_URL, url);
		itMain.putExtra(INTENT_VAR_IS_RELATED, true);
		startActivityForResult(itMain, 1);
	}

	// ////////////////////////////////////Comment
	public void comments(View view) {
		// if(!frmCommentListFragment.isListCharged())
		// frmCommentListFragment.loadComments();
		// if (!mDrawerLayout.isDrawerOpen(Gravity.END))
		// mDrawerLayout.openDrawer(Gravity.END);
		// else
		// mDrawerLayout.closeDrawer(Gravity.END);
	}

	// ////////////////////////////////////Download
	public void download(View view) {
		frmDoujinFragment.download();
	}

	// ////////////////////////////////////Read
	public void read(View view) {
		frmDoujinFragment.read(view);
	}

	// ////////////////////////////////////+ - Favo
	public void addOrRemoveFavorite(View view) {
		frmDoujinFragment.addOrRemoveFavorite(view);
	}

	// ////////////////////////////////////Check Ver
	class CheckerVersion extends AsyncTask<String, Float, VersionBean> {

		@Override
		protected VersionBean doInBackground(String... arg0) {
			VersionBean versionBean = null;
			try {
				versionBean = FakkuConnection.getLastversion();
			} catch (Exception e) {
				Helper.logError(this,
						"error in verifing if exists new version.", e);
			}
			return versionBean;
		}

		protected void onPostExecute(final VersionBean result) {
			if (result != null) {
				PackageInfo pInfo = null;
				try {
					pInfo = MainActivity.this.getPackageManager()
							.getPackageInfo(MainActivity.this.getPackageName(),
									0);
					String currentVersion = pInfo.versionName;
					if (currentVersion.compareToIgnoreCase(result
							.getVersion_code()) < 0) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this);
						builder.setMessage(R.string.msg_new_version)
								.setPositiveButton(R.string.visit_page,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Intent itVersion = new Intent(
														Intent.ACTION_VIEW);
												itVersion.setData(Uri.parse(result
														.getVersion_url()));
												MainActivity.this
														.startActivity(itVersion);
											}
										})
								.setNegativeButton(R.string.remind_never,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialogInterface,
													int i) {
												SharedPreferences prefs = PreferenceManager
														.getDefaultSharedPreferences(MainActivity.this);
												prefs.edit()
														.putBoolean(
																"check_updates_checkbox",
																false).commit();

											}
										})
								.setNeutralButton(R.string.remind_later,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialogInterface,
													int i) {
												// app.setRemindMeLater(true);
											}
										}).show();
					} else {
						// app.setRemindMeLater(true);
					}
				} catch (NameNotFoundException e) {
					Helper.logError(this, "error getting current version", e);
				}
			}
		}
	}

	public DoujinBean getCurrentBean() {
		return frmDoujinFragment != null ? frmDoujinFragment.getCurrentBean()
				: null;
	}

	private boolean doubleBackToExitPressedOnce = false;

//	 public void onBackPressed() {
//	 if (currentContent == MAIN_LIST) {
//	 if (doubleBackToExitPressedOnce) {
//	 super.onBackPressed();
//	 return;
//	 }
//	
//	 this.doubleBackToExitPressedOnce = true;
//	
//	
//	 new Handler().postDelayed(new Runnable() {
//	
//	 @Override
//	 public void run() {
//	 doubleBackToExitPressedOnce = false;
//	 }
//	 }, 2000);
//	 }else{
//	 mainlistload();
//	 }
//	 }

}