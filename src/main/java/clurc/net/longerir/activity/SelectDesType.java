package clurc.net.longerir.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import clurc.net.longerir.R;
import clurc.net.longerir.base.BaseActivity;
import clurc.net.longerir.data.CfgData;

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
                Intent intent = new Intent();
                intent.setClass(instance, SelectDesRemote.class);
                intent.putExtra("pagesel",getIntent().getIntExtra("pagesel",-1));
                intent.putExtra("bonly",getIntent().getBooleanExtra("bonly",false));
                seldestype = i;
                intent.putExtra("iseltype",seldestype);
                instance.startActivityForResult(intent, 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Intent at=new Intent();

            at.putExtra("desidx", data.getIntExtra("desidx",0));
            at.putExtra("myidx",data.getIntExtra("myidx",0));
            at.putExtra("pagesel",data.getIntExtra("pagesel",0));
            at.putExtra("destype",seldestype);

            setResult(Activity.RESULT_OK, at);
            finish();
        }
    }
}