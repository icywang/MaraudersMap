package cmu.team5.MaraudersMap.ViewController;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cmu.team5.MaraudersMap.Model.LevenshteinDistance;
import cmu.team5.MaraudersMap.R;

public class SpeechSearchActivity extends Activity {

    private String[] preStore = {"WC", "meeting room", "interview", "group study"};
    private String[] actualPOI = {"WC", "meeting room", "interview room", "group study area"};

    private ListView listView;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private ArrayList<String> curList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_search);

        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        listView = (ListView) findViewById(R.id.listView);

        curList = new ArrayList<>();
        List<String> tmp = Arrays.asList(actualPOI);
        curList.addAll(tmp);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            /**
             * On click: pass the selected item to the MAP page and start navigation
             */
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String target = curList.get(i);

                Intent intent = new Intent(getApplicationContext(), Navigation_INI.class);
                intent.putExtra("destination", target);

                startActivity(intent);
            }
        });

        // hide the action bar
        //getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LevenshteinDistance distance = new LevenshteinDistance();
        ArrayList<String> list = new ArrayList<>();
        int[] dis = new int[4];

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //calculate the distance between the result[0] and the preStore array
                    for(int i = 0; i < 4; i++) {
                        dis[i] = distance.apply(result.get(0), preStore[i]);
                    }
                    //sort the preStore and store in the list
                    for(int j = 0; j < 4; j++) {
                        int min = dis[0];
                        int pos = 0;
                        for(int i = 0; i < 4; i++) {
                            if(dis[i] < min) {
                                min = dis[i];
                                pos = i;
                            }
                        }
                        list.add(preStore[pos]);
                        dis[pos] = Integer.MAX_VALUE;
                    }

                    // save the result for indexing:
                    curList = list;

                    ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, curList);
                    listView.setAdapter(adapter);

                    // display in TextView:
                    TextView tv = (TextView) findViewById(R.id.textView);
                    tv.setText(R.string.on_speech_result);
                }
                break;
            }

        }
    }


}
