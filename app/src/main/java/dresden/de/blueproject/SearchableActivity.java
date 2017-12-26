package dresden.de.blueproject;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dresden.de.blueproject.daggerDependencyInjection.ApplicationForDagger;
import dresden.de.blueproject.data.EquipmentItem;
import dresden.de.blueproject.dataStructure.DatabaseEquipmentMininmal;
import dresden.de.blueproject.dataStructure.ItemAdapter;
import dresden.de.blueproject.viewmodels.ItemViewModel;
import dresden.de.blueproject.viewmodels.SearchViewModel;

public class SearchableActivity extends AppCompatActivity {

    private static final String LOG_TAG="SearchableActivity_LOG";

    ArrayList<EquipmentItem> equipmentItems;

    SearchViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    ArrayList<EquipmentItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        //Anweisung an Dagger, dass hier eine Injection vorgenommen wird ??
        ((ApplicationForDagger) this.getApplication())
                .getApplicationComponent()
                .inject(this);

        viewModel = ViewModelProviders.of(this,viewModelFactory)
                .get(SearchViewModel.class);

        itemList = new ArrayList<>();

        //Search-Intent abrufen und auswerten
        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            Bundle bundle = intent.getExtras();

            try {
                equipmentItems =  bundle.getParcelableArrayList("EL");
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Fehler beim Abrufen des Bundles in der SearchActivity! Cause: "+e.getCause() + " | Nachricht: " + e.getMessage());
            }

            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            search(searchQuery);
        }

        ListView lv = (ListView) this.findViewById(R.id.ListViewSearch);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Detailansicht anzeigen

                //TODO: Detailansicht aktivieren

                Intent intentMain = new Intent();
//                intentMain.

            }
        });

    }

    private void search(String query) {

        //Suchanfrange aufbereiten
        String[] keys = (query.toLowerCase()).split(" ");

        for (int i = 0; i<keys.length; i++) {
            String searchQuery = "%" + keys[i] + "%";

            viewModel.searchItems(searchQuery).observe(this, new Observer<List<DatabaseEquipmentMininmal>>() {
                @Override
                public void onChanged(@Nullable List<DatabaseEquipmentMininmal> databaseEquipmentMininmals) {
                    presentData(databaseEquipmentMininmals);
                }
            });
        }
    }

    private void presentData(List<DatabaseEquipmentMininmal> result) {
        for (int i = 0; i<result.size(); i++) {
            EquipmentItem item = new EquipmentItem();
            item.fromMinimal(result.get(i));
            if (!itemList.contains(item)) {
                itemList.add(item);
            }
        }

        ItemAdapter searchAdapter = new ItemAdapter(this, itemList);

        ListView lv = (ListView) this.findViewById(R.id.ListViewSearch);

        lv.setAdapter(searchAdapter);

        searchAdapter.notifyDataSetChanged();
    }
}
