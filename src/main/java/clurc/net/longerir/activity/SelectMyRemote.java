package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import clurc.net.longerir.R;
import clurc.net.longerir.Utils.MoudelFile;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;

public class SelectMyRemote extends BaseActivity {
    private int desidx;
    private int myidx = -1;
    private int pagesel;
    @Override
    public void getViewId() {
        layid = R.layout.sel_my_remote;
        title = getString(R.string.str_sel_mys);
    }

    @Override
    public void DoInit() {
        desidx = getIntent().getIntExtra("desidx",0);
        pagesel = getIntent().getIntExtra("pagesel",-1);

        ((TextView)findViewById(R.id.tvdes)).setText(CfgData.modellist.get(desidx).name);

        boolean isacrmt = CfgData.modellist.get(desidx).chip == 11?true:false;
        final int[] myid =CfgData.getMyRemoteIdList(isacrmt);
        String[] myrlist =CfgData.getMyRemoteStrings(myid);
        ListView list = findViewById(R.id.listview);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myrlist));//用android内置布局，或设计自己的样式
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myidx = myid[i];
                String[] pages = MoudelFile.getMoudlePage(instance, desidx, CfgData.modellist);
                if(pagesel<0 && pages.length<2) {
                    pagesel = 0;
                }
                if(pagesel>=0){
                    dofinnishThis();
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(instance, SelectPages.class);
                intent.putExtra("pages", pages);
                instance.startActivityForResult(intent, 1);
            }
        });
    }

    private void dofinnishThis(){
        Intent at=new Intent();
        at.putExtra("myidx", myidx);
        at.putExtra("pagesel",pagesel);
        setResult(Activity.RESULT_OK, at);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            pagesel = data.getIntExtra("pagesel",0);
            dofinnishThis();
        }
    }
}
