package com.example.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	
	public static final int NUMBER_OF_POSTS=20;
	public static final String TAG=MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;
    private static final String KEY_TITLE="title";
    private static final String KEY_AUTHOR="author";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		mProgressBar=(ProgressBar) findViewById(R.id.progressBar1);
		
		if (isNetworkAvailable()) {
			mProgressBar.setVisibility(View.VISIBLE);
			GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
			getBlogPostsTask.execute();
		}
		else
			Toast.makeText(this, "NETWORK IS UNAVAILABLE", Toast.LENGTH_LONG).show();
		
	}
	/**
	 * This method will be called when an item in the list is selected. 
	 * Parameters:
	 * l The ListView where the click happened
	 * v The view that was clicked within the ListView
	 * position The position of the view in the list
	 * id The row id of the item that was clicked
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		
		try {
			JSONArray blogPosts=mBlogData.getJSONArray("posts");
			JSONObject blogPost=blogPosts.getJSONObject(position);
			String targetURL=blogPost.getString("url");
			
			Intent intent=new Intent(this,BlogWebViewActivity.class);// open another activity
		//	Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(targetURL));//Set the data this intent is operating on. 
			startActivity(intent);
			
		} catch (JSONException e) {
			logExceptions(e);
		}
		
	}
	private void logExceptions(Exception e) {
		Log.e(TAG, "Exception caught!",e);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager managerConnection=(ConnectivityManager) getSystemService
				(Context.CONNECTIVITY_SERVICE);
		/*
		 * Returns details about the currently active default data network. When connected, this 
		 * network is the default route for outgoing connections. You should always check NetworkInfo.isConnected() 
		 * before initiating network traffic. This may return null when there is no default network.
		 */
		NetworkInfo myNetworkInfo=managerConnection.getActiveNetworkInfo();
		boolean isConnectionAvailable=false;
		
		/*
		 * Indicates whether network connectivity exists and it is possible to establish connections and pass data. 
		 */
		
		if(myNetworkInfo!=null && myNetworkInfo.isConnected() ){
			isConnectionAvailable=true;
		}
		
		return isConnectionAvailable;
					
		
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		if(mBlogData==null){
			updateDisplayForErrors();
		} else
			try {
				
				JSONArray jsonPosts=mBlogData.getJSONArray("posts");
				//mBlogListTitles=new String[jsonPosts.length()];
				ArrayList<HashMap<String, String>> blogPosts=new ArrayList<HashMap<String, String>>();
				
				for (int i=0; i<jsonPosts.length();i++){
					JSONObject post=jsonPosts.getJSONObject(i);
					String title=post.getString("title");
					
					/*
					 * @fromHTML Returns displayable styled text from the provided HTML string. Any <img> tags in the HTML 
					 * will display as a generic replacement image which your program can then go through and replace with real images
					 */
					title=Html.fromHtml(title).toString();
					String author=post.getString("author");
					author=Html.fromHtml(author).toString();
					
					HashMap<String, String> blogPost=new HashMap<String, String> ();
					blogPost.put(KEY_TITLE, title);
					blogPost.put(KEY_AUTHOR, author);
					blogPosts.add(blogPost);
				}
				
				String[] keys={KEY_TITLE,KEY_AUTHOR};
				int[] ids={ android.R.id.text1, android.R.id.text2};
				
				SimpleAdapter myAdapter=new SimpleAdapter(this, blogPosts, 
						android.R.layout.simple_list_item_2, keys, ids);
				setListAdapter(myAdapter);
			} catch (JSONException e) {
				logExceptions(e);
			}
	}

	private void updateDisplayForErrors() {
		AlertDialog.Builder dialogBoxBuilder=new AlertDialog.Builder(this);
		dialogBoxBuilder.setTitle(R.string.error_title);
		dialogBoxBuilder.setMessage(R.string.error_message);
		dialogBoxBuilder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialogBox=dialogBoxBuilder.create();
		dialogBox.show();
		TextView emptyView=(TextView) getListView().getEmptyView();
		emptyView.setText(getString(R.string.no_ITEMS));
	}
	/**
	 *AsyncTask enables proper and easy use of the UI thread. This class allows to perform
	 *background operations and publish results on the UI thread without having to manipulate threads and/or handlers 
	 * The three types used by an asynchronous task are the following:
	 * Params: the type of the parameters sent to the task upon execution. 
	 * Progress: the type of the progress units published during the background computation 
	 * Result: the type of the result of the background computation. 
	 * Not all types are always used by an asynchronous task. To mark a type as unused, simply use the type Void:
	 *
	 */
	private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject>{

		
		protected JSONObject doInBackground(Object... params) {
			int responseCode = -1;
			JSONObject jsonResponse=null;
			try {
				//A Uniform Resource Locator that identifies the location of an Internet resource as specified by RFC 1738.
				URL blogFeedURL=new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count="+NUMBER_OF_POSTS);
				
				/*
				 * An URLConnection for HTTP (RFC 2616) used to send and receive data over the web. Data may be of any type and length.
				 * This class may be used to send and receive streaming data whose length is not known in advance. 
				 */
				
				HttpURLConnection connection= (HttpURLConnection) blogFeedURL.openConnection();
				connection.connect();
				responseCode=connection.getResponseCode();
				/*
				 * Send an INFO log message.
				 * Parameters:
				 * tag Used to identify the source of a log message. It usually identifies the class or activity where 
				 * the log call occurs msg The message you would like logged.
				 */
				if(responseCode==HttpURLConnection.HTTP_OK){
					/*
					 * Returns an InputStream for reading data from the resource pointed by this URLConnection. 
					 * It throws an UnknownServiceException by default. This method must be overridden by its subclasses.
					 * Returns:
					 * the InputStream to read data from.
					 */
					InputStream inputStream=connection.getInputStream();
					Reader reader=new InputStreamReader(inputStream);
					char[] charArray= new char[connection.getContentLength()];
					
					/*
					 * Reads characters from this reader and stores them in the character array buffer starting at offset 0.
					 * Returns the number of characters actually read or -1 if the end of the reader has been reached.
					 */
					reader.read(charArray);
					String myBlogContent=new String(charArray);
					jsonResponse=new JSONObject(myBlogContent);
					
					
				}
				else
					Log.i(TAG,"Unsuccessfull HTTP response: "+responseCode);
				
			} catch (MalformedURLException e) {
				/*
				 * Send an ERROR log message.
				 * Parameters:
				 * tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
				 * msg The message you would like logged.
				 * e the exception to be log
				 */
				logExceptions(e);
			
				
				//e.printStackTrace();
			} catch (IOException e) {
				logExceptions(e);
			} catch(Exception e){
				logExceptions(e);
			}
			return jsonResponse;
		}
		
		@Override 
		protected void onPostExecute(JSONObject result){
			mBlogData=result;
			handleBlogResponse();
			
		}
		
		
	}

	
}
