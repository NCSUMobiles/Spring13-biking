package com.example.wsbiking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class SignUp extends Activity implements OnClickListener {

	private static final String TAG = "SIGNUP";
	EditText username, password, cpassword;
	ImageButton signup;
	
	//strings to save username and password
	String uname, passwd, cpasswd;
	
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
		setContentView(R.layout.activity_sign_up);
		
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		cpassword = (EditText) findViewById(R.id.cpassword);
		signup = (ImageButton) findViewById(R.id.signup);
		
		username.setSingleLine();
		progressbar = new ProgressDialog(SignUp.this);
		
		signup.setOnClickListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		httpclient = new DefaultHttpClient();
		
		//create a new http post with url to php file as param
		httppost = new HttpPost("http://152.46.19.183/android/signup.php");
		
		Log.i(TAG,"connection done");
		//assign input text to strings
		uname = username.getText().toString();
		passwd = password.getText().toString();
		cpasswd = cpassword.getText().toString();
		
		if(uname.isEmpty() || passwd.isEmpty() || cpasswd.isEmpty()) {
			Toast toast = Toast.makeText(getBaseContext(), "Fields cannot be empty !", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
		else {
			if(passwd.equals(cpasswd)) {
				
				uname = md5hash(uname);
				passwd = md5hash(passwd);
				
				Log.i(TAG,"signup else "+uname+" "+passwd);
				
				progressbar.setTitle("Please Wait");
				progressbar.setMessage("Connecting to server...");
				progressbar.show();
				new longOperation().execute("");
				//progressbar.dismiss();
				
			} else {
				Toast toast = Toast.makeText(getBaseContext(), "Passwords do not match !", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
				password.setText("");
				cpassword.setText("");
			}
		}
		
	}
	
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
					nameValuePairs.add(new BasicNameValuePair("uname", uname));
					nameValuePairs.add(new BasicNameValuePair("passwd", passwd));
					
					Log.i(TAG,"signup else "+uname+" "+passwd);
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
							if(jsonresponse.has("ERROR")) {
								String error = jsonresponse.getString("ERROR");
								Log.i(TAG, error.toString());	
								if(error.contains("Duplicate")) {
									result = 3;
								}
							} else {
													
								String retUser = jsonresponse.getString("user"); //mysql field
								String retPass = jsonresponse.getString("pass");
							
								Log.i(TAG,"Json else "+retUser+" "+retPass);
								
								//validate login credentials
								if(uname.equals(retUser) && passwd.equals(retPass)) {
									result = 0;
								
								} else {
									//Display message
									//Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
									result = 1;
								}
							}
						}
							
					}
					
				} catch(Exception e) {
					Log.i(TAG,e.toString());
					if(e.toString().contains("HttpHostConnectException") || e.toString().contains("HostException")) {
						result = 2;
					}

				}
				return result;
			}

			protected void onPostExecute(Long result)
			{
				progressbar.dismiss();
				switch(result.intValue()) {
				case 0:
					Toast toast = Toast.makeText(getBaseContext(), "SignUp successful. Please login", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					callMain();
					break;
				case 1:
					toast = Toast.makeText(getBaseContext(), "Something went wrong! Please enter again", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					break;
				case 2:
					toast = Toast.makeText(getBaseContext(), "Cannot connect to server! Please check network connectivity", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					break;
				case 3:
					toast =Toast.makeText(getBaseContext(), "Username "+ username.getText()+" already exists! Please choose a different one", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
					toast.show();
					username.setText("");
					break;
				}
				
			}
	 }

		public void callMain()
		{
			Intent intent = new Intent(this,Main.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	/*	@Override
		public void onBackPressed() {
			Main.isLogin = false;
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
	            session.closeAndClearTokenInformation();
	        }
			callMain();
		
		}*/
}
