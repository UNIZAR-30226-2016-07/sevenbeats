package sevenbits.sevenbeats;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tengo que tener un menu lateral que hay que rellenar con
 * las distintas opciones. De primeras se rellena con las canciones.
 *
 */
public class MainActivity extends AppCompatActivity {

    private BaseDatosAdapter bbdd;
    private MediaPlayer mMediaPlayer;

    public static final String CANCION_NOMBRE = "titulo";
    public static final String ALBUM_NOMBRE = "titulo";
    public static final String ARTISTA_NOMBRE = "nombre";
    public static final String LISTA_NOMBRE = "nombre";


    private ListView listaPrincipal;
    private ListView listaMenu;
    private GridView gridPrincipal;
    private DrawerLayout drawerPrincipal;
    private ArrayList<String> contenidoListaMenu;

    private int queOrden = 0;

    public static final int EDIT_ID = Menu.FIRST;
    public static final int DELETE_ID = Menu.FIRST + 1;
    public static final int SEE_ID = Menu.FIRST + 2;
    public static final int ADD_ID = Menu.FIRST + 3;
    public static  final int PLAY_ID = Menu.FIRST + 4;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        verifyStoragePermissions(this);
        mMediaPlayer = new MediaPlayer();
        super.onCreate(savedInstanceState);
        // ruido al abrir la aplicacion
        setContentView(R.layout.main_activity);

        bbdd = new BaseDatosAdapter(this);
        bbdd.open();
        // rellenar lista lateral y lista central
        setContentView(R.layout.main_activity);


        listaPrincipal = (ListView) findViewById(R.id.MainActivity_lista_principal);
        listaMenu = (ListView) findViewById(R.id.MainActivity_lista_menu);
        gridPrincipal = (GridView) findViewById(R.id.MainActivity_lista_cuadrada);
        drawerPrincipal = (DrawerLayout) findViewById(R.id.drawer_layout);


        final float ancho = getResources().getDisplayMetrics().widthPixels;
        gridPrincipal.setColumnWidth((int) (ancho / 3));
        setHandler();
        fillData();
        fillListaMenuData();
        registerForContextMenu(listaPrincipal);
        registerForContextMenu(gridPrincipal);
    }

    protected void onResume() {
        super.onResume();
        fillData();
        setHandler();
    }

    private void setHandler() {
        gridPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), SeeAlbum.class);
                Log.d("Debug", "El id es: " + id);
                i.putExtra("SeeAlbum_album", id);
                startActivity(i);
            }
        });

        listaPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent;
                int i = getIntent().getIntExtra("queMostrar", 0);
                // segun sea una cancion o una artista lanzamos la actividad correspondiente
                switch (i) {
                    case 0:
                        intent = new Intent(getApplicationContext(), SeeSong.class);
                        intent.putExtra("SeeCancion_cancion", id);
                        startActivity(intent);
                        break;
                    case 2:

                        intent = new Intent(getApplicationContext(), SeeArtist.class);
                        intent.putExtra("id_artista", id);
                        startActivity(intent); //ESTO FALLA
                        break;
                    case 3:
                        intent = new Intent(getApplicationContext(), SeeListaReproduccion.class);
                        String nombre = bbdd.fetchLista(id);
                        intent.putExtra("SeeListaReproduccion_album", nombre);
                        startActivity(intent);
                    default:
                        break;
                }
            }
        });
    }

    private void fillData(){
        int i = getIntent().getIntExtra("queMostrar",0);
        switch (i){
            case 0:
                setTitle("Canciones");
                fillSongData();
                break;
            case 1:
                setTitle("Albums");
                fillAlbumData();
                break;
            case 2:
                setTitle("Artistas");
                fillArtistData();
                break;
            case 3:
                setTitle("Listas de reproducción");
                fillListaData();
                break;
            default:
                fillSongData();
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //int orientation = this.getResources().getConfiguration().orientation;
        final float ancho = getResources().getDisplayMetrics().widthPixels;

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridPrincipal.setColumnWidth((int) (ancho / 3));
            Log.d("Debug", "Vertical " + (ancho / 3));
            gridPrincipal.refreshDrawableState();
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridPrincipal.setColumnWidth((int) (ancho / 3));
            Log.d("Debug", "Horizontal" + (ancho / 3));
            gridPrincipal.refreshDrawableState();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, "Editar");
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Borrar");
        menu.add(Menu.NONE, ADD_ID, Menu.NONE, "Anadir a lista de reproduccion");
        menu.add(Menu.NONE, PLAY_ID, Menu.NONE, "Reproducir");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Debug", "Al menu intenta entrar");
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        try{
        switch(item.getItemId()) {
            case EDIT_ID:
                int gridOP = getIntent().getIntExtra("queMostrar",0);
                switch(gridOP){
                    case 0:
                        Intent i = new Intent(this, SongEdit.class);
                        i.putExtra("id_cancion", info.id);
                        startActivity(i);
                        fillData();
                        return true;
                    case 1:
                        //ITERACION 2
                        return true;
                    case 2:
                        i = new Intent(this, SeeArtist.class);
                        Cursor aux = bbdd.fetchArtista(info.id);
                        aux.moveToFirst();
                        Log.d("Invocar artist", aux.getString(aux.getColumnIndexOrThrow("nombre")));
                        i.putExtra("id_artista", info.id);
                        startActivity(i);
                        fillData();
                        return true;
                    case 3:
                        i = new Intent(this, SeeListaReproduccion.class);
                        String nombre = bbdd.fetchLista(info.id);
                        i.putExtra("SeeListaReproduccion_album", nombre);
                        startActivity(i);
                        return true;
                    default:
                        return true;
                }
            case DELETE_ID:

                gridOP = getIntent().getIntExtra("queMostrar",0);
                switch(gridOP){
                    case 0:
                        bbdd.deleteCancion(info.id);
                        fillData();
                        return true;
                    case 1:
                        bbdd.deleteAlbum(info.id);
                        fillData();
                        return true;
                    case 2:
                        bbdd.deleteArtista(info.id);
                        fillData();
                        return true;
                    case 3:
                        bbdd.deleteList(info.id);
                        fillData();
                        return true;
                    default:
                        return true;
                }
            case ADD_ID:
                gridOP = getIntent().getIntExtra("queMostrar",0);

                switch(gridOP) {
                    case 0:
                        //Anadir cancion a la lista de reproduccion
                        final EditText txtLista = new EditText(this);
                        txtLista.setHint("Pon aqui el nombre de la lista. (Si no existe se creará");
                        new AlertDialog.Builder(this)
                                .setTitle("Añadir a lista")
                                .setView(txtLista)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String url = txtLista.getText().toString();
                                        long id = bbdd.fetchIdLista(url);
                                        if ( id == -1){
                                            bbdd.createList(url);
                                            id = bbdd.fetchIdLista(url);
                                        }
                                        bbdd.addSongToList(id, info.id);

                                    }

                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                        return true;
                    case 1:
                        //ITERACION 2
                        return true;
                    default:
                        return true;
                }
            case PLAY_ID:
                gridOP = getIntent().getIntExtra("queMostrar",0);

                switch(gridOP){
                    case 0:
                        //Reproducir cancion
                        Cursor aux = bbdd.fetchCancion(info.id);
                        play(aux.getString(aux.getColumnIndexOrThrow("ruta")));
                        return true;
                    case 1:
                        //ITERACION 2
                        return true;
                    default:
                        return true;
                }
        }
        return super.onContextItemSelected(item);
        }catch(Exception e){
            Log.e("Error", e.getMessage());
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.MainActivity_boton_busqueda).getActionView();

        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.MainActivity_boton_anyadir:
                Intent i = new Intent(this, SongEdit.class);
                startActivity(i);
                fillData();
                return true;
            case R.id.MainActivity_boton_ordenABC:
                queOrden=0;
                fillData();
                return true;
            case R.id.MainActivity_boton_ordenArtista:
                queOrden=1;
                fillData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Rellenamos la informacion de la lista de canciones.
     */
    private void fillSongData() {
        Cursor cursor;

        if(queOrden==0) cursor = bbdd.fetchAllCancionesByABC();
        else cursor = bbdd.fetchAllCancionesByArtista();

        String[] fromColumns = {CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list,
                cursor, fromColumns, toViews);
        gridPrincipal.setAdapter(null);
        listaPrincipal.setAdapter(adapter);
    }

    /**
     * Rellenamos la informacion del grid de caratulas de los albumes.
     */
    private void fillAlbumData() {
        Cursor cursor = bbdd.fetchAllAlbumsByABC();
        GridCursorAdapter adapter = new GridCursorAdapter(this, cursor,
                getResources().getDisplayMetrics().widthPixels);
        listaPrincipal.setAdapter(null);
        gridPrincipal.setAdapter(adapter);
    }

    /**
     * Rellena la lista principal con la informacion de artistas que aparezcan en la base de datos.
     */
    private void fillArtistData() {
        Cursor cursor = bbdd.fetchAllArtistasByABC();
        String[] fromColumns = {ARTISTA_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, cursor,
                fromColumns, toViews);
        gridPrincipal.setAdapter(null);
        listaPrincipal.setAdapter(adapter);
    }

    /**
     * Rellenamos la lista principal con la informacion de listas de reproduccion.
     */
    private void fillListaData() {
        Cursor cursor = bbdd.fetchAllListasByABC();
        String[] fromColumns = {LISTA_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, cursor,
                fromColumns, toViews);
        gridPrincipal.setAdapter(null);
        listaPrincipal.setAdapter(adapter);
    }

    /**
     * Rellenamos el menu lateral que nos permite cambiar entre canciones, albumes, artistas y
     * listas de reproduccion.
     */
    private void fillListaMenuData() {
        contenidoListaMenu  = new ArrayList<String>();
        contenidoListaMenu.add("Canciones");
        contenidoListaMenu.add("Álbumes");
        contenidoListaMenu.add("Artistas");
        contenidoListaMenu.add("Listas de reproducción");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_activity_listmenu,
                contenidoListaMenu);
        listaMenu.setAdapter(adapter);
        listaMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        getIntent().putExtra("queMostrar", 0);
                        fillData();
                        break;
                    case 1:
                        getIntent().putExtra("queMostrar", 1);
                        fillData();
                        break;
                    case 2:
                        getIntent().putExtra("queMostrar", 2);
                        fillData();
                        // Toast.makeText(getApplicationContext(),"Artistas",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        getIntent().putExtra("queMostrar", 3);
                        fillData();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Nada", Toast.LENGTH_SHORT).show();
                        break;
                }
                drawerPrincipal.closeDrawer(listaMenu);
            }
        });
    }

    private void play(String ruta) {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
            return;
        }
        try{
            File file = new File(ruta);
            Log.d("Play","Aqui");
            FileInputStream inputStream = new FileInputStream(file);
            mMediaPlayer.setDataSource(inputStream.getFD());
            inputStream.close();
            //mMediaPlayer.setDataSource(ruta);
            Log.d("Play", "Fichero encontrado " + ruta);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
        catch(IOException e){
            Log.e("Play",e.getMessage());
            Log.d("Play","No existe el fichero " +ruta);
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}