//
//package com.radiorunt.facebook;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageButton;
//
//import com.facebook.android.AsyncFacebookRunner;
//import com.radiorunt.R;
//import com.radiorunt.activities.LogInActivity;
//import com.radiorunt.facebook.BaseRequestListener;
//import com.facebook.android.DialogError;
//import com.facebook.android.Facebook;
//import com.facebook.android.FacebookError;
//import com.radiorunt.facebook.SessionEvents;
//import com.radiorunt.facebook.SessionStore;
//import com.facebook.android.Facebook.DialogListener;
//import com.radiorunt.facebook.SessionEvents.AuthListener;
//import com.radiorunt.facebook.SessionEvents.LogoutListener;
//import com.radiorunt.utilities.RalleeApp;
//import com.radiorunt.utilities.Settings;
//
//public class LoginButton extends ImageButton {
//
//    private Facebook mFb;
//    private Handler mHandler;
////    private SessionListener mSessionListener = new SessionListener();
//    private String[] mPermissions;
//    private Activity mActivity;
//    private int mActivityCode;
//   
//    Handler loginHandler;
//
//    public LoginButton(Context context) {
//        super(context);
//    }
//
//    public LoginButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public LoginButton(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//    }
//
//    public void init(final Activity activity, final int activityCode, final Facebook fb, Context context, Handler handler) {
//        init(activity, activityCode, fb, new String[] {}, context, handler);
//    }
//
//    public void init(final Activity activity, final int activityCode, final Facebook fb,
//            final String[] permissions, Context context, Handler handler) {
//        mActivity = activity;
//        mActivityCode = activityCode;
//        mFb = fb;
//        mPermissions = permissions;
//        mHandler = new Handler();
//        loginHandler = handler;
//        Settings settings = new Settings(context);
//        setBackgroundColor(Color.TRANSPARENT);
////      setImageResource(fb.isSessionValid() ? R.drawable.logout_button  : R.drawable.login_button);
//        setImageResource(R.drawable.login_button);
//        
//        drawableStateChanged();
//
////        SessionEvents.addAuthListener(mSessionListener);
////        SessionEvents.addLogoutListener(mSessionListener);
//        String exitAndLogin = settings.getExitAndLogInSettings("NOT LogIn");
//        
//        if(exitAndLogin.equals("LogIn")){
//        	if (RalleeApp.getInstance().getFacebook().isSessionValid()) {
//            	loginHandler.sendEmptyMessage(LogInActivity.MSG_REQ_USERDATA);
//            }else{
//
//        		Log.i("fbAuth", "Session is NOT valid...authorizing");
//            	mFb.authorize(mActivity, mPermissions, mActivityCode, new LoginDialogListener());
//            }
//        }
//        setOnClickListener(new ButtonOnClickListener());
//    }
//
//    private final class ButtonOnClickListener implements OnClickListener {
//        /*
//         * Source Tag: login_tag
//         */
//        @Override
//        public void onClick(View arg0) {
//	        if (RalleeApp.getInstance().getFacebook().isSessionValid()) {
//	//        	fbLoginButton.setEnabled(false);
//	    		Log.i("fbAuth", "Session is valid");
//	            loginHandler.sendEmptyMessage(LogInActivity.MSG_REQ_USERDATA);
//	        }else{
//	
//	    		Log.i("fbAuth", "Session is NOT valid...authorizing");
//	        	mFb.authorize(mActivity, mPermissions, mActivityCode, new LoginDialogListener());
//	        }
//        }
//    }
//    
//    public void initIntent(final LogInActivity activity, final int activityCode, final Facebook fb,
//            final String[] permissions, Context context, Handler handler) {
//        mActivity = activity;
//        mActivityCode = activityCode;
//        mFb = fb;
//        mPermissions = permissions;
//        mHandler = new Handler();
//        loginHandler = handler;
//
//        setBackgroundColor(Color.TRANSPARENT);
////      setImageResource(fb.isSessionValid() ? R.drawable.logout_button  : R.drawable.login_button);
//        setImageResource(R.drawable.login_button);
//        
//        drawableStateChanged();
//
////        SessionEvents.addAuthListener(mSessionListener);
////        SessionEvents.addLogoutListener(mSessionListener);
//        Settings settings = new Settings(context);
//        String exitAndLogin = settings.getExitAndLogInSettings("NOT LogIn");
//        if(exitAndLogin.equals("LogIn")){
//        	if (RalleeApp.getInstance().getFacebook().isSessionValid()) {
//            	loginHandler.sendEmptyMessage(LogInActivity.MSG_REQ_USERDATA);
//            }else{
//
//        		Log.i("fbAuth", "Session is NOT valid...authorizing");
//            	mFb.authorize(mActivity, mPermissions, mActivityCode, new LoginDialogListener());
//            }
//        }
//	        if (RalleeApp.getInstance().getFacebook().isSessionValid()) {
//		//        	fbLoginButton.setEnabled(false);
//		    		Log.i("fbAuth", "Session is valid");
//		            loginHandler.sendEmptyMessage(LogInActivity.MSG_REQ_USERDATA);
//		        }else{
//		
//		    		Log.i("fbAuth", "Session is NOT valid...authorizing");
//		        	mFb.authorize(mActivity, mPermissions, mActivityCode, new LoginDialogListener());
//		        }
//    }
//    
//    public final class LogOutFBAccount {
//        /*
//         * Source Tag: login_tag
//         */
//        public void logOut() {
//            if (mFb.isSessionValid()) {
//                SessionEvents.onLogoutBegin();
//                AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
//                asyncRunner.logout(getContext(), new LogoutRequestListener());
//            }
//        }
//    }
//
//    private final class LoginDialogListener implements DialogListener {
//        @Override
//        public void onComplete(Bundle values) {
//        	Log.d("FB", "LoginDialogListener ");
//            SessionEvents.onLoginSuccess();
//        }
//
//        @Override
//        public void onFacebookError(FacebookError error) {
//            SessionEvents.onLoginError(error.getMessage());
//            Log.d("FB", "LoginDialogListener " + error);
//        }
//
//        @Override
//        public void onError(DialogError error) {
//            SessionEvents.onLoginError(error.getMessage());
//            Log.d("FB", "LoginDialogListener " + error);
//        }
//
//        @Override
//        public void onCancel() {
//            SessionEvents.onLoginError("Action Canceled");
//        }
//    }
//
//    private class LogoutRequestListener extends BaseRequestListener {
//        @Override
//        public void onComplete(String response, final Object state) {
//            /*
//             * callback should be run in the original thread, not the background
//             * thread
//             */
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    SessionEvents.onLogoutFinish();
//                }
//            });
//        }
//    }
//
////    private class SessionListener implements AuthListener, LogoutListener {
////
////        @Override
////        public void onAuthSucceed() {
//////            setImageResource(R.drawable.logout_button);
////            SessionStore.save(mFb, getContext());
////        }
////
////        @Override
////        public void onAuthFail(String error) {
////        }
////
////        @Override
////        public void onLogoutBegin() {
////        }
////
////        @Override
////        public void onLogoutFinish() {
////            SessionStore.clear(getContext());
////            setImageResource(R.drawable.login_button);
////        }
////    }
//
// }
