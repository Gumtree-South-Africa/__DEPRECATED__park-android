package com.ebay.park.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ebay.park.R;

/**
 * Created by paula.baudo on 5/27/2016.
 */
public class DialogUtils {

    public static AlertDialog.Builder getDialogWithLabel(Context ctx, int idMessage){
        String message = ctx.getString(idMessage);

        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogLabelLollipop(ctx, null, message, -1);
        } else {
            builder = createDialogLabelPreLollipop(ctx, null, message, -1);
        }

        return builder;
    }

    public static AlertDialog.Builder getDialogWithLabel(Context ctx, String message){
        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogLabelLollipop(ctx, null, message, -1);
        } else {
            builder = createDialogLabelPreLollipop(ctx, null, message, -1);
        }

        return builder;
    }

    public static AlertDialog.Builder getDialogWithLabel(Context ctx, int idTitle, String message){
        String title = ctx.getString(idTitle);

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            return createDialogLabelLollipop(ctx, title, message, -1);
        } else {
            return createDialogLabelPreLollipop(ctx, title, message, -1);
        }
    }

    public static AlertDialog.Builder getDialogWithLabel(Context ctx, int idTitle, int idMessage){
        String title = ctx.getString(idTitle);
        String message = ctx.getString(idMessage);

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            return createDialogLabelLollipop(ctx, title, message, -1);
        } else {
            return createDialogLabelPreLollipop(ctx, title, message, -1);
        }
    }

    public static AlertDialog.Builder getDialogWithLabel(Context ctx, int idTitle, int idMessage,
                                                             int icon){
        String title = ctx.getString(idTitle);
        String message = ctx.getString(idMessage);

        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogLabelLollipop(ctx, title, message, icon);
        } else {
            builder = createDialogLabelPreLollipop(ctx, title, message, icon);
        }

        return builder;
    }

    public static AlertDialog.Builder getDialogWithField(Context ctx, int idMessage, int idHint){
        String message = ctx.getString(idMessage);
        String hint = ctx.getString(idHint);

        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogFieldLollipop(ctx, null, message, hint, null);
        } else {
            builder = createDialogFieldPreLollipop(ctx, null, message, hint, null);
        }

        return builder;
    }

    public static AlertDialog.Builder getDialogWithField(Context ctx, int idTitle, int idMessage,
                                                         int idHint){
        String title = ctx.getString(idTitle);
        String message = ctx.getString(idMessage);
        String hint = ctx.getString(idHint);

        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogFieldLollipop(ctx, title, message, hint, null);
        } else {
            builder = createDialogFieldPreLollipop(ctx, title, message, hint, null);
        }

        return builder;
    }

    public static AlertDialog.Builder getDialogWithField(Context ctx, int idMessage, int idHint,
                                                         String text){
        String message = ctx.getString(idMessage);
        String hint = ctx.getString(idHint);

        AlertDialog.Builder builder;

        if (DeviceUtils.isDeviceLollipopOrHigher()){
            builder = createDialogFieldLollipop(ctx, null, message, hint, text);
        } else {
            builder = createDialogFieldPreLollipop(ctx, null, message, hint, text);
        }

        return builder;
    }

    private static AlertDialog.Builder createDialogLabelLollipop(Context ctx, String title, String message, int icon){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setMessage(Html.fromHtml("<font color='#666666'>"
                        + message + "</font>"));

        if (title != null){
            builder.setTitle(Html.fromHtml("<font color='#666666'>"
                    + title + "</font>"));
        }

        if (icon != -1){
            builder.setIcon(icon);
        }

        return builder;
    }

    private static AlertDialog.Builder createDialogLabelPreLollipop(Context ctx, String title, String message, int icon){
        View aDialogView = View.inflate(ctx, R.layout.dialog_label, null);
        final TextView label = (TextView) aDialogView.findViewById(R.id.label);
        label.setText(message);

        if (title != null){
            return new AlertDialog.Builder(ctx)
                    .setCustomTitle(getDialogCustomTitleView(ctx, title, icon))
                    .setView(aDialogView);
        } else {
            return new AlertDialog.Builder(ctx).setView(aDialogView);
        }
    }

    private static AlertDialog.Builder createDialogFieldLollipop(Context ctx, String title,
                                                                 String message, String hint,
                                                                 String text){
        AlertDialog.Builder builder = createDialogLabelLollipop(ctx, title, message, -1);

        View aDialogView = View.inflate(ctx, R.layout.dialog_field, null);
        final EditText field = (EditText) aDialogView.findViewById(R.id.field);
        field.setHint(hint);
        field.setFilters(new InputFilter[]{KeyboardHelper.EMOJI_FILTER});

        if (text != null){
            field.setText(text);
        }

        builder.setView(aDialogView);

        return builder;
    }

    private static AlertDialog.Builder createDialogFieldPreLollipop(Context ctx, String title,
                                                                    String message, String hint,
                                                                    String text){
        View aDialogView = View.inflate(ctx, R.layout.dialog_field, null);
        final TextView label = (TextView) aDialogView.findViewById(R.id.label);
        label.setText(message);
        final EditText field = (EditText) aDialogView.findViewById(R.id.field);
        field.setHint(hint);
        field.setFilters(new InputFilter[]{KeyboardHelper.EMOJI_FILTER});

        if (text != null){
            field.setText(text);
        }

        if (title != null){
            return new AlertDialog.Builder(ctx)
                    .setCustomTitle(getDialogCustomTitleView(ctx, title, -1))
                    .setView(aDialogView);
        } else {
            return new AlertDialog.Builder(ctx).setView(aDialogView);
        }
    }

    public static EditText getDialogField(AlertDialog dialog){
        return (EditText) dialog.findViewById(R.id.field);
    }

    private static TextView getDialogCustomTitleView(Context ctx, String title, int icon){
        final TextView customTitle = new TextView(ctx);
        customTitle.setText(title);
        customTitle.setPadding(30, 35, 30, 30);
        customTitle.setTextColor(ctx.getResources().getColor(R.color.system_notification));
        customTitle.setTextSize(15);

        if (icon != -1){
            customTitle.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            customTitle.setCompoundDrawablePadding(10);
        }

        return customTitle;
    }

}
