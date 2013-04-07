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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.widget.Toast;

public class WOEIDProcessor {

	private final String yahooapisBase = "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.placefinder%20where%20text=";
	private final String yahooapisFormat = "&format=xml";
	private String yahooAPIsQuery;
	
	public String getWOEID(Context context, String lat, String longitude){
		yahooAPIsQuery = yahooapisBase + "%22" + lat + "," + longitude +"%22 AND gflags=%22R%22" + yahooapisFormat;
		yahooAPIsQuery = yahooAPIsQuery.replace(" ", "%20");
		String WOEID = getFromYahoo(context, yahooAPIsQuery);
		Document doc = convertToDoc(context, WOEID);
		return parseDoc(context, doc);
	}

	private String getFromYahoo(Context context, String query) {
		String result = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(query);
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
				result = stringBuilder.toString();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
					.show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
					.show();
		}
		return result;
	}
	
	private Document convertToDoc(Context context, String doc) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;
		Document result = null;
		try {
			parser = dbFactory.newDocumentBuilder();
			result = parser.parse(new ByteArrayInputStream(doc.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			Toast.makeText(context, e1.toString(), Toast.LENGTH_LONG)
					.show();
		} catch (SAXException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
					.show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
					.show();
		}
		return result;
	}

	private String parseDoc(Context context, Document doc) {
		try {
			NodeList nodeListDescription = doc.getElementsByTagName("woeid");
			if (nodeListDescription.getLength() > 0) {
				Node node = nodeListDescription.item(0);
				return node.getTextContent();
			} else {
				return "WOEID not found";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "WOEID not found";
		}	
	}
	
	
	
}
