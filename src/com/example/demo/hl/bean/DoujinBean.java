package com.example.demo.hl.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import com.example.demo.hl.util.Constants;
import com.example.demo.hl.util.Helper;
import com.example.demo.hl.util.ImageQuality;

import android.graphics.Bitmap;

public class DoujinBean {

	private String title;
	private String serie;
	private URLBean artist;
	private String description;
	private String urlImagePage;
	private String urlImageTitle;
	private String url;
	private int qtyPages;
	private URLBean language;
	private URLBean translator;
	private transient UserBean uploader;
	private String date;
	private List<URLBean> lstTags;
	private boolean addedInFavorite, completed;
	private transient Bitmap titleBitmap, pageBitmap;
	private String imageServer;
	private String urlDownload;
    private transient LinkedList<CommentBean> lstTopComments;
    private transient LinkedList<CommentBean> lstRecentComments;

	public String getId() {
		int idxStart = url.lastIndexOf("/");
//		+ 1;

		return url.substring(69,idxStart);
//				.replaceAll("'", "");
	}

	public String urlFavorite(String urlFavorite) {

		return urlFavorite.replace("@id",getId()) ;
	}

	public boolean isAddedInFavorite() {
		return addedInFavorite;
	}

	public void setAddedInFavorite(boolean addedInFavorite) {
		this.addedInFavorite = addedInFavorite;
	}

	public String getFileImageTitle() {
		return getId() + "title.jpg";
	}

	public String getFileImagePage() {
		return getId() + "page.jpg";
	}

	public Bitmap getBitmapImageTitle(File dir, ImageQuality imageQuality) {
		if (titleBitmap == null) {
			File titleFile = new File(dir, getFileImageTitle());
			if (titleFile.exists())
				titleBitmap = Helper.decodeSampledBitmapFromFile(
						titleFile.getAbsolutePath(), imageQuality.getWidth(),
						imageQuality.getHeight());
		}
		return titleBitmap;
	}

	public Bitmap getBitmapImagePage(File dir, ImageQuality imageQuality) {
		if (pageBitmap == null) {
			File titlePage = new File(dir, getFileImagePage());
			if (titlePage.exists())
				pageBitmap = Helper.decodeSampledBitmapFromFile(
						titlePage.getAbsolutePath(), imageQuality.getWidth(),
                        imageQuality.getHeight());
		}
		return pageBitmap;
	}

	public void loadImages(File dir, ImageQuality imageQuality) {
		if (pageBitmap == null) {
			File titlePage = new File(dir, getFileImagePage());
			if (titlePage.exists())
				pageBitmap = Helper.decodeSampledBitmapFromFile(
						titlePage.getAbsolutePath(), imageQuality.getWidth(),
                        imageQuality.getHeight());
		}
		if (titleBitmap == null) {
			File titleFile = new File(dir, getFileImageTitle());
			if (titleFile.exists())
				titleBitmap = Helper.decodeSampledBitmapFromFile(
						titleFile.getAbsolutePath(), imageQuality.getWidth(),
                        imageQuality.getHeight());
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public URLBean getArtist() {
		return artist;
	}

	public void setArtist(URLBean artist) {
		this.artist = artist;
	}

	public String getUrlImagePage() {
		return urlImagePage;
	}

	public void setUrlImagePage(String urlImagePage) {
		this.urlImagePage = urlImagePage;
	}
////
	public String getUrlImageTitle() {
		return urlImageTitle;
	}

	public void setUrlImageTitle(String urlImageTitle) {
		this.urlImageTitle = urlImageTitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getQtyPages() {
		return qtyPages;
	}

	public void setQtyPages(int qtyPages) {
		this.qtyPages = qtyPages;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URLBean getLanguage() {
		return language;
	}

	public void setLanguage(URLBean language) {
		this.language = language;
	}

	public URLBean getTranslator() {
		return translator;
	}

	public void setTranslator(URLBean translator) {
		this.translator = translator;
	}

	public UserBean getUploader() {
		return uploader;
	}

	public void setUploader(UserBean uploader) {
		this.uploader = uploader;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<URLBean> getLstTags() {
		return lstTags;
	}

	public void setLstTags(List<URLBean> lstTags) {
		this.lstTags = lstTags;
	}

	public List<String> getImages() {
		List<String> result = new ArrayList<String>();
		for (int i = 1; i <= qtyPages; i++) {
			Formatter fmt = new Formatter();
			result.add(imageServer + fmt.format("%03d", i) + ".jpg");
			fmt.close();
		}
		return result;
	}

	public List<String> getImagesFiles() {
		List<String> result = new ArrayList<String>();
		for (int i = 1; i <= qtyPages; i++) {
			Formatter fmt = new Formatter();
			result.add(fmt.format("%03d", i) + ".jpg");
			fmt.close();
		}
		return result;
	}

	public String getTags() {
		String result = "";

		for (URLBean url : lstTags) {
			result += url.getDescription() + ", ";
		}
		if (result.length() > 0)
			result = result.substring(0, result.length() - ", ".length());
		return result;
	}

	public String getTagsWithURL() {
		String result = "";

		for (URLBean url : lstTags) {
			result += url.getDescription() + "|" + url.getUrl() + ", ";
		}
		if (result.length() > 0)
			result = result.substring(0, result.length() - ", ".length());
		return result;
	}

	@Override
	public String toString() {
		return "DoujinBean [title=" + title 
				+ ", serie=" + serie 
				+ ", artist=" + artist 
				+ ", description=" + description + ", urlImagePage="
				+ urlImagePage + ", urlImageTitle=" + urlImageTitle + ", url="
				+ url + ", qtyPages=" + qtyPages 
				+ ", language=" + language 
				+ ", translator="+ translator 
				+ ", uploader=" + uploader 
				+ ", date=" + date
				+ ", lstTags=" + lstTags 
				+ "]";
	}

	public String getImageServer() {
		return imageServer;
	}

	public void setImageServer(String imageServer) {
		this.imageServer = imageServer;
	}

	public String getUrlDownload() {
		return urlDownload;
	}

	public void setUrlDownload(String urlDownload) {
		this.urlDownload = urlDownload;
	}

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isPageLoaded(){
        return pageBitmap!=null;
    }

    public boolean isTitleLoaded(){
        return titleBitmap!=null;
    }

    public String getRelatedUrl(){
        String relatedUrl = url.replaceAll(Constants.SITEROOT, Constants.SITEROOT + "/related");
        return relatedUrl;
    }
    public LinkedList<CommentBean> getLstTopComments() {
        return lstTopComments;
    }
    public void setLstTopComments(LinkedList<CommentBean> lstTopComments) {
        this.lstTopComments = lstTopComments;
    }
    public LinkedList<CommentBean> getLstRecentComments() {
        return lstRecentComments;
    }
    public void setLstRecentComments(LinkedList<CommentBean> lstRecentComments) {
        this.lstRecentComments = lstRecentComments;
    }
}
