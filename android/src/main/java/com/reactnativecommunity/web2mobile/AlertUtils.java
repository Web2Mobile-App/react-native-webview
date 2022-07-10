package com.reactnativecommunity.web2mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.widget.EditText;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;

public class AlertUtils {

  public static void showAlert(Context context,
                               String message,
                               GeckoSession.PromptDelegate.AlertPrompt geckoPrompt,
                               GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResponse,
                               JsResult webkitResult) {
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (geckoPrompt != null && !geckoPrompt.isComplete()) {
            geckoResponse.complete(geckoPrompt.dismiss());
          }
          if (webkitResult != null) {
            webkitResult.confirm();
          }
        }
      })
      .show();
  }

  public static void showConfirm(Context context,
                                 String message,
                                 GeckoSession.PromptDelegate.ButtonPrompt geckoPrompt,
                                 GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResponse,
                                 JsResult webkitResult) {
    new AlertDialog.Builder(context)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (geckoPrompt != null && !geckoPrompt.isComplete()) {
            geckoResponse.complete(
              geckoPrompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE)
            );
          }
          if (webkitResult != null) {
            webkitResult.confirm();
          }
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (geckoPrompt != null && !geckoPrompt.isComplete()) {
            geckoResponse.complete(
              geckoPrompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE)
            );
          }
          if (webkitResult != null) {
            webkitResult.cancel();
          }
        }
      })
      .show();
  }

  public static void showTextPrompt(Context context,
                                    String message,
                                    String defaultValue,
                                    GeckoSession.PromptDelegate.TextPrompt geckoPrompt,
                                    GeckoResult<GeckoSession.PromptDelegate.PromptResponse> geckoResponse,
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
          if (geckoPrompt != null && !geckoPrompt.isComplete()) {
            geckoResponse.complete(
              geckoPrompt.confirm(value)
            );
          }
          if (webkitResult != null) {
            webkitResult.confirm(value);
          }
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (geckoPrompt != null && !geckoPrompt.isComplete()) {
            geckoResponse.complete(
              geckoPrompt.dismiss()
            );
          }
          if (webkitResult != null) {
            webkitResult.cancel();
          }
        }
      })
      .show();
  }
}
