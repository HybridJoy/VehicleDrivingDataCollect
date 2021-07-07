package com.hybrid.tripleldc.util.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import java.util.MissingResourceException;


/**
 * Author: Tasun
 * Time: 2020/8/11-21:51:43
 */

public class DialogUtil {
    private static final String TAG = "DialogUtil";

    public static String DefaultTitle = "提示";
    public static DialogInterface.OnClickListener DefaultOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    public static AlertDialog createDialog(Context context, @LayoutRes int layoutID) {
        return createDialog(context, layoutID, null);
    }

    public static AlertDialog createDialog(Context context, @LayoutRes int layoutID, ViewGroup root) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutID, root);
        return createDialog(context, view);
    }

    public static AlertDialog createDialog(Context context, View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setView(view);
        return alertDialog;
    }

    /**
     * @param context
     * @param titleResID
     * @param msg
     * @param posBtnTextID 为-1时表示不需要此按钮
     * @param negBtnTextID 为-1时表示不需要此按钮
     * @return
     */
    public static AlertDialog createDialog(Context context, @StringRes int titleResID, String msg, @StringRes int posBtnTextID, @StringRes int negBtnTextID) {
        return createDialog(context, titleResID, msg, posBtnTextID, negBtnTextID, null, null);
    }

    public static AlertDialog createDialog(Context context, @StringRes int titleResID, String msg, @StringRes int posBtnTextID, @StringRes int negBtnTextID,
                                           DialogInterface.OnClickListener acceptClickListener, DialogInterface.OnClickListener cancelClickListener) {
        String title, posBtnText, negBtnText;

        title = "";
        posBtnText = "";
        negBtnText = "";

        if (titleResID == -1) {
            title = DefaultTitle;
        } else {
            try {
                title = context.getString(titleResID);
            } catch (MissingResourceException e) {
                Log.d(TAG, "title res " + titleResID + "not found!");
            }
        }

        if (posBtnTextID == -1) {
            posBtnText = "";
        } else {
            try {
                posBtnText = context.getString(posBtnTextID);
            } catch (MissingResourceException e) {
                Log.d(TAG, "pos btn text res " + posBtnTextID + "not found!");
            }
        }

        if (negBtnTextID == -1) {
            negBtnText = "";
        } else {
            try {
                negBtnText = context.getString(negBtnTextID);
            } catch (MissingResourceException e) {
                Log.d(TAG, "neg btn text res " + negBtnTextID + "not found!");
            }
        }

        return createDialog(context, title, msg, posBtnText, negBtnText, acceptClickListener, cancelClickListener);
    }

    public static AlertDialog createDialog(Context context, String title, String msg, String posBtnText, String negBtnText) {
        return createDialog(context, title, msg, posBtnText, negBtnText, null, null);
    }


    public static AlertDialog createDialog(Context context, String title, String msg, String posBtnText, String negBtnText,
                                           DialogInterface.OnClickListener acceptClickListener, DialogInterface.OnClickListener cancelClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        if (!posBtnText.equals("")) {
            builder.setPositiveButton(posBtnText, acceptClickListener == null ? DefaultOnClickListener : acceptClickListener);
        }
        if (!negBtnText.equals("")) {
            builder.setNegativeButton(negBtnText, cancelClickListener == null ? DefaultOnClickListener : cancelClickListener);
        }
        return builder.create();
    }
}
