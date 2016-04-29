package sevenbits.sevenbeats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class SeeAlbum extends AppCompatActivity {

    private BaseDatosAdapter dbHelper;
    private Cursor mNotesCursor;
    private ListView mList;
    private Button button;
    private ImageView imagen;
    private long idAlbumInterno;

    public static String imagenDefecto = "android.resource://"+"sevenbits.sevenbeats"+"/"+"drawable/default_image";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_see_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Capturo los parametros
        Bundle extras = getIntent().getExtras();
        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        mList = (ListView) findViewById(R.id.listSongsAlbum);

        final long idAlbum = extras.getLong("SeeAlbum_album");
        idAlbumInterno = idAlbum;
        Cursor query = dbHelper.fetchAlbum(idAlbum);
        /*Llamo a la base de datos para capturar los datos que necesito:
            -Lista de cancciones, genero de una de ellas, caratula y artista*/
        fillData(idAlbum);

        query.moveToFirst();

        final String nombreAlbum = query.getString(query.getColumnIndexOrThrow("titulo")); // PENDIENTES
        final String rutaImagen= query.getString(query.getColumnIndexOrThrow("ruta"));;//= dbHelper.imagen(nombreAlbum);
        final String artista= query.getString(query.getColumnIndexOrThrow("artista"));;//= dbHelper.artista(nombreAlbum);
        query = dbHelper.fetchCancionByAlbum(idAlbum);
        query.moveToFirst();
        String genero = "";
        if(query.getCount()>=1) {
            genero = query.getString(query.getColumnIndexOrThrow("genero"));
        }

        /*Asigno cada valor a sus correspondientes variables*/
        TextView asignador = (TextView)findViewById(R.id.nombreAlbum);
        asignador.setText(nombreAlbum);

        asignador = (TextView)findViewById(R.id.artistaAlbum);
        asignador.setText(artista);

        asignador = (TextView)findViewById(R.id.generoAlbum);
        asignador.setText(genero);

        /*Si no hay caratula, enseñar imagen por defecto*/
        imagen = (ImageView)findViewById(R.id.imageViewAlbum);
        if (!rutaImagen.equals(BaseDatosAdapter.rutaDefecto)){
            imagen.setImageURI(Uri.parse(rutaImagen));
        }

        final Context activity = this;
        final EditText txtUrl = new EditText(activity);
        button = (Button) findViewById(R.id.buttonAlbum);


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Si se pulsa el boton se abre una caja de texto para introducir la URL.

                txtUrl.setHint("Pon aqui la url de la imagen");

                new AlertDialog.Builder(activity)
                        .setTitle("Cargar caratula")
                        //.setMessage("Escribe la caratula de la imagen para poderla descargar")
                        .setView(txtUrl)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String url = txtUrl.getText().toString();
                                boolean isUrl =URLUtil.isHttpUrl(url);
                                ponerCaratula(url, idAlbum, nombreAlbum, artista, isUrl);

                            }

                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_see_album, menu);
        return true;
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



    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     */
    public void ponerCaratula(String ruta, long albumId, String nombreAlbum, String artista, boolean isURL){
        PonerCaratula caratula = new PonerCaratula(ruta, albumId, nombreAlbum, artista, isURL, dbHelper,this);
        Thread hilo = new Thread(caratula);
        hilo.start();
        try{
            hilo.join();
        } catch (InterruptedException e){
            Log.d("Problemas","Problema al gestionar el hilo de la caratula.");
        }
        ContextWrapper cw = new ContextWrapper(this);
        File dirImages = cw.getDir("Imagenes", Context.MODE_PRIVATE);
        File myPath = new File(dirImages, nombreAlbum+".jpg");

        imagen.setImageURI(Uri.parse(myPath.getAbsolutePath()));

    }

    /**
     * Carga todas las canciones asociadas al album en la base de datos en
     * la lista.
     *
     * @param album id del album
     */
    private void fillData(long album) {

        Cursor notesCursor = dbHelper.fetchCancionByAlbum(album);
        String[] fromColumns = {MainActivity.CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, notesCursor,
                fromColumns, toViews);
        mList.setAdapter(adapter);

    }
}
