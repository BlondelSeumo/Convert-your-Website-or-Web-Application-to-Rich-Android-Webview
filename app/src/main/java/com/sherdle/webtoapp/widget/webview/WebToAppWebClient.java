package com.sherdle.webtoapp.widget.webview;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import androidx.appcompat.app.AlertDialog;

import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sherdle.webtoapp.Config;
import com.sherdle.webtoapp.R;
import com.sherdle.webtoapp.fragment.WebFragment;
import com.sherdle.webtoapp.widget.AdvancedWebView;

public class WebToAppWebClient extends WebViewClient {

    WebFragment fragment;
    WebView browser;

    public WebToAppWebClient(WebFragment fragment, WebView browser)
    {
        super();
        this.fragment = fragment;
        this.browser = browser;
    }

    @TargetApi(android.os.Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (urlShouldOpenExternally(url)) {
            // Load new URL Don't override URL Link
            try {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch(ActivityNotFoundException e) {
                if (url.startsWith("intent://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("intent://", "http://"))));
                } else {
                    Toast.makeText(fragment.getActivity(), fragment.getActivity().getResources().getString(R.string.no_app_message), Toast.LENGTH_LONG).show();
                }
            }

            return true;
        } else if (url.endsWith(".mp4") || url.endsWith(".avi")
                || url.endsWith(".flv")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/mp4");
                view.getContext().startActivity(intent);
            } catch (Exception e) {
                // error
            }

            return true;
        } else if (url.endsWith(".mp3") || url.endsWith(".wav")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "audio/mp3");
                view.getContext().startActivity(intent);
            } catch (Exception e) {
                // error
            }

            return true;
        }

        // Return true to override url loading (In this case do
        // nothing).
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //
    }

    @TargetApi(android.os.Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
        // Redirect to deprecated method, so you can use it in all SDK versions
        onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setMessage(R.string.notification_error_ssl_cert_invalid);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    // handeling errors
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        if (hasConnectivity("", false)
                && !failingUrl.equals(((AdvancedWebView) view).lastDownloadUrl)) {
            //If an error occurred while we had connectivity, the page must be borken
            fragment.showErrorScreen(fragment.getActivity().getString(R.string.error));
        } else {
            //If we don't have connectivity, and this isn't an online page, let hasConnectivity handle the error
            if (!failingUrl.startsWith("file:///android_asset")) {
                hasConnectivity("", true);
            }
        }
    }

    /**
     * Check if we need an internet connection to load an url, and if we a connection, if it is present
     * @param loadUrl The url we are trying to load
     * @param showDialog If a dialog should be shown if a connection is required, but not found
     * @return If we can load the url (based on the fact if we need an connection for it, and if the connection, if needed, is present0
     */
    public boolean hasConnectivity(String loadUrl, boolean showDialog) {
        boolean enabled = true;

        if (loadUrl.startsWith("file:///android_asset")){
            return true;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) fragment.getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() || !info.isAvailable())) {

            enabled = false;

            if (showDialog){
                if (Config.NO_CONNECTION_PAGE.length() > 0 && Config.NO_CONNECTION_PAGE.startsWith("file:///android_asset")) {
                    browser.loadUrl(Config.NO_CONNECTION_PAGE);
                } else {
                    fragment.showErrorScreen(fragment.getActivity().getString(R.string.no_connection));
                }
            }
        }
        return enabled;
    }

    /**
     * Check if an url should load externally and not in the WebView
     * @param url The url that we would like to load
     * @return If it should be loaded inside or outside the WebView
     */
    public static boolean urlShouldOpenExternally(String url){

        /*
         * If there is a set of urls defined that may only open inside the WebView and
         * the passed url does not match to one of these urls, it should be opened outside the WebView
         */
        if (Config.OPEN_ALL_OUTSIDE_EXCEPT.length > 0) {
            for (String pattern : Config.OPEN_ALL_OUTSIDE_EXCEPT) {
                if (!url.contains(pattern))
                    return true;
            }
        }

        /*
         * If there is an url defined that should open outside the WebView, these urls will be loaded outside the webview
         */
        for (String pattern : Config.OPEN_OUTSIDE_WEBVIEW){
            if (url.contains(pattern))
                return true;
        }

        return false;
    }
}