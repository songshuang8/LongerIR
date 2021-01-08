package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.adapt.SingleTextAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;
import clurc.net.longerir.manager.QDPreferenceManager;

public class SelectDesType extends BaseActivity {
    private int seldestype;
    @Override
    public void getViewId() {
        layid = R.layout.sel_destype;
        title = getString(R.string.str_sel_kinds);
    }

    @Override
    public void DoInit() {
        String[] showsel = new String[4];
        for (int i = 0; i < showsel.length; i++) {
            showsel[i] = CfgData.getDesTypeDesc(i);
        }
        ListView list = findViewById(R.id.listview);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, showsel));//用android内置布局，或设计自己的样式
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(MoudelFile.isExist(instance)==false){
                    Toast.makeText(instance,
                            getString(R.string.str_err_net), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(instance, SelectDesRemote.class);
                intent.putExtra("pagesel",getIntent().getIntExtra("pagesel",-1));
                intent.putExtra("bonly",getIntent().getBooleanExtra("bonly",false));
                seldestype = i;
                intent.putExtra("iseltype",seldestype);
                instance.startActivityForResult(intent, 1);
            }
        });
        if(MoudelFile.isExist(instance)==false){
            getModelData();
        }
        List<String> favmode=QDPreferenceManager.getInstance(instance).getFavHis();
        if(favmode.size()==0){
            LinearLayout layfav =findViewById(R.id.pnlfav);
            layfav.setVisibility(View.GONE);
        }else {
            ListView listfav =findViewById(R.id.listfav);
            SingleTextAdapt adpt = new SingleTextAdapt(this, R.layout.listview_singletext, R.id.textView, favmode);
            listfav.setAdapter(adpt);
        }
        TextView tvsearch = findViewById(R.id.modelearch);
        tvsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CfgData.modellist==null)return;
                QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(instance);
                builder.setTitle(getString(R.string.str_search))
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction(getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String text = builder.getEditText().getText().toString();
                                if(text==null || text.length()==0){
                                    Toast.makeText(instance,
                                            getString(R.string.str_notbenull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                for (int i = 0; i <CfgData.modellist.size() ; i++) {
                                    String temp = CfgData.modellist.get(i).name;
                                    if(temp.equalsIgnoreCase(text)){
                                        dialog.dismiss();
                                        ToCloseIt(i,0,0);
                                        return;
                                    }
                                }
                                Toast.makeText(instance,
                                        getString(R.string.str_try), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
            }
        });
    }

    private void getModelData(){
        webHttpClientCom.getInstance(instance).RestkHttpCall("mod_export?flag=0",null,"GET", new webHttpClientCom.WevEvent_SucData() {
            @Override
            public void onSuc(byte[] data) {
                if(MoudelFile.saveFile(instance,data)==false){
                    Toast.makeText(instance,
                            getString(R.string.str_err_net), Toast.LENGTH_SHORT).show();
                    return;
                }
                CfgData.modellist = MoudelFile.getMoudleArr(instance);
            }
        });
    }

    private void ToCloseIt(int desidx,int myidx,int pagesel){
        Intent at=new Intent();
        QDPreferenceManager.getInstance(instance).AppendFavRemote(CfgData.modellist.get(desidx).id);
        at.putExtra("desidx", desidx);
        at.putExtra("myidx",myidx);
        at.putExtra("pagesel",pagesel);
        at.putExtra("destype",seldestype);

        setResult(Activity.RESULT_OK, at);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            ToCloseIt(data.getIntExtra("desidx",0),data.getIntExtra("myidx",0),data.getIntExtra("pagesel",0));
        }
    }
}
