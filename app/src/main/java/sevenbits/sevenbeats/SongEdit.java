package sevenbits.sevenbeats;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class SongEdit extends AppCompatActivity {

    private EditText SongEdit_texto_titulo;
    private EditText SongEdit_texto_album;
    private Integer SongEdit_texto_valoracion;
    private EditText SongEdit_texto_duracion;
    private Long mRowId;

    private BaseDatosAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        setContentView(R.layout.activity_song_edit);
        setTitle("Editar cancion");

        SongEdit_texto_titulo = (EditText) findViewById(R.id.titulo);
        SongEdit_texto_album = (EditText) findViewById(R.id.album);
        SongEdit_texto_valoracion = (EditText) findViewById(R.id.valoracion);
        SongEdit_texto_duracion = (EditText) findViewById(R.id.duracion);

        Button SongEdit_boton_guardar = (Button) findViewById(R.id.confirm);

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
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = dbHelper.fetchCancion(mRowId);
            startManagingCursor(note);
            SongEdit_texto_titulo.setText(note.getString(
                    note.getColumnIndexOrThrow("titulo")));
            SongEdit_texto_album.setText(note.getString(
                    note.getColumnIndexOrThrow("album")));
            SongEdit_texto_valoracion.setText(note.getString(
                    note.getColumnIndexOrThrow("valoracion")));
            SongEdit_texto_duracion.setText(note.getString(
                    note.getColumnIndexOrThrow("duracion")));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
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
        Integer valoracion = SongEdit_texto_valoracion.getText().toString();
        String duracion = SongEdit_texto_duracion.getText().toString();

        if (mRowId == null) {
            long id = dbHelper.createCancion(titulo, duracion, valoracion, album);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            dbHelper.updateCancion(mRowId, titulo, duracion, valoracion, album);
        }
    }
}
