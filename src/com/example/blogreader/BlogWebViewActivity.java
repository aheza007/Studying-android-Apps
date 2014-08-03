package com.example.blogreader;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class BlogWebViewActivity extends Activity {
	protected String mUriTarget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blog_web_view);
		
		Intent intent=getIntent();
		mUriTarget=intent.getData().toString();//Retrieve data this intent is operating on
		WebView viewBlog=(WebView) findViewById(R.id.webView1);
		viewBlog.loadUrl(mUriTarget);//load the given URL
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
		getMenuInflater().inflate(R.menu.blog_web_view, menu);
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
		
		if(id==R.id.action_share){
		// Deliver some data to someone else. Who the data is being delivered to is not specified;
		// it is up to the receiver of this action to ask the user where the data should be sent. 
			Intent shareIntent=new Intent(Intent.ACTION_SEND);
		// This is used to create intents that only specify a type and not data, for example 
		// to indicate the type of data to return. 
			
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, mUriTarget);
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_Chooser_Title)));
		}
		return super.onOptionsItemSelected(item);
	}
}
