package com.example.shortvideo;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_OTP = "otp";

    private static final String KEY_IMG = "img";


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(String userId, String email, String img, String username) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USERNAME,username);
        editor.putString(KEY_IMG,img);
        editor.apply();
    }

    public void saveToken (String customerToken){
        editor.putString(KEY_TOKEN,customerToken);
        editor.apply();
    }

    public void saveOTP (String otp){
        editor.putString(KEY_OTP,otp);
        editor.apply();
    }

    public void saveEmail(String email){
        editor.putString(KEY_EMAIL,email);
        editor.apply();
    }
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
    public String getCustomerToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    public String getOTP() {
        return sharedPreferences.getString(KEY_OTP, null);
    }
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getImg() {
        return sharedPreferences.getString(KEY_IMG, null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null ;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}

