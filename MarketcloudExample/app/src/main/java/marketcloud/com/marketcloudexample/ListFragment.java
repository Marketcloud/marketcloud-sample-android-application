package marketcloud.com.marketcloudexample;

import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ListFragment extends Fragment {

    public HashMap<Integer, HashMap<String, Object>> list = new HashMap<>();
    HashMap<String, Object> filters = new HashMap<>();
    Bitmap slideshow[];
    String name[];
    Double price[];
    static int id[];
    int count = 0;
    int max = 0;
    Context context;
    ProgressBar pb;
    LinearLayout productLayout;
    AsyncDataRetriever async = new AsyncDataRetriever();
    SendToCart stc = new SendToCart(context);

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        //inflate the view layout
        View v = inflater.inflate(R.layout.list_layout, container, false);

        //retrieve the bundle data (if there are any). they are filters to the list passed by the calling fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            for (String t : bundle.keySet()) {
                filters.put(t, bundle.get(t));
            }
        }

        //get a reference to some view elements
        pb = (ProgressBar) v.findViewById(R.id.progressBar2);
        productLayout = (LinearLayout) inflater.inflate(R.layout.product_layout, container, false);

        context = v.getContext();

        //retrieve the data that will be shown in the list
        async = new AsyncDataRetriever();
        async.execute();

        return v;
    }

    private class AsyncDataRetriever extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                //double API call. it requests a product list with some filters applied, and retrieves the data
                JSONObject jsonObjects[] = MainActivity.marketcloud.json.getData(
                        MainActivity.marketcloud.products.list(filters));

                int index = 0;

                //parses every product
                for (JSONObject j : jsonObjects) {
                    try {
                        //require name, price, images, id
                        list.put(index, MainActivity.marketcloud.json.parseData(
                                new String[]{"name", "price", "images", "id"},
                                j));
                    } catch (JSONException e) {

                        //some elements may be options and throw an exception
                        String err = e.getMessage();

                        //deal with these cases
                        try {
                            if (err.contains("price")) {
                                list.put(index, MainActivity.marketcloud.json.parseData(
                                        new String[]{"name", "images", "id"},
                                        j));
                            }
                            else list.put(index, MainActivity.marketcloud.json.parseData(
                                    new String[]{"name", "price", "id"},
                                    j));
                        }  catch (JSONException ex) {
                            list.put(index, MainActivity.marketcloud.json.parseData(
                                    new String[]{"name", "id"},
                                    j));
                        }
                    }
                    index++;
                }

                //get the number of products
                count = jsonObjects.length;

                //initialize some arrays
                slideshow = new Bitmap[count];
                name = new String[count];
                price = new Double[count];
                id = new int[count];

                //iterate over every products
                for (Map.Entry<Integer, HashMap<String, Object>> entry : list.entrySet()) {
                    try {
                        //require the image list
                        JSONArray images = (JSONArray) entry.getValue().get("images");

                        try {
                            //I will use just the first one
                            String jo = images.getString(0);

                            //fetch the image
                            slideshow[entry.getKey()] = BitmapFactory.decodeStream(
                                    new URL(jo).openStream());

                        } catch (JSONException e) {
                            //if no image was provided, a default fail image is used
                            slideshow[entry.getKey()] = BitmapFactory.decodeResource(getResources(), R.drawable.no_pic);
                        }

                        //get the name
                        name[entry.getKey()] = (String) entry.getValue().get("name");

                        try {
                            //get the price
                            price[entry.getKey()] = (Double) entry.getValue().get("price");
                        }
                        catch (ClassCastException c) {
                            //Java has some casting problems between ints and doubles. I deal with them in this way:
                            try {
                                price[entry.getKey()] = 0.0 + (int) entry.getValue().get("price");
                            } catch (ClassCastException e) {
                                price[entry.getKey()] = 0.0;
                            }
                        }

                        //get the id
                        id[entry.getKey()] = (int) entry.getValue().get("id");

                        //mark the item as done
                        max++;
                    } catch (IOException ignored) {}
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }

            while (true) {
                //if all items are done, I may proceed
                if (max == count) return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            //loads the data in the UI. needs to be done in the UI thred
            load();
        }
    }

    public void load() {

        //hide the progress bar
        pb.setVisibility(View.GONE);

        //get a reference to the list
        ListView lv = (ListView) getActivity().findViewById(R.id.listView);

        //create a list adapter for the list
        ListAdapter la = new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
            }

            @Override
            public long getItemId(int position) {
                Log.i("id", list.get(position).get("id") + "");
                return (int) list.get(position).get("id");
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @SuppressWarnings("deprecation")
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                //set the proper view for each item

                //if null, create a new one
                if (convertView == null) {
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(R.layout.product_list_item_layout, parent, false);
                }

                //set a tag to the item. this will be very important in order to open a dialog fragment when an
                //item is tapped: without it, I can't know which product the user tapped on
                convertView.setTag(id[position]);

                //fills the title text view with the product name
                TextView listTitle = (TextView) convertView.findViewById(R.id.textView6);
                listTitle.setTypeface(null, Typeface.BOLD);
                listTitle.setText(name[position]);

                //fills the price text view
                TextView prices = (TextView) convertView.findViewById(R.id.textView8);
                prices.setText(String.format("Price: %s€", price[position]));
                prices.setTextColor(Color.BLACK);

                //loads the fetched image
                ImageView img = (ImageView) convertView.findViewById(R.id.prod);
                img.setImageBitmap(slideshow[position]);

                //associate a listener to the "add to cart" button
                final ImageButton ib = (ImageButton) convertView.findViewById(R.id.button2);
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.imagebutton));

                        Toast.makeText(context, "Adding to cart...", Toast.LENGTH_SHORT).show();

                        //adds the item to the cart
                        addToCart(id[position]);
                    }
                });

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return list.size() == 0;
            }
        };

        //associate the adapter to the list
        lv.setAdapter(la);

        //if the list is empty, show a "No Results" message
        if (count == 0) {
            TextView no = (TextView) getActivity().findViewById(R.id.no_results);
            no.setVisibility(View.VISIBLE);
        }
    }

    public void addToCart(int id) {
        //adds an item to the cart
        stc = new SendToCart(context);
        stc.execute(id + "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        async.cancel(true);
        stc.cancel(true);
    }
}