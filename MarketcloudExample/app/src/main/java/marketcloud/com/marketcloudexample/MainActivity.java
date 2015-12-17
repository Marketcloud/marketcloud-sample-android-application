package marketcloud.com.marketcloudexample;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.marketcloud.marketcloud.Marketcloud;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static Marketcloud marketcloud;
    DrawerLayout drawer;

    static String categoryList[];
    static int categoryIDs[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the main view
        setContentView(R.layout.activity_main);

        //set the custom toolbar as the app's toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //create the toggle for the navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //instantiate a new Marketcloud object. Using this object, you can easily access Marketcloud's APIs
        marketcloud = new Marketcloud(this, "86c1b899-b6ba-4dee-9cd9-995e4faa4973");

        //associate a listener to the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //instantiate the default fragment (Home) and load it
        Fragment newFragment = new HomeFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, newFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        //manage the "back" key actions
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //associate the custom menu to the toolbar. this menu contains the "cart" and "search" icons
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //associate an action to the tap on the toolbar menu icons
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //deals with the taps on the items in the navigation drawer

        //set the selected item as checked
        item.setChecked(true);

        //instantiate a null fragment
        Fragment newFragment = null;

        //depending on the selected item, create a proper fragment
        switch (item.getItemId()) {
            case R.id.nav_home:
                newFragment = new HomeFragment();
                break;
            case R.id.nav_prod_list:
                newFragment = new ListFragment();
                break;
            case R.id.nav_search:
                newFragment = new SearchFragment();
                break;
            case R.id.nav_cart:
                newFragment = new CartFragment();
                break;
        }

        //load the fragment in the view
        getFragmentManager().beginTransaction().replace(R.id.frame, newFragment).commit();

        //close the drawer
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    public void ItemClickHandler(View view) {

        //associate an action to the tap on a product in the products list view
        //in particular, this loads a dialog fragment in the view
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        //creates a new instance of the product dialog fragment. note the use of view.getTag():
        //in the item's tag is saved the product ID
        ProductDialog newFragment = ProductDialog.newInstance((Integer) view.getTag());

        //show the dialog fragment
        newFragment.show(ft, "dialog");
    }

    public void searchItem(MenuItem item) {
        //load a new search fragment when the search icon in the toolbar is tapped
        Fragment newFragment = new SearchFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, newFragment)
                .commit();
    }

    public void openCart(MenuItem item) {
        //load a new cart fragment when the cart icon in the toolbar is tapped
        Fragment newFragment = new CartFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, newFragment)
                .commit();
    }
}