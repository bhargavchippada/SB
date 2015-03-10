package sbpack;

import utility.Utils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sb.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class HomePage extends FragmentActivity 
implements
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,
OnMyLocationButtonClickListener,
OnMapReadyCallback {

	public static String classname = "HomePage";
	private GoogleApiClient mGoogleApiClient;
	private TextView txtvw_your_location;
	private ParseGeoPoint mypoint;
	private UiSettings mUiSettings;
	
	private Bitmap smallHeartBitmap;
	private ParseObject partnerObject;
	private Marker partnerMarker;
	private LatLng partnerLocation;
	
	private GoogleMap mMap;

	// These settings are the same as the settings for the map. They will in fact give you updates
	// at the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16*30)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_home);
		txtvw_your_location = (TextView) findViewById(R.id.txtvw_your_location);

		SupportMapFragment mapFragment =
				(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
		mapFragment.getMapAsync(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addApi(LocationServices.API)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGoogleApiClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onMapReady(GoogleMap map) {
		mMap = map;
		
		map.setMyLocationEnabled(true);
		map.setOnMyLocationButtonClickListener(this);
		
		
		mUiSettings = map.getUiSettings();
		mUiSettings.setZoomControlsEnabled(true);
		
		BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.icon_curved_heart);
		Bitmap b=bd.getBitmap();
		smallHeartBitmap=Bitmap.createScaledBitmap(b, b.getWidth()/4,b.getHeight()/4, false);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationSB");
		query.getInBackground(Utils.sloc, new GetCallback<ParseObject>() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		    	partnerObject = object;
		    	Utils.logv(classname, "Partner location retrival successful");
		    	ParseGeoPoint partnerPoint = object.getParseGeoPoint("location");
		    	partnerLocation = new LatLng(partnerPoint.getLatitude(), partnerPoint.getLongitude());
				partnerMarker = mMap.addMarker(new MarkerOptions()
				                          .position(partnerLocation)
				                          .title("Bhargav :)")
				                          .snippet("Your bubu")
				                          .icon(BitmapDescriptorFactory.fromBitmap(smallHeartBitmap)));
		    } else {
		    	e.printStackTrace();
				Utils.logv(classname, "Partner location retrival failed",e);
		    }
		  }
		});
	}

	/**
	 * Button to get current Location. This demonstrates how to get the current Location as required
	 * without needing to register a LocationListener.
	 */
	public void showMyLocation(View view) {
		if (mGoogleApiClient.isConnected()) {
			String msg = "Location = "
					+ LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		txtvw_your_location.setText("Location = " + location);
		mypoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationSB");
		query.getInBackground(Utils.bloc, new GetCallback<ParseObject>() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		    	object.put("location", mypoint);
		    	object.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						if(e==null){
							Utils.logv(classname, "Updation successful");
						}else{
							e.printStackTrace();
							Utils.logv(classname, "Updation failed",e);
						}
					}
				});
		    } else {
		    	e.printStackTrace();
				Utils.logv(classname, "Object retrival failed",e);
		    }
		  }
		});
	}

	/**
	 * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient,
				REQUEST,
				this);  // LocationListener
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		// Do nothing
		Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onMyLocationButtonClick() {
		Toast.makeText(this, "My Location button clicked", Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}
}
