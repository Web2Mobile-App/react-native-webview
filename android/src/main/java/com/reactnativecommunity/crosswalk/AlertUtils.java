package com.reactnativecommunity.crosswalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.widget.EditText;

import com.pakdata.xwalk.refactor.XWalkJavascriptResult;

public class AlertUtils {

  public static void showAlert(Context context,
                               String url,
                               String message,
                               XWalkJavascriptResult walkResult,
                               JsResult webkitResult) {
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (walkResult != null) {
            walkResult.confirm();
          }
          if (webkitResult != null) {
            webkitResult.confirm();
          }
        }
      })
      .show();
  }

  public static void showConfirm(Context context,
                                 String url,
                                 String message,
                                 XWalkJavascriptResult walkResult,
                                 JsResult webkitResult) {
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (walkResult != null) {
            walkResult.confirm();
          }
          if (webkitResult != null) {
            webkitResult.confirm();
          }
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (walkResult != null) {
            walkResult.cancel();
          }
          if (webkitResult != null) {
            webkitResult.cancel();
          }
        }
      })
      .show();
  }

  public static void showPrompt(Context context,
                                String url,
                                String message,
                                String defaultValue,
                                XWalkJavascriptResult walkResult,
                                JsPromptResult webkitResult) {
    final EditText editText = new EditText(context);
    editText.setHint(defaultValue);
    editText.setText(defaultValue);
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setView(editText)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          String value = editText.getText().toString();
          if (walkResult != null) {
            walkResult.confirmWithResult(value);
          }
          if (webkitResult != null) {
            webkitResult.confirm(value);
          }
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (walkResult != null) {
            walkResult.cancel();
          }
          if (webkitResult != null) {
            webkitResult.cancel();
          }
        }
      })
      .show();
  }
}
