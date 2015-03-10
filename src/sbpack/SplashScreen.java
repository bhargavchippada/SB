package sbpack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.sb.R;

public class SplashScreen extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_logosplash);

		Thread logoTimer=new Thread(){
			public void run(){
				try{
					sleep(500);
					Intent loginIntent=new Intent(getBaseContext(),LockPage.class);
					startActivity(loginIntent);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					finish();
				}
			}
		};
		logoTimer.start();
	}

}
