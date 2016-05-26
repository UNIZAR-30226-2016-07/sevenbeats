package sevenbits.sevenbeats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

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
        setContentView(R.layout.activity_see_artist);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();
        // rellenar lista lateral y lista central
        //setContentView(R.layout.main_activity);


        mList = (GridView) findViewById(R.id.SeeArtist_lista_cuadrada);
        button = (Button) findViewById(R.id.SeeArtist_boton_confirmar);
        imagen = (ImageView) findViewById(R.id.SeeArtist_Imagen_Album);
        nombreArtista = (TextView) findViewById(R.id.SeeArtist_texto_nombre);

        Bundle extras = getIntent().getExtras();
        final long idArtist = extras.getLong("id_artista");
        Log.d("SeeArtist",idArtist+"");
        Cursor query = dbHelper.fetchArtista(idArtist);
        query.moveToFirst();
        final String artista= query.getString(query.getColumnIndexOrThrow("nombre"));
        Log.d("2 mirando", artista);
        nombreArtista.setText(artista);

        String rutaImagen = query.getString(query.getColumnIndexOrThrow("ruta"));
        if ((rutaImagen != null) && !rutaImagen.equals(BaseDatosAdapter.rutaDefecto)){
            imagen.setImageURI(Uri.parse(rutaImagen));
        }

        final float ancho = getResources().getDisplayMetrics().widthPixels;
        mList.setColumnWidth((int) (ancho / 3));
        fillAlbumData(idArtist);

        final Context activity = this;
        final EditText txtUrl = new EditText(activity);
        button = (Button) findViewById(R.id.SeeArtist_boton_confirmar);


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Si se pulsa el boton se abre una caja de texto para introducir la URL.

                txtUrl.setHint("Pon aqui la url de la imagen");

                new AlertDialog.Builder(activity)
                        .setTitle("Cargar imagen")
                                //.setMessage("Escribe la caratula de la imagen para poderla descargar")
                        .setView(txtUrl)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String url = txtUrl.getText().toString();
                                boolean isUrl = URLUtil.isHttpUrl(url);
                                ponerImagen(url, idArtist, isUrl, artista);


                            }

                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });

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

    private void fillAlbumData(long idArtist){
        Cursor cursor = dbHelper.fetchAllAlbumsByABC(idArtist);
        GridCursorAdapter adapter = new GridCursorAdapter(this, cursor,getResources().getDisplayMetrics().widthPixels);
        mList.setAdapter(adapter);
    }

    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     */
    public void ponerImagen(String ruta, long artistId, boolean isURL, String name){
        PonerImagenArtista caratula = new PonerImagenArtista(ruta, artistId, isURL, dbHelper,this, name);
        Thread hilo = new Thread(caratula);
        hilo.start();
        try{
            hilo.join();
        } catch (InterruptedException e){
            Log.d("Problemas","Problema al gestionar el hilo de la caratula.");
        }
        ContextWrapper cw = new ContextWrapper(this);
        File dirImages = cw.getDir("Imagenes", Context.MODE_PRIVATE);
        File myPath = new File(dirImages, artistId+".jpg");

        imagen.setImageURI(Uri.parse(myPath.getAbsolutePath()));

    }
}
