package clurc.net.longerir.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.BtnInfo;
import clurc.net.longerir.data.BtnModelData;
import clurc.net.longerir.data.modeldata.DataModelBtnInfo;
import clurc.net.longerir.data.modeldata.DataModelInfo;
import clurc.net.longerir.view.RemoteBtnView;
import clurc.net.longerir.view.ViewDragGrid;

public class ActivityModelEdit extends BaseActivity {
    private QMUIDialog.EditTextDialogBuilder builder;
    private ViewDragGrid dragGridview;
    private LinearLayout pnlEdtor;
    private ImageView btnCircle,btnRect,btnDelete,btnAdd;
    private TextView vname,vkeyidx;

    private RemoteBtnView mselectbtn = null;
    private DataModelInfo amodel;

    @Override
    public void getViewId() {
        layid = R.layout.activity_model_editor;
        title = getString(R.string.str_model_editor);

        amodel = new DataModelInfo();
        amodel.id = getIntent().getExtras().getInt("id",-1);
        if(amodel.id<0) {
            amodel.colcnt = getIntent().getExtras().getInt("colcnt", -1);
            int rows = getIntent().getExtras().getInt("rowcnt", -1);
            amodel.strdesc = null;

            amodel.btns = new ArrayList<DataModelBtnInfo>();
            int btncnt = rows*amodel.colcnt;
            for (int i = 0; i < btncnt; i++) {
                DataModelBtnInfo abtn = new DataModelBtnInfo();
                abtn.keyidx = 255;
                abtn.sid = 255;
                abtn.kinds = 0;
                abtn.btnname = "btn"+i;
                abtn.cols = i % amodel.colcnt;
                abtn.rows = i / amodel.colcnt;
                amodel.btns.add(abtn);
            }
        }else {
            amodel = BtnModelData.readAMyModel(instance, amodel.id);
            amodel.btns = BtnModelData.getBtnInfo(instance,amodel.id);
        }
    }

    @Override
    public void DoInit() {
        pnlEdtor = findViewById(R.id.pnleditor);
        btnCircle = findViewById(R.id.btncircle);
        btnRect = findViewById(R.id.btnrect);
        btnDelete = findViewById(R.id.btndelete);
        btnAdd = findViewById(R.id.btnappend);
        vname = findViewById(R.id.btnname);
        vkeyidx = findViewById(R.id.btnkeyidx);

        dragGridview = findViewById(R.id.vgv);

        dragGridview.setCOL_CNT(amodel.colcnt);
        dragGridview.setSelectChangedListen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mselectbtn = (RemoteBtnView) v;
                if(mselectbtn == null){
                    pnlEdtor.setVisibility(View.GONE);
                }else {
                    dataToUI();

                    pnlEdtor.setVisibility(View.VISIBLE);
                }
            }
        });

        for(int i=0;i<amodel.btns.size();i++){
            RemoteBtnView image = new RemoteBtnView(instance,
                    getResources().getColor(android.R.color.darker_gray),
                    getResources().getColor(android.R.color.holo_blue_dark),true);
            image.setBtnName(amodel.btns.get(i).btnname);
            image.setCustomName(amodel.btns.get(i).btnname);
            image.setRownCol(amodel.btns.get(i).cols,amodel.btns.get(i).rows);
            image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[amodel.btns.get(i).kinds]);
            image.setKeyidx(amodel.btns.get(i).keyidx);
            image.setSno(amodel.btns.get(i).sid);
            dragGridview.addView(image);
        }
        mTopBar.addRightImageButton(R.mipmap.select_button, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(amodel.strdesc==null){
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
                                                    amodel.strdesc = text.toString();
                                                    save_file();
                                                    dialog.dismiss();
                                                    showMessage(getString(R.string.str_info),getString(R.string.str_save_ok));
                                                } else {
                                                    Toast.makeText(instance, instance.getString(R.string.str_notbenull), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
                                return;
                        }else {
                            save_file();
                            showMessage(getString(R.string.str_info),getString(R.string.str_save_ok));
                        }
                    }
                });
        //------------ edit
        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mselectbtn==null)return;
                mselectbtn.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[0]);
                mselectbtn.invalidate();
            }
        });
        btnRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mselectbtn==null)return;
                mselectbtn.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[1]);
                mselectbtn.invalidate();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mselectbtn==null)return;
                dragGridview.removeView(mselectbtn);
                mselectbtn = null;
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mselectbtn==null)return;
                int cnt = 0;
                int[] colidx = new int[8];
                for (int i = 0; i < colidx.length; i++) {
                    colidx[i] = -1;
                }
                for (int i = 0; i < dragGridview.getChildCount(); i++) {
                    RemoteBtnView btnv = (RemoteBtnView) dragGridview.getChildAt(i);
                    if(btnv.getRow()==mselectbtn.getRow()){
                        cnt++;
                        colidx[btnv.getCol()] = 1;
                    }
                }
                if(cnt>=amodel.colcnt)return;
                //
                int m = -1;
                for (int i = 0; i < colidx.length; i++) {
                    if(colidx[i]<0){
                        m = i;
                        break;
                    }
                }
                RemoteBtnView image = new RemoteBtnView(instance,
                        getResources().getColor(android.R.color.darker_gray),
                        getResources().getColor(android.R.color.holo_blue_dark),true);
                image.setBtnName("11");
                image.setCustomName("11");
                image.setRownCol(m,mselectbtn.getRow());
                image.setmShapeKinds(RemoteBtnView.ShapeKinds.values()[0]);
                image.setKeyidx(255);
                image.setSno(255);
                dragGridview.addView(image);
            }
        });
        if(mselectbtn==null)pnlEdtor.setVisibility(View.GONE);
    }

    private void dataToUI(){
        vname.setText(mselectbtn.getBtnName());
        vkeyidx.setText(String.valueOf(mselectbtn.getKeyidx()+1));
    }

    public void ToClickName(View view){
        if(mselectbtn==null)return;
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(instance);
        builder.setTitle(getString(R.string.str_rename))
                .setPlaceholder(getString(R.string.str_inputnew))
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setDefaultText(mselectbtn.getBtnName())
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
                        mselectbtn.setBtnName(text);
                        mselectbtn.invalidate();
                        dataToUI();
                        dialog.dismiss();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    public void ToClickIdx(View view){
        if(mselectbtn==null)return;

        String[] items = new String[186];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf (i+1);
        }
        new QMUIDialog.CheckableDialogBuilder(instance)
                .setCheckedIndex(mselectbtn.getKeyidx())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mselectbtn.setKeyidx(which);
                        dataToUI();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    public void ToClickNo(View view){
        if(mselectbtn==null)return;
        String[] items = new String[64];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf (i+1);
        }
        new QMUIDialog.CheckableDialogBuilder(instance)
                .setCheckedIndex(mselectbtn.getSno()-1)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mselectbtn.setSno(which+1);
                        dataToUI();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    private void save_file(){
        amodel.btns.clear();
        for (int i = 0; i < dragGridview.getChildCount(); i++) {
            RemoteBtnView btnv = (RemoteBtnView)dragGridview.getChildAt(i);

            DataModelBtnInfo abtn = new DataModelBtnInfo();
            abtn.keyidx = btnv.getKeyidx();
            abtn.sid = btnv.getSno();
            abtn.kinds = btnv.getmShapeKinds().ordinal();
            abtn.btnname = btnv.getBtnName();
            abtn.cols = btnv.getCol();
            abtn.rows = btnv.getRow();
            amodel.btns.add(abtn);
        }
        BtnModelData.SaveLearMyFile(instance,amodel);
    }
}
