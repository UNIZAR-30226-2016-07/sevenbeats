package sevenbits.sevenbeats;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SearchResultsActivity extends ActionBarActivity {

    private BaseDatosAdapter bbdd;
    private ListView listado;
    private static final String CANCION_NOMBRE = "titulo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bbdd = new BaseDatosAdapter(this);
        bbdd.open();
        setContentView(R.layout.activity_result);
        listado = (ListView) findViewById(R.id.Activity_result_lista);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.MainActivity_boton_busqueda).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Cursor cursor = bbdd.searchSongs(query);
            String[] fromColumns = {CANCION_NOMBRE};
            int[] toViews = {R.id.MainActivity_texto_testolista};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, cursor,
                    fromColumns, toViews);
            listado.setAdapter(adapter);
        }
    }
}