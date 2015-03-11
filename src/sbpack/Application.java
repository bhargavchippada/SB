package sbpack;

import utility.FontsOverride;
import utility.Utils;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;


public class Application extends android.app.Application{
	@Override
	public void onCreate() {
		super.onCreate();
		FontsOverride.overrideFont(getApplicationContext(), "MONOSPACE","fonts/CuteLove-Medium.ttf");

		//System.setProperty("https.proxyHost", "netmon.iitb.ac.in"); 
		//System.setProperty("https.proxyPort", "80");

		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "46Rjl3imto1wDeXPzlOXctbHd0WWBYkuT3JdV7In", "8UdbWMLSEzZSfTpzDf8MkjPaD2eellTOqrJKBZfj");

		ParseInstallation.getCurrentInstallation().saveEventually();

		//ParsePush.subscribeInBackground(Utils.parseChannel);

		ParsePush.subscribeInBackground("", new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
				} else {
					Log.e("com.parse.push", "failed to subscribe for push", e);
				}
			}
		});

	}
}
