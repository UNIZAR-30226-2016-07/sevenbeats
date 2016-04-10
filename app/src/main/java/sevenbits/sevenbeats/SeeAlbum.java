package sevenbits.sevenbeats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class SeeAlbum extends AppCompatActivity {

    private BaseDatosAdapter dbHelper;
    private Cursor mNotesCursor;
    private ListView mList;

    private String imagenDefecto = "drawable://defaultimage.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_album);

        //Capturo los parametros
        Bundle extras = getIntent().getExtras();

        final String nombreAlbum = extras.getString("SeeAlbum_album");

        /*Llamo a la base de datos para capturar los datos que necesito:
            -Lista de cancciones, genero de una de ellas, caratula y artista*/
        fillData(nombreAlbum);
        final String rutaImagen = dbHelper.imagen(nombreAlbum);
        String artista = dbHelper.artista(nombreAlbum);
        String genero = dbHelper.generos(nombreAlbum).getString(1);

        /*Asigno cada valor a sus correspondientes variables*/
        TextView asignador = (TextView)findViewById(R.id.nombreAlbum);
        asignador.setText(nombreAlbum);

        asignador = (TextView)findViewById(R.id.artistaAlbum);
        asignador.setText(artista);

        asignador = (TextView)findViewById(R.id.generoAlbum);
        asignador.setText(genero);

        /*Si no hay caratula, enseñar imagen por defecto*/
        ImageView imagen = (ImageView)findViewById(R.id.imageViewAlbum);
        if ( rutaImagen != null ){
            imagen.setImageURI(Uri.parse(rutaImagen));
        }
        else{
            imagen.setImageURI(Uri.parse(imagenDefecto));
        }

        final Context activity = this;
        final EditText txtUrl = new EditText(activity);

        final Button button = (Button) findViewById(R.id.buttonAlbum);
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
                                ponerCaratula(url, nombreAlbum);
                                ImageView imagen = (ImageView)findViewById(R.id.imageViewAlbum);
                                imagen.setImageURI(Uri.parse(rutaImagen));

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     * @return true si solo si el proceso se ha realizado de forma correcta.
     */
    public boolean ponerCaratula(String ruta, String nombreAlbum){
        Image image = null;
        try {
            URL url = new URL(ruta);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1!=(n=in.read(buf)))
            {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            FileOutputStream fos = new FileOutputStream("drawable://" + nombreAlbum + ".jpg");
            fos.write(response);
            fos.close();

            dbHelper.anadirCaratula(nombreAlbum, nombreAlbum + ".jpg");

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Carga todas las canciones asociadas al album en la base de datos en
     * la lista.
     *
     * @param album nombre del album
     */
    private void fillData(String album) {

        Cursor notesCursor = dbHelper.buscarPorAlbum(album);
        startManagingCursor(notesCursor);

        String[] from = new String[] { dbHelper.NOMBRE_CANCION };

        int[] to = new int[] { 0 };

        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.id.listSongsAlbum, notesCursor, from, to);
        mList.setAdapter(notes);
    }
}
