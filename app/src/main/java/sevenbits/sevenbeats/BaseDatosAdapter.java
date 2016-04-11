package sevenbits.sevenbeats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDatosAdapter {

    private static final String TAG = " BaseDatosAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String TABLE_ARTIST_CREATE =
            "create table artistas (_id integer primary key autoincrement, " +
                    "nombre text not null);";
    private static final String TABLE_ALBUM_CREATE =
            "create table albums (_id integer primary key autoincrement, " +
                    "titulo text not null," +
                    "ruta text not null" +
                    " artista integer, " +
                    "foreign key (artista) references artistas(_id));";
    private static final String TABLE_SONG_CREATE =
            "create table canciones (_id integer primary key autoincrement, " +
                    "titulo text not null, " +
                    "duracion text not null, " +
                    "valoracion integer, " +
                    "album integer, " +
                    "foreign key (album) references albums(_id));";


    private static final String DATABASE_NAME = "sevenbeats";
    private static final String DATABASE_TABLE_ARTISTAS = "artistas";
    private static final String DATABASE_TABLE_ALBUMS = "albums";
    private static final String DATABASE_TABLE_CANCIONES = "canciones";
    private static final int DATABASE_VERSION = 2;

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

            db.execSQL("insert into categories (title) values ('Defecto');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS artistas");
            db.execSQL("DROP TABLE IF EXISTS canciones");
            db.execSQL("DROP TABLE IF EXISTS albums");

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

    public long createArtista(String name){
        ContentValues args = new ContentValues();
        args.put("nombre",name);
        //meter todos los atributos

        return mDb.insert(DATABASE_TABLE_ARTISTAS, null, args);
    }

    public boolean deleteArtista(long rowId){

        return mDb.delete(DATABASE_TABLE_ARTISTAS, "_id" + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllArtistasByABC(){

        return mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id","nombre"}
                , null,null,null,null,"nombre");
    }

    public Cursor fetchArtista(long rowId) throws SQLException{

        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_ARTISTAS, new String[] {"_id","nombre"},
                        "_id = "+rowId, null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateArtista(long rowId, String name){
        ContentValues args = new ContentValues();
        args.put("nombre",name);
        //meter todos los atributos

        return mDb.update(DATABASE_TABLE_ARTISTAS, args, "_id = " + rowId, null) > 0;
    }

    public long createAlbum(String titulo,String ruta, String artista){
        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("ruta",ruta);

        if(existArtista(artista)){

            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id", "nombre"},
                            "nombre = '" + artista + "'", null, null, null, null, null);
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            args.put("artista",id);

        } else {

            //crear artista
        }

        return mDb.insert(DATABASE_TABLE_ARTISTAS, null, args);
    }

    public boolean deleteAlbum(long rowId){

        return mDb.delete(DATABASE_TABLE_ALBUMS, "_id" + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllAlbumsByABC(){

        return mDb.query(DATABASE_TABLE_ALBUMS, new String[] {"_id","titulo","ruta","artista"}
                , null,null,null,null,"titulo");
    }

    public Cursor fetchAlbum(long rowId) throws SQLException{

        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_ALBUMS, new String[] {"_id","titulo","ruta","artista"},
                        "_id = "+rowId, null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateAlbum(long rowId, String titulo, String ruta, String artista){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("ruta",ruta);
        //meter todos los atributos

        if(existArtista(artista)){

            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ARTISTAS, new String[] {"_id", "nombre"},
                            "nombre = '" + artista + "'", null, null, null, null, null);
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            args.put("artista",id);

        } else {

            //crear artista
        }

        return mDb.update(DATABASE_TABLE_ARTISTAS, args, "_id = " + rowId, null) > 0;
    }

    public long createCancion(String titulo, String duracion, int valoracion, String album){

        ContentValues args = new ContentValues();
        args.put("titulo",titulo);
        args.put("duracion",duracion);
        args.put("valoracion",valoracion);

        if(existAlbum(album)){

            Cursor cursor =
                    mDb.query(DATABASE_TABLE_ALBUMS,
                            new String[] {"_id","titulo","duracion","valoracion","album"},
                            "titulo = '" + titulo + "'", null, null, null, null, null);
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            args.put("artista",id);

        } else {

            //crear album
        }

        return mDb.insert(DATABASE_TABLE_CANCIONES, null, args);
    }

    public boolean deleteCancion(long rowId){

        return mDb.delete(DATABASE_TABLE_CANCIONES, "_id" + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllCancionesByABC(){

        return mDb.query(DATABASE_TABLE_CANCIONES,
                new String[] {"_id","titulo","duracion","valoracion","album"}
                , null,null,null,null,"titulo");
    }

    public Cursor fetchCancion(long rowId) throws SQLException{

        Cursor cursor =
                mDb.query(true,DATABASE_TABLE_CANCIONES,
                        new String[] {"_id","titulo","duracion","valoracion","album"},
                        "_id = "+rowId, null, null, null, null, null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    //buscar canciones dado album(su id)
    public Cursor fetchCancionByAlbum(long rowId){

        return mDb.query(true,DATABASE_TABLE_CANCIONES,
                new String[] {"_id","titulo","duracion","valoracion","album"},
                "album = "+rowId, null, null, null, null, null);
    }

    public boolean updateCancion(long rowId, String titulo, String duracion, int valoracion, int album){

        return false;
    }


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
}
