package sevenbits.sevenbeats;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by pablo on 10/4/16.
 */


public class GridCursorAdapter extends CursorAdapter {


    private String imagenDefecto = "drawable://" + R.drawable.default_image;

    public GridCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.main_activity_gridview, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView titulo = (TextView) view.findViewById(R.id.MainActivity_texto_nombrealbum);
        ImageView imagen = (ImageView) view.findViewById(R.id.MainActivity_imagen_fotoalbum);
        // Extract properties from cursor
        String title = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
        String rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow("ruta"));
        if ( rutaImagen != null && !rutaImagen.equals("poner ruta") ){
            Log.d("Debug", "Intenta poner imagen bbdd");
            imagen.setImageURI(Uri.parse(rutaImagen));
        }
        else{
            Log.d("Debug", "Intenta poner imagen defecto");
            imagen.setImageURI(Uri.parse("android.resource://"+"sevenbits.sevenbeats"+"/"+"drawable/default_image"));
        }
        // Populate fields with extracted properties
        titulo.setText(title);
    }
}
