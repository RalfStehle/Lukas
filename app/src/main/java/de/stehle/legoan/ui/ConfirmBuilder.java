package de.stehle.legoan.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import de.stehle.legoan.R;

public class ConfirmBuilder {
    private final Activity activity;
    private int title = R.string.make_input;
    private int cancelText = R.string.cancel;
    private int confirmText = R.string.ok;
    private String value = "";
    private int maxLength;

    public interface ClickListener {
        void ok(String text);
    }

    public ConfirmBuilder(Activity activity) {
        this.activity = activity;
    }

    public ConfirmBuilder setTitle(int title) {
        this.title = title;
        return this;
    }

    public ConfirmBuilder setCancelText(int cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public ConfirmBuilder setLMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public ConfirmBuilder setConfirmText(int confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    public ConfirmBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    public void show(ClickListener listener) {
        Context context = activity.getBaseContext();

        int marginLeft = dpToPx(20, context);
        int marginTop = dpToPx(10, context);
        int marginRight = dpToPx(20, context);
        int marginBottom = dpToPx(4, context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(marginLeft, marginTop, marginRight, marginBottom);

        final EditText editText = new EditText(activity);   // Set up the input
        editText.setText(value);  //
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setLayoutParams(params);
        editText.requestFocus();

        if (maxLength > 0) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            editText.setMaxLines(1);
        }

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.addView(editText);

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(frameLayout)
                .setPositiveButton(confirmText, (dialog, which) -> listener.ok(editText.getText().toString()))
                .setNeutralButton(cancelText, (DialogInterface.OnClickListener) (dialog, which) -> dialog.cancel())
                .show();
    }

    private static int dpToPx(final float dp, final Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
