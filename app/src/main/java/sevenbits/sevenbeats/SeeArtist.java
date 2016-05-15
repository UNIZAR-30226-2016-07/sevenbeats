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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Javi on 13/05/2016.
 */
public class SeeArtist extends AppCompatActivity {

    private BaseDatosAdapter dbHelper;
    private Cursor mNotesCursor;
    private GridView mList;
    private Button button;
    private ImageView imagen;
    private TextView nombreArtista;
    private long idAlbumInterno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ruido al abrir la aplicacion
        setContentView(R.layout.main_activity);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();
        // rellenar lista lateral y lista central
        setContentView(R.layout.main_activity);


        mList = (GridView) findViewById(R.id.MainActivity_lista_cuadrada);
        button = (Button) findViewById(R.id.SeeArtist_boton_confirmar);
        imagen = (ImageView) findViewById(R.id.SeeArtist_Imagen_Album);
        nombreArtista = (TextView) findViewById(R.id.SeeArtist_texto_nombre);



        final float ancho = getResources().getDisplayMetrics().widthPixels;
        mList.setColumnWidth((int) (ancho / 3));
        fillAlbumData();
        registerForContextMenu(mList);
    }



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

    private void fillAlbumData(){
        Cursor cursor = dbHelper.fetchAllAlbumsByABC();
        GridCursorAdapter adapter = new GridCursorAdapter(this, cursor,getResources().getDisplayMetrics().widthPixels);
        mList.setAdapter(adapter);
    }
}
