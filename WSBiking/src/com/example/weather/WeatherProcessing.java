package com.example.weather;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

public class WeatherProcessing {
	private WeatherInfoListener weatherInfoListener;
	private boolean callFlag;

	public void queryYahooWeather(Context context, String[] latLong,
			boolean flag, WeatherInfoListener result) {
		weatherInfoListener = result;
		callFlag = flag;
		WeatherQueryTask task = new WeatherQueryTask();
		task.setContext(context);
		task.execute(latLong);
	}

	public String getWeather(Context context, String[] latlong) {
		String qResult = "";
		String queryString = "http://api.wunderground.com/api/950884302095eee4/hourly/q/"
				+ latlong[0] + "," + latlong[1] + ".xml";

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}

		return qResult;
	}

	private Document convertStringToDocument(Context context, String src) {
		Document dest = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			Toast.makeText(context, e1.toString(), Toast.LENGTH_LONG).show();
		} catch (SAXException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}

		return dest;
	}

	public StoreInfo parseWeatherInfo(Context context, Document doc) {
		StoreInfo storeInfo = new StoreInfo();
		try {

			int i = 0;
			Node node1, node2;
			NodeList nodeList;

			for (i = 0; i < 5; i++) {
				node1 = doc.getElementsByTagName("forecast").item(i);
				nodeList = node1.getChildNodes();
				node2 = nodeList.item(1);
				storeInfo.setTime(node2.getChildNodes().item(2)
						.getTextContent()
						+ ":" + node2.getChildNodes().item(3).getTextContent(),
						i); // Time
				node2 = nodeList.item(3);
				storeInfo.setTemp(node2.getChildNodes().item(1)
						.getTextContent(), i); // Temperature
				node2 = nodeList.item(7);
				storeInfo.setCondition(node2.getTextContent(), i); // Condition
				node2 = nodeList.item(11);
				storeInfo.setImgURL(node2.getTextContent(), i);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(context, "Parse XML failed - Unrecognized Tag",
					Toast.LENGTH_SHORT).show();
			storeInfo = null;
		}
		return storeInfo;
	}

	private class WeatherQueryTask extends AsyncTask<String, Void, StoreInfo> {
		private Context context;

		public void setContext(Context context) {
		}

		@Override
		protected StoreInfo doInBackground(String... latLong) {
			String weather = getWeather(context, latLong);
			Document weatherDoc = convertStringToDocument(context, weather);
			StoreInfo storeInfo = parseWeatherInfo(context, weatherDoc);
			for (int i = 0; i <= 4; i++) {
				try {
					storeInfo.setImg(Drawable.createFromStream(
							((java.io.InputStream) new java.net.URL(storeInfo
									.getImageURL(i)).getContent()), ""), i);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return storeInfo;
		}

		@Override
		protected void onPostExecute(StoreInfo result) {
			super.onPostExecute(result);

			if (callFlag)
				weatherInfoListener.gotWeatherInfo(result);
			else
				weatherInfoListener.wetherDataSet(result);
		}

	}

}
