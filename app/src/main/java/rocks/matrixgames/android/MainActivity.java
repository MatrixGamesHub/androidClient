package rocks.matrixgames.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "mtx";
    private static final int RESULT_SETTINGS = 1;

    private ListView listGames;
    private StableArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        listGames = (ListView) findViewById(R.id.list_games);
        listGames.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                showSettings();
                break;

        }

        return true;
    }

    private void showSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, RESULT_SETTINGS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Resume main activity");
        if (!App.getInstance().isConnected()) {
            if (adapter != null) adapter.clear();
            initGame();
        }
    }

    private void initGame() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        Log.d(LOG_TAG, "initGame");
        if (!App.getInstance().isConnected()) {
            ArrayList<String> gameList = new ArrayList<String>();

            String host = sharedPrefs.getString("prefServerHost", "");
            String sPort = sharedPrefs.getString("prefServerPort", "50505");
            Log.d(LOG_TAG, String.format("Host %s; Port %s", host, sPort));

            int port = Integer.parseInt(sPort);

            if (host.length() > 3 && App.getInstance().connect(host, port)) {

                try {
                    List<String> games = App.getInstance().getClient().GetGames();

                    adapter = new StableArrayAdapter(this,
                            android.R.layout.simple_list_item_1, games);
                    listGames.setAdapter(adapter);
                } catch (TException exc) {
                    Log.e(LOG_TAG, "Error", exc);
                    App.getInstance().clearConnection();
                }

            } else {
                showSettings();
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String game = (String) adapterView.getItemAtPosition(position);

        try {
            App.getInstance().getClient().LoadGame(game);
        } catch (TException exc) {
            Log.e(LOG_TAG, "Error", exc);
            App.getInstance().clearConnection();
            return;
        }

        Intent i = new Intent("rocks.matrixgames.android.activities.PlayActivity");
        startActivity(i);
    }
}
