package marketcloud.com.marketcloudexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        //inflate the search view
        View v = inflater.inflate(R.layout.search_layout, container, false);

        //get a reference to the spinner
        final Spinner spinner = (Spinner) v.findViewById(R.id.spinner);

        //add all the categories to the spinner
        ArrayList<String> al = new ArrayList<>();
        al.add("All");

        if (MainActivity.categoryList != null) {
            al.addAll(Arrays.asList(MainActivity.categoryList));
        }

        //create an array adapter and add it to the spinner
        ArrayAdapter<String> aa = new ArrayAdapter<>(v.getContext(), R.layout.spinner_dropdown, al);
        spinner.setAdapter(aa);

        //associate a query listener to the search view
        SearchView sv = (SearchView) v.findViewById(R.id.searchView);
        sv.setSubmitButtonEnabled(true);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Bundle bundle = new Bundle();

                //detect the selected category
                String selected = spinner.getSelectedItem().toString();

                //if not "All", a filter is passed to the list fragment
                if (!selected.equals("All")) {

                    int position = Arrays.asList(MainActivity.categoryList).indexOf(selected);

                    bundle.putInt("category_id", MainActivity.categoryIDs[position]);
                }

                bundle.putString("q", query);

                //create a new list fragment, pass the filters to it, and load it
                Fragment newFragment = new ListFragment();
                newFragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, newFragment)
                        .commit();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return v;
    }
}