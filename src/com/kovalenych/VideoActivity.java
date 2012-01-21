package com.kovalenych;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoActivity extends Activity {

    private ArrayList<Video> videoList;
    ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoList = new ArrayList<Video>();

        fillList();

        setContentView(R.layout.videos);

        lv = (ListView) findViewById(R.id.video_list);
        lv.setOnItemClickListener(listener);

        invalidateList();
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoList.get(i).getUri()));
            startActivity(intent);
        }
    };

    private void fillList() {
        videoList.add(new Video("TEDxVienna - Herbert Nitsch - Breathless", "http://www.youtube.com/watch?v=INqG2YtgU08"));
        videoList.add(new Video("Intervew with Herbert 83m CNF", "http://www.youtube.com/watch?v=2exs67Npnas"));
        videoList.add(new Video("Freediver Blackout", "http://www.youtube.com/watch?v=PBnEIMTrgFk"));
        videoList.add(new Video("Carlos Coste Black Out WC Italy 2011", "http://www.youtube.com/watch?v=rS3wYUOBxgo&feature=related"));
        videoList.add(new Video("Goran Colak - Freediving World Record 273 m", "http://www.youtube.com/watch?v=9Th8Zt-HMCg"));
        videoList.add(new Video("Guillaume Nery base jumping at Dean's Blue Hole, filmed on breath hold by Julie Gautier", "http://www.youtube.com/watch?v=uQITWbAaDx0"));
        videoList.add(new Video("YogaDa! Freediving - In space and back without scuba", "http://www.youtube.com/watch?v=89HXF4PkAw4"));
        videoList.add(new Video("Breathe Teaser Trailer #1", "http://www.youtube.com/watch?v=2osGJLA18lk&feature=related"));
        videoList.add(new Video("Dave 265M Dynamic Apnea with fin World Record 25 Sept 2010.MOD", "http://www.youtube.com/watch?v=0WFDWYNs4Ac&feature=related"));
        videoList.add(new Video("William Trubridge 101 CNF Record", "http://www.youtube.com/watch?v=UKLo5j53h10&feature=related"));
        videoList.add(new Video("Record mundial de apnea \"No Limits\" - Herbert Nitsch -214 m", "http://www.youtube.com/watch?v=WBNaGscqcyc"));
    }

    private void invalidateList() {

        SimpleAdapter adapter = new SimpleAdapter(this, createCyclesList(), R.layout.video_item,
                new String[]{"title", "picture"},
                new int[]{R.id.video_name, R.id.video_picture});
        adapter.setViewBinder(new VideoViewBinder());
        lv.setAdapter(adapter);
        lv.setVisibility(View.VISIBLE);
    }

    private List<? extends Map<String, ?>> createCyclesList() {

        List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();

        for (int i = 0; i < videoList.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", videoList.get(i).getTitle());
            map.put("picture", videoList.get(i).getPictureUri());
            items.add(map);
        }

        return items;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menushka, menu);
        return true;
    }


     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.articles:
                Intent intent = new Intent(lv.getContext(), ArticlesActivity.class);
                startActivity(intent);
                return true;
            case R.id.videos:
                Intent intent2 = new Intent(lv.getContext(), VideoActivity.class);
                startActivity(intent2);
                return true;
            case R.id.ranking:
                Intent intent3 = new Intent(lv.getContext(), RankingActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
