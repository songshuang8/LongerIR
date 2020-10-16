package clurc.net.longerir.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogView;

import clurc.net.longerir.R;
import clurc.net.longerir.activity.IrLearn;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.webHttpClientCom;

public class DialogRemoteInfo extends QMUIDialogBuilder {
    private Activity context;
    private AutoCompleteTextView text;
    private ArrayAdapter<String> adapter;
    private View view;
    private RemoteInfo aremote;

    public DialogRemoteInfo(Activity context,RemoteInfo armt) {
        super(context);
        this.context = context;
        aremote = armt;
    }

    @Nullable
    @Override
    protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.dialog_remoteinfo_input, parent, false);

        DoDownDevList();
        adapter = new ArrayAdapter<String>(context,
                R.layout.listview_singletext,R.id.textView, CfgData.devitems);
        text = (AutoCompleteTextView) view.findViewById(R.id.editdevice);
        text.setAdapter(adapter);
        ImageButton button = (ImageButton) view.findViewById(R.id.btndevdrop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setDropDownHeight(view.getMeasuredHeight());
                text.showDropDown();
            }
        });
        if(aremote.dev != null && aremote.dev.length() > 0){
            text.setText(aremote.dev,false);
        }
        if(aremote.pp != null && aremote.pp.length() > 0){
            ((TextView) view.findViewById(R.id.edtbrand)).setText(aremote.pp);
        }
        if(aremote.xh != null && aremote.xh.length() > 0){
            ((TextView) view.findViewById(R.id.editModel)).setText(aremote.xh);
        }
        return view;
    }

    private void DoDownDevList(){
        if(CfgData.devitems.size()>0)return;
        webHttpClientCom.getInstance(context).RestkHttpCall("search_devs?data=" + CfgData.dataidx, null, "GET", new webHttpClientCom.WevEvent_SucString() {
            @Override
            public void onSuc(String res) {
                CfgData.DoChkdevs(res);
            }
        });
    }

    public interface OnRemoteInfoSuc{
        void onSuc();
    }

    public void CustomShow(final OnRemoteInfoSuc e){
        addAction(context.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        addAction(context.getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        AutoCompleteTextView edtdev = dialog.findViewById(R.id.editdevice);
                        aremote.dev = edtdev.getText().toString();
                        if (aremote.dev == null || aremote.dev.length() == 0) {
                            Toast.makeText(context, "Please input device", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        EditText edtbrand = dialog.findViewById(R.id.edtbrand);
                        aremote.pp = edtbrand.getText().toString();
                        if (aremote.pp == null || aremote.pp.length() == 0) {
                            Toast.makeText(context, "Please input brand", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        EditText edtmodel = dialog.findViewById(R.id.editModel);
                        aremote.xh = edtmodel.getText().toString();
                        if (aremote.xh == null || aremote.xh.length() == 0) {
                            Toast.makeText(context, "Please input model", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        aremote.descname = aremote.pp + "/" + aremote.xh;
                        dialog.dismiss();
                        e.onSuc();
                    }
        });
        create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }
}
