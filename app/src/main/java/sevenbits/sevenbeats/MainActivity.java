package sevenbits.sevenbeats;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

/**
 * Tengo que tener un menu lateral que hay que rellenar con
 * las distintas opciones. De primeras se rellena con las canciones.
 *
 */
public class MainActivity extends AppCompatActivity {

    private BaseDatosAdapter bbdd;
    private static final String CANCION_NOMBRE = "nombre";

    private MediaPlayer Mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // rellenar lista lateral y lista central

        Mp = MediaPlayer.create(this, R.raw.sonido_inicio_app);
        Mp.start();
        setContentView(R.layout.main_activity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.MainActivity_boton_busqueda:
                return true;
            case R.id.MainActivity_boton_anyadir:
                anyadir();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean anyadir(){
        Intent i = new Intent(this, SongEdit.class);
        startActivity(i);
        return true;
    }

    private void fillSongData() {
        Cursor cursor = bbdd.fetchAllSongs();
        String[] fromColumns = {CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_lista_principal};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity, cursor,
                fromColumns, toViews, 0);
    }
}