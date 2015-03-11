package sbpack;

import sbpack.LatLngInterpolator.Linear;
import utility.MarkerAnimation;
import utility.Utils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

	private ParseGeoPoint myLocation;
	private ParseObject myObject;
	private LatLng myLatLng;
	private Marker myMarker;
	private UiSettings mUiSettings;

	private Bitmap smallHeartBitmap;
	private ParseObject partnerObject;
	private Marker partnerMarker;
	private LatLng partnerLocation;

	private GoogleMap mMap;
	final Handler handler = new Handler();
	private CameraPosition cameraPosition;
	
	private boolean secure = true;
	
	EditText edtxt_msgcontent;
	
	private String partnerMsg="It's your partner";
	private String myMsg="Yo! It's me";

	// These settings are the same as the settings for the map. They will in fact give you updates
	// at the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(Utils.mapInterval)    // 16ms = 60fps; i slowed down the rate by 5 times
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_home);

		SupportMapFragment mapFragment =
				(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
		mapFragment.getMapAsync(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addApi(LocationServices.API)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();

		secure = getIntent().getBooleanExtra("secure", true);
		
		if(secure){
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.icon_marker);
			Bitmap b=bd.getBitmap();
			smallHeartBitmap=Bitmap.createScaledBitmap(b, b.getWidth()/4,b.getHeight()/4, false);
			ImageButton imgvw_partnerlocatin = (ImageButton) findViewById(R.id.imgvw_partnerlocatin);
			imgvw_partnerlocatin.setImageResource(R.drawable.icon_search);
			
			TextView txtvw_app_title = (TextView) findViewById(R.id.txtvw_app_title);
			txtvw_app_title.setText("Location Tracker");
		}else{
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.icon_curved_heart);
			Bitmap b=bd.getBitmap();
			smallHeartBitmap=Bitmap.createScaledBitmap(b, b.getWidth()/4,b.getHeight()/4, false);
		}
		
		edtxt_msgcontent = (EditText) findViewById(R.id.edtxt_msgcontent);
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

		cameraPosition = new CameraPosition.Builder()
		.target(Utils.center).zoom(Utils.zoom).bearing(Utils.bearing)
		.build();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		Thread partnerData=new Thread(){
			public void run(){
				//Retrieve partners last location
				ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationSB");
				while(true){
					if(partnerObject==null){
						try {
							partnerObject = query.get(Utils.partnerLoc);
							partnerObject.pinInBackground();
							// Success!
							Utils.logv(classname, "Partner location retrival successful");
							ParseGeoPoint partnerPoint = partnerObject.getParseGeoPoint("location");
							partnerLocation = new LatLng(partnerPoint.getLatitude(), partnerPoint.getLongitude());
							partnerMsg = partnerObject.getString("msg");
							handler.post(new Runnable() {
								public void run() {
									partnerMarker = mMap.addMarker(new MarkerOptions()
									.position(partnerLocation)
									.title("partner :)")
									.snippet(partnerMsg)
									.icon(BitmapDescriptorFactory.fromBitmap(smallHeartBitmap)));
								};
							});

						} catch (ParseException e1) {
							e1.printStackTrace();
							Utils.logv(classname, "Partner location retrival failed",e1);
						}
					}else{
						try {
							partnerObject.fetch();
							// Success!
							ParseGeoPoint partnerPoint = partnerObject.getParseGeoPoint("location");
							partnerLocation = new LatLng(partnerPoint.getLatitude(), partnerPoint.getLongitude());
							partnerMsg = partnerObject.getString("msg");
							Utils.logv(classname, "Partner object fetch successful");
							handler.post(new Runnable() {
								public void run() {
									int currentapiVersion = android.os.Build.VERSION.SDK_INT;
									if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB_MR2){
										MarkerAnimation.animateMarkerToGB(partnerMarker,partnerLocation, new Linear());
									} else if(currentapiVersion < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
										MarkerAnimation.animateMarkerToHC(partnerMarker,partnerLocation,new Linear());
									} else{
										MarkerAnimation.animateMarkerToICS(partnerMarker,partnerLocation,new Linear());
									}
									if(!partnerMarker.getSnippet().equals(partnerMsg)){
										partnerMarker.setSnippet(partnerMsg);
										partnerMarker.hideInfoWindow();
										partnerMarker.showInfoWindow();
									}
								};
							});
						} catch (ParseException e1) {
							e1.printStackTrace();
							// Fail!
							Utils.logv(classname, "Partner object fetch failed",e1);
						}
					}

					try {
						Thread.sleep(Utils.mapInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Utils.logv(classname, "Partner Thread sleep interrupted",e);
					}
				}
			}
		};

		partnerData.start();

	}

	/**
	 * Button to get current Location. This demonstrates how to get the current Location as required
	 * without needing to register a LocationListener.
	 */
	public void showPartnerLocation(View view) {
		if(partnerLocation!=null){
			cameraPosition = new CameraPosition.Builder()
			.target(partnerLocation).zoom(15.5f).bearing(Utils.bearing)
			.build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}else{
			ParseQuery<ParseObject> localquery = ParseQuery.getQuery("LocationSB");
			localquery.fromLocalDatastore();
			localquery.getInBackground(Utils.partnerLoc, new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						Utils.logv(classname, "Retrieved Partner's last known location from local database");
						partnerObject = object;
						ParseGeoPoint partnerPoint = partnerObject.getParseGeoPoint("location");
						partnerLocation = new LatLng(partnerPoint.getLatitude(), partnerPoint.getLongitude());
						partnerMsg = partnerObject.getString("msg");
						partnerMarker = mMap.addMarker(new MarkerOptions()
						.position(partnerLocation)
						.title("partner :)")
						.snippet(partnerMsg)
						.icon(BitmapDescriptorFactory.fromBitmap(smallHeartBitmap)));

						cameraPosition = new CameraPosition.Builder()
						.target(partnerLocation).zoom(15.5f).bearing(Utils.bearing)
						.build();
						mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
						
						Toast.makeText(getBaseContext(),"No internet connection", Toast.LENGTH_SHORT).show();
					} else {
						e.printStackTrace();
						Utils.logv(classname, "Retrieval of Partner's last known location from local database failed!",e);
					}
				}
			});
		}
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if(myLocation!=null){
			myLocation.setLatitude(location.getLatitude());
			myLocation.setLongitude(location.getLongitude());
		}else myLocation = new ParseGeoPoint(location.getLatitude() , location.getLongitude());

		myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

		handler.post(new Runnable() {
			public void run() {
				if(myMarker==null){
					myMarker = mMap.addMarker(new MarkerOptions()
					.position(myLatLng)
					.title("me :)")
					.snippet(myMsg)
					.icon(BitmapDescriptorFactory.fromBitmap(smallHeartBitmap)));
				}else{
					int currentapiVersion = android.os.Build.VERSION.SDK_INT;
					if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB_MR2){
						MarkerAnimation.animateMarkerToGB(myMarker,myLatLng, new Linear());
					} else if(currentapiVersion < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
						MarkerAnimation.animateMarkerToHC(myMarker,myLatLng,new Linear());
					} else{
						MarkerAnimation.animateMarkerToICS(myMarker,myLatLng,new Linear());
					}
					if(!myMarker.getSnippet().equals(myMsg)){
						myMarker.setSnippet(myMsg);
						myMarker.hideInfoWindow();
						myMarker.showInfoWindow();
					}
				}
			};
		});


		ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationSB");
		query.getInBackground(Utils.myLoc, new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					myObject = object;
					myMsg = myObject.getString("msg");
					object.put("location", myLocation);
					object.pinInBackground();
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
		Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onMyLocationButtonClick() {

		if(myLatLng!=null){
			cameraPosition = new CameraPosition.Builder()
			.target(myLatLng).zoom(15.5f).bearing(Utils.bearing)
			.build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}else{
			ParseQuery<ParseObject> localquery = ParseQuery.getQuery("LocationSB");
			localquery.fromLocalDatastore();
			localquery.getInBackground(Utils.myLoc, new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						Utils.logv(classname, "Retrieved my last known location from local database");
						myObject = object;
						ParseGeoPoint myPoint = myObject.getParseGeoPoint("location");
						myLatLng = new LatLng(myPoint.getLatitude(), myPoint.getLongitude());
						myMsg = myObject.getString("msg");
						myMarker = mMap.addMarker(new MarkerOptions()
						.position(myLatLng)
						.title("me :)")
						.snippet(myMsg)
						.icon(BitmapDescriptorFactory.fromBitmap(smallHeartBitmap)));

						cameraPosition = new CameraPosition.Builder()
						.target(myLatLng).zoom(15.5f).bearing(Utils.bearing)
						.build();
						mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
					} else {
						e.printStackTrace();
						Utils.logv(classname, "Retrieval of Partner's last known location from local database failed!",e);
					}
				}
			});
		}


		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

	public void sendMsg(View view){
		ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationSB");
		query.getInBackground(Utils.myLoc, new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					myObject = object;
					object.put("msg", edtxt_msgcontent.getText().toString());
					object.pinInBackground();
					object.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException e) {
							if(e==null){
								Utils.logv(classname, "Msg Updation successful");
								edtxt_msgcontent.setText("");
								myMsg = myObject.getString("msg");
								Toast.makeText(getBaseContext(),"message sent successfully", Toast.LENGTH_SHORT).show();
								handler.post(new Runnable() {
									public void run() {
										myMarker.setSnippet(myMsg);
										myMarker.hideInfoWindow();
										myMarker.showInfoWindow();
									};
								});
							}else{
								e.printStackTrace();
								Utils.logv(classname, "Msg Updation failed",e);
								Toast.makeText(getBaseContext(),"Failed to send message", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					e.printStackTrace();
					Utils.logv(classname, "For msg Object retrival failed",e);
					Toast.makeText(getBaseContext(),"Failed to send message", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
