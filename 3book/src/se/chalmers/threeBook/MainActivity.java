package se.chalmers.threeBook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	
	private Button btnOpenImport;
	private Button btnSettings;
	private Button btnCollection;
	private Button btnFavourites;
	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnOpenImport = (Button) findViewById(R.id.btn_open_import);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        btnCollection = (Button) findViewById(R.id.btn_collection);
        btnFavourites = (Button) findViewById(R.id.btn_favourites);
        
        btnOpenImport.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(),
						FileBrowserActivity.class);
				startActivity(intent);
			}
		});
    }
}