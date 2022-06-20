package com.example.visitjamshedpur;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class Attractions extends Fragment {

    RecyclerView recyclerAttraction;
    BasicItemsAdapter mBasicItemsAdapter;
    RelativeLayout progressBar;
    ArrayList<BasicListItem> attractions = new ArrayList<>();
    Toolbar toolbar;
    TextView textView;
    androidx.appcompat.widget.SearchView searchView;

    public Attractions() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attractions, container, false);
        initializations(rootView);
        if (attractions.size() > 1 || mBasicItemsAdapter == null) getAttractionList();
        toolbarSearch();
        return rootView;
    }

    private void initializations(View rootView) {
        progressBar = rootView.findViewById(R.id.progressBarLayout);
        toolbar = rootView.findViewById(R.id.toolbar);
        searchView = rootView.findViewById(R.id.searchBar);
        textView = rootView.findViewById(R.id.toolbarTitle);
        recyclerAttraction = rootView.findViewById(R.id.attractionFrag);
    }

    private void getAttractionList() {
        attractions.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Attractions")
                .orderBy("aName")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.VISIBLE);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String a = (String) document.get("aName");
                            String b = (String) document.get("aAddress");
                            String c = (String) document.get("image-0");
                            String d = (String) document.getString("aID");
                            BasicListItem basicListItem = new BasicListItem(c, a, b, d);
                            attractions.add(basicListItem);
                            if (mBasicItemsAdapter != null)
                                mBasicItemsAdapter.updateData(attractions);
                            else {
                                if (attractions.size() >= 1) {
                                    mBasicItemsAdapter = new BasicItemsAdapter(getActivity(), attractions);
                                    recyclerAttraction.setAdapter(mBasicItemsAdapter);
                                    if (getContext() != null)
                                        recyclerAttraction.setLayoutManager(new LinearLayoutManager(getContext()
                                                , RecyclerView.VERTICAL, false));
                                }
                            }
                        }
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Could not get all documents successfully", Toast.LENGTH_SHORT).show();
                    }
                    if (attractions.size() >= 1) {
                        mBasicItemsAdapter = new BasicItemsAdapter(getActivity(), attractions);
                        recyclerAttraction.setAdapter(mBasicItemsAdapter);
                        if (getContext() != null)
                            recyclerAttraction.setLayoutManager(new LinearLayoutManager(getContext()
                                    , RecyclerView.VERTICAL, false));
                    }
                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
            if (getContext() != null)
                Toast.makeText(getContext(), "Data not retrieved. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void toolbarSearch() {
        toolbar.inflateMenu(R.menu.search_menu);
        toolbar.setNavigationOnClickListener(view -> {
            toolbar.setNavigationIcon(null);
            textView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
            getAttractionList();
        });
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.searchOption) {
                toolbar.setNavigationIcon(R.drawable.back_icon_white);
                textView.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
            }
            return false;
        });
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String s) {
        ArrayList<BasicListItem> temp = new ArrayList<>();
        if (attractions.size() > 0) {
            for (BasicListItem b : attractions) {
                if (b.getTitle().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))
                    temp.add(b);
            }
        }
        if (mBasicItemsAdapter != null) mBasicItemsAdapter.updateData(temp);
    }
}