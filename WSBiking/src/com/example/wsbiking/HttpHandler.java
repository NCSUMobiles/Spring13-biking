package com.example.wsbiking;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;


import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * @author Leon Dmello
 * All the server interaction code should be in this class file.
 * 
 */
public class HttpHandler extends AsyncTask <Void, Void, Void>{

	private static final int SECS = 10;
	
	@SuppressWarnings("null")
	@Override
	protected Void doInBackground(Void... params) {
		  
		//HttpParams httpparams = new BasicHttpParams();
		
		//HttpConnectionParams.setConnectionTimeout(httpparams, 3000);
		
		//HttpConnectionParams.setSoTimeout(httpparams, 10000);
		
		HttpResponse response;
		
        //HttpClient httpClient = new DefaultHttpClient(httpparams);
		
		HttpClient httpClient = new DefaultHttpClient();
            
   		//HttpParams httpParameters = null;
        //JSONObject responses = new JSONObject();
        
        DatabaseHandler dbHandler = null;
        
        dbHandler = DatabaseHandler.getInstance(null);
        
   		//int timeoutConnection = 10000, timeoutSocket = 10000;   		
   		       
   		ArrayList<Route> routeList = new ArrayList<Route>();
        
        HttpPost request = new HttpPost("http://152.46.16.223:2001/BikingService/Service1.svc/SaveRoute");

        request.setHeader("Content-type", "text/plain");  
        
              
        try{
        	JSONObject routeJSON;
        	JSONArray jsonarray = new JSONArray();

        	Route tempRoute;

        	routeList = dbHandler.getUnsyncedRoutes();
        	// routeList.add(route[0]);

        	for (int i = 0; i < routeList.size(); i++) {

        	tempRoute = routeList.get(i);
        	routeJSON = new JSONObject();

        	routeJSON.put("description", tempRoute.getDescription());
        	routeJSON.put("distance", tempRoute.getDistance());
        	routeJSON.put("endtime", tempRoute.getEndTime());
        	routeJSON.put("routeid", tempRoute.getID());
        	routeJSON.put("speed", tempRoute.getSpeed());
        	routeJSON.put("starttime", tempRoute.getStartTime());
        	routeJSON.put("title", tempRoute.getTitle());
        	routeJSON.put("username", "rjagird");
        	routeJSON.put("weatherinfo", "Sunny day!");
        	routeJSON.put("pointCollection", new JSONArray(tempRoute.getPointsJSON()));

        	jsonarray.put(routeJSON);
        	}

        	routeJSON = new JSONObject();
        	routeJSON.put("routeCollection", jsonarray);

        	StringEntity entity = new StringEntity(routeJSON.toString());        	
        	
        	       		
        	request.setEntity(entity);   
        	
        		
         	response = httpClient.execute(request);
        	       		
        	InputStream ips  = response.getEntity().getContent();
            BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));
                
            StringBuilder sb = new StringBuilder();
            String s;
                
            while(true)
            {
                 s = buf.readLine();
                 if(s==null || s.length()==0)
                   break;
                 sb.append(s);
             }
                
              buf.close();
              ips.close();               
        		
        		        		
        		JSONTokener tokener = new JSONTokener( sb.toString() );
        	    JSONArray array = new JSONArray( tokener );
        		
        	    int [] routesToBeMarked = new int[array.length()];
        	    
        		for(int j = 0; j < array.length(); j++)
        		{
        			routesToBeMarked[j] = array.getJSONObject(j).getInt("routeid");
        		}
        		        		       		       		
        		dbHandler.markRoutesSynced(routesToBeMarked);
        		
        		// Set timeout in milliseconds until connection is established
              	//HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
         	   
            	//Set the default socket timeout 
            	//HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        		
        		           
        }
        catch (Exception ex) {
            Log.e("HTTP error", ex.getMessage());
        }
		return null;
	}
	

}
