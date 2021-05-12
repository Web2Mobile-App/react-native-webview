package com.pakdata.xwalk.refactor;

import android.content.Context;

import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.WebContents;
import org.chromium.content_public.common.Referrer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class MyWalkContent extends XWalkContent {

  public MyWalkContent(Context context, XWalkView xwView) {
    super(context, xwView);
  }

  public MyWalkContent(Context context, XWalkView xwView, int zoneId, int tabId) {
    super(context, xwView, zoneId, tabId);
  }

  @Override
  public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
    if (this.mNativeContent != 0L) {
      LoadUrlParams params = new LoadUrlParams(url);
      if (additionalHttpHeaders != null) {
        String referer = "Referer";
        String lowerReferer = referer.toLowerCase();
        if (additionalHttpHeaders.containsKey(referer)
          || additionalHttpHeaders.containsKey(lowerReferer)) {
          String value
            = additionalHttpHeaders.containsKey(referer)
            ? additionalHttpHeaders.remove(referer)
            : additionalHttpHeaders.remove(lowerReferer);
          Referrer referrer = new Referrer(value, 0);
          params.setReferrer(referrer);
        }
        params.setExtraHeaders(additionalHttpHeaders);
      }

      try {
        Field field = XWalkContent.class.getDeclaredField("mWebContents");
        field.setAccessible(true);
        WebContents webContents = (WebContents) field.get(this);
        field.setAccessible(false);

        if (params.getUrl() != null && params.getUrl().equals(webContents.getLastCommittedUrl()) && params.getTransitionType() == 0) {
          params.setTransitionType(8);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      params.setTransitionType(params.getTransitionType() | 134217728);

      try {
        Method method = XWalkContent.class.getDeclaredMethod("doLoadUrl", LoadUrlParams.class);
        method.setAccessible(true);
        method.invoke(this, params);
        method.setAccessible(false);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
