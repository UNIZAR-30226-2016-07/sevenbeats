package sevenbits.sevenbeats;

import android.media.Image;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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

    public PonerCaratula(String ruta, long albumId, String nombreAlbum, String artista, BaseDatosAdapter dbHelper){
        this.ruta = ruta;
        this.albumId = albumId;
        this.nombreAlbum = nombreAlbum;
        this.artista = artista;
        this.dbHelper = dbHelper;
    }

    /**
     * Coge una imagen de internet y la guarda en la carpeta drawable. Luego, anota
     * esa caratula a la base de datos.
     *
     * @return true si solo si el proceso se ha realizado de forma correcta.
     */
    public void run(){
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

            FileOutputStream fos = new FileOutputStream("android.resource://"+"sevenbits.sevenbeats"+"/"+"drawable/"+ nombreAlbum + ".jpg");
            fos.write(response);
            fos.close();

            dbHelper.updateAlbum(albumId, nombreAlbum, nombreAlbum + ".jpg", artista);

        } catch (Exception e) {

        }
    }
}
