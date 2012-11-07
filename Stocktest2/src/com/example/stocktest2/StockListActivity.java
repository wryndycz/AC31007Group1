package com.example.stocktest2;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Lists all stocks in portfolio and current trade price
 * Red = Negative
 * Green = Positive
 */
public class StockListActivity extends ListActivity
{
	private String stocks;
    private String[] listItems;
    private ProgressDialog dialog;
    

    /** Called when the activity is first created.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            //Get the Extras from MainActivity
        stocks = getIntent().getStringExtra("Stocks");
        
        
        //dialog = ProgressDialog.show(this, "Please Wait...","Parsing Stocks...", true);
        listItems = stocks.split(";");
        setListAdapter(new ArrayAdapter<String>(StockListActivity.this, R.layout.list_item, listItems));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        //Calculates list items on separate GUI thread.  Shows progress dialog during...
//        new AsyncTask<String, Integer, String>()
//        {
//
//            @Override
//            protected void onProgressUpdate(Integer... progress)
//            {
//                super.onProgressUpdate(progress);
//            }
//
//
//            @Override
//            protected String doInBackground(String... arg0)
//            {
//            	            	
//            	return "Finished!";
//
//            }
//
//            @Override
//            protected void onPostExecute(String result)
//            {
//                dialog.dismiss();
//                
//                    //Set up a UI List
//                
//
//            }
//
//        }.execute("");



    }

	@Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }



}
