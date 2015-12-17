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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class CartFragment extends Fragment {

    HashMap<String, Object> list;
    HashMap<Integer, HashMap<String, Object>> map = new HashMap<>();
    ProgressBar pb;
    ListView lv;
    Button b;
    Bitmap slideshow[];
    String name[];
    Double price[];
    int quantity[];
    int stock[];
    static int id[];
    int count;
    int max;
    Context context;
    int cartID;
    AsyncCartRetriever async = new AsyncCartRetriever();
    AsyncCartUpdate asyncup = new AsyncCartUpdate();
    AsyncRemoveFromCart asyncrem = new AsyncRemoveFromCart();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //initialize some variables
        count = 0;
        max = 0;

        //inflate the correct view
        View v = inflater.inflate(R.layout.cart_layout, container, false);

        //get a reference to the view items
        pb = (ProgressBar) v.findViewById(R.id.progressBar4);
        lv = (ListView) v.findViewById(R.id.cartlist);
        b = (Button) v.findViewById(R.id.checkout);

        //associate an action to the "Checkout" button
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            }
        });

        //show the progress bar
        pb.setVisibility(View.VISIBLE);

        context = v.getContext();

        //detect if there is a cart in memory. if yes, it is loaded
        String t = MainActivity.marketcloud.tokenManager.getCookie("cart");
        if (t != null) {
            cartID = Integer.parseInt(t);
            async = new AsyncCartRetriever();
            async.execute(cartID);
        }

        return v;
    }

    private class AsyncCartRetriever extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {

            map = new HashMap<>();

            //in background, the cart data are retrieved
            try {
                //double API call. request a cart by its ID, and then get its data.
                JSONObject jsonObjects[] = MainActivity.marketcloud.json.getData(
                        MainActivity.marketcloud.carts.getById(params[0], false));

                //parse the cart data, requesting the items in the cart
                for (JSONObject j : jsonObjects) {
                    try {
                        list = MainActivity.marketcloud.json.parseData(new String[]{"items"}, j);
                    } catch (JSONException e) {
                        list.put("items", null);
                    }
                }

                int index = 0;

                //some casts, to better deal with the data formats
                JSONArray items = (JSONArray) list.get("items");

                //parse each element in the "items" array
                for (int i = 0; i < items.length(); i++) {

                    try {
                        //require name, price, images, id, quantity, stock_level of every item
                        map.put(index, MainActivity.marketcloud.json.parseData(
                                new String[]{"name", "price", "images", "id", "quantity", "stock_level"},
                                items.getJSONObject(i)));
                        } catch (JSONException e) {

                            //some fields may be optional: if they are not filled, an exception will be thrown
                            String err = e.getMessage();

                            //deal with these cases
                            try {
                                if (err.contains("price")) {
                                    map.put(index, MainActivity.marketcloud.json.parseData(
                                            new String[]{"name", "images", "id", "quantity", "stock_level"},
                                            items.getJSONObject(i)));
                                }
                                else map.put(index, MainActivity.marketcloud.json.parseData(
                                        new String[]{"name", "price", "id", "quantity", "stock_level"},
                                        items.getJSONObject(i)));
                            }  catch (JSONException ex) {
                                map.put(index, MainActivity.marketcloud.json.parseData(
                                        new String[]{"name", "id", "quantity", "stock_level"},
                                        items.getJSONObject(i)));
                            }
                        }
                        index++;
                    }

                    //get the number of items in the cart
                    count = items.length();

                    //initialize some arrays that will host the data
                    slideshow = new Bitmap[count];
                    name = new String[count];
                    price = new Double[count];
                    id = new int[count];
                    quantity = new int[count];
                    stock = new int[count];

                    //iterate over each item
                    for (Map.Entry<Integer, HashMap<String, Object>> entry : map.entrySet()) {
                        try {

                            //get the images list
                            JSONArray images = (JSONArray) entry.getValue().get("images");

                            try {
                                //I will use just the first
                                String jo = images.getString(0);

                                //fetches the image from the web
                                slideshow[entry.getKey()] = BitmapFactory.decodeStream(
                                        new URL(jo).openStream());

                            } catch (JSONException e) {
                                //if there are no images, a default fail image is used
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

                            //get the stock_level (how many pieces are left in the store)
                            stock[entry.getKey()] = (int) entry.getValue().get("stock_level");

                            //get the quantity (how many pieces the user wants to buy)
                            quantity[entry.getKey()] = (int) entry.getValue().get("quantity");

                            //get the product id
                            id[entry.getKey()] = (int) entry.getValue().get("id");

                            //everything about this item is fetched. I can check it as done
                            max++;
                        } catch (IOException ignored) {}
                    }

                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();

                    return false;
                }

            while (true) {
                //if every product was marked as done, I may proceed
                if (max == count) return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //call to a load method. this will fill the list: you cannot do UI-related updates from a background thread
            load();
        }
    }

    public void load() {
        //hides the progress bar
        pb.setVisibility(View.GONE);

        //associate a list adapter to the list
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
            public void registerDataSetObserver(DataSetObserver observer) {}

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {}

            @Override
            public int getCount() {
                return map.size();
            }

            @Override
            public Object getItem(int position) {
                return map.get(position);
            }

            @Override
            public long getItemId(int position) {
                return (long) map.get(position).get("id");
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                //set the proper view for each item

                //if null, create a new one
                if (convertView == null) {
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(R.layout.cart_item_layout, parent, false);
                }

                //fills the title text view with the product name
                TextView listTitle = (TextView) convertView.findViewById(R.id.textView6);
                listTitle.setTypeface(null, Typeface.BOLD);
                listTitle.setText(name[position]);

                //fills the stock_level text view accordingly
                TextView level = (TextView) convertView.findViewById(R.id.textView8);
                level.setText(String.format("In stock: %s", stock[position]));
                level.setTextColor(Color.BLACK);

                //loads the fetched image in the proper position
                ImageView img = (ImageView) convertView.findViewById(R.id.prod);
                img.setImageBitmap(slideshow[position]);

                //fills the subtotal text view
                final TextView subtotal = (TextView) convertView.findViewById(R.id.subtotal);
                subtotal.setText(String.format("Subtotal: %s€", price[position] * quantity[position]));
                subtotal.setTextColor(Color.BLACK);

                //fills the quantity text view
                final TextView qty = (TextView) convertView.findViewById(R.id.qty);
                qty.setText(String.format("%d", quantity[position]));
                qty.setTextColor(Color.BLACK);

                //associate a listener to the "remove product" button
                final ImageButton ib = (ImageButton) convertView.findViewById(R.id.button2);
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.imagebutton));

                        Toast.makeText(context, "Removing...", Toast.LENGTH_SHORT).show();

                        //remove the product from the cart
                        asyncrem = new AsyncRemoveFromCart();
                        asyncrem.execute(position);
                    }
                });

                //associate a listener to the "minus" button
                final ImageButton minus = (ImageButton) convertView.findViewById(R.id.buttonRemove);
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.imagebutton));

                        //reduce quantity. by default, it will decrease by 1
                        quantity[position]--;

                        //check if the new value is legal; in case, updates the quantity accordingly
                        if (0 == quantity[position]) {
                            quantity[position]++;
                            Toast.makeText(context, "Quantity cannot be 0 or less!", Toast.LENGTH_SHORT).show();
                        } else {
                            qty.setText(String.format("%d", quantity[position]));
                            asyncup = new AsyncCartUpdate();
                            asyncup.execute(position);
                            subtotal.setText(String.format("Subtotal: %s€", price[position] * quantity[position]));
                        }
                    }
                });

                final ImageButton plus = (ImageButton) convertView.findViewById(R.id.buttonAdd);

                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.imagebutton));

                        //augments the quantity. by default, it will increase by 1
                        quantity[position]++;

                        //check if the new value is legal; in case, updates the quantity accordingly
                        if (stock[position] < quantity[position]) {
                            quantity[position]--;
                            Toast.makeText(context, "Quantity not available!", Toast.LENGTH_SHORT).show();
                        } else {
                            qty.setText(String.format("%d", quantity[position]));
                            asyncup = new AsyncCartUpdate();
                            asyncup.execute(position);
                            subtotal.setText(String.format("Subtotal: %s€", price[position] * quantity[position]));
                        }
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
                return map.size() == 0;
            }
        };

        //associate the adapter to the list, and show the list
        lv.setAdapter(la);
        lv.setVisibility(View.VISIBLE);

        //if no items are in the list, a "No Results" text view is displayed
        if (count == 0) {
            TextView no_results = (TextView) getActivity().findViewById(R.id.no_results);
            no_results.setVisibility(View.VISIBLE);
        }
    }

    private class AsyncCartUpdate extends AsyncTask<Integer, Void, JSONObject> {

        int position;

        @Override
        protected JSONObject doInBackground(Integer... params) {

            //get the item position
            position = params[0];

            try {
                //changes the quantity in the remote cart
                return MainActivity.marketcloud.carts.update(cartID, new Object[][] {{id[params[0]], quantity[params[0]]}}, false);
            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            set(jsonObject);
        }
    }

    private class AsyncRemoveFromCart extends AsyncTask<Integer, Void, JSONObject> {

        int position;

        @Override
        protected JSONObject doInBackground(Integer... params) {

            position = params[0];

            try {
                //removes the product from the remote cart
                return MainActivity.marketcloud.carts.remove(cartID, new Object[]{id[params[0]]}, false);
            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            //show a result toast
            set(jsonObject);

            //reloads the fragment: this is necessary when an item is removed from the list. other solutions may be
            //better, but this is the quickest
            Fragment newFragment = new CartFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, newFragment)
                    .commit();
        }
    }

    public void set(JSONObject jo) {

        //show a result message
        if (MainActivity.marketcloud.json.countErrors(jo) != 0)
            Toast.makeText(context, "Update failed!", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(context, "Update succeeded!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        async.cancel(true);
        asyncup.cancel(true);
        asyncrem.cancel(true);
    }
}