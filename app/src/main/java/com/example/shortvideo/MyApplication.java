package com.example.shortvideo;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map config = new HashMap();
        config.put("cloud_name", "dgmlgi7p4");
        config.put("api_key", "628256499685731");
        config.put("api_secret", "uWiZf7yK-HjxcHpghszH8tzI2P0");
        MediaManager.init(this, config);
    }
}
