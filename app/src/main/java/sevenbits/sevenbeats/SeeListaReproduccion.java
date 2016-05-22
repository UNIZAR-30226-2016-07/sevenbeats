package sevenbits.sevenbeats;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * Created by Javi on 21/05/2016.
 */
public class SeeListaReproduccion extends AppCompatActivity {

    BaseDatosAdapter dbHelper;
    ListView mList;
    long idLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ruido al abrir la aplicacion
        setContentView(R.layout.activity_see_listareproduccion);
        mList = (ListView) findViewById(R.id.SeeListaReproduccion_lista_lista);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        Bundle extras = getIntent().getExtras();
        idLista = extras.getLong("SeeListaReproduccion_album");


        Cursor query = dbHelper.fetchNombreLista(idLista);
        query.moveToFirst();
        String nombreLista =  query.getString(query.getColumnIndexOrThrow("nombre"));

        TextView asignador = (TextView)findViewById(R.id.SeeListaReproduccion_texto_titulo);
        asignador.setText(nombreLista);

        fillData(idLista);
        registerForContextMenu(mList);
    }

    /**
     * Carga todas las canciones asociadas al album en la base de datos en
     * la lista.
     *
     * @param lista id del album
     */
    private void fillData(long lista) {

        Cursor notesCursor = dbHelper.fetchCancionByLista(lista);
        String[] fromColumns = {MainActivity.CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, notesCursor,
                fromColumns, toViews);
        mList.setAdapter(adapter);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Debug", "Al menu intenta entrar");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case MainActivity.EDIT_ID:
                Intent i = new Intent(this, SongEdit.class);
                i.putExtra("id_cancion", info.id);
                startActivity(i);
                return true;
            case MainActivity.DELETE_ID:
                dbHelper.deleteCancionLista(idLista, info.id);
                return true;
            case MainActivity.PLAY_ID:
               //Reproducir cancion
                return true;
        }

        return super.onContextItemSelected(item);
    }
    

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, MainActivity.EDIT_ID, Menu.NONE, "Editar");
        menu.add(Menu.NONE, MainActivity.DELETE_ID, Menu.NONE, "Quitar de la lista");
        menu.add(Menu.NONE, MainActivity.PLAY_ID, Menu.NONE, "Reproducir");
    }
}
