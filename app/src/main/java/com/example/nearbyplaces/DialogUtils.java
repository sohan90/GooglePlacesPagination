package com.example.nearbyplaces;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by sohan on 21/7/16.
 */
public class DialogUtils {


    private static ProgressDialog mProgressDialogWithNoTitle;

    public static void showProgress(Context context, String message) {
        try {
            if (mProgressDialogWithNoTitle == null || !mProgressDialogWithNoTitle.isShowing()) {
                mProgressDialogWithNoTitle = new ProgressDialog(context);
                mProgressDialogWithNoTitle.setCancelable(false);
                mProgressDialogWithNoTitle.show();
            }
            if (message != null)
                mProgressDialogWithNoTitle.setMessage(message);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }

    }

    public static  void dismissProgress() {
        try {
            if (mProgressDialogWithNoTitle != null && mProgressDialogWithNoTitle.isShowing()) {
                mProgressDialogWithNoTitle.dismiss();
            }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

}
