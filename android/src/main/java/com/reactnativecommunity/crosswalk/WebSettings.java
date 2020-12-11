package com.reactnativecommunity.crosswalk;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;

import androidx.annotation.RequiresApi;

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

public class WebSettings {

  public static final int LOAD_DEFAULT = -1;
  public static final int LOAD_NORMAL = 0;
  public static final int LOAD_CACHE_ELSE_NETWORK = 1;
  public static final int LOAD_NO_CACHE = 2;
  public static final int LOAD_CACHE_ONLY = 3;

  public static final int MIXED_CONTENT_ALWAYS_ALLOW = 0;
  public static final int MIXED_CONTENT_NEVER_ALLOW = 1;
  public static final int MIXED_CONTENT_COMPATIBILITY_MODE = 2;

  private XWalkCookieManager walkCookieManager;
  private XWalkView walkView;
  private XWalkSettings walkSettings;
  private android.webkit.WebView webView;
  private android.webkit.WebSettings webkitSettings;

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
  private boolean supportMultipleWindows;
  private boolean databaseEnabled;
  private String databasePath;

  public WebSettings() {
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

  public void updateWalk(XWalkView walkView,
                         XWalkSettings walkSettings) {
    this.walkView = walkView;
    this.walkSettings = walkSettings;
    this.walkCookieManager = new XWalkCookieManager();

    reload();
  }

  public void setBuiltInZoomControls(boolean enabled) {
    this.builtInZoomControls = enabled;
    if (walkSettings != null) {
      walkSettings.setBuiltInZoomControls(enabled);
    } else if (webkitSettings != null) {
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
    if (walkSettings != null) {
      walkSettings.setDomStorageEnabled(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setDomStorageEnabled(flag);
    }
  }

  public void setAllowFileAccess(boolean allow) {
    this.allowFileAccess = allow;
    if (walkSettings != null) {
      walkSettings.setAllowFileAccess(allow);
    } else if (webkitSettings != null) {
      webkitSettings.setAllowFileAccess(allow);
    }
  }

  public void setAllowContentAccess(boolean allow) {
    this.allowContentAccess = allow;
    if (walkSettings != null) {
      walkSettings.setAllowContentAccess(allow);
    } else if (webkitSettings != null) {
      webkitSettings.setAllowContentAccess(allow);
    }
  }

  public void setAllowFileAccessFromFileURLs(boolean flag) {
    this.allowFileAccessFromFileURLs = flag;
    if (walkSettings != null) {
      walkSettings.setAllowFileAccessFromFileURLs(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setAllowFileAccessFromFileURLs(flag);
    }
  }

  public boolean getJavaScriptEnabled() {
    return this.javaScriptEnabled;
  }

  public void setJavaScriptEnabled(boolean flag) {
    this.javaScriptEnabled = flag;
    if (walkSettings != null) {
      walkSettings.setJavaScriptEnabled(flag);
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
    if (walkSettings != null) {
      walkSettings.setCacheMode(mode);
    } else if (webkitSettings != null) {
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
    if (walkSettings != null) {
      walkSettings.setTextZoom(textZoom);
    } else if (webkitSettings != null) {
      webkitSettings.setTextZoom(textZoom);
    }
  }

  public void setLoadWithOverviewMode(boolean overview) {
    this.loadWithOverviewMode = overview;
    if (walkSettings != null) {
      walkSettings.setLoadWithOverviewMode(overview);
    } else if (webkitSettings != null) {
      webkitSettings.setLoadWithOverviewMode(overview);
    }
  }

  public void setUseWideViewPort(boolean use) {
    this.useWideViewPort = use;
    if (walkSettings != null) {
      walkSettings.setUseWideViewPort(use);
    } else if (webkitSettings != null) {
      webkitSettings.setUseWideViewPort(use);
    }
  }

  public void setUserAgentString(String ua) {
    this.userAgentString = ua;
    if (walkSettings != null) {
      walkSettings.setUserAgentString(ua);
    } else if (webkitSettings != null) {
      webkitSettings.setUserAgentString(ua);
    }
  }

  public void setMediaPlaybackRequiresUserGesture(boolean require) {
    this.mediaPlaybackRequiresUserGesture = require;
    if (walkSettings != null) {
      walkSettings.setMediaPlaybackRequiresUserGesture(require);
    } else if (webkitSettings != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        webkitSettings.setMediaPlaybackRequiresUserGesture(require);
      }
    }
  }

  public void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {
    this.javaScriptCanOpenWindowsAutomatically = flag;
    if (walkSettings != null) {
      walkSettings.setJavaScriptCanOpenWindowsAutomatically(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setJavaScriptCanOpenWindowsAutomatically(flag);
    }
  }

  public void setAllowUniversalAccessFromFileURLs(boolean flag) {
    this.allowUniversalAccessFromFileURLs = flag;
    if (walkSettings != null) {
      walkSettings.setAllowUniversalAccessFromFileURLs(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setAllowUniversalAccessFromFileURLs(flag);
    }
  }

  public void setSaveFormData(boolean save) {
    this.saveFormData = save;
    if (walkSettings != null) {
      walkSettings.setSaveFormData(save);
    } else if (webkitSettings != null) {
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
    if (walkCookieManager != null) {
      walkCookieManager.setAcceptCookie(enabled);
      walkCookieManager.setAcceptFileSchemeCookies(enabled);
    } else if (webView != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, enabled);
      }
    }
  }

  public void setSupportMultipleWindows(boolean support) {
    this.supportMultipleWindows = support;
    if (walkSettings != null) {
      walkSettings.setSupportMultipleWindows(support);
    } else if (webkitSettings != null) {
      webkitSettings.setSupportMultipleWindows(support);
    }
  }

  public void setDatabaseEnabled(boolean flag) {
    this.databaseEnabled = flag;
    if (walkSettings != null) {
      walkSettings.setDatabaseEnabled(flag);
    } else if (webkitSettings != null) {
      webkitSettings.setDatabaseEnabled(flag);
    }
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
    if (webkitSettings != null) {
      webkitSettings.setDatabasePath(databasePath);
    }
  }

  private void reload() {
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
    setSupportMultipleWindows(supportMultipleWindows);
    setDatabaseEnabled(databaseEnabled);
    setDatabasePath(databasePath);
  }
}
