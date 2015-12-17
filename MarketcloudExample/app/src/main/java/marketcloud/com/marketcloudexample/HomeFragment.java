package marketcloud.com.marketcloudexample;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
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

public class HomeFragment extends Fragment {

    public HashMap<Integer, HashMap<String, Object>> list = new HashMap<>();
    public HashMap<Integer, HashMap<String, Object>> list2 = new HashMap<>();
    Bitmap slideshow[];
    String name[];
    int id[];
    int count = 0;
    int max = 0;
    Bitmap slideshow2[];
    String name2[];
    int id2[];
    int count2 = 0;
    int max2 = 0;
    Context context;
    ProgressBar pb;
    ProgressBar pb2;
    LinearLayout ll;
    LinearLayout ll2;
    AsyncDataRetriever async1 = new AsyncDataRetriever();
    AsyncProductsRetriever async2 = new AsyncProductsRetriever();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate the proper view
        View v = inflater.inflate(R.layout.home_layout, container, false);

        //create a reference to some of the view elements
        pb = (ProgressBar) v.findViewById(R.id.progressBar);
        ll = (LinearLayout) v.findViewById(R.id.slide_cat_ll);
        pb2 = (ProgressBar) v.findViewById(R.id.progressBar2);
        ll2 = (LinearLayout) v.findViewById(R.id.slide_prod_ll);

        //set a fake banner
        ImageView banner = (ImageView) v.findViewById(R.id.mainBanner);
        banner.setImageResource(R.drawable.mainbanner);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Loading special offer...", Toast.LENGTH_SHORT).show();

                //associate the loading of a list fragment to the tap
                Fragment newFragment = new ListFragment();

                Bundle bundle = new Bundle();
                bundle.putInt("id", 10255);

                newFragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, newFragment)
                        .commit();
            }
        });

        context = v.getContext();

        //retrieve the data from the server
        async1 = new AsyncDataRetriever();
        async1.execute();
        async2 = new AsyncProductsRetriever();
        async2.execute();

        return v;
    }

    private class AsyncDataRetriever extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HashMap<String, Object> map = new HashMap<>();

            try {
                //API double call. it requests a list of all the categories in the database, and gets their data.
                JSONObject jsonObjects[] = MainActivity.marketcloud.json.getData(
                        MainActivity.marketcloud.categories.list(map));

                int index = 0;

                //parse each item
                for (JSONObject j : jsonObjects) {
                    //require id, name, url, image_url
                    list.put(index, MainActivity.marketcloud.json.parseData(
                            new String[]{"id", "name", "url", "image_url"},
                            j));
                    index++;
                }

                //get the number of categories
                count = jsonObjects.length;

                //initialize the data array
                id = new int[count];
                slideshow = new Bitmap[count];
                name = new String[count];

                //iterate over each category
                for (Map.Entry<Integer, HashMap<String, Object>> entry : list.entrySet()) {
                    try {
                        //get the ID
                        id[entry.getKey()] = (int) entry.getValue().get("id");

                        //fetch the category image
                        slideshow[entry.getKey()] = BitmapFactory.decodeStream(
                                new URL((String) entry.getValue().get("image_url")).openStream());

                        //get the name
                        name[entry.getKey()] = (String) entry.getValue().get("name");

                        //set the category as done
                        max++;
                    } catch (IOException ignored) {}
                }
            } catch (JSONException | ExecutionException | InterruptedException ignored) {}

            while (true) {
                //if all categories are loaded, I may proceed
                if (max == count) return true;
            }
        }

        @Override
        public void onPostExecute(Boolean b) {
            //loads the data in the UI. it needs to be done on the main thread
            loadCategoryCards();
        }
    }

    public void loadCategoryCards() {

        //save the data for future uses
        MainActivity.categoryList = name;
        MainActivity.categoryIDs = id;

        //hide the progress bar
        pb.setVisibility(View.GONE);

        //create a layout inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //iterate over reach category
        for (int i = 0; i < count; i++) {

            //create a card view for each category
            CardView cv = (CardView) inflater.inflate(R.layout.card_layout, ll, false);

            final int finalI = i;

            //associate a listener to it
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pass the category ID as bundle data: this will allow us to easily retrieve the ID when the card
                    //is tapped and use it
                    Bundle bundle = new Bundle();
                    bundle.putInt("category_id", id[finalI]);

                    //associate the loading of a list fragment to the tap
                    Fragment newFragment = new ListFragment();
                    newFragment.setArguments(bundle);
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, newFragment)
                            .commit();
                }
            });

            //add the card view to the layout
            ll.addView(cv);

            //add a space between the cards
            if (i != count-1) {
                Space space = (Space) inflater.inflate(R.layout.spaaaaaaaaace_im_in_space, ll, false);

                ll.addView(space);
            }

            //create a reference to the view items
            ImageView img = (ImageView) cv.findViewById(R.id.imageView2);
            TextView tv = (TextView) cv.findViewById(R.id.textView3);

            //fill them
            img.setImageBitmap(slideshow[i]);
            tv.setText(name[i]);
        }

        //show the cards
        ll.setVisibility(View.VISIBLE);
    }


    private class AsyncProductsRetriever extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HashMap<String, Object> map = new HashMap<>();

            try {
                //API double call. it requests a list of all the products in the database, and gets their data.
                JSONObject jsonObjects[] = MainActivity.marketcloud.json.getData(
                        MainActivity.marketcloud.products.list(map));

                int index = 0;

                //parse each item
                for (JSONObject j : jsonObjects) {
                    //require id, name, url, images
                    list2.put(index, MainActivity.marketcloud.json.parseData(
                            new String[]{"id", "name", "images"},
                            j));
                    index++;
                }

                //get the number of products
                count2 = jsonObjects.length;

                //initialize the data array
                id2 = new int[count2];
                slideshow2 = new Bitmap[count2];
                name2 = new String[count2];

                //iterate over each product
                for (Map.Entry<Integer, HashMap<String, Object>> entry : list2.entrySet()) {
                    try {
                        //get the ID
                        id2[entry.getKey()] = (int) entry.getValue().get("id");

                        JSONArray images = (JSONArray) entry.getValue().get("images");

                        try {
                            //I will use just the first one
                            String jo = images.getString(0);

                            //fetch the image
                            slideshow2[entry.getKey()] = BitmapFactory.decodeStream(
                                    new URL(jo).openStream());

                        } catch (JSONException e) {
                            //if no image was provided, a default fail image is used
                            slideshow2[entry.getKey()] = BitmapFactory.decodeResource(getResources(), R.drawable.no_pic);
                        }

                        //get the name
                        name2[entry.getKey()] = (String) entry.getValue().get("name");

                        //set the product as done
                        max2++;
                    } catch (IOException ignored) {}
                }
            } catch (JSONException | ExecutionException | InterruptedException ignored) {}

            while (true) {
                //if all products are loaded, I may proceed
                if (max2 == count2) return true;
            }
        }

        @Override
        public void onPostExecute(Boolean b) {
            //loads the data in the UI. it needs to be done on the main thread
            loadProductCards();
        }
    }

    public void loadProductCards() {

        //hide the progress bar
        pb2.setVisibility(View.GONE);

        //create a layout inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //iterate over reach product
        for (int i = 0; i < count2; i++) {

            //create a card view for each product
            CardView cv = (CardView) inflater.inflate(R.layout.card_layout, ll2, false);

            final int finalI = i;

            //associate a listener to it
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pass the product ID as bundle data: this will allow us to easily retrieve the ID when the card
                    //is tapped and use it
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id2[finalI]);

                    //associate the loading of a list fragment to the tap
                    Fragment newFragment = new ListFragment();
                    newFragment.setArguments(bundle);
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, newFragment)
                            .commit();
                }
            });

            //add the card view to the layout
            ll2.addView(cv);

            //add a space between the cards
            if (i != count2-1) {
                Space space = (Space) inflater.inflate(R.layout.spaaaaaaaaace_im_in_space, ll2, false);

                ll2.addView(space);
            }

            //create a reference to the view items
            ImageView img = (ImageView) cv.findViewById(R.id.imageView2);
            TextView tv = (TextView) cv.findViewById(R.id.textView3);

            //fill them
            img.setImageBitmap(slideshow2[i]);
            tv.setText(name2[i]);
        }

        //show the cards
        ll2.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        async1.cancel(true);
        async2.cancel(true);
    }
}