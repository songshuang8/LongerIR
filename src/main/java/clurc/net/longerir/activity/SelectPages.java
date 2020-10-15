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

public class SelectPages extends BaseActivity {
    @Override
    public void getViewId() {
        layid = R.layout.sel_page;
        title = getString(R.string.str_sel_page);
    }

    @Override
    public void DoInit() {
        ((TextView)findViewById(R.id.tvdes)).setText(getIntent().getExtras().getString("pp"));
        ((TextView)findViewById(R.id.tvmodel)).setText(getIntent().getExtras().getString("xh"));

        String[] pages = getIntent().getStringArrayExtra("pages");
        ListView list = findViewById(R.id.listview);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pages));//用android内置布局，或设计自己的样式
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("pagesel",i);
                setResult(Activity.RESULT_OK, intent);
                instance.finish();
            }
        });
    }
}
