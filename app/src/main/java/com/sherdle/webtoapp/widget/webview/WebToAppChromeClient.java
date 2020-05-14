package com.sherdle.webtoapp.widget.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sherdle.webtoapp.Config;
import com.sherdle.webtoapp.R;
import com.sherdle.webtoapp.activity.MainActivity;
import com.sherdle.webtoapp.fragment.WebFragment;
import com.sherdle.webtoapp.widget.AdvancedWebView;

/**
 * Created by imac on 05-04-16.
 */
public class WebToAppChromeClient extends WebChromeClient {

    protected WebFragment fragment;
    protected FrameLayout container;
    protected WebView popupView;

    protected AdvancedWebView browser;
    public SwipeRefreshLayout swipeLayout;
    public ProgressBar progressBar;

    public View mCustomView;
    public WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalSystemUiVisibility;
    private int mOriginalOrientation;

    public WebToAppChromeClient(
            WebFragment fragment,
            FrameLayout container,
            AdvancedWebView browser,
            SwipeRefreshLayout swipeLayout,
            ProgressBar progressBar)
    {
        super();
        this.fragment = fragment;
        this.container = container;
        this.browser = browser;
        this.swipeLayout = swipeLayout;
        this.progressBar = progressBar;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

        this.browser.setVisibility(WebView.GONE);

        this.popupView = new WebView(this.fragment.getActivity());

        // setup popuview and add
        this.popupView.getSettings().setJavaScriptEnabled(true);
        this.popupView.setWebChromeClient(this);
        this.popupView.setWebViewClient(new WebToAppWebClient(this.fragment, popupView));
        this.popupView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT
        ));
        this.container.addView(this.popupView);

        // send popup window infos back to main (cross-document messaging)
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(popupView);
        resultMsg.sendToTarget();

        return true;
    }

    // remove new added webview on close
    @Override
    public void onCloseWindow(WebView window) {
        this.popupView.setVisibility(WebView.GONE);
        this.browser.setVisibility(WebView.VISIBLE);
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        if (Config.LOAD_AS_PULL && swipeLayout != null){
            swipeLayout.setRefreshing(true);
            if (progress == 100)
                swipeLayout.setRefreshing(false);
        } else {
            progressBar.setProgress(0);

            progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(progress);

            progressBar.incrementProgressBy(progress);

            if (progress > 99) {
                progressBar.setVisibility(View.GONE);

                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
            }

            if (progress > 80){
                try {
                    ((MainActivity) fragment.getActivity()).hideSplash();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            request.grant(request.getResources());
        }
    }

    // Setting the title
    @Override
    public void onReceivedTitle(WebView view, String title) {
        ((MainActivity) fragment.getActivity()).setTitle(browser.getTitle());
    }

    @SuppressWarnings("unused")
    @Override
    public Bitmap getDefaultVideoPoster() {
        if (this.fragment.getActivity()== null) {
            return null;
        }

        return BitmapFactory.decodeResource(fragment.getActivity()
                        .getApplicationContext().getResources(),
                R.drawable.vert_loading);
    }

    @SuppressLint("InlinedApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onShowCustomView(View view,
                                 WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            onHideCustomView();
            return;
        }

        // 1. Stash the current state
        mCustomView = view;
        mCustomView.setBackgroundColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOriginalSystemUiVisibility = fragment.getActivity().getWindow()
                    .getDecorView().getSystemUiVisibility();
        }
        mOriginalOrientation = fragment.getActivity().getRequestedOrientation();

        // 2. Stash the custom view callback
        mCustomViewCallback = callback;

        // 3. Add the custom view to the view hierarchy
        FrameLayout decor = (FrameLayout) fragment.getActivity().getWindow()
                .getDecorView();
        decor.addView(mCustomView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 4. Change the state of the window
        fragment.getActivity()
                .getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        fragment.getActivity()
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onHideCustomView() {
        // 1. Remove the custom view
        FrameLayout decor = (FrameLayout) fragment.getActivity().getWindow()
                .getDecorView();
        decor.removeView(mCustomView);
        mCustomView = null;

        // 2. Restore the state to it's original form
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            fragment.getActivity().getWindow().getDecorView()
                    .setSystemUiVisibility(mOriginalSystemUiVisibility);
        }
        fragment.getActivity().setRequestedOrientation(mOriginalOrientation);

        // 3. Call the custom view callback
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;

    }

    public View getCustomView(){
        return mCustomView;
    }

    public CustomViewCallback getCustomViewCallback(){
        return mCustomViewCallback;
    }

}