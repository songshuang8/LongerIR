package clurc.net.longerir.Utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import clurc.net.longerir.R;

import static clurc.net.longerir.manager.UiUtils.getContext;
import static clurc.net.longerir.manager.UiUtils.getString;

public class DialogTxtEditor extends QMUIDialog.AutoResizeDialogBuilder {
    private EditText mEditText;
    private String txt;

    public DialogTxtEditor(Context context) {
        super(context);
    }

    public EditText getEditText() {
        return mEditText;
    }

    @Override
    public View onBuildContent(@NonNull QMUIDialog dialog, @NonNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = QMUIDisplayHelper.dp2px(context, 4);
        layout.setPadding(padding, padding, padding, padding);
        mEditText = new AppCompatEditText(context);
        QMUIViewHelper.setBackgroundKeepingPadding(mEditText, QMUIResHelper.getAttrDrawable(context, R.drawable.qmui_divider_bottom_bitmap));
        mEditText.setTextColor(Color.BLACK);
        mEditText.setText(txt);
        LinearLayout.LayoutParams editTextLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editTextLP.bottomMargin = QMUIDisplayHelper.dp2px(getContext(), 4);
        mEditText.setLayoutParams(editTextLP);
        layout.addView(mEditText);
        return layout;
    }

    public void showEditor(String txtstr){
        txt = txtstr;
        setTitle(getString(R.string.str_edit_remote));
        setSkinManager(QMUISkinManager.defaultInstance(getContext()))
        .addAction(getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
        });
        create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
        QMUIKeyboardHelper.showKeyboard(mEditText, true);
    }
}