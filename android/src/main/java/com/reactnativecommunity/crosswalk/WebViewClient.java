package com.reactnativecommunity.crosswalk;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.RequiresApi;

import com.pakdata.xwalk.refactor.XWalkWebResourceRequest;
import com.pakdata.xwalk.refactor.XWalkWebResourceResponse;

public class WebViewClient {

  public void onPageStarted(WebView view,
                            String url,
                            Bitmap favicon) {
  }

  public void onPageFinished(WebView view, String url) {
  }

  public boolean shouldOverrideUrlLoading(WebView view,
                                          String url) {
    return false;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    return this.shouldOverrideUrlLoading(view, request.getUrl().toString());
  }

  public void onReceivedSslError(WebView view,
                                 SslErrorHandler handler,
                                 SslError error) {
  }

  public void onReceivedError(
    WebView webView,
    int errorCode,
    String description,
    String failingUrl) {
  }

  public void onReceivedError(WebView view,
                              WebResourceRequest request,
                              WebResourceError error) {
  }

  public void onReceivedHttpError(
    WebView webView,
    WebResourceRequest request,
    WebResourceResponse errorResponse) {
  }

  @TargetApi(Build.VERSION_CODES.O)
  public boolean onRenderProcessGone(WebView webView, RenderProcessGoneDetail detail) {
    return false;
  }

  public WebResourceResponse shouldInterceptRequest(WebView view,
                                                    WebResourceRequest request) {
    return null;
  }

  public WebResourceResponse shouldInterceptRequest(WebView view,
                                                    String url) {
    return null;
  }

  public XWalkWebResourceResponse shouldInterceptLoadRequest(WebView view,
                                                             XWalkWebResourceRequest request) {
    return null;
  }
}
