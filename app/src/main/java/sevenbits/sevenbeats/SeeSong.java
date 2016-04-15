package sevenbits.sevenbeats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class SeeSong extends AppCompatActivity {

    private BaseDatosAdapter dbHelper;
    private Cursor mNotesCursor;
    private RatingBar ratingBar;
    private long idCancionInterno;
    private ImageView imagen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_song);
        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        //Capturo los parametros
        Bundle extras = getIntent().getExtras();

        long idCancion = extras.getLong("SeeCancion_cancion");

        Cursor query = dbHelper.fetchCancion(idCancion);
        query.moveToFirst();

        /*Llamo a la base de datos para capturar los datos que necesito*/
        final String nombreCancion = query.getString(query.getColumnIndexOrThrow("titulo"));
        idCancionInterno = idCancion;
        final String duracion = query.getString(query.getColumnIndexOrThrow("duracion"));
        final String genero = query.getString(query.getColumnIndexOrThrow("genero"));
        final int valoracion = query.getInt(query.getColumnIndexOrThrow("valoracion"));
        final long idAlbum = query.getLong(query.getColumnIndexOrThrow("album"));
        Log.d("Debug",""+query.getColumnIndexOrThrow("duracion"));
        Log.d("Debug",""+query.getColumnIndexOrThrow("genero"));
        Log.d("Debug", "El id del abum de esta canción es: " + idAlbum);

        query = dbHelper.fetchAlbum(idAlbum);
        query.moveToFirst();
        final String rutaImagen = query.getString(query.getColumnIndexOrThrow("ruta"));
        final String nombreAlbum = query.getString(query.getColumnIndexOrThrow("titulo"));
        final String artista = query.getString(query.getColumnIndexOrThrow("artista"));

        /*Asigno cada valor a sus correspondientes variables*/
        TextView asignador = (TextView)findViewById(R.id.SeeSong_texto_titulo);
        asignador.setText(nombreCancion);

        asignador = (TextView)findViewById(R.id.SeeSong_texto_artista);
        asignador.setText(artista);

        asignador = (TextView)findViewById(R.id.SeeSong_texto_duracion);
        asignador.setText(duracion);

        asignador = (TextView)findViewById(R.id.SeeSong_texto_genero);
        asignador.setText(genero);

        ratingBar = (RatingBar) findViewById(R.id.SeeSong_rating_ratingBar);
        ratingBar.setRating(valoracion);

        /*Si no hay caratula, enseñar imagen por defecto*/
        imagen = (ImageView)findViewById(R.id.SeeSong_imageButton);
        if ( rutaImagen != null ){
            imagen.setImageURI(Uri.parse(rutaImagen));
        }
        else{
            imagen.setImageURI(Uri.parse(SeeAlbum.imagenDefecto));
        }

        final Context activity = this;
        final EditText txtUrl = new EditText(activity);

        final ImageButton button = (ImageButton) findViewById(R.id.SeeSong_imageButton);



        addListenerOnRatingBar();

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Si se pulsa el boton se abre una caja de texto para introducir la URL.

                txtUrl.setHint("Pon aqui la url de la imagen");
                new AlertDialog.Builder(activity)
                        .setTitle("Cargar caratula")
                        .setMessage("Escribe la caratula de la imagen para poderla descargar")
                        .setView(txtUrl)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String url = txtUrl.getText().toString();
                                ponerCaratula(url, idAlbum, nombreAlbum, artista);


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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_see_song, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.SeeSong_rating_ratingBar);

        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                Thread hilo = new Thread(new ActualizarRating(idCancionInterno, rating, dbHelper));
                hilo.start();
                try{
                    hilo.join();
                    Log.d("Problemas", "Bien al gestionar el hilo de actualizar rating.");
                } catch (InterruptedException e){
                    Log.d("Problemas","Problema al gestionar el hilo de actualizar rating.");
                }

            }
        });
    }

    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     */
    public void ponerCaratula(String ruta, long albumId, String nombreAlbum, String artista){
        PonerCaratula caratula = new PonerCaratula(ruta, albumId, nombreAlbum, artista, dbHelper,this);
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
}
