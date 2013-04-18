package com.example.wsbiking;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class Main extends Activity {

	private ImageButton FLogin;
	private ImageButton NLogin;
	private ImageButton signUp;
	
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	public static boolean isLogin = false;
	public static String logged_user;
	private static final String TAG = "MAIN";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		NLogin = (ImageButton)findViewById(R.id.NLogin);
		FLogin = (ImageButton)findViewById(R.id.FLogin);
		signUp = (ImageButton) findViewById(R.id.SignUp);
		//anon = (Button) findViewById(R.id.Anonymous);
		
		
		NLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, NormalLogin.class);
                startActivity(intent);
            }
        });
		
		signUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Main.this, SignUp.class);
				startActivity(intent);
			}
		});
		
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		
		Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
            	Log.i(TAG,"in if session not null");
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
            	Log.i(TAG,"in if session null");
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
            	Log.i(TAG,"in if session create token loaded");
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
        
        updateView();
        
		/*FLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, FacebookLogin.class);
                startActivity(intent);
            }
        });*/
	}

    private void updateView() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
            //FLogin.setText(R.string.logout);
        	Log.i(TAG,"inside update view if");
            NLogin.setEnabled(false);
            /*FLogin.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });*/
            callHome();
        } else {
            //FLogin.setText(R.string.login);
        	Log.i(TAG,"inside update view else");
            NLogin.setEnabled(true);
            FLogin.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin();  }
            });
        }
       
    }
    
    private void onClickLogin() {
    	//FLogin.setBackgroundResource(R.drawable.login_down);
        Session session = Session.getActiveSession();
        Log.i(TAG,"inside onclickLogin");
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG,"in on activity result");
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        Session session1 = Session.getActiveSession();
        if (session1.isOpened()) {
        	Log.i(TAG,"in on activity result session opened");
        	callHome();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
        Log.i(TAG,"in on save instance state");
    }
    
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	Log.i(TAG,"in session status callback");
            updateView();
        }
    }
    
	public void callHome()
	{
		Intent intent = new Intent(this,RecordActivity.class);
		
		startActivity(intent);
	}
	
}
