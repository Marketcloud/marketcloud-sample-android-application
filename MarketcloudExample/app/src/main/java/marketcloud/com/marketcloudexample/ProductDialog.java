package marketcloud.com.marketcloudexample;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ProductDialog extends DialogFragment {

    static int id;
    View v;
    SendToCart stc = new SendToCart(getActivity());
    AsyncProductRetriever async = new AsyncProductRetriever();

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        //set the dialog opening animation
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogOpening;
    }

    @SuppressWarnings("unused")
    static ProductDialog newInstance(int num) {

        //create a new dialog instance
        ProductDialog f = new ProductDialog();

        //manage the id
        Bundle args = new Bundle();
        args.putInt("num", num);
        id = num;
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the product view
        v = inflater.inflate(R.layout.product_layout, container, false);

        //set the title of the dialog
        getDialog().setTitle("Product View");

        //associate an action to the add to cart button
        Button button = (Button) v.findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.imagebutton));

                //add product to cart
                stc = new SendToCart(v.getContext());
                stc.execute("" + id);
            }
        });

        //associate an action to the close button
        Button close = (Button) v.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.imagebutton));

                //close the dialog
                getDialog().dismiss();
            }
        });

        //retrieve product data
        async = new AsyncProductRetriever();
        async.execute("" + id);

        return v;
    }

    private class AsyncProductRetriever extends AsyncTask<String, Void, Boolean> {

        HashMap<String, Object> map;
        Bitmap local;

        @Override
        protected Boolean doInBackground(String... params) {

            int id = Integer.parseInt(params[0]);

            try {
                //API call. get a product data given its ID
                JSONObject jo = MainActivity.marketcloud.products.getById(id);

                try {
                    //parse the product data
                    map = MainActivity.marketcloud.json.parseData(new String[]{"name", "price", "stock_level", "description", "images"}, jo);

                    try {
                        //get the images
                        String b = ((JSONArray) map.get("images")).getString(0);

                        //fetch the image
                        local = BitmapFactory.decodeStream(
                                new URL(b).openStream());

                    } catch (JSONException e) {
                        //in case no pic was set, use a default fail pic
                        local = BitmapFactory.decodeResource(getResources(), R.drawable.no_pic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    //deal with optional fields. note: this code is just sketched
                    map = MainActivity.marketcloud.json.parseData(new String[] {"name"}, jo);
                    map.put("price", 0.0);
                    map.put("stock_level", 0);
                    map.put("description", "no description");
                    map.put("images", new JSONArray());
                    local = BitmapFactory.decodeResource(getResources(), R.drawable.no_pic);
                }

                while (true) {
                    //if the product was retrieved, I may proceed
                    if (map != null) return true;
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //get the reference to the view elements
            TextView name = (TextView) v.findViewById(R.id.textView6);
            TextView price = (TextView) v.findViewById(R.id.textView7);
            TextView stock = (TextView) v.findViewById(R.id.textView8);
            TextView descr = (TextView) v.findViewById(R.id.description);
            ImageView img = (ImageView) v.findViewById(R.id.prodImage);
            ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressBar3);
            LinearLayout ll = (LinearLayout) v.findViewById(R.id.prod_ll);

            //set the values
            name.setText((String) map.get("name"));
            price.setText(String.format("Price: %s€", map.get("price")));
            stock.setText(String.format("In stock: %s", map.get("stock_level")));
            descr.setText((String) map.get("description"));
            img.setImageBitmap(local);

            //hide progress bar and show the layout
            pb.setVisibility(View.GONE);
            ll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        async.cancel(true);
        stc.cancel(true);
    }
}