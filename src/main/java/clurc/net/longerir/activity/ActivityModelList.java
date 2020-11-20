package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.adapt.ModelListAdapt;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.RemoteInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;

//空调模板放第一个，不可修改删除
public class ActivityModelList extends BaseActivity {
    private RecyclerView collist;
    private ModelListAdapt modeladapt;

    private List<DataModelInfo> models;
    private int temp;
    private int selected=-1;
    private boolean canselect;
    private QMUIDialog.EditTextDialogBuilder builder;

    @Override
    public void getViewId() {
        layid = R.layout.activity_model_btn_list;
        title = getString(R.string.str_model_btn);

        canselect = false;
        if(getIntent().getExtras()!=null)
            canselect = getIntent().getExtras().getBoolean("canselect",false);
    }

    @Override
    public void DoInit() {
        if(canselect==false) {
            mTopBar.addRightImageButton(R.mipmap.add, R.id.topbar_right_change_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String[] items = new String[3];
                            for (int i = 0; i < items.length; i++) {
                                items[i] = String.valueOf(i + 2) + " " + getString(R.string.str_col);
                            }
                            //新建模板 ，输入 列数
                            new QMUIDialog.CheckableDialogBuilder(instance)
                                    .setCheckedIndex(2)
                                    .addItems(items, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            temp = which + 2;
                                            String[] items = new String[20];
                                            for (int i = 0; i < items.length; i++) {
                                                items[i] = String.valueOf(i + 2) + " " + getString(R.string.str_rows);
                                            }
                                            new QMUIDialog.CheckableDialogBuilder(instance)
                                                    .setCheckedIndex(5)
                                                    .addItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            Intent intent = new Intent();
                                                            intent.setClass(instance, ActivityModelEdit.class);
                                                            intent.putExtra("id", -1);
                                                            intent.putExtra("colcnt", temp);
                                                            intent.putExtra("rowcnt", which + 2);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                        }
                                    })
                                    .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                            //
                        }
                    });
        }
        collist = findViewById(R.id.modellist);
    }

    @Override
    public void DoShowing(){
        updateData();
        modeladapt.notifyDataSetChanged();
    }


    private void updateData(){
        models = BtnModelData.readMyModels(instance);

        if(canselect){
            DataModelInfo ainfo = new DataModelInfo();
            ainfo.id = -2;
            ainfo.strdesc = getString(R.string.str_modelname_ac);
            models.add(0,ainfo);
        }
        for (int i = 0; i < models.size(); i++) {
            DataModelInfo amodel = (DataModelInfo)models.get(i);
            if(amodel.id>0)
                amodel.btns = BtnModelData.getBtnInfo(instance,amodel.id);
        }
        collist.setLayoutManager(new GridLayoutManager(instance,3));
        modeladapt = new ModelListAdapt(models, instance, new ModelListAdapt.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selected = position;
                if(canselect){
                    Intent intent=new Intent();
                    intent.putExtra("selmodelidx",selected-1);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }else {
                    showMenu();
                }
            }
        }, new ModelListAdapt.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //长按弹出菜单
                if(canselect)return;
                selected = position;
                if (canselect && selected == 0)
                    return;
                showMenu();
            }
        });
        collist.setAdapter(modeladapt);
    }

    private void showMenu(){
        QMUIBottomSheet.BottomListSheetBuilder bd = new QMUIBottomSheet.BottomListSheetBuilder(instance);
        bd.addItem(getString(R.string.str_delete));
        bd.addItem(getString(R.string.str_copy));
        bd.addItem(getString(R.string.str_edit));
        bd.addItem(getString(R.string.str_rename));
        bd.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(QMUIBottomSheet dialog, View itemView, final int pos, String tag) {
                Intent intent = new Intent();
                switch (pos) {
                    case 0:
                        new QMUIDialog.MessageDialogBuilder(instance)
                                .setTitle(getString(R.string.str_info))
                                .setMessage(getString(R.string.str_suredelete))
                                .addAction(getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .addAction(0, getString(R.string.str_Ok), QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                        BtnModelData.DelAMyModel(instance,models.get(selected).id);
                                        updateData();
                                        modeladapt.notifyDataSetChanged();
                                    }
                                })
                                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                        break;
                    case 1:
                        builder = new QMUIDialog.EditTextDialogBuilder(instance);
                        builder.setTitle(instance.getString(R.string.str_model_input_modelname))
                                .setPlaceholder(instance.getString(R.string.str_model_input_modelname))
                                .setInputType(InputType.TYPE_CLASS_TEXT)
                                .addAction(instance.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .addAction(instance.getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        String text = builder.getEditText().getText().toString();
                                        if (text != null && text.length() > 0) {
                                            DataModelInfo amodel = new DataModelInfo();
                                            amodel.strdesc = text.toString();
                                            amodel.colcnt = models.get(selected).colcnt;
                                            amodel.btns = BtnModelData.getBtnInfo(instance,models.get(selected).id);
                                            BtnModelData.SaveLearMyFile(instance,amodel);
                                            updateData();
                                            modeladapt.notifyDataSetChanged();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(instance, instance.getString(R.string.str_notbenull), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                        break;
                    case 2:
                        intent.setClass(instance, ActivityModelEdit.class);
                        intent.putExtra("id",models.get(selected).id);
                        startActivity(intent);
                        break;
                    case 3:
                        builder = new QMUIDialog.EditTextDialogBuilder(instance);
                        builder.setTitle(instance.getString(R.string.str_model_input_modelname))
                                .setPlaceholder(instance.getString(R.string.str_model_input_modelname))
                                .setInputType(InputType.TYPE_CLASS_TEXT)
                                .addAction(instance.getString(R.string.str_cancel), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .addAction(instance.getString(R.string.str_Ok), new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        String text = builder.getEditText().getText().toString();
                                        if (text != null && text.length() > 0) {
                                            models.get(selected).strdesc = text.toString();
                                            modeladapt.notifyDataSetChanged();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(instance, instance.getString(R.string.str_notbenull), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        }).build().show();
    }
}
