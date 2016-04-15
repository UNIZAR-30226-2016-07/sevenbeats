package sevenbits.sevenbeats;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class SongEdit extends AppCompatActivity {

    private EditText SongEdit_texto_titulo;
    private EditText SongEdit_texto_album;
    private EditText SongEdit_texto_duracion;
    private EditText SongEdit_texto_genero;

    private Long mRowId;

    private BaseDatosAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        setContentView(R.layout.activity_song_edit);
        setTitle("Editar cancion");

        SongEdit_texto_titulo = (EditText) findViewById(R.id.SongEdit_texto_titulo);
        SongEdit_texto_album = (EditText) findViewById(R.id.SongEdit_texto_album);
        SongEdit_texto_duracion = (EditText) findViewById(R.id.SongEdit_texto_duracion);
        SongEdit_texto_genero  = (EditText) findViewById(R.id.SongEdit_texto_genero);

        Button SongEdit_boton_guardar = (Button) findViewById(R.id.SongEdit_boton_guardar);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable("_id");
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong("_id")
                    : null;
        }
        populateFields();
        SongEdit_boton_guardar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                saveState();
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor cancion = dbHelper.fetchCancion(mRowId);
            startManagingCursor(cancion);
            SongEdit_texto_titulo.setText(cancion.getString(
                    cancion.getColumnIndexOrThrow("titulo")));
            SongEdit_texto_album.setText(cancion.getString(
                    cancion.getColumnIndexOrThrow("album")));
            SongEdit_texto_duracion.setText(cancion.getString(
                    cancion.getColumnIndexOrThrow("duracion")));
            SongEdit_texto_genero.setText(cancion.getString(
                    cancion.getColumnIndexOrThrow("genero")));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        Log.d("Debug","Aqui");
        outState.putSerializable("_id", mRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_edit, menu);
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

    private void saveState() {
        String titulo = SongEdit_texto_titulo.getText().toString();
        String album = SongEdit_texto_album.getText().toString();
        Integer valoracion = 0;
        String duracion = SongEdit_texto_duracion.getText().toString();
        String genero = SongEdit_texto_genero.getText().toString();

        Log.d("Debug","Se va a insertar: " + titulo);
        Log.d("Debug","Duracion: " + duracion);

        if (mRowId == null) {
            long id = dbHelper.createCancion(titulo, duracion, valoracion, album, genero);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            dbHelper.updateCancion(mRowId, titulo, duracion, valoracion, album, genero);
        }
    }
}
