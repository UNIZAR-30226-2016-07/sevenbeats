package sevenbits.sevenbeats;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.Image;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;

/**
 * Created by Javi on 25/05/2016.
 */
public class PonerImagenArtista implements Runnable{

    private String ruta, artistaN;
    private long artistId;
    private BaseDatosAdapter dbHelper;
    private Context context;
    private boolean isURL;


    public PonerImagenArtista(String ruta, long artistId, boolean isURL, BaseDatosAdapter bbdd, Context context,
                              String artistaN){
        this.ruta = ruta;
        this.context = context;
        this.isURL = isURL;
        dbHelper = bbdd;
        this.artistId = artistId;
        this.artistaN = artistaN;
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
                File myPath0 = new File(dirImages, artistId + ".jpg");
                if ( myPath0.exists() ) myPath0.delete();
                File myPath = new File(dirImages, artistId + ".jpg");

                FileOutputStream fos = new FileOutputStream(myPath);
                fos.write(response);
                fos.flush();
                fos.close();
                Log.d("Debug", "Al poner caratula en thread ruta da: " + myPath.getAbsolutePath());
                correcto = dbHelper.updateArtista(artistId, myPath.getAbsolutePath(), artistaN);

            }
            else{
                ContextWrapper cw = new ContextWrapper(context);
                File dirImages = cw.getDir("Imagenes", Context.MODE_PRIVATE);
                File myPath = new File(dirImages, artistId + ".jpg");
                File original = new File(ruta);
                myPath.delete();
                myPath.createNewFile();
                Log.d("Debug","Al poner caratula en thread ruta da: " + original.exists() + " " + original.getName());
                copy(original, myPath);
                correcto = dbHelper.updateArtista(artistId, myPath.getAbsolutePath(), artistaN);
            }
            Log.d("Debug","Al poner caratula en thread da: " + correcto);

        } catch (Exception e) {
            Log.d("Error",e.getMessage());
        }
    }

    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


}
