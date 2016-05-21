package sevenbits.sevenbeats;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Tengo que tener un menu lateral que hay que rellenar con
 * las distintas opciones. De primeras se rellena con las canciones.
 *
 */
public class MainActivity extends AppCompatActivity {

    private BaseDatosAdapter bbdd;
    public static final String CANCION_NOMBRE = "titulo";
    public static final String ALBUM_NOMBRE = "titulo";
    public static final String ARTISTA_NOMBRE = "nombre";


    private ListView listaPrincipal;
    private ListView listaMenu;
    private GridView gridPrincipal;
    private DrawerLayout drawerPrincipal;
    private ArrayList<String> contenidoListaMenu;

    public static final int EDIT_ID = Menu.FIRST;
    public static final int DELETE_ID = Menu.FIRST + 1;
    public static final int SEE_ID = Menu.FIRST + 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    protected void onResume(){
        super.onResume();
        fillData();
    }

    private void setHandler(){
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
                Intent i = new Intent(getApplicationContext(), SeeSong.class);
                i.putExtra("SeeCancion_cancion", id);
                startActivity(i);
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Debug", "Al menu intenta entrar");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
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
                    default:
                        return true;
                }
        }
        return super.onContextItemSelected(item);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Rellenamos la informacion de la lista de canciones
     */
    private void fillSongData() {
        Cursor cursor = bbdd.fetchAllCancionesByABC();
        String[] fromColumns = {CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list,
                cursor, fromColumns, toViews);
        gridPrincipal.setAdapter(null);
        listaPrincipal.setAdapter(adapter);
    }

    private void fillAlbumData() {
        Cursor cursor = bbdd.fetchAllAlbumsByABC();
        GridCursorAdapter adapter = new GridCursorAdapter(this, cursor,
                getResources().getDisplayMetrics().widthPixels);
        listaPrincipal.setAdapter(null);
        gridPrincipal.setAdapter(adapter);
    }

    /**
     * Rellena la lista principal con la informacion de artistas que aparezcan en la base de datos
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

    private void fillListaMenuData() {
        contenidoListaMenu  = new ArrayList<String>();
        contenidoListaMenu.add("Canciones");
        contenidoListaMenu.add("Álbumes");
        contenidoListaMenu.add("Artistas");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_activity_listmenu,
                contenidoListaMenu);
        listaMenu.setAdapter(adapter);
        listaMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch(position){
                    case 0:
                        getIntent().putExtra("queMostrar",0);
                        fillData();
                        break;
                    case 1:
                        getIntent().putExtra("queMostrar",1);
                        fillData();
                        break;
                    case 2:
                        getIntent().putExtra("queMostrar", 2);
                        fillData();
                        // Toast.makeText(getApplicationContext(),"Artistas",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"Nada",Toast.LENGTH_SHORT).show();
                        break;
                }
                drawerPrincipal.closeDrawer(listaMenu);
            }
        });
    }
}