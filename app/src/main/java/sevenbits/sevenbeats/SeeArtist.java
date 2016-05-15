package sevenbits.sevenbeats;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Javi on 13/05/2016.
 */
public class SeeArtist extends AppCompatActivity {

    private BaseDatosAdapter dbHelper;
    private Cursor mNotesCursor;
    private ListView mList;
    private Button button;
    private ImageView imagen;
    private long idAlbumInterno;



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, MainActivity.SEE_ID, Menu.NONE, "Ver");
        menu.add(Menu.NONE, MainActivity.DELETE_ID, Menu.NONE, "Borrar");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Debug", "Al menu intenta entrar");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case MainActivity.SEE_ID:
                Intent i = new Intent(this, SeeAlbum.class);
                i.putExtra("SeeAlbum_album", info.id);
                startActivity(i);
                return true;
            case MainActivity.DELETE_ID:
                dbHelper.deleteAlbum(info.id);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Carga todas las canciones asociadas al artist en la base de datos en
     * la lista.
     *
     * @param artist id del artist
     */
    private void fillData(long artist) {

        Cursor notesCursor = dbHelper.fetchAllAlbumsByArtist(artist);
        String[] fromColumns = {MainActivity.ALBUM_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, notesCursor,
                fromColumns, toViews);
        mList.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
