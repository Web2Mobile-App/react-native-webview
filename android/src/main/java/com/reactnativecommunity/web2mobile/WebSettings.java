package com.reactnativecommunity.web2mobile;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoView;

public class WebSettings {

  public static final int LOAD_DEFAULT = -1;
  public static final int LOAD_NORMAL = 0;
  public static final int LOAD_CACHE_ELSE_NETWORK = 1;
  public static final int LOAD_NO_CACHE = 2;
  public static final int LOAD_CACHE_ONLY = 3;

  public static final int MIXED_CONTENT_ALWAYS_ALLOW = 0;
  public static final int MIXED_CONTENT_NEVER_ALLOW = 1;
  public static final int MIXED_CONTENT_COMPATIBILITY_MODE = 2;

  private static boolean webContentsDebuggingEnabled = false;

  private android.webkit.WebView webView;
  private android.webkit.WebSettings webkitSettings;
  private GeckoView geckoView;
  private GeckoRuntimeSettings geckoRuntimeSettings;

  private boolean builtInZoomControls;
  private boolean displayZoomControls;
  private boolean domStorageEnabled;
  private boolean allowFileAccess;
  private boolean allowContentAccess;
  private boolean allowFileAccessFromFileURLs;
  private boolean javaScriptEnabled;
  private String appCachePath;
  private int cacheMode;
  private boolean appCacheEnabled;
  private int textZoom;
  private boolean loadWithOverviewMode;
  private boolean useWideViewPort;
  private String userAgentString;
  private boolean mediaPlaybackRequiresUserGesture;
  private boolean javaScriptCanOpenWindowsAutomatically;
  private boolean allowUniversalAccessFromFileURLs;
  private boolean saveFormData;
  private boolean savePassword;
  private int mixedContentMode;
  private boolean geolocationEnabled;
  private boolean cookiesEnabled;
  private boolean supportMultipleWindows;
  private boolean databaseEnabled;
  private String databasePath;
  private boolean forceDarkOn;

  public WebSettings() {
  }

  public static void setWebContentsDebuggingEnabled(boolean enabled) {
    webContentsDebuggingEnabled = enabled;
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public static String getDefaultUserAgent(Context context) {
    return android.webkit.WebSettings.getDefaultUserAgent(context);
  }

  public void updateWebkit(android.webkit.WebView webView,
                           android.webkit.WebSettings webkitSettings) {
    this.webView = webView;
    this.webkitSettings = webkitSettings;

    reload();
  }

  public void updateGecko(GeckoView geckoView,
                          GeckoRuntimeSettings geckoRuntimeSettings) {
    this.geckoView = geckoView;
    this.geckoRuntimeSettings = geckoRuntimeSettings;

    reload();
  }

  public void setBuiltInZoomControls(boolean enabled) {
    this.builtInZoomControls = enabled;
    if (webkitSettings != null) {
      webkitSettings.setBuiltInZoomControls(enabled);
    }
  }

  public void setDisplayZoomControls(boolean enabled) {
    this.displayZoomControls = enabled;
    if (webkitSettings != null) {
      webkitSettings.setDisplayZoomControls(enabled);
    }
  }

  public void setDomStorageEnabled(boolean flag) {
    this.domStorageEnabled = flag;
    if (webkitSettings != null) {
      webkitSettings.setDomStorageEnabled(flag);
    }
  }

  public void setAllowFileAccess(boolean allow) {
    this.allowFileAccess = allow;
    if (webkitSettings != null) {
      webkitSettings.setAllowFileAccess(allow);
    }
  }

  public void setAllowContentAccess(boolean allow) {
    this.allowContentAccess = allow;
    if (webkitSettings != null) {
      webkitSettings.setAllowContentAccess(allow);
    }
  }

  public void setAllowFileAccessFromFileURLs(boolean flag) {
    this.allowFileAccessFromFileURLs = flag;
    if (webkitSettings != null) {
      webkitSettings.setAllowFileAccessFromFileURLs(flag);
    }
  }

  public boolean getJavaScriptEnabled() {
    return this.javaScriptEnabled;
  }

  public void setJavaScriptEnabled(boolean flag) {
    this.javaScriptEnabled = flag;
    if (geckoRuntimeSettings != null) {
      geckoRuntimeSettings.setJavaScriptEnabled(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setJavaScriptEnabled(flag);
    }
  }

  public void setAppCachePath(String appCachePath) {
    this.appCachePath = appCachePath;
    if (webkitSettings != null) {
      webkitSettings.setAppCachePath(appCachePath);
    }
  }

  public void setCacheMode(int mode) {
    this.cacheMode = mode;
    if (webkitSettings != null) {
      webkitSettings.setCacheMode(mode);
    }
  }

  public void setAppCacheEnabled(boolean flag) {
    this.appCacheEnabled = flag;
    if (webkitSettings != null) {
      webkitSettings.setAppCacheEnabled(flag);
    }
  }

  public void setTextZoom(int textZoom) {
    this.textZoom = textZoom;
    if (webkitSettings != null) {
      webkitSettings.setTextZoom(textZoom);
    }
  }

  public void setLoadWithOverviewMode(boolean overview) {
    this.loadWithOverviewMode = overview;
    if (webkitSettings != null) {
      webkitSettings.setLoadWithOverviewMode(overview);
    }
  }

  public void setUseWideViewPort(boolean use) {
    this.useWideViewPort = use;
    if (webkitSettings != null) {
      webkitSettings.setUseWideViewPort(use);
    }
  }

  public void setUserAgentString(String ua) {
    this.userAgentString = ua;
    if (ua == null || ua.length() == 0) {
      return;
    }
    if (webkitSettings != null) {
      webkitSettings.setUserAgentString(ua);
    }
  }

  public void setMediaPlaybackRequiresUserGesture(boolean require) {
    this.mediaPlaybackRequiresUserGesture = require;
    if (webkitSettings != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        webkitSettings.setMediaPlaybackRequiresUserGesture(require);
      }
    }
  }

  public void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {
    this.javaScriptCanOpenWindowsAutomatically = flag;
    if (webkitSettings != null) {
      webkitSettings.setJavaScriptCanOpenWindowsAutomatically(flag);
    }
  }

  public void setAllowUniversalAccessFromFileURLs(boolean flag) {
    this.allowUniversalAccessFromFileURLs = flag;
    if (webkitSettings != null) {
      webkitSettings.setAllowUniversalAccessFromFileURLs(flag);
    }
  }

  public void setSaveFormData(boolean save) {
    this.saveFormData = save;
    if (webkitSettings != null) {
      webkitSettings.setSaveFormData(save);
    }
  }

  public void setSavePassword(boolean save) {
    this.savePassword = save;
    if (webkitSettings != null) {
      webkitSettings.setSavePassword(save);
    }
  }

  public void setMixedContentMode(int mode) {
    this.mixedContentMode = mode;
    if (webkitSettings != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        webkitSettings.setMixedContentMode(mode);
      }
    }
  }

  public void setGeolocationEnabled(boolean flag) {
    this.geolocationEnabled = flag;
    if (webkitSettings != null) {
      webkitSettings.setGeolocationEnabled(flag);
    }
  }

  public void setCookiesEnabled(boolean enabled) {
    this.cookiesEnabled = enabled;
    if (webView != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, enabled);
      }
    }
  }

  public void setSupportMultipleWindows(boolean support) {
    this.supportMultipleWindows = support;
    if (webkitSettings != null) {
      webkitSettings.setSupportMultipleWindows(support);
    }
  }

  public void setDatabaseEnabled(boolean flag) {
    this.databaseEnabled = flag;
    if (webkitSettings != null) {
      webkitSettings.setDatabaseEnabled(flag);
    }
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
    if (webkitSettings != null) {
      webkitSettings.setDatabasePath(databasePath);
    }
  }

  public void clearCookies() {
    if (webkitSettings != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
      }
    }
  }

  public void setForceDarkOn(boolean forceDarkOn) {
    this.forceDarkOn = forceDarkOn;

    if (webkitSettings != null) {
      // Only Android 10+ support dark mode
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        // Switch WebView dark mode
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
          int forceDarkMode = forceDarkOn ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF;
          WebSettingsCompat.setForceDark(webkitSettings, forceDarkMode);
        }

        // Set how WebView content should be darkened.
        // PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING:  checks for the "color-scheme" <meta> tag.
        // If present, it uses media queries. If absent, it applies user-agent (automatic)
        // More information about Force Dark Strategy can be found here:
        // https://developer.android.com/reference/androidx/webkit/WebSettingsCompat#setForceDarkStrategy(android.webkit.WebSettings)
        if (forceDarkOn && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
          WebSettingsCompat.setForceDarkStrategy(webkitSettings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
        }
      }
    }
  }

  private void reload() {
    if (geckoRuntimeSettings != null) {
      geckoRuntimeSettings.setRemoteDebuggingEnabled(webContentsDebuggingEnabled);
      geckoRuntimeSettings.setConsoleOutputEnabled(webContentsDebuggingEnabled);
    } else if (webkitSettings != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        android.webkit.WebView.setWebContentsDebuggingEnabled(webContentsDebuggingEnabled);
      }
    }

    setBuiltInZoomControls(builtInZoomControls);
    setDisplayZoomControls(displayZoomControls);
    setDomStorageEnabled(domStorageEnabled);
    setAllowFileAccess(allowFileAccess);
    setAllowContentAccess(allowContentAccess);
    setAllowFileAccessFromFileURLs(allowFileAccessFromFileURLs);
    setJavaScriptEnabled(javaScriptEnabled);
    setAppCachePath(appCachePath);
    setCacheMode(cacheMode);
    setAppCacheEnabled(appCacheEnabled);
    setTextZoom(textZoom);
    setLoadWithOverviewMode(loadWithOverviewMode);
    setUseWideViewPort(useWideViewPort);
    setUserAgentString(userAgentString);
    setMediaPlaybackRequiresUserGesture(mediaPlaybackRequiresUserGesture);
    setJavaScriptCanOpenWindowsAutomatically(javaScriptCanOpenWindowsAutomatically);
    setAllowUniversalAccessFromFileURLs(allowUniversalAccessFromFileURLs);
    setSaveFormData(saveFormData);
    setSavePassword(savePassword);
    setMixedContentMode(mixedContentMode);
    setGeolocationEnabled(geolocationEnabled);
    setCookiesEnabled(cookiesEnabled);
    setSupportMultipleWindows(supportMultipleWindows);
    setDatabaseEnabled(databaseEnabled);
    setDatabasePath(databasePath);
    setForceDarkOn(forceDarkOn);
  }
}
