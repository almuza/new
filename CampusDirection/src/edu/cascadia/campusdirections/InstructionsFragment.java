package edu.cascadia.campusdirections;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class InstructionsFragment extends Fragment {

	private static final String EXTRA_CODE = "com.example.testingcodereading.code";
	private Button scanButton;
	private TextView textDirection;
	private TextView textLookFor;
	private static InstructionsFragment fragment;
	SearchFragment searchFrag;
	


	public static InstructionsFragment newInstance() {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_CODE, "");

		fragment = new InstructionsFragment();
		fragment.setArguments(args);

		return fragment;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.activity_instructions, parent, false);
	    
		setHasOptionsMenu(true); // this fragment has menu items to display

		// display what user look for on top of screen.
		String tempStr = getResources().getString(R.string.lookForStr, MainActivity.lookFor);
		tempStr += "\n" + getResources().getString(R.string.yourLocation, MainActivity.scanBuild, String.valueOf(MainActivity.scanFloor));
		if(MainActivity.searchClick)
			tempStr += "\n" + MainActivity.scanLocation();

		//Display what is user looking for; room number
		textLookFor = (TextView) v.findViewById(R.id.lookForText);
		textLookFor.setText(tempStr);

		//Display Text direction result after scanned
		textDirection = (TextView) v.findViewById(R.id.textDirection);
		String tempDirection = MainActivity.direction;
		
		//If there is special direction for certain room locate in select building/floor
		if(MainActivity.specialDirection != "")
			tempDirection += getResources().getString(R.string.specialDir, MainActivity.lookFor, MainActivity.specialDirection);
		
		if(tempDirection == "" )
			textDirection.setText(getResources().getString(R.string.directionStr));
		else
			textDirection.setText(tempDirection);
		
		//launch the qr code scanner if first time open this activity
		if(!MainActivity.searchClick){
			MainActivity.searchClick = true;
			IntentIntegrator integrator = new IntentIntegrator(getActivity());
			integrator.initiateScan();			
		}


		//open ZXing scanner when click on button
		scanButton = (Button) v.findViewById(R.id.scanButton);		
		scanButton.setOnClickListener(new View.OnClickListener() {
	
		@Override
		public void onClick(View v) {
			IntentIntegrator integrator = new IntentIntegrator(getActivity());
			integrator.initiateScan();
			}
		});

		return v;
	}
	
	//See MainActivity for onActivityResult after scan

	/*
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
//		System.out.println("the code is catch");

		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		// handle scan result
		if (scanResult != null) {
			FragmentManager fm = getFragmentManager();

			String myResult = intent.getStringExtra("SCAN_RESULT");

			InstructionsFragment newFrame = InstructionsFragment.newInstance(scanResult.toString(), myResult);
//			Fragment newFrame = InstructionsFragment.newInstance(myResult);

			//send result to new fragment.
			fm.beginTransaction().replace(R.id.fragmentContainer, newFrame).commit();
		}
	}
	*/
	
	   // display this fragment's menu items
	   @Override
	   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	   {
	      super.onCreateOptionsMenu(menu, inflater);
	      inflater.inflate(R.menu.main, menu);
	   }

	   // handle menu item selections
	   @Override
	   public boolean onOptionsItemSelected(MenuItem item) 
	   {
	      switch (item.getItemId())
	      {
	         case R.id.action_reset:
	        	 MainActivity.resetResult();
	        	 searchFragment();
	            return true;
	      }
	      
	      return super.onOptionsItemSelected(item);
	   } 
	   
	   // launch Search fragment for input
	   public void searchFragment()
	   {
		   	searchFrag = new SearchFragment();
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragmentContainer, searchFrag);
			ft.addToBackStack(null);
			ft.commit(); // causes CollectionListFragment to display		      
	   }	   	
}