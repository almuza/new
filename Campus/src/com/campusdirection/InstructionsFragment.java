package com.campusdirection;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
		
		// display what user look for on top of screen.
		String tempStr = getResources().getString(R.string.lookForStr, MainActivity.lookFor);
		tempStr += "\n"+ getResources().getString(R.string.yourLocation, MainActivity.scanBuild, String.valueOf(MainActivity.scanFloor));
//		String tempStr = "\n"+ getResources().getString(R.string.testLook, "CC2", "2", "230");
		textLookFor = (TextView) v.findViewById(R.id.lookForText);
		textLookFor.setText(tempStr);

		
		textDirection = (TextView) v.findViewById(R.id.textDirection);
		String tempDirection = MainActivity.direction;
		if(tempDirection == "" )
			textDirection.setText(getResources().getString(R.string.directionStr));
		else
			textDirection.setText(tempDirection);


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
}