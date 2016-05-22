package sevenbits.sevenbeats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDatosAdapter {

    public static String rutaDefecto = "android.resource://sevenbits.sevenbeats/drawable/default_image";

    private static final String TAG = " BaseDatosAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String TABLE_ARTIST_CREATE =
            "create table artistas (_id integer primary key autoincrement, " +
                    "nombre text not null);";
    private static final String TABLE_LIST_CREATE =
            "create table listas (_id integer primary key autoincrement, " +
                    "nombre text not null);";
    private static final String TABLE_ASIGNAR_SONG_CREATE =
            "create table asignaciones (_id integer primary key autoincrement, " +
                    "lista integer not null, "+
                    "cancion integer not null, " +
                    "posicion integer not null, " +
                    "foreign key(lista) references listas(_id), " +
                    "foreign key(cancion) references canciones(_id));";
    private static final String TABLE_ALBUM_CREATE =
            "create table albums (_id integer primary key autoincrement, " +
                    "titulo text not null," +
                    "ruta text not null," +
                    " artista integer, " +
                    "foreign key (artista) references artistas(_id));";
    private static final String TABLE_SONG_CREATE =
            "create table canciones (_id integer primary key autoincrement, " +
                    "titulo text not null, " +
                    "duracion text not null, " +
                    "valoracion integer, " +
                    "album integer, " +
                    "genero text not null," +
                    "ruta text, " +
                    "foreign key (album) references albums(_id));";


    private static final String DATABASE_NAME = "sevenbeats";
    private static final String DATABASE_TABLE_ARTISTAS = "artistas";
    private static final String DATABASE_TABLE_ALBUMS = "albums";
    private static final String DATABASE_TABLE_CANCIONES = "canciones";
    private static final String DATABASE_TABLE_LIST = "listas";
    private static final String DATABASE_TABLE_ASIGNAR_SONG_CREATE = "asignaciones";


    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;


    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(TABLE_ARTIST_CREATE);
            db.execSQL(TABLE_ALBUM_CREATE);
            db.execSQL(TABLE_SONG_CREATE);
            db.execSQL(TABLE_LIST_CREATE);
            db.execSQL(TABLE_ASIGNAR_SONG_CREATE);


            db.execSQL("insert into artistas (nombre) values ('Artista Desconocido');");
            db.execSQL("insert into albums (titulo,ruta,artista) values " +
                    "('Album Desconocido','"+rutaDefecto+"',1);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS artistas");
            db.execSQL("DROP TABLE IF EXISTS canciones");
            db.execSQL("DROP TABLE IF EXISTS albums");
            db.execSQL("DROP TABLE IF EXISTS listas");
            db.execSQL("DROP TABLE IF EXISTS asignaciones");



            onCreate(db);
        }
    }

    public BaseDatosAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public BaseDatosAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /*
     Inserta en la BD un artista de nombre=@name
     */
    public long createArtista(String name){
        ContentValues args = new ContentValues();
        args.put("nombre",name);
        //meter todos los atributos

        return mDb.insert(DATABASE_TABLE_ARTISTAS, null, args);
    }

    /*
    Borra de la BD un artista con _id=@rowId
     */
    public boolean deleteArtista(long rowId){

        return mDb.delete(DATABASE_TABLE_ARTISTAS, "_id" + "=" + rowId, null) > 0;
    }

    /*
    Devuelve un Cursor a todos los artistas ordenados por orden alfabético
     */
    public Cursor fetchAllArtistasByABC(){

        return mDb.query(DATABASE_TABLE_ARTISTAS, new String[]{"_id", "nombre"}
                , null, null, null, null, "nombre");
    }

    /*
    Devuelve el artista con _id=@rowId
     */
    public Cursor fetchArtista(long rowId) throws SQLException{

        return mDb.query(true,DATABASE_TABLE_ARTISTAS, new String[] {"_id","nombre"},
                "_id = "+rowId, null, null, null, null, null);
    }

    /*
    Da el valor @name al nombre de un artista ya existente con _id=@rowId
     */
    public boolean updateArtista(long rowId, String name){
        ContentValues args = new ContentValues();
        args.put("nombre",name);
        //meter todos los atributos

        return mDb.update(DATABASE_TABLE_ARTISTAS, args, "_id = " + rowId, null) > 0;
    }

    /*
    Crea un album con los valores @titulo, @ruta y @artista. En caso de que @artista no sea
    el nombre de un artista ya existente lo crea.
     */
    public long createAlbum(String titulo,String ruta, String artista){
        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("ruta",ruta);

        if(existArtista(artista)){
            //el artista ya existe, buscamos su id y lo añadimos a los argumentos
            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id", "nombre"},
                            "nombre = '" + artista + "'", null, null, null, null, null);
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            args.put("artista",id);

        } else {
            //el artista no existe, lo creamos, buscamos su id y lo añadimos a los args
            ContentValues aux = new ContentValues();
            aux.put("nombre", artista);
            long id = mDb.insert(DATABASE_TABLE_ARTISTAS,null,aux);
            args.put("artista", id);
        }

        return mDb.insert(DATABASE_TABLE_ALBUMS, null, args);
    }

    /*
    Borra de la BD un album con _id=@rowId
     */
    public boolean deleteAlbum(long rowId){

        return mDb.delete(DATABASE_TABLE_ALBUMS, "_id" + "=" + rowId, null) > 0;
    }

    /*
    Devuelve un Cursor a todos los albums ordenados por orden alfabético
     */
    public Cursor fetchAllAlbumsByABC(){

        return mDb.query(DATABASE_TABLE_ALBUMS, new String[]{"_id", "titulo", "ruta", "artista"}
                , null, null, null, null, "titulo");
    }

    /*
    Devuelve el album con _id=@rowId
     */
    public Cursor fetchAlbum(long rowId) throws SQLException{

        return mDb.query(true,DATABASE_TABLE_ALBUMS, new String[] {"_id","titulo","ruta","artista"},
                "_id = '"+rowId+"'", null, null, null, null, null);
    }

    /*
    Actualiza los valores de un album con _id=@rowId. En caso de que @artista no corresponda a
    ningún artista existente en la BD lo crea
     */
    public boolean updateAlbum(long rowId, String titulo, String ruta, String artista){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("ruta",ruta);
        //meter todos los atributos
        Log.d("Debug", "En updateAlbum");
        if(existArtista(artista)){
            //el artista ya existe, buscamos su id y lo añadimos a los argumentos
            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id", "nombre"},
                            "nombre = '" + artista + "'", null, null, null, null, null);
            cursor.moveToFirst();
            long id = cursor.getInt(0);
            cursor.close();
            args.put("artista",id);
            Log.d("Debug", "En existe Artista");

        } else {
            //el artista no existe, lo creamos, buscamos su id y lo añadimos a los args
            ContentValues aux = new ContentValues();
            aux.put("nombre", artista);
            long id = mDb.insert(DATABASE_TABLE_ARTISTAS,null,aux);
            args.put("artista", id);
            Log.d("Debug", "En no existe artista");
        }
        Log.d("Debug", "El id del album vale: " + rowId);
        return mDb.update(DATABASE_TABLE_ALBUMS, args, "_id = " + rowId, null) > 0;
    }

    /*
    Actualiza los valores de un album con _id=@rowId. @artista es el _id del artista del album
    */
    public boolean updateAlbum1(long rowId, String titulo, String ruta, int artista){
        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("ruta", ruta);
        args.put("artista", artista);
        //meter todos los atributos

        return mDb.update(DATABASE_TABLE_ALBUMS, args, "_id = " + rowId, null) > 0;
    }

    /*
    Crea una canción con los valores @titulo, @duracion, @valoracion y @album.
    En caso de que @album no sea el nombre de un album ya existente lo crea.
     */
    public long createCancion(String titulo, String duracion, int valoracion, String album, String genero){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("duracion",duracion);
        args.put("valoracion",valoracion);
        args.put("genero",genero);

        if(existAlbum(album)){

            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ALBUMS,
                            new String[]{"_id", "titulo", "artista"},
                            "titulo = '" + album + "'", null, null, null, null, null);
            cursor.moveToFirst();
            long id = cursor.getInt(0);
            cursor.close();
            args.put("album", id);

        } else {
            //crear album
            ContentValues aux = new ContentValues();
            aux.put("titulo",album);
            aux.put("ruta", rutaDefecto);
            aux.put("artista",1);
            long id=mDb.insert(DATABASE_TABLE_ALBUMS, null,aux);
            args.put("album", id);
        }
        args.put("titulo",titulo);
        return mDb.insert(DATABASE_TABLE_CANCIONES, null, args);
    }

    /*
    Borra de la BD una canción con _id=@rowId
     */
    public boolean deleteCancion(long rowId){

        return mDb.delete(DATABASE_TABLE_CANCIONES, "_id" + "=" + rowId, null) > 0;
    }

    /*
    Devuelve un Cursor a todas las canciones ordenados por orden alfabético
     */
    public Cursor fetchAllCancionesByABC(){

        return mDb.query(DATABASE_TABLE_CANCIONES,
                new String[] {"_id","titulo","duracion","valoracion","album","genero"}
                , null,null,null,null,"titulo");
    }

    /*
    Devuelve la canción con _id=@rowId
     */
    public Cursor fetchCancion(long rowId) throws SQLException{

        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_CANCIONES,
                        new String[] {"_id","titulo","duracion","valoracion","album","genero"},
                        "_id = "+rowId, null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //buscar canciones dado album(su id)
    public Cursor fetchCancionByAlbum(long rowId){

        return mDb.query(true,DATABASE_TABLE_CANCIONES,
                new String[] {"_id","titulo","duracion","valoracion","album","genero"},
                "album = '"+rowId+"'", null, null, null, null, null);
    }

    /*
    Actualiza los valores de una canción con _id=@rowId. En caso de que @alubm no corresponda a
    ningun artista existente en la BD lo crea
     */
    public boolean updateCancion(long rowId, String titulo, String duracion, int valoracion, String album,String genero){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("duracion",duracion);
        args.put("valoracion",valoracion);
        args.put("genero",genero);

        if(existAlbum(album)){

            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ALBUMS,
                            new String[] {"_id","titulo","artista"},
                            "titulo = '" + album + "'", null, null, null, null, null);
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            args.put("album",id);

        } else {
            //crear album
            ContentValues aux = new ContentValues();
            aux.put("titulo",album);
            aux.put("artista", 1);                               //artista desconocido
            long id = mDb.insert(DATABASE_TABLE_ALBUMS,null,aux);
            args.put("album", id);
        }

        return mDb.update(DATABASE_TABLE_CANCIONES, args, "_id = " + rowId, null) > 0;
    }

    /*
    Actualiza los valores de una canción con _id=@rowId. @album es el _id del album de la canción
     */
    public boolean updateCancion(long rowId, String titulo, String duracion, int valoracion, int album,String genero){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("duracion",duracion);
        args.put("valoracion",valoracion);
        args.put("album",album);
        args.put("genero",genero);
        //meter todos los atributos

        return mDb.update(DATABASE_TABLE_CANCIONES, args, "_id = " + rowId, null) > 0;
    }

    /*
    Devuelve true si existe un artista con nombre=@comprobar, false en el resto de casos
     */
    public boolean existArtista(String comprobar){

        Cursor cursor =
                mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id","nombre"},
                        "nombre = '" + comprobar + "'", null, null, null, null, null);
        if (cursor.getCount() > 0) {
            // La query ejecutada devuelve algun resultado
            cursor.close();
            return true;
        }
        return false;
    }

    public Cursor searchSongs(String nombre){
        return mDb.query(DATABASE_TABLE_CANCIONES,
                new String[] {"_id","titulo","duracion","valoracion","album","genero"}
                , "titulo LIKE '%"+ nombre+ "%'",null,null,null,"titulo");
    }

    /*
    Devuelve true si existe un album con titulo=@comprobar, false en el resto de casos
     */
    public boolean existAlbum(String comprobar){

        Cursor cursor =
                mDb.query(DATABASE_TABLE_ALBUMS, new String[] {"_id","titulo", "ruta","artista"},
                        "titulo = '" + comprobar + "'", null, null, null, null, null);
        if (cursor.getCount() > 0) {
            // La query ejecutada devuelve algun resultado
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * Cambia la valoracion de una cancion en una base de datos dado su id.
     */
    public void updateValoracion(long id, float valoracion){
        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_CANCIONES,
                        new String[] {"_id","titulo","duracion","valoracion","album","genero"},
                        "_id = "+id, null, null, null, null, null);
        cursor.moveToFirst();
        String titulo=cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
        String duracion=cursor.getString(cursor.getColumnIndexOrThrow("duracion"));
        int album=cursor.getInt(cursor.getColumnIndexOrThrow("album"));
        String genero=cursor.getString(cursor.getColumnIndexOrThrow("genero"));
        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("duracion",duracion);
        args.put("valoracion",valoracion);
        args.put("album",album);
        args.put("genero",genero);
        mDb.update(DATABASE_TABLE_CANCIONES, args, "_id = " + id, null);

    }

    /**COSAS NUEVAS**/

    /**
     * Devuelve el id de una lista dado su nombre
     */
    public int fetchIdLista(String nombre){
        int res = -1;
        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_LIST,
                        new String[] {"_id", "nombre"},
                        "nombre = '"+nombre+"'", null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
            try{
                res = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            } catch(CursorIndexOutOfBoundsException e){

            }
        }
        return res;
    }

    public Cursor fetchCancionByLista(long id){
        return mDb.query(true,DATABASE_TABLE_ASIGNAR_SONG_CREATE,
                new String[] {"_id","lista","cancion", "posicion"},
                "lista = "+id, null, null, null, "posicion DESC", null);
    }

    public void deleteCancionLista(long lista, long cancion){
        mDb.delete(DATABASE_TABLE_ASIGNAR_SONG_CREATE, "(lista = " + lista + ") " +
                "AND (cancion = " + cancion+")", null);
    }

    public Cursor fetchAllListasByABC(){

        return mDb.query(DATABASE_TABLE_LIST,
                new String[] {"_id","nombre"}
                , null,null,null,null,"nombre");
    }

    public void addSongToList(long lista, long cancion){
        ContentValues args = new ContentValues();
        Cursor count = fetchCancion(lista);
        int posicion = count.getCount() + 1;
        args.put("lista",lista);
        args.put("cancion",cancion);
        args.put("posicion",posicion);
        mDb.insert(TABLE_ASIGNAR_SONG_CREATE, null, args);
    }

    public void createList(String nombre){
        ContentValues args = new ContentValues();
        args.put("nombre",nombre);
        mDb.insert(DATABASE_TABLE_LIST, null, args);
    }

    public void deleteList(String nombre){
        mDb.delete(DATABASE_TABLE_LIST, "nombre = " + nombre, null);
    }

    public void deleteList(int id){
        mDb.delete(DATABASE_TABLE_LIST, "_id = " + id, null);
    }


}
