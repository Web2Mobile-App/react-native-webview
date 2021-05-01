package com.reactnativecommunity.crosswalk;

import android.app.Application;
import android.content.Context;

import com.pakdata.xwalk.refactor.MyWalkView;
import com.pakdata.xwalk.refactor.XWalkView;

import org.chromium.base.ApplicationStatus;
import org.xwalk.core.XWalkInitializer;

import java.util.ArrayList;
import java.util.List;

public class XWalkManager implements XWalkInitializer.XWalkInitListener {

  public interface OnCreateListener {
    void onCreate(XWalkView walkView);
  }

  private static final XWalkManager instance = new XWalkManager();

  private XWalkInitializer walkInitializer;
  private boolean ready;
  private List<OnCreateListener> listeners;
  private List<Context> contexts;

  private XWalkManager() {
    ready = false;
    listeners = new ArrayList<>();
    contexts = new ArrayList<>();
  }

  public static XWalkManager getInstance() {
    return instance;
  }

  @Override
  public void onXWalkInitStarted() {

  }

  @Override
  public void onXWalkInitCancelled() {

  }

  @Override
  public void onXWalkInitFailed() {

  }

  @Override
  public void onXWalkInitCompleted() {
    synchronized (this) {
      ready = true;

      for (int i = 0; i < contexts.size(); i++) {
        doCreateInstance(contexts.get(i), listeners.get(i));
      }

      contexts = new ArrayList<>();
      listeners = new ArrayList<>();
    }
  }

  public void initialize(Application application) {
    assert application != null;

    ApplicationStatus.initialize(application);

    walkInitializer = new XWalkInitializer(this, application.getApplicationContext());
    walkInitializer.initAsync();
  }

  public void createInstance(Context context,
                             OnCreateListener listener) {
    if (context == null || listener == null) {
      return;
    }
    synchronized (this) {
      if (ready) {
        doCreateInstance(context, listener);
      } else {
        contexts.add(context);
        listeners.add(listener);
      }
    }
  }

  private void doCreateInstance(Context context,
                                OnCreateListener listener) {
    XWalkView walkView = new MyWalkView(context);
    listener.onCreate(walkView);
  }
}
