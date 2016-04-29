package sevenbits.sevenbeats;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.Image;
import android.os.Debug;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Javi on 14/04/2016.
 */
public class PonerCaratula implements Runnable {

    private String ruta, nombreAlbum, artista;
    private long albumId;
    private BaseDatosAdapter dbHelper;
    private Context context;
    private boolean isURL;

    public PonerCaratula(String ruta, long albumId, String nombreAlbum, String artista, boolean isURL, BaseDatosAdapter dbHelper, Context context){
        this.ruta = ruta;
        this.albumId = albumId;
        this.nombreAlbum = nombreAlbum;
        this.artista = artista;
        this.dbHelper = dbHelper;
        this.context = context;
        this.isURL = isURL;
    }

    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     * @return true si solo si el proceso se ha realizado de forma correcta.
     */
    public void run(){
        Image image = null;
        boolean correcto;
        try {
            if ( isURL ) {
                URL url = new URL(ruta);
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();

                ContextWrapper cw = new ContextWrapper(context);
                File dirImages = cw.getDir("Imagenes", Context.MODE_PRIVATE);
                File myPath = new File(dirImages, nombreAlbum + ".jpg");
                FileOutputStream fos = new FileOutputStream(myPath);
                fos.write(response);
                fos.close();
                correcto = dbHelper.updateAlbum(albumId, nombreAlbum, myPath.getAbsolutePath(), artista);
            }
            else{
                correcto = dbHelper.updateAlbum(albumId, nombreAlbum, ruta, artista);
            }
            Log.d("Debug","Al poner caratula en thread da: " + correcto);

        } catch (Exception e) {
            Log.d("Error",e.getMessage());
        }
    }
}
