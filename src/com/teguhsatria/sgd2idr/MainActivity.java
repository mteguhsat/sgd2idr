package com.teguhsatria.sgd2idr;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.KeyEvent;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			if (position == 0){
				Fragment fragment = new RateSectionFragment();
				Bundle args = new Bundle();
				args.putInt(RateSectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}else{
				Fragment fragment = new CalculatorFragment();
				Bundle args = new Bundle();
				args.putInt(CalculatorFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;				
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class RateSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
	    static final String BNI_URL = "http://ptbni.com.sg/index.cfm?GPID=64";
	    static final String DBS_URL = "http://www.dbs.com/ratesonline/xml/Pages/XFXBRA.xml";
	    static final String BCA_URL = "http://www.klikbca.com/";
	    public static ConnectToWeb bniconnect;
	    public static ConnectToWeb dbsconnect;
	    public static ConnectToWeb bcaconnect;
	    Button refresh_button;
	    NumberFormat formatter;
	    DecimalFormatSymbols dfs;
	    private static float rates[];
	    
		public RateSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.rates,
					container, false);
			
			formatter = NumberFormat.getCurrencyInstance();
			dfs = new DecimalFormatSymbols();
			dfs.setCurrencySymbol("Rp. ");
			dfs.setGroupingSeparator('.');
			dfs.setMonetaryDecimalSeparator(',');
			((DecimalFormat) formatter).setDecimalFormatSymbols(dfs);
			
			rates = new float[3];
			
			refresh_button = (Button) rootView.findViewById(R.id.refresh_btn);
			refresh_button.setOnClickListener(new OnClickListener()
		    {
		        @Override
		        public void onClick(View view)
		        {
		        	deleteConnections();
		        	createConnections();
		        }
		    });
			
			createConnections();
			
			return rootView;
		}
		
		private void deleteConnections(){
			bniconnect = null;
			dbsconnect= null;
			bcaconnect= null;
			TextView constat = (TextView) getView().findViewById(R.id.connect_stat_bni);
			constat.setText(R.string.trytoconnect);				
			TextView datetv = (TextView) getView().findViewById(R.id.last_update_bni);
			datetv.setText(R.string.get_data);
			TextView rate = (TextView) getView().findViewById(R.id.rate_bni);
			rate.setText(R.string.get_data);
			
			TextView constat2 = (TextView) getView().findViewById(R.id.connect_stat_dbs);
			constat2.setText(R.string.trytoconnect);				
			TextView datetv2 = (TextView) getView().findViewById(R.id.last_update_dbs);
			datetv2.setText(R.string.get_data);
			TextView rate2 = (TextView) getView().findViewById(R.id.rate_dbs);
			rate2.setText(R.string.get_data);
			
			TextView constat3 = (TextView) getView().findViewById(R.id.connect_stat_bca);
			constat3.setText(R.string.trytoconnect);				
			TextView datetv3 = (TextView) getView().findViewById(R.id.last_update_bca);
			datetv3.setText(R.string.get_data);
			TextView rate3 = (TextView) getView().findViewById(R.id.rate_bca);
			rate3.setText(R.string.get_data);
		}
		
		private void createConnections(){			
			setRetainInstance(true);			
			bniconnect = new ConnectToWeb(this, 0);
			new WeakReference<ConnectToWeb >(bniconnect );
			bniconnect.execute(BNI_URL);
			
			dbsconnect = new ConnectToWeb(this, 1);
			new WeakReference<ConnectToWeb >(dbsconnect );
			dbsconnect.execute(DBS_URL);
			
			bcaconnect = new ConnectToWeb(this, 2);
			new WeakReference<ConnectToWeb >(bcaconnect );
			bcaconnect.execute(BCA_URL);
		}
		
		public void doSomething(ConnectToWeb connect){			
			if(connect.getID() == 0){ // BNI
				TextView constat = (TextView) getView().findViewById(R.id.connect_stat_bni);
				constat.setText("Connection [OK]");				
				try {
					Document doc = connect.response.parse();
					Elements eldate = doc.select("td b");
					String ldate = eldate.get(3).text();
					String ltime = eldate.get(4).text();
					
					Elements elrate = doc.select("td[bgcolor]");
					rates[connect.getID()] = Float.parseFloat(elrate.get(7).text());
					
					TextView datetv = (TextView) getView().findViewById(R.id.last_update_bni);
					datetv.setText("Last update: " + ldate + "  " + ltime+"am");
					
					TextView rate = (TextView) getView().findViewById(R.id.rate_bni);
					rate.setText(formatter.format(rates[connect.getID()]));
		    	} catch (IOException ex) {
		    	}
			}else if(connect.getID() == 1){ // id = 1, DBS
				TextView constat = (TextView) getView().findViewById(R.id.connect_stat_dbs);
				constat.setText("Connection [OK]");
				try {
					Document doc = connect.response.parse();
					Elements eldate = doc.select("effective_date");
					String ldate = eldate.get(0).attr("value");
					Elements eltime = doc.select("last_updated");
					String ltime = eltime.get(0).attr("value");
					
					Elements elunit = doc.select("unit[value]");
					int unit = Integer.parseInt(elunit.get(14).attr("value"));
					Elements elrate = doc.select("selling_ttod[value]");
					float idr2sgd = Float.parseFloat(elrate.get(28).attr("value"));
					rates[connect.getID()] = unit/idr2sgd;
					
					TextView datetv = (TextView) getView().findViewById(R.id.last_update_dbs);
					datetv.setText("Last update: " + ldate + " " + ltime);
					
					TextView rate = (TextView) getView().findViewById(R.id.rate_dbs);
					rate.setText(formatter.format(rates[connect.getID()]));
		    	} catch (IOException ex) {
		    	}
			}else { // BCA
				TextView constat = (TextView) getView().findViewById(R.id.connect_stat_bca);
				constat.setText("Connection [OK]");
				try {
					Document doc = connect.response.parse();
					
					Elements eldata = doc.select("td[class]");
					String ldate = eldata.get(0).text();
					float jual = Float.parseFloat(eldata.get(5).text());
					float beli = Float.parseFloat(eldata.get(6).text());
					rates[connect.getID()] = (jual+beli)/2;

					TextView datetv = (TextView) getView().findViewById(R.id.last_update_bca);
					datetv.setText("Last update: " + ldate);

					TextView rate = (TextView) getView().findViewById(R.id.rate_bca);
					rate.setText(formatter.format(rates[connect.getID()]));
		    	} catch (IOException ex) {
		    	}
			}
		}
		
		public static float[] getRates(){
			return rates;
		}
	        
		public class ConnectToWeb extends AsyncTask<String, Void, Integer> {
		    public Connection.Response response = null;
		    public Document doc;
	        private WeakReference<RateSectionFragment> fragmentWeakRef;
	        private int id;

	        private ConnectToWeb (RateSectionFragment fragment, int ID) {
	            this.fragmentWeakRef = new WeakReference<RateSectionFragment>(fragment);
	            this.id = ID;
	        }
	        
	        public int getID(){
	        	return this.id;
	        }
			
			@Override
			protected Integer doInBackground(String... urls) {
				int connectStat = 0;
			   	try {
			   		response = Jsoup.connect(urls[0]).execute();
			   		connectStat = response.statusCode();
		    	} catch (IOException ex) {
		    	}
			   	
			   	return connectStat;
			}
			
			protected void onProgressUpdate(Void... progress) {
		    }
			
			
			@Override
			protected void onPostExecute(Integer connectStat) {
				super.onPostExecute(connectStat);
	            if (this.fragmentWeakRef.get() != null) {
	            	if (connectStat == 200){
	            		this.fragmentWeakRef.get().doSomething(this);
	            	}
	            }
		    }

		}
		
	}

	
	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class CalculatorFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static int convMode = 0;
		public static int bankRate = 0;
		public static double amount = 0.0;
		Button resetBtn;
		TextView amountx;
		TextView result;
		NumberFormat formatter;
		DecimalFormatSymbols idrsym;
		DecimalFormatSymbols sgdsym;
		RateSectionFragment fragRate;
		EditText amountCol;
		DecimalFormat dec;

		public CalculatorFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.calculator,
					container, false);
			
			dec = new DecimalFormat("###,###.00");
			formatter = NumberFormat.getCurrencyInstance();
			idrsym = new DecimalFormatSymbols();
			idrsym.setCurrencySymbol("Rp. ");
			idrsym.setGroupingSeparator('.');
			idrsym.setMonetaryDecimalSeparator(',');
			((DecimalFormat) formatter).setDecimalFormatSymbols(idrsym);
			
			sgdsym = new DecimalFormatSymbols();
			sgdsym.setCurrencySymbol("S$ ");
			sgdsym.setGroupingSeparator(',');
			sgdsym.setMonetaryDecimalSeparator('.');
			amountx = (TextView) rootView.findViewById(R.id.amountv);
			Spinner spinner = (Spinner) rootView.findViewById(R.id.spinconversmode);
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
			        R.array.conversion_mode, android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					convMode = arg2;
					if (convMode == 0){
						amountx.setText(R.string.amountSg);
						((DecimalFormat) formatter).setDecimalFormatSymbols(idrsym);
					}else{
						amountx.setText(R.string.amountRp);
						((DecimalFormat) formatter).setDecimalFormatSymbols(sgdsym);
					}
					amountCol.setText("");
					amount = 0.0;
					calculate();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
			});

			Spinner spinb = (Spinner) rootView.findViewById(R.id.spinbank);
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayAdapter<CharSequence> adaptbank = ArrayAdapter.createFromResource(getActivity(),
			        R.array.banks, android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adaptbank.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinb.setAdapter(adaptbank);
			
			spinb.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					bankRate = arg2;
					calculate();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
			});
			
			resetBtn = (Button) rootView.findViewById(R.id.resetbtn);
			resetBtn.setOnClickListener(new OnClickListener()
		    {
		        @Override
		        public void onClick(View view)
		        {
		    		convMode = 0;
					Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinconversmode);
		    		spinner.setSelection(0);
		    		bankRate = 0;
					Spinner spinbk = (Spinner) getActivity().findViewById(R.id.spinbank);
					spinbk.setSelection(0);
		    		amount = 0.0;
		    		((DecimalFormat) formatter).setDecimalFormatSymbols(idrsym);
		    		calculate();
					amountCol.setText("");
		        }
		    });
			
			result = (TextView) rootView.findViewById(R.id.result);
			result.setText(formatter.format(0));
			
			String bankstr = getResources().getString(R.string.title_section1);
			fragRate = (RateSectionFragment)getActivity()
				     .getSupportFragmentManager().findFragmentByTag(bankstr);
			
			
			amountCol = (EditText) rootView.findViewById(R.id.editAmount);			
			amountCol.setOnClickListener(new OnClickListener() {
			    @Override
			    public void onClick(View v) {
			    	amountCol.setFocusableInTouchMode(true);
			    	amountCol.requestFocus();
			    	amountCol.setText("");
			    	amount = 0.0;
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.showSoftInput(amountCol, InputMethodManager.SHOW_IMPLICIT);
			    }
			});
			
			amountCol.setOnEditorActionListener(new EditText.OnEditorActionListener() {				
	            @Override
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
	                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED || //calculate
	                        event.getAction() == KeyEvent.ACTION_DOWN &&
	                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) 
	                {
	                	String itemString = v.getText().toString();
	                	if (itemString.equals(""));
	                	else{
	                		amount = Double.parseDouble(itemString);
	                		v.setText(dec.format(amount));
	                	
	                		InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                		in.hideSoftInputFromWindow(amountCol.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	     	            
	                		amountCol.setFocusable(false);
	                    
	                		calculate();
	                	}
	                	
	                    return true;
	                }
	                return false;
	            }
	        });
			
			return rootView;
		}
		
		@SuppressWarnings("static-access")
		private void calculate() {
			if (convMode == 0) {
				result.setText(formatter.format(amount*fragRate.getRates()[bankRate]));
			} else {
				result.setText(formatter.format(amount/fragRate.getRates()[bankRate]));
			}
		}
		
	}
		
}
