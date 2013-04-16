package com.example.weather;
import java.util.ArrayList;

import com.example.wsbiking.R;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Weather adapter class to populate listview of weather
 * 
 * @author Nimitt Sarda
 * 
 */
public class WeatherAdapter extends ArrayAdapter<StoreInfo> {
	private Context context;
	private int layoutResourceId;
	private ArrayList<StoreInfo> weather = null;

	public WeatherAdapter(Context context, int layoutResourceId,
			ArrayList<StoreInfo> weather) {
		super(context, layoutResourceId, weather);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.weather = weather;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RouteHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RouteHolder();
			holder.txtVwWeatherTime = (TextView) row
					.findViewById(R.id.txtVwWeatherTime);
			holder.TxtVwWeatherDesc = (TextView) row
					.findViewById(R.id.txtVwWeatherDesc);
			/*holder.txtVwRouteIndex = (TextView) row
					.findViewById(R.id.txtVwRouteIndex);*/
			holder.txtVwWeatherTemp = (TextView) row.findViewById(R.id.txtVwWeatherTemp);
			holder.imgBtnShowMap = (ImageView) row
					.findViewById(R.id.imgBtnShowMap1);
			row.setTag(holder);
		} else {
			holder = (RouteHolder) row.getTag();
		}

		StoreInfo wea = weather.get(position);
		holder.txtVwWeatherTime.setTextColor(Color.GRAY);
		holder.txtVwWeatherTime.setText(wea.getTime(0) );
		holder.TxtVwWeatherDesc.setTextColor(Color.DKGRAY);
		holder.TxtVwWeatherDesc.setText(wea.getCondition(0));
		holder.txtVwWeatherTemp.setText(wea.getTemp(0)+ " F");
	//	holder.txtVwTemp.setText(route.getTemp(0));
		holder.imgBtnShowMap.setBackground(wea.getImg(0));
		return row;
	}

	static class RouteHolder {
		TextView txtVwWeatherTime;
		TextView TxtVwWeatherDesc;
		TextView txtVwWeatherTemp;		
		ImageView imgBtnShowMap;
	}
}
