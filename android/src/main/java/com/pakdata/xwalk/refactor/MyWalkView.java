package com.pakdata.xwalk.refactor;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import java.lang.reflect.Field;

public class MyWalkView extends XWalkView {

  public MyWalkView(Context context) {
    super(context);
  }

  public MyWalkView(Context context, int zoneId, int tabId) {
    super(context, zoneId, tabId);
  }

  public MyWalkView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MyWalkView(Context context, Activity activity) {
    super(context, activity);
  }

  @Override
  protected void initXWalkContent() {
    try {
      Field field = XWalkView.class.getDeclaredField("mIsHidden");
      field.setAccessible(true);
      field.set(this, false);
      field.setAccessible(false);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.createWalkContent();
    this.setXWalkClient(new XWalkClient(this));
    this.setXWalkWebChromeClient(new XWalkWebChromeClient());
    this.setUIClient(new XWalkUIClient(this));
    this.setResourceClient(new XWalkResourceClient());
    this.setDownloadListener(new XWalkDownloadListenerImpl(this.getContext()));
    this.setNavigationHandler(new XWalkNavigationHandlerImpl(this.getContext()));
    this.setNotificationService(new XWalkNotificationServiceImpl(this.getContext(), this));
  }

  private void createWalkContent() {
    MyWalkContent walkContent = new MyWalkContent(getContext(), this);
    walkContent.resumeTimers();

    try {
      Field field = XWalkView.class.getDeclaredField("mContent");
      field.setAccessible(true);
      field.set(this, walkContent);
      field.setAccessible(false);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
