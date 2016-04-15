package sevenbits.sevenbeats;

/**
 * Created by Javi on 15/04/2016.
 */
public class ActualizarRating implements  Runnable {

    long idCancionInterno;
    float rating;
    BaseDatosAdapter dbHelper;

    public ActualizarRating(long idCancionInterno, float rating, BaseDatosAdapter dbHelper){
        this.idCancionInterno = idCancionInterno;
        this.rating = rating;
        this.dbHelper = dbHelper;
    }

    @Override
    public void run(){

        dbHelper.updateValoracion(idCancionInterno, rating);

    }

}
