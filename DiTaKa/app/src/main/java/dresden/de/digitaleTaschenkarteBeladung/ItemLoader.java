package dresden.de.digitaleTaschenkarteBeladung;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import dresden.de.digitaleTaschenkarteBeladung.data.EquipmentItem;
import dresden.de.digitaleTaschenkarteBeladung.util.Util_Http;


/**
 * Loader für die Gegenstände
 */
public class ItemLoader extends AsyncTaskLoader<List<EquipmentItem>> {

    private static final String LOG_TAG = "ItemLoader_LOG";

    public ItemLoader(Context context){
        super(context);
    }

    //Hauptmethode der Klasse. Bewältigt die Hintergrundarbeit
    @Override
    public List<EquipmentItem> loadInBackground() {

        Util_Http utilities = new Util_Http();

        return utilities.requestItems();
    }



}