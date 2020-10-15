package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.print.PageRange;
import android.view.View;
import android.widget.Toast;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;

public class SelectDeviceType extends BaseActivity {
    private int looktype;
    @Override
    public void getViewId() {
        layid = R.layout.sel_device;
        title = getString(R.string.str_sel_device);
        looktype = getIntent().getExtras().getInt("look",0);
    }

    public void DoClick(View v){
        int seldevidx = Integer.parseInt((String) v.getTag());
        Intent intent = new Intent();
        if(looktype==0) {
            intent.putExtra("seldev", seldevidx);
            intent.setClass(instance, SelectARemote.class);
            startActivityForResult(intent, 1);
        }else {
            String seldevname = SelectDeviceType.getDevName(seldevidx);
            //
            intent.setClass(instance, Search_Brand2.class);
            intent.putExtra("devname",seldevname);
            startActivityForResult(intent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Intent at=new Intent();

            at.putExtra("dev",data.getStringExtra("dev"));
            at.putExtra("pp",data.getStringExtra("pp"));
            at.putExtra("xh",data.getStringExtra("xh"));
            at.putExtra("code",data.getStringExtra("code"));
            at.putExtra("type",data.getIntExtra("type",0));
            setResult(Activity.RESULT_OK, at);
            finish();
        }
    }

    public static String getDevName(int index){
        String ret = null;
        switch (index){
            case 0:
                ret="TV";
                break;
            case 1:
                ret="DVD";
                break;
            case 2:
                ret="SAT";
                break;
            case 3:
                ret="DTT";
                break;
            case 4:
                ret="VCR";
                break;
            case 5:
                ret="PC";
                break;
            case 6:
                ret="HIFI";
                break;
            case 7:
                ret="COMBI";
                break;
            case 8:
                ret="AC";
                break;
            case 9:
                ret="ALL";
                break;
        }
        return ret;
    }
}
