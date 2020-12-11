package com.reactnativecommunity.crosswalk;

import android.net.Uri;

public class WebUtils {
  public static boolean isSameUrl(String url1, String url2) {
    if (url1 == null || url2 == null) {
      return false;
    }
    try {
      url1 = url1.replaceFirst("\\/$", "");
      url1 = url1.toLowerCase();
      url2 = url2.replaceFirst("\\/$", "");
      url2 = url2.toLowerCase();

      Uri uri1 = Uri.parse(url1);
      Uri uri2 = Uri.parse(url2);
      if (uri1 == null || uri2 == null) {
        return false;
      }

      String host1 = uri1.getHost();
      String host2 = uri2.getHost();
      if (host1 == null || host2 == null) {
        return false;
      }

      url1 = uri1.toString();
      url2 = uri2.toString();
      if (!host1.equals(host2)) {
        if (host1.startsWith("www") && !host2.startsWith("www")) {
          String newHost = String.format("www.%s", host2);
          url2 = url2.replace(host2, newHost);
        } else if (!host1.startsWith("www") && host2.startsWith("www")) {
          String newHost = String.format("www.%s", host1);
          url1 = url2.replace(host1, newHost);
        }
      }

      return url1.equals(url2);
    } catch (Exception ex) {
      return false;
    }
  }
}
