package com.example.yweather;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class WeatherProcessing {
	private String WOEID;
	private YahooWeatherInfoListener weatherInfoListener;

	public void queryYahooWeather(Context context, String[] latLong, YahooWeatherInfoListener result) {
		weatherInfoListener = result;
		WeatherQueryTask task = new WeatherQueryTask();
		task.setContext(context);
		task.execute(latLong);
	}

	public String getWeather(Context context, String WOEID){
		String qResult = "";
		String queryString = "http://weather.yahooapis.com/forecastrss?w=" +  WOEID;

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
	
	public StoreInfo parseWeatherInfo(Context context, Document doc){
		StoreInfo storeInfo = new StoreInfo();
		try {
			
			Node titleNode = doc.getElementsByTagName("title").item(0);
			
			if(titleNode.getTextContent().equals("Yahoo! Weather - Error")) {
				return null;
			}			
				
			Node locationNode = doc.getElementsByTagName("yweather:location").item(0);
			
			storeInfo.setCity(locationNode.getAttributes().getNamedItem("city").getNodeValue());			
			storeInfo.setCountry(locationNode.getAttributes().getNamedItem("country").getNodeValue());

			Node atmosphereNode = doc.getElementsByTagName("yweather:atmosphere").item(0);
			storeInfo.setHumidity(atmosphereNode.getAttributes().getNamedItem("humidity").getNodeValue());
					
			Node currentConditionNode = doc.getElementsByTagName("yweather:condition").item(0);

			storeInfo.setmCurrentText(currentConditionNode.getAttributes().getNamedItem("text").getNodeValue());
			storeInfo.setTemperature(Integer.parseInt(currentConditionNode.getAttributes().getNamedItem("temp").getNodeValue()));		
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(context, "Parse XML failed - Unrecognized Tag", Toast.LENGTH_SHORT).show();
			storeInfo = null;
		}		
		return storeInfo;
	}
	
	private class WeatherQueryTask extends AsyncTask<String, Void, StoreInfo>{
		private Context context;
		public void setContext(Context context) {
			
			
			
		}

		@Override
		protected StoreInfo doInBackground(String... latLong) {
			WOEIDProcessor woeidProcessor = new WOEIDProcessor();
			WOEID = woeidProcessor.getWOEID(context, latLong[0], latLong[1]);			
			if(!WOEID.equals("WOEID_NOT_FOUND")) {
				String weather = getWeather(context, WOEID);
				Document weatherDoc = convertStringToDocument(context, weather);
				StoreInfo storeInfo = parseWeatherInfo(context, weatherDoc);
				return storeInfo;
			} else {
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(StoreInfo result) {
			super.onPostExecute(result);
			weatherInfoListener.gotWeatherInfo(result);
		}
		
	}
	
}
