package sevenbits.sevenbeats;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Tengo que tener un menu lateral que hay que rellenar con
 * las distintas opciones. De primeras se rellena con las canciones.
 *
 */
public class MainActivity extends AppCompatActivity {

    private BaseDatosAdapter bbdd;
    private static final String CANCION_NOMBRE = "titulo";

    private MediaPlayer Mp;
    private ListView listaPrincipal;
    private ListView listaMenu;
    private GridView gridPrincipal;
    private DrawerLayout drawerPrincipal;
    private ArrayList<String> contenidoListaMenu;
    private int queMostrar=0;

    private static final int EDIT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ruido al abrir la aplicacion
        Mp = MediaPlayer.create(this, R.raw.sonido_inicio_app);
        // Mp.start();
        bbdd = new BaseDatosAdapter(this);
        bbdd.open();
        // rellenar lista lateral y lista central
        setContentView(R.layout.main_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        MenuItem boton = (MenuItem) findViewById(android.R.id.home);
        boton.setIcon(R.drawable.ic_drawer);

        listaPrincipal = (ListView) findViewById(R.id.MainActivity_lista_principal);
        listaMenu = (ListView) findViewById(R.id.MainActivity_lista_menu);
        gridPrincipal = (GridView) findViewById(R.id.MainActivity_lista_cuadrada);
        drawerPrincipal = (DrawerLayout) findViewById(R.id.drawer_layout);

        setHandler();
        fillData();
        fillListaMenuData();
    }

    private void setHandler(){
        gridPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), SeeAlbum.class);
                Log.d("Debug", "El id es: " + id);
                i.putExtra("SeeAlbum_album",id);
                startActivity(i);
            }
        });
        listaPrincipal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), SeeSong.class);
                i.putExtra("SeeCancion_cancion",id);
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
            default:
                fillSongData();
                break;
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
        switch(item.getItemId()) {
            case EDIT_ID:
                Intent i = new Intent(this, SongEdit.class);
                startActivity(i);
                return true;
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                bbdd.deleteCancion(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.MainActivity_boton_busqueda:
                return true;
            case R.id.MainActivity_boton_anyadir:
                Intent i = new Intent(this, SongEdit.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillSongData() {
        Cursor cursor = bbdd.fetchAllCancionesByABC();
        String[] fromColumns = {CANCION_NOMBRE};
        int[] toViews = {R.id.MainActivity_texto_testolista};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_activity_list, cursor,
                fromColumns, toViews);
        gridPrincipal.setAdapter(null);
        listaPrincipal.setAdapter(adapter);
    }

    private void fillAlbumData(){
        Cursor cursor = bbdd.fetchAllAlbumsByABC();
        GridCursorAdapter adapter = new GridCursorAdapter(this, cursor);
        listaPrincipal.setAdapter(null);
        gridPrincipal.setAdapter(adapter);
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
                        Toast.makeText(getApplicationContext(),"Artistas",Toast.LENGTH_SHORT).show();
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