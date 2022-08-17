package com.reactnativecommunity.web2mobile;

import android.annotation.SuppressLint;
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
import android.webkit.WebStorage;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.build.ReactBuildConfig;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.view.ReactViewGroup;

import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.StorageController;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebRequestError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebView extends FrameLayout implements
  WebExtension.MessageDelegate, WebExtension.PortDelegate {
  private static final String LOG_TAG = WebView.class.getCanonicalName();

  private static final String BLANK_URL = "about:blank";

  // gecko constants
  private static final String WEB_EXTENSION_DOCUMENT_IDLE_ID
    = "{00000000-0000-0000-0000-000000000000}";
  private static final String WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP
    = "web2mobile_document_idle";
  private static final String WEB_EXTENSION_DOCUMENT_START_URI
    = "resource://android/assets/Web2Mobile/start/";
  private static final String WEB_EXTENSION_DOCUMENT_START_ID
    = "{11111111-1111-1111-1111-111111111111}";
  private static final String WEB_EXTENSION_DOCUMENT_START_NATIVE_APP
    = "web2mobile_document_start";
  private static final String WEB_EXTENSION_DOCUMENT_END_URI
    = "resource://android/assets/Web2Mobile/end/";
  private static final String WEB_EXTENSION_DOCUMENT_END_ID
    = "{22222222-2222-2222-2222-222222222222}";
  private static final String WEB_EXTENSION_DOCUMENT_END_NATIVE_APP
    = "web2mobile_document_end";
  private static final String WEB_EXTENSION_DOCUMENT_IDLE_URI
    = "resource://android/assets/Web2Mobile/idle/";
  private static final String WEB_EXTENSION_BLOCKER_ID
    = "{33333333-3333-3333-3333-333333333333}";
  private static final String WEB_EXTENSION_BLOCKER_NATIVE_APP
    = "web2mobile_blocker";
  private static final String WEB_EXTENSION_BLOCKER_URI
    = "resource://android/assets/Web2Mobile/blocker/";

  private static GeckoRuntime geckoRuntime;

  private WebSettings webSettings;
  private WebViewClient webViewClient;
  private WebChromeClient webChromeClient;

  private boolean useGecko = true;
  private android.webkit.WebView webkitView;
  private GeckoSession geckoSession;
  private GeckoView geckoView;

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
  private Map<String, Object> javascriptInterfaceLookup;
  private Map<String, ValueCallback<String>> javascriptLookup;

  // gecko variables
  private Map<String, List<WebExtension.Port>> portsLookup = new HashMap<>();
  private String geckoTitle;
  private String geckoUrl;
  private boolean geckoCanGoBack = false;
  private boolean geckoCanGoForward = false;
  protected @Nullable
  String injectedJavascriptRunAtDocumentEnd;
  private boolean injectedJavascriptRunAtDocumentEndForMainFrameOnly = true;

  protected @Nullable
  String injectedJS;
  protected boolean injectedJavaScriptForMainFrameOnly = true;
  protected @Nullable
  String injectedJSBeforeContentLoaded;
  private boolean injectedJavaScriptBeforeContentLoadedForMainFrameOnly = true;

  public static void setWebContentsDebuggingEnabled(boolean enabled) {
    WebSettings.setWebContentsDebuggingEnabled(enabled);
  }

  public WebView(@NonNull ThemedReactContext reactContext) {
    super(reactContext);

    webSettings = new WebSettings();
    javascriptInterfaceLookup = new HashMap<>();
    javascriptLookup = new HashMap<>();
  }

  @Override
  public void requestLayout() {
    super.requestLayout();

    updateLayout();
  }

  @Override
  public void onConnect(@NonNull WebExtension.Port port) {
    if (ReactBuildConfig.DEBUG) {
      FLog.d(LOG_TAG, "on connect web extension port: " + port.name);
    }

    port.setDelegate(this);

    List<WebExtension.Port> ports = portsLookup.get(port.name);
    if (ports == null) {
      ports = new ArrayList<>();
      portsLookup.put(port.name, ports);
    }
    ports.add(port);

    switch (port.name) {
      case WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP:
        if (!injectedJavaScriptForMainFrameOnly
          || port.sender.isTopLevel()) {
          injectJavascript(port, injectedJS);
        }
        break;
      case WEB_EXTENSION_DOCUMENT_START_NATIVE_APP:
        if (!injectedJavaScriptBeforeContentLoadedForMainFrameOnly
          || port.sender.isTopLevel()) {
          injectJavascript(port, injectedJSBeforeContentLoaded);
        }
        break;
      case WEB_EXTENSION_DOCUMENT_END_NATIVE_APP:
        if (!injectedJavascriptRunAtDocumentEndForMainFrameOnly
          || port.sender.isTopLevel()) {
          injectJavascript(port, injectedJavascriptRunAtDocumentEnd);
        }
        break;
    }
  }

  @Override
  public void onDisconnect(@NonNull WebExtension.Port port) {
    if (ReactBuildConfig.DEBUG) {
      FLog.d(LOG_TAG, "on disconnect web extension port: " + port.name);
    }
    List<WebExtension.Port> ports = portsLookup.get(port.name);
    if (ports != null) {
      ports.remove(port);
    }
  }

  @Override
  public GeckoResult<Object> onMessage(@NonNull String nativeApp,
                                       @NonNull Object message,
                                       @NonNull WebExtension.MessageSender sender) {
    if (ReactBuildConfig.DEBUG) {
      FLog.d(LOG_TAG, String.format("on message: %s from native app: %s with sender: %s",
        message, nativeApp, sender.url));
    }
    if (nativeApp.equals(WEB_EXTENSION_BLOCKER_NATIVE_APP)) {
      try {
        JSONObject jsonObject = (JSONObject) message;
        String url = jsonObject.getString("url");
        return GeckoResult.fromValue(shouldBlockUrl(url));
      } catch (Exception ex) {
        return GeckoResult.fromException(ex);
      }
    }
    onMessage(message instanceof String ? (String) message : message.toString());
    return null;
  }

  @Override
  public void onPortMessage(@NonNull Object message, @NonNull WebExtension.Port port) {
    if (ReactBuildConfig.DEBUG) {
      FLog.d(LOG_TAG, String.format("on port message: %s from port: %s with sender: %s",
        message, port.name, port.sender.url));
    }
    onMessage(message instanceof String ? (String) message : message.toString());
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    ThemedReactContext reactContext = (ThemedReactContext) getContext();
    if (useGecko) {
      initializeGecko(reactContext);
      onGeckoReady();
    } else {
      initializeWebkit(reactContext);
      onWebkitReady();
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    updateLayout(right - left, bottom - top);
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

  public boolean isUseGecko() {
    return useGecko;
  }

  public void setUseGecko(boolean useGecko) {
    this.useGecko = useGecko;
  }

  public void setInjectedJavaScript(@Nullable String js) {
    this.injectedJS = js;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP,
      this.injectedJavaScriptForMainFrameOnly,
      this.injectedJS
    );
  }

  public void setInjectedJavaScriptForMainFrameOnly(boolean injectedJavaScriptForMainFrameOnly) {
    this.injectedJavaScriptForMainFrameOnly = injectedJavaScriptForMainFrameOnly;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP,
      this.injectedJavaScriptForMainFrameOnly,
      this.injectedJS
    );
  }

  public void setInjectedJSBeforeContentLoaded(@Nullable String injectedJSBeforeContentLoaded) {
    this.injectedJSBeforeContentLoaded = injectedJSBeforeContentLoaded;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_START_NATIVE_APP,
      this.injectedJavaScriptBeforeContentLoadedForMainFrameOnly,
      this.injectedJSBeforeContentLoaded
    );
  }

  public void setInjectedJavaScriptBeforeContentLoadedForMainFrameOnly(boolean injectedJavaScriptBeforeContentLoadedForMainFrameOnly) {
    this.injectedJavaScriptBeforeContentLoadedForMainFrameOnly = injectedJavaScriptBeforeContentLoadedForMainFrameOnly;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_START_NATIVE_APP,
      this.injectedJavaScriptBeforeContentLoadedForMainFrameOnly,
      this.injectedJSBeforeContentLoaded
    );
  }

  public void setInjectedJavascriptRunAtDocumentEnd(String injectedJavascriptRunAtDocumentEnd) {
    if (areStringsEqual(this.injectedJavascriptRunAtDocumentEnd, injectedJavascriptRunAtDocumentEnd)) {
      return;
    }
    this.injectedJavascriptRunAtDocumentEnd = injectedJavascriptRunAtDocumentEnd;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_END_NATIVE_APP,
      this.injectedJavascriptRunAtDocumentEndForMainFrameOnly,
      this.injectedJavascriptRunAtDocumentEnd
    );
  }

  public void setInjectedJavascriptRunAtDocumentEndForMainFrameOnly(boolean injectedJavascriptRunAtDocumentEndForMainFrameOnly) {
    if (this.injectedJavascriptRunAtDocumentEndForMainFrameOnly == injectedJavascriptRunAtDocumentEndForMainFrameOnly) {
      return;
    }
    this.injectedJavascriptRunAtDocumentEndForMainFrameOnly = injectedJavascriptRunAtDocumentEndForMainFrameOnly;
    injectJavascript(
      WEB_EXTENSION_DOCUMENT_END_NATIVE_APP,
      this.injectedJavascriptRunAtDocumentEndForMainFrameOnly,
      this.injectedJavascriptRunAtDocumentEnd
    );
  }

  public void setDownloadListener(final DownloadListener listener) {
    this.downloadListener = listener;
  }

  public String getTitle() {
    if (!ready) {
      return null;
    }
    if (geckoView != null) {
      return geckoTitle;
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
    if (geckoView != null) {
      return geckoUrl;
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
    if (webkitView != null) {
      webkitView.resumeTimers();
    }

    /*AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
    } else {
      manager.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }*/
  }

  public void onPause() {
    if (!ready) {
      return;
    }
    if (webkitView != null) {
      webkitView.pauseTimers();
    }

    /*AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
    } else {
      manager.setStreamMute(AudioManager.STREAM_MUSIC, true);
    }*/
  }

  public boolean canGoBack() {
    if (!ready) {
      return false;
    }
    if (geckoView != null) {
      return geckoCanGoBack;
    }
    if (webkitView != null) {
      return webkitView.canGoBack();
    }
    return false;
  }

  public boolean canGoForward() {
    if (!ready) {
      return false;
    }
    if (geckoView != null) {
      return geckoCanGoForward;
    }
    if (webkitView != null) {
      return webkitView.canGoForward();
    }
    return false;
  }

  public void clearHistory() {
    if (!ready) {
      return;
    }

    if (geckoView != null) {
      geckoSession.purgeHistory();
    }
    if (webkitView != null) {
      webkitView.clearHistory();
    }
  }

  public void clearCache(boolean includeDiskFiles) {
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      geckoSession.reload(GeckoSession.LOAD_FLAGS_BYPASS_CACHE);
    }
    if (webkitView != null) {
      webkitView.clearCache(includeDiskFiles);
    }
  }

  public void clearFormData() {
    if (!ready) {
      return;
    }
    if (webkitView != null) {
      webkitView.clearFormData();
    }
  }

  public void reset() {
    if (!ready) {
      return;
    }
    webSettings.clearCookies();
    if (webkitView != null) {
      WebStorage.getInstance().deleteAllData();
      webkitView.clearCache(true);
      webkitView.clearFormData();
      webkitView.clearHistory();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        webkitView.evaluateJavascript("localStorage.clear();sessionStorage.clear();", new ValueCallback<String>() {
          @Override
          public void onReceiveValue(String value) {
            webkitView.reload();
          }
        });
      }
    } else if (geckoRuntime != null) {
      geckoRuntime.getStorageController().clearData(StorageController.ClearFlags.ALL);
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
    if (geckoSession != null) {
      GeckoSession.Loader request
        = new GeckoSession.Loader()
        .data(data, mimeType);
      geckoSession.load(request);
    }
    if (webkitView != null) {
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
    if (geckoView != null) {
      GeckoSession.Loader request
        = new GeckoSession.Loader()
        .uri(url);
      if (additionalHttpHeaders == null) {
        additionalHttpHeaders = new HashMap<>();
      }
      if (additionalHttpHeaders.containsKey("referer")
        || additionalHttpHeaders.containsKey("Referer")) {
        String referer
          = additionalHttpHeaders.containsKey("referer")
          ? additionalHttpHeaders.get("referer")
          : additionalHttpHeaders.get("Referer");
        request.referrer(referer);
        additionalHttpHeaders.remove("referer");
        additionalHttpHeaders.remove("Referer");
      }
      request.additionalHeaders(additionalHttpHeaders);
      geckoSession.load(request);
    }
    if (webkitView != null) {
      webkitView.loadUrl(url, additionalHttpHeaders);
    }
  }

  public void loadUrl(String url) {
    this.url = url;
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      GeckoSession.Loader request
        = new GeckoSession.Loader()
        .uri(url);
      geckoSession.load(request);
    }
    if (webkitView != null) {
      webkitView.loadUrl(url);
    }
  }

  public void goBack() {
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      geckoSession.goBack();
    } else if (webkitView != null) {
      webkitView.goBack();
    }
  }

  public void goForward() {
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      geckoSession.goForward();
    } else if (webkitView != null) {
      webkitView.goForward();
    }
  }

  public void reload() {
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      geckoSession.reload();
    } else if (webkitView != null) {
      webkitView.reload();
    }
  }

  public void stopLoading() {
    if (!ready) {
      return;
    }
    if (geckoView != null) {
      geckoSession.stop();
    } else if (webkitView != null) {
      webkitView.stopLoading();
    }
  }

  @SuppressLint("JavascriptInterface")
  public void addJavascriptInterface(Object object,
                                     String name) {
    if (!ready) {
      javascriptInterfaceLookup.put(name, object);
      return;
    }
    if (webkitView != null) {
      webkitView.addJavascriptInterface(object, name);
    }
  }

  public void removeJavascriptInterface(String name) {
    if (!ready) {
      if (javascriptInterfaceLookup.containsKey(name)) {
        javascriptInterfaceLookup.remove(name);
      }
      return;
    }
    if (webkitView != null) {
      webkitView.removeJavascriptInterface(name);
    }
  }

  public void evaluateJavascript(String script,
                                 ValueCallback<String> resultCallback) {
    if (!ready) {
      javascriptLookup.put(script, resultCallback);
      return;
    }
    if (geckoView != null) {
      injectJavascript(
        WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP,
        true,
        script
      );
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
  }

  public void onMessage(String message) {
  }

  public boolean shouldBlockUrl(String url) {
    return false;
  }

  private void initializeGecko(@NonNull ThemedReactContext reactContext) {
    if (geckoRuntime == null) {
      GeckoRuntimeSettings settings
        = new GeckoRuntimeSettings.Builder()
        .build();
      geckoRuntime = GeckoRuntime.create(reactContext, settings);
    }

    geckoView = new GeckoView(reactContext);

    geckoSession = new GeckoSession();
    geckoSession.open(geckoRuntime);
    geckoView.setSession(geckoSession);

    addExtension(
      WEB_EXTENSION_DOCUMENT_IDLE_URI,
      WEB_EXTENSION_DOCUMENT_IDLE_ID,
      WEB_EXTENSION_DOCUMENT_IDLE_NATIVE_APP
    );
    addExtension(
      WEB_EXTENSION_DOCUMENT_START_URI,
      WEB_EXTENSION_DOCUMENT_START_ID,
      WEB_EXTENSION_DOCUMENT_START_NATIVE_APP
    );
    addExtension(
      WEB_EXTENSION_DOCUMENT_END_URI,
      WEB_EXTENSION_DOCUMENT_END_ID,
      WEB_EXTENSION_DOCUMENT_END_NATIVE_APP
    );
    addExtension(
      WEB_EXTENSION_BLOCKER_URI,
      WEB_EXTENSION_BLOCKER_ID,
      WEB_EXTENSION_BLOCKER_NATIVE_APP
    );

    geckoSession.setContentDelegate(new GeckoSession.ContentDelegate() {
      @Override
      public void onTitleChange(@NonNull GeckoSession session, @Nullable String title) {
        WebView.this.geckoTitle = title;
      }

      @Override
      public void onCrash(@NonNull GeckoSession session) {
        if (webViewClient != null) {
          webViewClient.onCrash(WebView.this);
        }
      }

      @Override
      public void onKill(@NonNull GeckoSession session) {
        WebView.this.reloadData();
      }
    });
    geckoSession.setProgressDelegate(new GeckoSession.ProgressDelegate() {
      @Override
      public void onPageStart(@NonNull GeckoSession session, @NonNull String url) {
        if (webViewClient != null) {
          webViewClient.onPageStarted(WebView.this, url, null);
        }
      }

      @Override
      public void onPageStop(@NonNull GeckoSession session, boolean success) {
        if (webViewClient != null) {
          webViewClient.onPageFinished(WebView.this, url);
        }
      }

      @Override
      public void onProgressChange(@NonNull GeckoSession session, int progress) {
        if (webChromeClient != null) {
          webChromeClient.onProgressChanged(WebView.this, progress);
        }
        WebView.this.progress = progress;
      }
    });
    geckoSession.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
      @Override
      public void onLocationChange(@NonNull GeckoSession session,
                                   @Nullable String url) {
        WebView.this.geckoUrl = url;
      }

      @Override
      public void onLocationChange(@NonNull GeckoSession session,
                                   @Nullable String url,
                                   @NonNull List<GeckoSession.PermissionDelegate.ContentPermission> perms) {
        WebView.this.geckoUrl = url;
      }

      @Override
      public GeckoResult<AllowOrDeny> onLoadRequest(@NonNull GeckoSession session,
                                                    @NonNull LoadRequest request) {
        if (webViewClient != null
          && webViewClient.shouldOverrideUrlLoading(WebView.this, request.uri)) {
          return GeckoResult.deny();
        }
        return GeckoSession.NavigationDelegate.super.onLoadRequest(session, request);
      }

      @Override
      public GeckoResult<String> onLoadError(@NonNull GeckoSession session,
                                             @Nullable String uri,
                                             @NonNull WebRequestError error) {
        if (webViewClient != null) {
          webViewClient.onReceivedError(WebView.this, error.code, error.getMessage(), uri);
        }
        return null;
      }

      @Override
      public void onCanGoBack(@NonNull GeckoSession session, boolean canGoBack) {
        WebView.this.geckoCanGoBack = canGoBack;
      }

      @Override
      public void onCanGoForward(@NonNull GeckoSession session, boolean canGoForward) {
        WebView.this.geckoCanGoForward = canGoForward;
      }
    });
    geckoSession.setPromptDelegate(new GeckoSession.PromptDelegate() {
      @Override
      public GeckoResult<PromptResponse> onAlertPrompt(@NonNull GeckoSession session,
                                                       @NonNull AlertPrompt prompt) {
        GeckoResult<PromptResponse> response = new GeckoResult<>();
        AlertUtils.showAlert(
          getContext(),
          prompt.message,
          prompt,
          response,
          null
        );
        return response;
      }

      @Override
      public GeckoResult<PromptResponse> onButtonPrompt(@NonNull GeckoSession session,
                                                        @NonNull ButtonPrompt prompt) {
        GeckoResult<PromptResponse> response = new GeckoResult<>();
        AlertUtils.showConfirm(
          getContext(),
          prompt.message,
          prompt,
          response,
          null
        );
        return response;
      }

      @Override
      public GeckoResult<PromptResponse> onTextPrompt(@NonNull GeckoSession session,
                                                      @NonNull TextPrompt prompt) {
        GeckoResult<PromptResponse> response = new GeckoResult<>();
        AlertUtils.showTextPrompt(
          getContext(),
          prompt.message,
          prompt.defaultValue,
          prompt,
          response,
          null
        );
        return response;
      }
    });
  }

  private void onGeckoReady() {
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
    reactViewGroup.addView(geckoView, index + 1, layoutParams);

    webSettings.updateGecko(geckoView, geckoRuntime.getSettings());

    onResume();
    requestLayout();
    reloadData();
    addPendingJavascriptInterfaces();
    evaluatePendingJavascripts();
  }

  private void initializeWebkit(@NonNull ThemedReactContext reactContext) {
    webkitView = new android.webkit.WebView(reactContext);

    android.webkit.WebSettings webkitSetting = webkitView.getSettings();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      webkitSetting.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    }
    webSettings.updateWebkit(webkitView, webkitSetting);

    webkitView.setWebViewClient(new android.webkit.WebViewClient() {
      @Override
      public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        if (webViewClient != null) {
          webViewClient.onPageStarted(WebView.this, url, favicon);
        }
      }

      @Override
      public void onPageFinished(android.webkit.WebView view, String url) {
        super.onPageFinished(view, url);

        if (webViewClient != null) {
          webViewClient.onPageFinished(WebView.this, url);
        }
      }

      @Override
      public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
        if (webViewClient != null) {
          return webViewClient.shouldOverrideUrlLoading(WebView.this, url);
        }
        return super.shouldOverrideUrlLoading(view, url);
      }

      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
      public boolean shouldOverrideUrlLoading(android.webkit.WebView view, WebResourceRequest request) {
        if (webViewClient != null) {
          return webViewClient.shouldOverrideUrlLoading(WebView.this, request);
        }

        return super.shouldOverrideUrlLoading(view, request);
      }

      @Override
      public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);

        if (webViewClient != null) {
          webViewClient.onReceivedSslError(WebView.this, handler, error);
        }
      }

      @Override
      public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        if (webViewClient != null) {
          webViewClient.onReceivedError(WebView.this, request, error);
        }
      }

      @Override
      public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        if (webViewClient != null) {
          webViewClient.onReceivedError(WebView.this, errorCode, description, failingUrl);
        }
      }

      @Override
      public void onReceivedHttpError(android.webkit.WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        if (webViewClient != null) {
          webViewClient.onReceivedHttpError(WebView.this, request, errorResponse);
        }
      }

      @Override
      public boolean onRenderProcessGone(android.webkit.WebView view, RenderProcessGoneDetail detail) {
        if (webViewClient != null) {
          return webViewClient.onRenderProcessGone(WebView.this, detail);
        }

        return super.onRenderProcessGone(view, detail);
      }

      @Nullable
      @Override
      public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, WebResourceRequest request) {
        if (webViewClient != null) {
          return webViewClient.shouldInterceptRequest(WebView.this, request);
        }

        return super.shouldInterceptRequest(view, request);
      }

      @Nullable
      @Override
      public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view, String url) {
        if (webViewClient != null) {
          return webViewClient.shouldInterceptRequest(WebView.this, url);
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
          webChromeClient.onProgressChanged(WebView.this, newProgress);
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
          return webChromeClient.onShowFileChooser(WebView.this, filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(view, filePathCallback, fileChooserParams);
      }

      @Override
      public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);

        if (webChromeClient != null) {
          webChromeClient.onShowCustomView(view, callback);
        }
      }

      @Override
      public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        super.onShowCustomView(view, requestedOrientation, callback);

        if (webChromeClient != null) {
          webChromeClient.onShowCustomView(view, requestedOrientation, callback);
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
          webChromeClient.onCloseWindow(WebView.this);
        }
      }

      @Override
      public boolean onJsAlert(android.webkit.WebView view,
                               String url,
                               String message,
                               JsResult result) {
        AlertUtils.showAlert(
          getContext(),
          message,
          null,
          null,
          result
        );

        return true;
      }

      @Override
      public boolean onJsConfirm(android.webkit.WebView view,
                                 String url,
                                 String message,
                                 JsResult result) {
        AlertUtils.showConfirm(
          getContext(),
          message,
          null,
          null,
          result
        );

        return true;
      }

      @Override
      public boolean onJsPrompt(android.webkit.WebView view,
                                String url,
                                String message,
                                String defaultValue,
                                JsPromptResult result) {
        AlertUtils.showTextPrompt(
          getContext(),
          message,
          defaultValue,
          null,
          null,
          result
        );

        return true;
      }
    });
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
    addPendingJavascriptInterfaces();
    evaluatePendingJavascripts();
  }

  private void addExtension(String uri, String id, String nativeApp) {
    if (ReactBuildConfig.DEBUG) {
      FLog.d(LOG_TAG, "add extension from uri: " + uri + " with id: " + id);
    }
    geckoRuntime.getWebExtensionController()
      .ensureBuiltIn(uri, id)
      .accept(new GeckoResult.Consumer<WebExtension>() {
        @SuppressLint("WrongThread")
        @Override
        public void accept(@Nullable WebExtension extension) {
          if (extension == null) {
            return;
          }
          if (ReactBuildConfig.DEBUG) {
            FLog.d(LOG_TAG, "add web extension ok: " + id);
          }
          if (id.equals(WEB_EXTENSION_BLOCKER_ID)) {
            extension.setMessageDelegate(WebView.this, nativeApp);
          } else {
            WebExtension.SessionController sessionController
              = geckoSession.getWebExtensionController();
            sessionController.setMessageDelegate(extension, WebView.this, nativeApp);
          }
        }
      }, new GeckoResult.Consumer<Throwable>() {
        @Override
        public void accept(@Nullable Throwable throwable) {
          if (ReactBuildConfig.DEBUG) {
            FLog.d(LOG_TAG, "add web extension error: " + id);
            throwable.printStackTrace();
          }
        }
      });
  }

  private void injectJavascript(String nativeApp,
                                boolean isTopLevel,
                                String javascript) {
    if (nativeApp == null || javascript == null) {
      return;
    }
    List<WebExtension.Port> ports = portsLookup.get(nativeApp);
    if (ports == null || ports.size() == 0) {
      return;
    }
    for (WebExtension.Port port : ports) {
      if (isTopLevel && !port.sender.isTopLevel()) {
        continue;
      }
      injectJavascript(port, javascript);
    }
  }

  private void injectJavascript(WebExtension.Port port,
                                String javascript) {
    if (port == null || javascript == null) {
      return;
    }
    try {
      JSONObject message = new JSONObject();
      message.put("javascript", javascript);
      port.postMessage(message);
    } catch (Exception ex) {
      if (ReactBuildConfig.DEBUG) {
        ex.printStackTrace();
      }
    }
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

  private void addPendingJavascriptInterfaces() {
    if (!ready || javascriptInterfaceLookup.size() == 0) {
      return;
    }
    for (Map.Entry<String, Object> entry : javascriptInterfaceLookup.entrySet()) {
      addJavascriptInterface(entry.getValue(), entry.getKey());
    }
    javascriptInterfaceLookup = new HashMap<>();
  }

  private void evaluatePendingJavascripts() {
    if (!ready || javascriptLookup.size() == 0) {
      return;
    }
    for (Map.Entry<String, ValueCallback<String>> entry : javascriptLookup.entrySet()) {
      evaluateJavascript(entry.getKey(), entry.getValue());
    }
    javascriptLookup = new HashMap<>();
  }

  private void updateLayout() {
    updateLayout(getWidth(), getHeight());
  }

  private void updateLayout(int width, int height) {
    updateContentLayout(geckoView, width, height);
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

  private boolean areStringsEqual(String str1, String str2) {
    if (str1 == null) {
      return str2 == null;
    }
    return str1.equals(str2);
  }
}