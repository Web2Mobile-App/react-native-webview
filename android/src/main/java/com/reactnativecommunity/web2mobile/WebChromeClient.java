package com.reactnativecommunity.web2mobile;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;

public abstract class WebChromeClient {
  public Bitmap getDefaultVideoPoster() {
    return null;
  }

  public void onShowCustomView(View view, android.webkit.WebChromeClient.CustomViewCallback callback) {
  }

  public void onShowCustomView(View view,
                               int requestedOrientation,
                               android.webkit.WebChromeClient.CustomViewCallback callback) {
  }

  public void onHideCustomView() {
  }

  public boolean onConsoleMessage(ConsoleMessage message) {
    return false;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public void onPermissionRequest(final PermissionRequest request) {
  }

  public void onProgressChanged(WebView webView, int newProgress) {
  }

  public void onGeolocationPermissionsShowPrompt(String origin,
                                                 GeolocationPermissions.Callback callback) {
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public boolean onShowFileChooser(WebView webView,
                                   ValueCallback<Uri[]> filePathCallback,
                                   android.webkit.WebChromeClient.FileChooserParams fileChooserParams) {
    return false;
  }

  public boolean onCreateWindow(android.webkit.WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
    return false;
  }

  public void onCloseWindow(WebView window) {
  }
}
