package com.example.wsbiking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class NormalLogin extends Activity implements android.view.View.OnClickListener {

	private static final String TAG = "NORMALLOGIN";
	EditText etUser, etPass;
	//Button bLogin;
	ImageButton bLogin;
	
	//strings to save username and password
	String username, password;
	
	//http client as form container
	HttpClient httpclient;
	
	//use http post method
	HttpPost httppost;
	
	//create arraylist for input data
	ArrayList<NameValuePair> nameValuePairs;
	
	//create htpp response and entity
	HttpResponse response;
	HttpEntity entity;
	
	ProgressDialog progressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_normal_login);
		progressbar = new ProgressDialog(NormalLogin.this);
		initialize();
	}

	private void initialize() {
		// TODO Auto-generated method stub
		etUser = (EditText) findViewById(R.id.etUser);
		etPass = (EditText) findViewById(R.id.etPass);
		bLogin = (ImageButton) findViewById(R.id.bSubmit);
		
		etUser.setSingleLine();
		Log.i(TAG,"inside initialize");
		bLogin.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.normal_login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		httpclient = new DefaultHttpClient();
		
		//create a new http post with url to php file as param
		//httppost = new HttpPost("http://10.0.2.2/android/index.php");
		httppost = new HttpPost("http://152.46.19.183/android/index.php");
		Log.i(TAG,"connection done " + httppost.toString());
				
		//assign input text to strings
		username = etUser.getText().toString();
		password = etPass.getText().toString();
			
		if(username.isEmpty() || password.isEmpty()){
			Toast toast = Toast.makeText(getBaseContext(), "Fields cannot be empty !", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
		else
		{
			username = md5hash(username);
			password = md5hash(password);
			
			Log.i(TAG,"encry is "+md5hash(username));
			
			//ProgressDialog.show(NormalLogin.this, "Loading", "Connecting to server...");
			
			progressbar.setTitle("Please Wait");
			progressbar.setMessage("Connecting to Server...");
			progressbar.show();
			
			new longOperation().execute("");
			//progressbar.dismiss();
			Log.i(TAG, "hello" + Main.isLogin);
		}
	}
	
	public void callHome()
	{
		Intent intent = new Intent(this,RecordActivity.class);
		intent.putExtra("com.login.username", username);
		progressbar.dismiss();
		startActivity(intent);
	}
	
	 private static String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
	         */
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();
	 
	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return sb.toString();
	    }//End of convertStreamToString
	 
	 private class longOperation extends AsyncTask<String, Void, Long>
	 {

			@Override
			protected Long doInBackground(String... params) {
				// TODO Auto-generated method stub
				long result = -1;
				try {
					
					//create new arraylist
					nameValuePairs = new ArrayList<NameValuePair>();
					
					//place them in arraylist
					nameValuePairs.add(new BasicNameValuePair("username", username));
					nameValuePairs.add(new BasicNameValuePair("password", password));
					
					Log.i(TAG,"inside try");
					//add array to http post
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					Log.i(TAG,"url");
					//assign executed  from container to response
					response = httpclient.execute(httppost);
					
					Log.i(TAG,"response");
					//need to check if status code is 200
					if(response.getStatusLine().getStatusCode() == 200)
					{
					
						Log.i(TAG,"inside status loop");
						
						
						//assign response entity to http entity
						entity = response.getEntity();
						
						//check if entity is non null
						if(entity != null) {
							
							//create a new input stream with received data
							InputStream instream = entity.getContent();
							
							//create a new json object and assign converted data as params
							JSONObject jsonresponse = new JSONObject(convertStreamToString(instream));
							
							//assign json responses to local var
							String retUser = jsonresponse.getString("user"); //mysql field
							String retPass = jsonresponse.getString("pass");
							
							//validate login credentials
							if(username.equals(retUser) && password.equals(retPass)) {
							//if(username.equals("pratik") && password.equals("qwerty")) {
								
								//create a new shared preference  by getting the preference
								//give the shared preference nay name you like
								SharedPreferences sp = getSharedPreferences("logindetails", 0);
															
								//edit shared preference
								SharedPreferences.Editor spedit  = sp.edit();
								
								//put login details as strings
								spedit.putString("user", username);
								spedit.putString("pass", password);
								
								Main.logged_user = username;
								//close the editor
								spedit.commit();
								Log.i(TAG,"before toast");
								//display toast
								//Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_SHORT).show();
								result = 0;
								
							} else {
								//Display message
								//Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
								result = 1;
							}
						}
							
					}
					
				} catch(Exception e) {
					Log.i(TAG,e.toString());
					if(e.toString().contains("HttpHostConnectException") || e.toString().contains("HostException")) {
						result = 2;
					}
					if(e.toString().contains("End of input at")) {
						result = 1;
					}
				}
				return result;
			}

			protected void onPostExecute(Long result)
			{
				progressbar.dismiss();
				switch(result.intValue()) {
				case 0:
					Toast toast = Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					Main.isLogin = true;
					callHome();
					break;
				case 1:
					toast =Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					Main.isLogin = false;
					etPass.setText("");
					break;
				case 2:
					toast = Toast.makeText(getBaseContext(), "Cannot connect to server! Please check network connectivity", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					Main.isLogin = false;
					break;
				}
				
			}
	 }
	 
/*		@Override
		public void onBackPressed() {
			Main.isLogin = false;
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
	            session.closeAndClearTokenInformation();
	        }
			callMain();
		
		}*/
		public void callMain()
		{
			Intent intent = new Intent(this,Main.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		
		public static String md5hash(String message){
	        String digest = null;
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            byte[] hash = md.digest(message.getBytes("UTF-8"));
	           
	           
	            //converting byte array to Hexadecimal String
	           StringBuilder sb = new StringBuilder(2*hash.length);
	           for(byte b : hash){
	               sb.append(String.format("%02x", b&0xff));
	           }
	          
	           digest = sb.toString();
	          
	        } catch (UnsupportedEncodingException ex) {
	            Log.i(TAG,"UnsupportedEncodingException "+ex.toString());
	        } catch (NoSuchAlgorithmException ex) {
	            Log.i(TAG,"NoSuchAlgorithmException "+ex.toString());
	        }
	        return digest;
	    }
	
}
