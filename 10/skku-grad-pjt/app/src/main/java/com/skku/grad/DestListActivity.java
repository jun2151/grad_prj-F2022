package com.skku.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class DestListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_list);

        getDestList();
    }

    private void getDestList() {
        ArrayList destList = new ArrayList();
        String[] destInfo;

        String[][] statInfoData = StationInfo.data;
        int statNumDataLeng = statInfoData.length;
        for(int i = 0; i< statNumDataLeng; i++) {
            String id = statInfoData[i][0];
            String name = statInfoData[i][1];

            destInfo = new String[2];
            destInfo[0] = name;
            destInfo[1] = id.substring(1,2)+"호선";
            destList.add(destInfo);
        }

        ListView listView = findViewById(R.id.destLV);
        DestListAdapter adapter = new DestListAdapter(destList, getApplicationContext());
        listView.setAdapter(adapter);
    }
}