package com.sparc.frjvcapp;

import android.content.SharedPreferences;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewPage extends AppCompatActivity {
    public static final String data = "data";
    SharedPreferences shared;
    String uid, divid, fbid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        WebView myWebView = findViewById(R.id.webview);
        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        myWebView.setScrollbarFadingEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        shared = getApplicationContext().getSharedPreferences(data, MODE_PRIVATE);
        divid = shared.getString("fbdivcode", "0");
        fbid = shared.getString("fbcode", "0");
        uid = shared.getString("userid", "0");
        myWebView.loadUrl("https://odishaforestlandsurvey.co.in/ORSACCertificate/IndexFBWebView?fbid="+fbid+"&&uid="+uid+"&&divId="+divid+"");
       // myWebView.loadUrl("https://odishaforestlandsurvey.co.in/ORSACCertificate/IndexFBWebView?fbid=1202&&uid=DGATH15&&divId=2");
    }
}
