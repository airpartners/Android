package com.example.airpartners;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    public static class WebViewErrorDialogFragment extends DialogFragment {

        private int errorCode;
        private String errorDescription;
        private String failingUrl;

        public static WebViewErrorDialogFragment newInstance(int errorCode, String description,
                                                             String failingUrl) {
            Bundle args = new Bundle();
            args.putInt("errorCode", errorCode);
            args.putString("errorDescription", description);
            args.putString("failingUrl", failingUrl);

            WebViewErrorDialogFragment frag = new WebViewErrorDialogFragment();
            frag.setArguments(args);
            return frag;
        }

        private void readArgs(Bundle args) {
            if (args != null) {
                errorCode = args.getInt("errorCode");
                errorDescription = args.getString("errorDescription");
                failingUrl = args.getString("failingUrl");
            }
        }

        private String buildMessage() {
            return (getString(R.string.webview_error_text)
                    + "\n\n" + getString(R.string.error_code) + " " + Integer.toString(errorCode)
                    + "\n" + getString(R.string.failing_url) + " " + failingUrl
                    + "\n" + getString(R.string.error_description) + " " + errorDescription);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            readArgs(getArguments());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(buildMessage());
            return builder.create();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        // prevent external links from opening in webview
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if ("airpartners-ade.web.app".equals(Uri.parse(url).getHost())) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            showWebViewErrorDialog(errorCode, description, failingUrl);
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
            // Redirect to deprecated method
            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());

        webView.loadUrl("https://airpartners-ade.web.app/?lang=" + Locale.getDefault().toString());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void showWebViewErrorDialog(int errorCode, String description,
                                        String failingUrl) {
        FragmentManager fm = getSupportFragmentManager();
        WebViewErrorDialogFragment dialogFragment =
                WebViewErrorDialogFragment.newInstance(errorCode, description, failingUrl);
        dialogFragment.show(fm, "webview_error");
    }
}