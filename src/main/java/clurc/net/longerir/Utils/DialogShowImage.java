package clurc.net.longerir.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogView;

import clurc.net.longerir.R;
import clurc.net.longerir.data.CfgData;

import static clurc.net.longerir.manager.UiUtils.getContext;

public class DialogShowImage extends QMUIDialogBuilder{
    private ImageView image;
    private Bitmap bitmap;
    private Context context;
    public DialogShowImage(Activity context, Bitmap bitmap) {
        super(context);
        this.context = context;
        this.bitmap = bitmap;
    }

    @Nullable
    @Override
    public View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = QMUIDisplayHelper.dp2px(context, 4);
        layout.setPadding(padding, padding, padding, padding);
        image = new AppCompatImageView(context);
        QMUIViewHelper.setBackgroundKeepingPadding(image, QMUIResHelper.getAttrDrawable(context, R.drawable.qmui_divider_bottom_bitmap));
        image.setImageBitmap(bitmap);
        LinearLayout.LayoutParams editTextLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editTextLP.bottomMargin = QMUIDisplayHelper.dp2px(getContext(), 4);
        image.setLayoutParams(editTextLP);
        layout.addView(image);
        return layout;
    }

    public void CustomShow(){
        addAction(context.getString(R.string.str_file_save), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                String filename = System.currentTimeMillis()+".png";
                if(CfgData.SaveFile(bitmap,filename)){
                    Toast.makeText(context,
                            CfgData.sdcard_paht+filename, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context,
                            context.getString(R.string.str_err), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addAction(context.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }
}
