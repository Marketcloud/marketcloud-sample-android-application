package marketcloud.com.marketcloudexample;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SendToCart extends AsyncTask<String, Void, Boolean> {

    Context context;

    public SendToCart(Context ct) {
        context = ct;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            //API call: get the "cart" cookie
            String t = MainActivity.marketcloud.tokenManager.getCookie("cart");

            JSONObject jsonObject;

            //if a cart code is memorized, I add the new product to the remote cart
            if (t != null) {
                int cartCode = Integer.parseInt(t);

                jsonObject = MainActivity.marketcloud.carts.update(cartCode, new Object[][]{{Integer.parseInt(params[0]), 1}}, false);
            } else {
                //or I create a new one
                jsonObject = MainActivity.marketcloud.carts.create(new Object[][]{{Integer.parseInt(params[0]), 1}}, false);
                MainActivity.marketcloud.tokenManager.setToken("cart", String.valueOf(MainActivity.marketcloud.json.getId(jsonObject)));
            }

            //and return whether the operation was successful or not
            return MainActivity.marketcloud.json.countErrors(jsonObject) == 0;
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean b) {
        //toast the result of the operation
        if (b) toast("Added to cart!");
        else toast("Some problem occurred. Try again");
    }

    public void toast(String message) {
        //make toast (UI thread)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}