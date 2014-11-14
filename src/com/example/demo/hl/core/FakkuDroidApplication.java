package com.example.demo.hl.core;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.bean.UserBean;
import com.example.demo.hl.util.Constants;
import com.example.demo.hl.util.Helper;

public class FakkuDroidApplication extends Application {

	private DoujinBean current = null;
	private UserBean settingBean = null;
    private boolean remindMeLater = false;
	
	public UserBean getSettingBean() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
		if(settingBean==null){
			DataBaseHandler db = new DataBaseHandler(this.getApplicationContext());
			settingBean = db.getSetting();

			if(settingBean==null)
				settingBean = db.addSetting();
		}
		settingBean.setUrlUser(prefs.getString("url_user", null));
		return settingBean;
	}

	public void setSettingBean(UserBean settingBean) {
		this.settingBean = settingBean;
	}

	public DoujinBean getCurrent() {
		return current;
	}

	public void setCurrent(DoujinBean current) {
		this.current = current;
	}

	public String getTitle(int nroPage, String title){
		return title + " #" + nroPage;
	}
	public String getUrl(int nroPage, String url) {
        if (nroPage > 1) {
            return url + Constants.PAGE + nroPage;
        }
        return url;
    }
    public String getRelatedUrl(int nroPage, String url) {
        return url + "/" + (--nroPage*10);
    }
	public String getUrlFavorite(int numPage, String user) {
		return Helper.escapeURL(Constants.SITEFAVORITE.replace("@user", user.toLowerCase()).replace("@numpage", numPage + ""));
	}

    public boolean isRemindMeLater() {
        return remindMeLater;
    }

    public void setRemindMeLater(boolean remindMeLater) {
        this.remindMeLater = remindMeLater;
    }
}
