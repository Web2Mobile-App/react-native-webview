package com.reactnativecommunity.crosswalk;

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

import com.pakdata.xwalk.refactor.XWalkUIClient;
import com.pakdata.xwalk.refactor.XWalkView;

public abstract class WebChromeClient {

  public static class CustomViewCallback implements com.pakdata.xwalk.refactor.CustomViewCallback {
    private com.pakdata.xwalk.refactor.CustomViewCallback walkCustomViewCallback;
    private android.webkit.WebChromeClient.CustomViewCallback webkitCustomViewCallback;

    public CustomViewCallback(com.pakdata.xwalk.refactor.CustomViewCallback walkCustomViewCallback) {
      this.walkCustomViewCallback = walkCustomViewCallback;
    }

    public CustomViewCallback(android.webkit.WebChromeClient.CustomViewCallback webkitCustomViewCallback) {
      this.webkitCustomViewCallback = webkitCustomViewCallback;
    }

    @Override
    public void onCustomViewHidden() {
      if (walkCustomViewCallback != null) {
        walkCustomViewCallback.onCustomViewHidden();
      } else if (webkitCustomViewCallback != null) {
        webkitCustomViewCallback.onCustomViewHidden();
      }
    }
  }

  public Bitmap getDefaultVideoPoster() {
    return null;
  }

  public void onShowCustomView(View view, CustomViewCallback callback) {
  }

  public void onShowCustomView(View view,
                               int requestedOrientation,
                               CustomViewCallback callback) {
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

  public boolean onCreateWindowRequested(XWalkView view, XWalkUIClient.InitiateByInternal initiator, ValueCallback<XWalkView> callback) {
    return false;
  }

  public void onCloseWindow(WebView window) {
  }
}
