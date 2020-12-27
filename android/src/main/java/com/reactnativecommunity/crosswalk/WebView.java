package com.reactnativecommunity.crosswalk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.view.ReactViewGroup;
import com.pakdata.xwalk.refactor.CustomViewCallback;
import com.pakdata.xwalk.refactor.XWalkClient;
import com.pakdata.xwalk.refactor.XWalkDownloadListener;
import com.pakdata.xwalk.refactor.XWalkNavigationHandlerImpl;
import com.pakdata.xwalk.refactor.XWalkNavigationHistory;
import com.pakdata.xwalk.refactor.XWalkResourceClient;
import com.pakdata.xwalk.refactor.XWalkSettings;
import com.pakdata.xwalk.refactor.XWalkUIClient;
import com.pakdata.xwalk.refactor.XWalkView;
import com.pakdata.xwalk.refactor.XWalkWebChromeClient;
import com.pakdata.xwalk.refactor.XWalkWebResourceRequest;
import com.pakdata.xwalk.refactor.XWalkWebResourceResponse;
import com.reactnativecommunity.crosswalk.events.TopResourceLoadFinishedEvent;
import com.reactnativecommunity.crosswalk.events.TopResourceLoadStartedEvent;

import org.chromium.components.navigation_interception.NavigationParams;

import java.util.Map;

public class WebView extends FrameLayout {

  private static final String BLANK_URL = "about:blank";

  private WebSettings webSettings;
  private WebViewClient webViewClient;
  private WebChromeClient webChromeClient;

  private boolean useCrosswalk = false;
  private android.webkit.WebView webkitView;
  private XWalkView walkView;

  private boolean ready;
  private String url;
  private Map<String, String> additionalHttpHeaders;
  private String baseUrl;
  private String data;
  private String mimeType;
  private String encoding;
  private String historyUrl;
  private DownloadListener downloadListener;
  private int progress;

  public static void setWebContentsDebuggingEnabled(boolean enabled) {
    WebSettings.setWebContentsDebuggingEnabled(enabled);
  }

  public WebView(@NonNull ThemedReactContext reactContext) {
    super(reactContext);

    webSettings = new WebSettings();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    ThemedReactContext reactContext = (ThemedReactContext) getContext();
    if (useCrosswalk) {
      initializeCrosswalk(reactContext);
    } else {
      initializeWebkit(reactContext);
      onWebkitReady();
    }
  }

  @Override
  public void requestLayout() {
    super.requestLayout();

    updateLayout();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    updateLayout(right - left, bottom - top);
  }

  private void onCreateWalkView(XWalkView walkView) {
    this.walkView = walkView;
    onXWalkReady();
  }

  private void initializeCrosswalk(@NonNull ThemedReactContext reactContext) {
    XWalkManager.getInstance().createInstance(reactContext, new XWalkManager.OnCreateListener() {
      @Override
      public void onCreate(XWalkView walkView) {
        onCreateWalkView(walkView);
      }
    });
  }

  private void initializeWebkit(@NonNull ThemedReactContext reactContext) {
    webkitView = new android.webkit.WebView(reactContext);

    android.webkit.WebSettings webkitSetting = webkitView.getSettings();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      webkitSetting.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    }
    webSettings.updateWebkit(webkitView, webkitSetting);

    final WebView webView = this;
    webkitView.setWebViewClient(new android.webkit.WebViewClient() {
      @Override
      public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        if (webViewClient != null) {
          webViewClient.onPageStarted(webView, url, favicon);
        }
      }

      @Override
      public void onPageFinished(android.webkit.WebView view, String url) {
        super.onPageFinished(view, url);

        if (webViewClient != null) {
          webViewClient.onPageFinished(webView, url);
        }
      }

      @Override
      public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
        if (webViewClient != null) {
          return webViewClient.shouldOverrideUrlLoading(webView, url);
        }
        return super.shouldOverrideUrlLoading(view, url);
      }

      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
      public boolean shouldOverrideUrlLoading(android.webkit.WebView view, WebResourceRequest request) {
        if (webViewClient != null) {
          return webViewClient.shouldOverrideUrlLoading(webView, request);
        }

        return super.shouldOverrideUrlLoading(view, request);
      }

      @Override
      public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);

        if (webViewClient != null) {
          webViewClient.onReceivedSslError(webView, handler, error);
        }
      }

      @Override
      public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        if (webViewClient != null) {
          webViewClient.onReceivedError(webView, request, error);
        }
      }

      @Override
      public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        if (webViewClient != null) {
          webViewClient.onReceivedError(webView, errorCode, description, failingUrl);
        }
      }

      @Override
      public void onReceivedHttpError(android.webkit.WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        if (webViewClient != null) {
          webViewClient.onReceivedHttpError(webView, request, errorResponse);
        }
      }

      @Override
      public boolean onRenderProcessGone(android.webkit.WebView view, RenderProcessGoneDetail detail) {
        if (webViewClient != null) {
          return webViewClient.onRenderProcessGone(webView, detail);
        }

        return super.onRenderProcessGone(view, detail);
      }

      @Nullable
      @Override
      public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, WebResourceRequest request) {
        if (webViewClient != null) {
          return webViewClient.shouldInterceptRequest(webView, request);
        }

        return super.shouldInterceptRequest(view, request);
      }

      @Nullable
      @Override
      public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, String url) {
        if (webViewClient != null) {
          return webViewClient.shouldInterceptRequest(webView, url);
        }

        return super.shouldInterceptRequest(view, url);
      }
    });
    webkitView.setWebChromeClient(new android.webkit.WebChromeClient() {
      @Override
      public void onPermissionRequest(PermissionRequest request) {
        super.onPermissionRequest(request);

        if (webChromeClient != null) {
          webChromeClient.onPermissionRequest(request);
        }
      }

      @Override
      public void onProgressChanged(android.webkit.WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);

        if (webChromeClient != null) {
          webChromeClient.onProgressChanged(webView, newProgress);
        }
      }

      @Override
      public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);

        if (webChromeClient != null) {
          webChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
        }
      }

      @Override
      public boolean onShowFileChooser(android.webkit.WebView view,
                                       ValueCallback<Uri[]> filePathCallback,
                                       FileChooserParams fileChooserParams) {
        if (webChromeClient != null) {
          return webChromeClient.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(view, filePathCallback, fileChooserParams);
      }

      @Override
      public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);

        if (webChromeClient != null) {
          webChromeClient.onShowCustomView(view, new WebChromeClient.CustomViewCallback(callback));
        }
      }

      @Override
      public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        super.onShowCustomView(view, requestedOrientation, callback);

        if (webChromeClient != null) {
          webChromeClient.onShowCustomView(view, requestedOrientation, new WebChromeClient.CustomViewCallback(callback));
        }
      }

      @Override
      public void onHideCustomView() {
        super.onHideCustomView();

        if (webChromeClient != null) {
          webChromeClient.onHideCustomView();
        }
      }

      @Override
      public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (webChromeClient != null) {
          return webChromeClient.onConsoleMessage(consoleMessage);
        }

        return super.onConsoleMessage(consoleMessage);
      }

      @Override
      public boolean onCreateWindow(android.webkit.WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        if (webChromeClient != null) {
          return webChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
      }

      @Override
      public void onCloseWindow(android.webkit.WebView window) {
        super.onCloseWindow(window);

        if (webChromeClient != null) {
          webChromeClient.onCloseWindow(webView);
        }
      }

      @Override
      public boolean onJsAlert(android.webkit.WebView view,
                               String url,
                               String message,
                               JsResult result) {
        AlertUtils.showAlert(getContext(), url, message, null, result);

        return true;
      }

      @Override
      public boolean onJsConfirm(android.webkit.WebView view,
                                 String url,
                                 String message,
                                 JsResult result) {
        AlertUtils.showConfirm(getContext(), url, message, null, result);

        return true;
      }

      @Override
      public boolean onJsPrompt(android.webkit.WebView view,
                                String url,
                                String message,
                                String defaultValue,
                                JsPromptResult result) {
        AlertUtils.showPrompt(getContext(), url, message, defaultValue, null, result);

        return true;
      }
    });

    reloadData();
  }

  private WritableMap createWebViewEvent(String url) {
    WritableMap event = Arguments.createMap();
    event.putDouble("target", getId());
    event.putString("mainDocumentURL", getUrl());
    event.putString("url", url);
    event.putString("title", getTitle());
    event.putBoolean("canGoBack", canGoBack());
    event.putBoolean("canGoForward", canGoForward());
    return event;
  }

  private void dispatchEvent(Event event) {
    ReactContext reactContext = (ReactContext) getContext();
    EventDispatcher eventDispatcher =
      reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
    eventDispatcher.dispatchEvent(event);
  }

  private void reloadData() {
    if (this.baseUrl != null) {
      loadDataWithBaseURL(this.baseUrl, this.data, this.mimeType, this.encoding, this.historyUrl);
    } else if (this.url == null) {
      loadUrl(BLANK_URL);
    } else if (this.additionalHttpHeaders != null) {
      loadUrl(this.url, this.additionalHttpHeaders);
    } else {
      loadUrl(this.url);
    }
  }

  private void updateLayout() {
    updateLayout(getWidth(), getHeight());
  }

  private void updateLayout(int width, int height) {
    updateContentLayout(walkView, width, height);
  }

  private void updateContentLayout(View contentView, int width, int height) {
    if (contentView == null) {
      return;
    }
    contentView.measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    );
    contentView.layout(0, 0, width, height);
  }

  private void onXWalkReady() {
    if (ready) {
      return;
    }
    ready = true;

    ViewGroup.LayoutParams layoutParams
      = new ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    );
    ReactViewGroup reactViewGroup = (ReactViewGroup) getParent();
    int index = reactViewGroup.indexOfChild(this);
    reactViewGroup.addView(walkView, index + 1, layoutParams);

    XWalkSettings walkSettings = walkView.getSettings();
    walkSettings.setLayoutAlgorithm(XWalkSettings.LayoutAlgorithmInternal.TEXT_AUTOSIZING);
    webSettings.updateWalk(walkView, walkSettings);

    final WebView webView = this;
    walkView.setNavigationHandler(new XWalkNavigationHandlerImpl(getContext()) {
      @Override
      public boolean handleNavigation(NavigationParams params) {
        if (webViewClient != null
          && webViewClient.shouldOverrideUrlLoading(webView, params.url)) {
          return true;
        }
        return super.handleNavigation(params);
      }
    });
    walkView.setXWalkWebChromeClient(new XWalkWebChromeClient() {
      @Override
      public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (webChromeClient != null) {
          return webChromeClient.onConsoleMessage(consoleMessage);
        }
        return super.onConsoleMessage(consoleMessage);
      }
    });
    walkView.setXWalkClient(new XWalkClient(walkView) {
      @Override
      public void onLoadResource(XWalkView view, String url) {
        dispatchEvent(
          new TopResourceLoadStartedEvent(
            webView.getId(),
            createWebViewEvent(url)));
        
        super.onLoadResource(view, url);
      }
    });
    walkView.setResourceClient(new XWalkResourceClient() {
      @Override
      public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        if (webViewClient != null) {
          return webViewClient.shouldOverrideUrlLoading(webView, url);
        }
        return super.shouldOverrideUrlLoading(view, url);
      }

      @Override
      public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
        super.onReceivedSslError(view, callback, error);

        if (webViewClient != null) {
          webViewClient.onReceivedSslError(webView, null, error);
        }
      }

      @Override
      public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
        super.onReceivedLoadError(view, errorCode, description, failingUrl);

        if (webViewClient != null) {
          webViewClient.onReceivedError(webView, errorCode, description, failingUrl);
        }
      }

      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
      public void onReceivedResponseHeaders(XWalkView view, XWalkWebResourceRequest request, XWalkWebResourceResponse response) {
        super.onReceivedResponseHeaders(view, request, response);

        if (webViewClient != null && response.getStatusCode() >= 400) {
          WebResourceRequest webResourceRequest = new WebResourceRequest() {
            @Override
            public Uri getUrl() {
              return request.getUrl();
            }

            @Override
            public boolean isForMainFrame() {
              return request.isForMainFrame();
            }

            @Override
            public boolean isRedirect() {
              return false;
            }

            @Override
            public boolean hasGesture() {
              return request.hasGesture();
            }

            @Override
            public String getMethod() {
              return request.getMethod();
            }

            @Override
            public Map<String, String> getRequestHeaders() {
              return request.getRequestHeaders();
            }
          };
          WebResourceResponse webResourceResponse
            = response.getReasonPhrase() != null && response.getReasonPhrase().length() > 0
            ? new WebResourceResponse(response.getMimeType(),
            response.getEncoding(),
            response.getStatusCode(),
            response.getReasonPhrase(),
            response.getResponseHeaders(),
            response.getData())
            : new WebResourceResponse(response.getMimeType(), response.getEncoding(), response.getData());
          webViewClient.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
        }
      }

      @Override
      public void onProgressChanged(XWalkView view, int progressInPercent) {
        super.onProgressChanged(view, progressInPercent);

        progress = progressInPercent;
        if (webChromeClient != null) {
          webChromeClient.onProgressChanged(webView, progressInPercent);
        }
      }

      @Override
      public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
        if (webViewClient != null) {
          return webViewClient.shouldInterceptLoadRequest(webView, request);
        }

        return super.shouldInterceptLoadRequest(view, request);
      }

      @Override
      public void onLoadStarted(XWalkView view,
                                String url) {
        dispatchEvent(
          new TopResourceLoadStartedEvent(
            webView.getId(),
            createWebViewEvent(url)));

        super.onLoadStarted(view, url);
      }

      @Override
      public void onLoadFinished(XWalkView view,
                                 String url) {
        dispatchEvent(
          new TopResourceLoadFinishedEvent(
            webView.getId(),
            createWebViewEvent(url)));

        super.onLoadFinished(view, url);
      }
    });
    walkView.setUIClient(new XWalkUIClient(walkView) {
      @Override
      public void onPageLoadStarted(XWalkView view, String url) {
        super.onPageLoadStarted(view, url);

        if (webViewClient != null) {
          webViewClient.onPageStarted(webView, url, null);
        }
      }

      @Override
      public void onPageLoadStopped(XWalkView view, String url, LoadStatusInternal status) {
        super.onPageLoadStopped(view, url, status);

        if (webViewClient != null) {
          webViewClient.onPageFinished(webView, url);
        }
      }

      @Override
      public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);

        if (webChromeClient != null) {
          webChromeClient.onShowCustomView(view, new WebChromeClient.CustomViewCallback(callback));
        }
      }

      @Override
      public void onHideCustomView() {
        super.onHideCustomView();

        if (webChromeClient != null) {
          webChromeClient.onHideCustomView();
        }
      }

      @Override
      public boolean onConsoleMessage(XWalkView view, String message, int lineNumber, String sourceId, ConsoleMessageType messageType) {
        if (webChromeClient != null) {
          ConsoleMessage.MessageLevel messageLevel = ConsoleMessage.MessageLevel.DEBUG;
          switch (messageType) {
            case DEBUG: {
              messageLevel = ConsoleMessage.MessageLevel.DEBUG;
              break;
            }
            case ERROR: {
              messageLevel = ConsoleMessage.MessageLevel.ERROR;
              break;
            }
            case LOG: {
              messageLevel = ConsoleMessage.MessageLevel.LOG;
              break;
            }
            case INFO: {
              messageLevel = ConsoleMessage.MessageLevel.TIP;
              break;
            }
            case WARNING: {
              messageLevel = ConsoleMessage.MessageLevel.WARNING;
              break;
            }
          }
          ConsoleMessage consoleMessage
            = new ConsoleMessage(message, sourceId, lineNumber, messageLevel);
          return webChromeClient.onConsoleMessage(consoleMessage);
        }

        return super.onConsoleMessage(view, message, lineNumber, sourceId, messageType);
      }

      @Override
      public boolean onCreateWindowRequested(XWalkView view, InitiateByInternal initiator, ValueCallback<XWalkView> callback) {
        if (webChromeClient != null) {
          return webChromeClient.onCreateWindowRequested(view, initiator, callback);
        }
        return super.onCreateWindowRequested(view, initiator, callback);
      }

      @Override
      public void onJavascriptCloseWindow(XWalkView view) {
        super.onJavascriptCloseWindow(view);

        if (webChromeClient != null) {
          webChromeClient.onCloseWindow(webView);
        }
      }
    });

    walkView.setDownloadListener(new XWalkDownloadListener(getContext()) {
      @Override
      public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (downloadListener != null) {
          downloadListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
        }
      }
    });

    onResume();
    requestLayout();
    reloadData();
  }

  private void onWebkitReady() {
    if (ready) {
      return;
    }
    ready = true;

    FrameLayout.LayoutParams layoutParams
      = new FrameLayout.LayoutParams(
      LayoutParams.MATCH_PARENT,
      LayoutParams.MATCH_PARENT
    );
    addView(webkitView, layoutParams);

    onResume();
    requestLayout();
    reloadData();
  }

  public WebSettings getSettings() {
    return webSettings;
  }

  public int getProgress() {
    return progress;
  }

  public void setWebViewClient(WebViewClient client) {
    this.webViewClient = client;
  }

  public void setWebChromeClient(WebChromeClient client) {
    this.webChromeClient = client;
  }

  public void setUseCrosswalk(boolean useCrosswalk) {
    this.useCrosswalk = useCrosswalk;
  }

  public void setDownloadListener(final DownloadListener listener) {
    this.downloadListener = listener;
  }

  public String getTitle() {
    if (!ready) {
      return null;
    }
    if (walkView != null) {
      return walkView.getTitle();
    }
    if (webkitView != null) {
      return webkitView.getTitle();
    }
    return null;
  }

  public String getUrl() {
    if (!ready) {
      return null;
    }
    if (walkView != null) {
      return walkView.getUrl();
    }
    if (webkitView != null) {
      return webkitView.getUrl();
    }
    return null;
  }

  public void onResume() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.resumeTimers();
      walkView.onShow();
    } else if (webkitView != null) {
      webkitView.resumeTimers();
    }
  }

  public void onPause() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.pauseTimers();
      walkView.onHide();
    } else if (webkitView != null) {
      webkitView.pauseTimers();
    }
  }

  public void onDestroy() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.onDestroy();
    }
  }

  public boolean canGoBack() {
    if (!ready) {
      return false;
    }
    if (walkView != null) {
      return walkView.getNavigationHistory() != null
        && walkView.getNavigationHistory().canGoBack();
    } else if (webkitView != null) {
      return webkitView.canGoBack();
    }
    return false;
  }

  public boolean canGoForward() {
    if (!ready) {
      return false;
    }
    if (walkView != null) {
      return walkView.getNavigationHistory() != null
        && walkView.getNavigationHistory().canGoForward();
    } else if (webkitView != null) {
      return webkitView.canGoForward();
    }
    return false;
  }

  public void clearHistory() {
    if (!ready) {
      return;
    }
    if (walkView != null
      && walkView.getNavigationHistory() != null) {
      walkView.getNavigationHistory().clear();
    } else if (webkitView != null) {
      webkitView.clearHistory();
    }
  }

  public void clearCache(boolean includeDiskFiles) {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.clearCache(includeDiskFiles);
    } else if (webkitView != null) {
      webkitView.clearCache(includeDiskFiles);
    }
  }

  public void clearFormData() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.clearFormData();
    } else if (webkitView != null) {
      webkitView.clearFormData();
    }
  }

  public void loadDataWithBaseURL(String baseUrl,
                                  String data,
                                  String mimeType,
                                  String encoding,
                                  String historyUrl) {
    this.baseUrl = baseUrl;
    this.data = data;
    this.mimeType = mimeType;
    this.encoding = encoding;
    this.historyUrl = historyUrl;
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    } else if (webkitView != null) {
      webkitView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }
  }

  public void postUrl(String url,
                      byte[] postData) {
    if (webkitView != null) {
      webkitView.postUrl(url, postData);
    }
  }

  public void loadUrl(String url,
                      Map<String, String> additionalHttpHeaders) {
    this.url = url;
    this.additionalHttpHeaders = additionalHttpHeaders;
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.loadUrl(url, additionalHttpHeaders);
    } else if (webkitView != null) {
      webkitView.loadUrl(url, additionalHttpHeaders);
    }
  }

  public void loadUrl(String url) {
    this.url = url;
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.loadUrl(url);
    } else if (webkitView != null) {
      webkitView.loadUrl(url);
    }
  }

  public void goBack() {
    if (!ready) {
      return;
    }
    if (walkView != null && walkView.getNavigationHistory() == null) {
      walkView.getNavigationHistory().navigate(XWalkNavigationHistory.DirectionInternal.BACKWARD, 1);
    } else if (webkitView != null) {
      webkitView.goBack();
    }
  }

  public void goForward() {
    if (!ready) {
      return;
    }
    if (walkView != null && walkView.getNavigationHistory() == null) {
      walkView.getNavigationHistory().navigate(XWalkNavigationHistory.DirectionInternal.FORWARD, 1);
    } else if (webkitView != null) {
      webkitView.goForward();
    }
  }

  public void reload() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.reload(XWalkView.RELOAD_NORMAL);
    } else if (webkitView != null) {
      webkitView.reload();
    }
  }

  public void stopLoading() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.stopLoading();
    } else if (webkitView != null) {
      webkitView.stopLoading();
    }
  }

  @SuppressLint("JavascriptInterface")
  public void addJavascriptInterface(Object object,
                                     String name) {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.addJavascriptInterface(object, name);
    } else if (webkitView != null) {
      webkitView.addJavascriptInterface(object, name);
    }
  }

  public void removeJavascriptInterface(String name) {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.removeJavascriptInterface(name);
    } else if (webkitView != null) {
      webkitView.removeJavascriptInterface(name);
    }
  }

  public void evaluateJavascript(String script,
                                 ValueCallback<String> resultCallback) {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.evaluateJavascript(script, resultCallback);
    } else if (webkitView != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        webkitView.evaluateJavascript(script, resultCallback);
      }
    }
  }

  public void destroy() {
    if (!ready) {
      return;
    }
    if (walkView != null) {
      walkView.onDestroy();
    }
  }

  private static class XWalkActivityEventListener extends BaseActivityEventListener {
    private final XWalkView walkView;

    public XWalkActivityEventListener(XWalkView walkView) {
      this.walkView = walkView;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
      walkView.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      walkView.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
      walkView.onNewIntent(intent);
    }
  }
}
