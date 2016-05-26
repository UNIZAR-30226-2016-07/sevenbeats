package sevenbits.sevenbeats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * Created by Javi on 21/05/2016.
 */
public class SeeListaReproduccion extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private Button buttonPlay, buttonSiguiente;
    BaseDatosAdapter dbHelper;
    ListView mList;
    long idLista;
    int lista=0;
    int pos = 0;
    LinkedList<String> rutas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mMediaPlayer = new MediaPlayer();
        super.onCreate(savedInstanceState);
        // ruido al abrir la aplicacion
        setContentView(R.layout.activity_see_listareproduccion);
        mList = (ListView) findViewById(R.id.SeeListaReproduccion_lista_lista);

        dbHelper = new BaseDatosAdapter(this);
        dbHelper.open();

        Bundle extras = getIntent().getExtras();
        String nombreLista = extras.getString("SeeListaReproduccion_album");


        idLista = dbHelper.fetchIdLista(nombreLista);

        TextView asignador = (TextView)findViewById(R.id.SeeListaReproduccion_texto_titulo);
        asignador.setText(nombreLista);

        fillData(idLista);
        registerForContextMenu(mList);

        final Context activity = this;
        final EditText txtUrl = new EditText(activity);
        buttonPlay = (Button) findViewById(R.id.SeeListaReproduccion_boton_play);
        buttonSiguiente = (Button) findViewById(R.id.SeeListaReproduccion_boton_siguiente);

        rutas = new LinkedList<String>();

        Cursor notesCursor = dbHelper.fetchCancionByLista(idLista);
        notesCursor.moveToFirst();
        int contador = notesCursor.getCount();
        for(int i=0;i<contador;i++){
            String ruta = notesCursor.getString(notesCursor.getColumnIndexOrThrow("ruta"));
            notesCursor.moveToNext();
            rutas.add(ruta);
        }


        buttonPlay.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                play();
            }
        });

        buttonSiguiente.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.d("Lista","Se pulsa siguiente");
                    playEverything();
            }

        });
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

    private void play() {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            pos = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
        } else {
            try {
                String ruta = rutas.get(lista);
                //mMediaPlayer = new MediaPlayer();
                File file = new File(ruta);
                FileInputStream inputStream = new FileInputStream(file);
                mMediaPlayer.setDataSource(inputStream.getFD());
                inputStream.close();
                //mMediaPlayer.setDataSource(ruta);
                Log.d("Play", "Fichero encontrado " + ruta);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.seekTo(pos);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        lista++;
                        try {
                            rutas.get(lista);
                        } catch (IndexOutOfBoundsException e) {
                            lista = 0;
                        }
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                        play(rutas.get(lista));
                    }

                });
            } catch (IOException e) {
                Log.d("Play", "No existe el fichero ");
            }
        }
    }

    private void play(String ruta){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
        }
            try {
                //mMediaPlayer = new MediaPlayer();
                File file = new File(ruta);
                FileInputStream inputStream = new FileInputStream(file);
                mMediaPlayer.setDataSource(inputStream.getFD());
                inputStream.close();
                //mMediaPlayer.setDataSource(ruta);
                Log.d("Play", "Fichero encontrado " + ruta);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                Log.d("Play", "No existe el fichero ");
            }
    }

    private void playEverything(){

        lista++;
        try{
            rutas.get(lista);
        } catch(IndexOutOfBoundsException e){
            lista = 0;
        }

        play(rutas.get(lista));
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                lista++;
                try{
                    rutas.get(lista);
                } catch(IndexOutOfBoundsException e){
                    lista = 0;
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = new MediaPlayer();
                play(rutas.get(lista));
            }

        });
    }
}
