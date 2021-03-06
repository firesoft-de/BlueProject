/*  Diese App stellt die Beladung von BOS Fahrzeugen in digitaler Form dar.
    Copyright (C) 2017  David Schlossarczyk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    For the full license visit https://www.gnu.org/licenses/gpl-3.0.*/

package dresden.de.digitaleTaschenkarteBeladung.fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dresden.de.digitaleTaschenkarteBeladung.MainActivity;
import dresden.de.digitaleTaschenkarteBeladung.R;
import dresden.de.digitaleTaschenkarteBeladung.daggerDependencyInjection.CustomApplication;
import dresden.de.digitaleTaschenkarteBeladung.data.DatabaseEquipmentMininmal;
import dresden.de.digitaleTaschenkarteBeladung.data.EquipmentItem;
import dresden.de.digitaleTaschenkarteBeladung.dataStructure.ItemAdapter;
import dresden.de.digitaleTaschenkarteBeladung.util.GroupManager;
import dresden.de.digitaleTaschenkarteBeladung.util.PreferencesManager;
import dresden.de.digitaleTaschenkarteBeladung.util.Util;
import dresden.de.digitaleTaschenkarteBeladung.util.VariableManager;
import dresden.de.digitaleTaschenkarteBeladung.viewmodels.ItemViewModel;

/**
 * Das {@link ItemFragment}Fragment zum Darstellen der Fächer in einem vordefinierten Listen Layout
 */
public class ItemFragment extends Fragment {
    IFragmentCallbacks masterCallback;

    private final static String LOG_TAG="ItemFragment_LOG";

    public final static  String BUNDLE_TAG_ITEMS="bundleItems";
    public final static  String BUNDLE_TAG_DETAIL="bundleDetail";

    private boolean noItems = false;

    private int catID;

    private ItemAdapter itemAdapter;

    private ArrayList<DatabaseEquipmentMininmal> itemList;

    //Diese Variable wird zum Speichern des Scroll Index der ListView gebraucht
    Parcelable state;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    GroupManager gManager;

    @Inject
    VariableManager vManager;


    ItemViewModel itemViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Anweisung an Dagger, dass hier eine Injection vorgenommen wird ??
        ((CustomApplication) getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        //Hier wird das Viewmodel erstellt und durch die Factory mit Eigenschaften versehen
        itemViewModel = ViewModelProviders.of(this,viewModelFactory)
                .get(ItemViewModel.class);

        if (this.getArguments() != null) {

            Bundle args = this.getArguments();

            catID =  args.getInt(BUNDLE_TAG_ITEMS);
            //Observer einrichten
            itemViewModel.getItemsByCatID(catID, gManager.getActiveGroup().getName()).observe(this, new Observer<List<DatabaseEquipmentMininmal>>() {
                @Override
                public void onChanged(@Nullable List<DatabaseEquipmentMininmal> items) {
                        insertData(null,items);
                }
            });

        }
        else {
            throw new IllegalArgumentException("Keine Behälter-ID angegeben!");
        }

        vManager.liveSort.observe(this, new Observer<Util.Sort>() {
            @Override
            public void onChanged(@Nullable Util.Sort sort) {
                changeSorting(sort);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.list_layout,parent,false);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            masterCallback = (IFragmentCallbacks) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public void onDetach() {
        if (itemList != null) {
        itemList.clear(); }

        if (itemAdapter != null) {
        itemAdapter.clear(); }

        super.onDetach();
    }

    @Override
    public void onPause() {
        ListView lv = getActivity().findViewById(R.id.ListViewMain);

        state = lv.onSaveInstanceState();
        super.onPause();
    }

    private void insertData(@Nullable ArrayList<EquipmentItem> equipmentItems, @Nullable List<DatabaseEquipmentMininmal> minimalItem) {

        if ((equipmentItems == null && minimalItem == null) || (equipmentItems == null && minimalItem.size() == 0)) {
            noItems = true;
            DatabaseEquipmentMininmal mininmal = new DatabaseEquipmentMininmal();
            mininmal.setName("Keine verfügbaren Daten");
            mininmal.setPosition("Für diesen Eintrag sind keine Daten verfügbar");

            minimalItem.add(mininmal);
        }
        Trace.beginSection("insertData");

        if (equipmentItems == null && minimalItem != null) {
            itemList = (ArrayList<DatabaseEquipmentMininmal>) minimalItem;

        } else {
            throw new IllegalArgumentException("Es darf nur ein Argument der Methode insertData null sein!");
        }

        changeSorting(vManager.liveSort.getValue());

        //Click Listener für die ListViewItems setzen um Details anzeigen zu können
        ListView lv = (ListView) getActivity().findViewById(R.id.ListViewMain);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!noItems) {
                    Trace.beginSection("LV ItemClickListener");
                    DatabaseEquipmentMininmal item = itemList.get(i);

                    DetailFragment detailFragment = new DetailFragment();
                    Bundle bundle = new Bundle();

                    bundle.putInt(BUNDLE_TAG_DETAIL, item.getId());

                    detailFragment.setArguments(bundle);

                    masterCallback.switchFragment(R.id.MainFrame, detailFragment, Util.FRAGMENT_DETAIL);

                    Trace.endSection();
                }

            }
        });

        // Restore previous state (including selected item index and scroll position)
        // https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview/5688490#5688490
        if (state != null) {
            lv.onRestoreInstanceState(state);
        }

        Trace.endSection();
    }

    private void setData(List<DatabaseEquipmentMininmal> items, @Nullable ListView lv) {

        Trace.beginSection("setData");
        itemList = (ArrayList<DatabaseEquipmentMininmal>) items;

        itemAdapter = new ItemAdapter(getActivity(),(ArrayList) items);

        if (lv == null) {
            lv = (ListView) getActivity().findViewById(R.id.ListViewMain);
        }

        lv.setAdapter(itemAdapter);

        Trace.endSection();

    }

    /**
     * Sortiert die Liste entsprechend des Nutzerwunsches
     * @param sort
     */
    private void changeSorting(Util.Sort sort) {

        if (itemList != null) {

            switch (sort) {

                case AZ:
                    Collections.sort(itemList, new Comparator<DatabaseEquipmentMininmal>() {
                        @Override
                        public int compare(DatabaseEquipmentMininmal o1, DatabaseEquipmentMininmal o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    setData(itemList, null);
                    break;

                case ZA:
                    Collections.sort(itemList, new Comparator<DatabaseEquipmentMininmal>() {
                        @Override
                        public int compare(DatabaseEquipmentMininmal o1, DatabaseEquipmentMininmal o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    Collections.reverse(itemList);
                    setData(itemList, null);
                    break;

                default:
                    //Entspricht PRESET
                    Collections.sort(itemList, new Comparator<DatabaseEquipmentMininmal>() {
                        @Override
                        public int compare(DatabaseEquipmentMininmal o1, DatabaseEquipmentMininmal o2) {
                            Integer x1 = o1.getId();
                            Integer x2 = o2.getId();
                            return x1.compareTo(x2);
                        }
                    });
                    setData(itemList, null);
                    break;
            }
        }
    }

    public void changeGroup(String activeGroup) {
        itemViewModel.getItemsByCatID(catID, activeGroup).observe(this, new Observer<List<DatabaseEquipmentMininmal>>() {
            @Override
            public void onChanged(@Nullable List<DatabaseEquipmentMininmal> items) {
                insertData(null,items);
            }
        });
    }

}
