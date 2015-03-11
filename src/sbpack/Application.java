package sbpack;

import utility.FontsOverride;
import utility.Utils;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;


public class Application extends android.app.Application{
	@Override
	public void onCreate() {
		super.onCreate();
		FontsOverride.overrideFont(getApplicationContext(), "MONOSPACE","fonts/CuteLove-Medium.ttf");
		
		// Enable Local Datastore.
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "46Rjl3imto1wDeXPzlOXctbHd0WWBYkuT3JdV7In", "8UdbWMLSEzZSfTpzDf8MkjPaD2eellTOqrJKBZfj");
	
		ParseInstallation.getCurrentInstallation().saveEventually();
		
		ParsePush.subscribeInBackground(Utils.parseChannel);
		
	}
}
