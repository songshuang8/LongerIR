package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;
import clurc.net.longerir.data.webHttpClientCom;

public class SelectSearchMode extends BaseActivity {
    private int selectmode;
    @Override
    public void getViewId() {
        layid = R.layout.sel_search_mode;
        title = getString(R.string.str_searchmode);
    }

    public void DoClick(View v){
        int seldevidx = Integer.parseInt((String) v.getTag());
        Intent intent = new Intent();
        switch (seldevidx){
            case 0:
                intent = new Intent();
                intent.setClass(instance, Search_Brand.class);
                intent.putExtra("prc",false);
                startActivityForResult(intent, 1);
                break;
            case 1:
                intent = new Intent();
                intent.setClass(instance, SelectDeviceType2.class);
                intent.putExtra("look",1);
                startActivityForResult(intent, 1);
                break;
            case 2: //学习
                intent = new Intent();
                intent.setClass(instance, ActivityModelList.class);
                intent.putExtra("canselect", true);
                intent.putExtra("pos", -1);
                startActivityForResult(intent, 5);
                break;
            case 3:
                intent = new Intent();
                intent.setClass(instance, SelectDeviceType2.class);
                intent.putExtra("look",0);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==1) {

            }else if(requestCode==5){
                int modelidx = data.getIntExtra("selmodelidx",-10);
                Intent intent = new Intent();
                if(modelidx>=0) {
                    intent.putExtra("selmodelidx", modelidx);
                    intent.setClass(instance, IrLearn.class);
                }else{
                    intent.setClass(instance, IrLearAc.class);
                }
                startActivityForResult(intent, 4);
                return;
            }
            Intent intent=new Intent();
            setResult(Activity.RESULT_OK, intent);
            // syn my data
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // push
                    for (int i = 0; i < CfgData.myremotelist.size(); i++) {
                        if(CfgData.myremotelist.get(i).rid<1)
                            webHttpClientCom.getInstance(null).thread_UploadRemote(CfgData.myremotelist.get(i),false);
                    }
                }
            }).start();
            //
            finish();
        }
    }
}
