package com.example.mapsample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mapsample.BusinessMarker.BuisnessType;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {
	
	/**google map object to show the map*/
	private GoogleMap gMap;
	
	/**temporary LatLng objects for example, should be deleted*/
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	static final LatLng JAFFA_STREET = new LatLng(31.78507,35.214328);
	
	/**A list of all the businesses which are nearby.
	 * shold be updated using a DBHandler object*/
	private ArrayList<BusinessMarker> businessesList = new ArrayList<BusinessMarker>();
	
	/**maps from map markers to businesses and from businesses to markers*/
	private HashMap <Marker, BusinessMarker> markerToBusiness = new HashMap <Marker, BusinessMarker>();
	private HashMap <BusinessMarker, Marker> BusinessToMarker = new HashMap <BusinessMarker, Marker>();
	
	/**map filters*/
	private ImageView restBtn,pubBtn,hotelBtn,shoppingBtn,coffeeBtn;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_window_fragment);
		
		gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		//loadDBs_debug();
		
		//load personal info from sqlite (favourites)
		loadPersonalInfo();
		
		if (gMap!=null){
			gMap.setOnMarkerClickListener(markerListener);
		}
		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAFFA_STREET, 15));
		
		(LoadBuisnessesTask()).execute();
		gMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
		
		restBtn = (ImageView)findViewById(R.id.resturant_filter_btn);
		pubBtn = (ImageView)findViewById(R.id.pub_filter_btn);
		hotelBtn = (ImageView)findViewById(R.id.hotel_filter_btn);
		shoppingBtn = (ImageView)findViewById(R.id.shopping_filter_btn);
		coffeeBtn = (ImageView)findViewById(R.id.coffee_filter_btn);
		
		restBtn.setOnClickListener(filterBtnClickListener);
		pubBtn.setOnClickListener(filterBtnClickListener);
		hotelBtn.setOnClickListener(filterBtnClickListener);
		shoppingBtn.setOnClickListener(filterBtnClickListener);
		coffeeBtn.setOnClickListener(filterBtnClickListener);
		
		Spinner spinner = (Spinner)findViewById(R.id.filter_spinner);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        String item = parent.getItemAtPosition(pos).toString();
		        if(item.equals("My favourites")){
		        	
		        }else if(item.equals("Top businesses")){
		        	
		        }else if(item.equals("Top deals")){
		        	
		        }
		    }
		    public void onNothingSelected(AdapterView<?> parent) {
		    }
		});
	}
	
	private OnClickListener filterBtnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ImageView btn;
			BusinessMarker.BuisnessType type;
			if(v==restBtn){
				btn = (ImageView)findViewById(R.id.resturant_filter_btn);
				type = BuisnessType.RESTURANT;
			}else if(v==pubBtn){
				btn = (ImageView)findViewById(R.id.pub_filter_btn);
				type = BuisnessType.PUB;
			}else if(v==hotelBtn){
				btn = (ImageView)findViewById(R.id.hotel_filter_btn);
				type = BuisnessType.HOTEL;
			}else if(v==shoppingBtn){
				btn = (ImageView)findViewById(R.id.shopping_filter_btn);
				type = BuisnessType.SHOPPING;
			}else if(v==coffeeBtn){
				btn = (ImageView)findViewById(R.id.coffee_filter_btn);
				type = BuisnessType.COFFEE;
			}else{
				Log.d("filterBtnClickListener", "filterBtnClickListener was called, but the selected button wasnt found");
				return;
			}
			
			boolean newState = !btn.isSelected();
			btn.setSelected(newState);
			for(BusinessMarker bm :  BusinessToMarker.keySet()){
				if(bm.type==type){
					Marker m = BusinessToMarker.get(bm);
					if(m==null){
						Log.d("filterBtnClickListener","didn't find corresponding marker for a business");
					}else{
						m.setVisible(newState);
					}
					
				}
			}
		}

	};
	
	private void loadPersonalInfo() {
		
		
	}

	private void putOverlayOnMap(BusinessMarker bm){
		Marker m =  gMap.addMarker(new MarkerOptions().position(bm.pos).title(bm.name).icon(BitmapDescriptorFactory.fromResource(bm.iconID)));
    	markerToBusiness.put(m, bm);
    	BusinessToMarker.put(bm, m);

	}
	
	private LoadBusinessesToMap LoadBuisnessesTask(){return new LoadBusinessesToMap(this);}
	private OnMarkerClickListener markerListener = new OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker) {
			BusinessMarker bMarker = markerToBusiness.get(marker);
			if(bMarker==null)
				Toast.makeText(getApplicationContext(),"sry null", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getApplicationContext(),"your in: " +  bMarker.name, Toast.LENGTH_SHORT).show();
			return false;
		}
		
	};
	

	    public class LoadBusinessesToMap extends AsyncTask<Void, ArrayList<BusinessMarker>, Void> {
	        Context mContext;
	        int NUM_OF_LOADS_BEFORE_REFRESH = 5;
	        public LoadBusinessesToMap(Context context) {
	            super();
	            mContext = context;
	        }
	        
	        /**
	         * Get a Geocoder instance, get the latitude and longitude
	         * look up the address, and return it
	         *
	         * @params params One or more Location objects
	         * @return A string containing the address of the current
	         * location, or an empty string if no address can be found,
	         * or an error message
	         */
	        protected Void doInBackground(Void... params) {
	        	//Marker Jerusalem = gMap.addMarker(new MarkerOptions().position(JAFFA_STREET).title("Jerusalem").snippet("many dosim").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
	            int counter = 0;
	            ArrayList<BusinessMarker> newBusinesses = new ArrayList<BusinessMarker>();
	        	for(BusinessMarker m:markersDB){
	            	businessesList.add(m);
	            	newBusinesses.add(m);
	            	counter++;
	            	if(counter%5==0){
	            		publishProgress(newBusinesses);
	            		newBusinesses = new ArrayList<BusinessMarker>();
	            	}
	            }
	        	if(counter%5!=0){
	        		publishProgress(newBusinesses);
	        	}
	        	return null;
	        }
	        
	        @Override
	        protected void onProgressUpdate(ArrayList<BusinessMarker>... values) {
	        	for(BusinessMarker bm : values[0]){
	            	putOverlayOnMap(bm);
	        	}
	        	super.onProgressUpdate(values);
	        	
	        }
	        
	        /**
	         * A method that's called once doInBackground() completes. 
	         */
	        @Override
	        protected void onPostExecute(Void result) {
	            
	        	Toast.makeText(getApplicationContext(),"finished update", Toast.LENGTH_SHORT).show();
	        }

	        
	    }
	    
	    private static List<Long> favouriteIDs;
	    private static List<BusinessMarker> markersDB; //TODO: delete
	    /*public void loadDBs_debug()
	    {
	    	markersDB = new ArrayList<BusinessMarker>();
	    	favouriteIDs = new ArrayList<Long>();
	    	Random r = new Random();
	    	long id = 0;
	    	markersDB.add(new BusinessMarker("MCdonalds", BuisnessType.RESTURANT, new LatLng(31.781099, 35.217668), "Jerusalem",++id));
	    	markersDB.add(new BusinessMarker("Ivo", BuisnessType.RESTURANT, new LatLng(31.779949, 35.218948), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Dolfin Yam", BuisnessType.RESTURANT, new LatLng(31.779968, 35.221209), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Birman", BuisnessType.PUB, new LatLng(31.781855, 35.218086), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Bullinat", BuisnessType.PUB, new LatLng(31.781984, 35.218221), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Hamarush", BuisnessType.RESTURANT, new LatLng(31.781823, 35.219065), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Adom", BuisnessType.RESTURANT, new LatLng(31.781334, 35.220703), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Tel Aviv Bar", BuisnessType.PUB, new LatLng(31.781455, 35.220525), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Jabutinski Bar", BuisnessType.PUB, new LatLng(31.779654, 35.221654), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Reva Sheva", BuisnessType.SHOPPING, new LatLng(31.779793, 35.219728), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("The one with the shirts", BuisnessType.SHOPPING, new LatLng(31.779293, 35.221624), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Hamashbir Latsarchan", BuisnessType.SHOPPING, new LatLng(31.781824, 35.219959), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Hataklit", BuisnessType.PUB, new LatLng(31.781905, 35.221372), "Jerusalem"));
	    	markersDB.add(new BusinessMarker("Hatav Hashmini", BuisnessType.SHOPPING, new LatLng(31.781191, 35.219621), "Jerusalem"));
	    
	    	favouriteIDs.add(markersDB.get(2).businessId);
	    	favouriteIDs.add(markersDB.get(5).businessId);
	    	favouriteIDs.add(markersDB.get(8).businessId);
	    	favouriteIDs.add(markersDB.get(11).businessId);
	    }*/
	
}
