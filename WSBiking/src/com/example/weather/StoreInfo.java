package com.example.weather;

import android.graphics.drawable.Drawable;

public class StoreInfo {
	String []temp= new String[5];
	String []condition = new String[5];
	String []time = new String[5];
	String []imgURL = new String[5];
	Drawable []img = new Drawable[5];
	
	
	public String getTemp(int i) {
		return temp[i];
	}
	public void setTemp(String temp, int i) {
		this.temp[i] = temp;
	}
	public String getCondition(int i) {
		return condition[i];
	}
	public void setCondition(String condition, int i) {
		this.condition[i] = condition;
	}
	public String getTime(int i) {
		return time[i];
	}
	public void setTime(String time, int i) {
		this.time[i] = time;
	}
	
	public void setImgURL(String url, int i) {
		this.imgURL[i] = url;
	}
	
	public String getImageURL(int i) {
		return imgURL[i];
	}
	
	public void setImg(Drawable img, int i){
		this.img[i] = img;
	}
	
	public Drawable getImg(int i){
		return img[i];
	}
}
