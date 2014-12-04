//MainActivity Campus Direction

package com.campusdirection;

import java.util.Arrays;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity implements SearchFragment.SearchFragmentListener{

	SearchFragment searchFragment;
	public static String lookFor = "Kodiac Corner";
	public static String direction = "";
	// Determine QR Code string
	public static String scanBuild, scanRoom, scanName, scanExit, inputBuild, inputRoom;
	public static int scanFloor, scanSide, scanIndex, inputFloor, inputLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(savedInstanceState != null)
			return;

		if(findViewById(R.id.fragmentContainer) != null)
		{
			searchFragment = new SearchFragment();
	        FragmentTransaction transaction = getFragmentManager().beginTransaction();
	        transaction.add(R.id.fragmentContainer, searchFragment);
	        transaction.commit(); // causes CollectionListFragment to display		
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		// handle scan result
		if (scanResult != null) {

//			Fragment newFrame = MainFragment.newInstance(scanResult.toString());
			String result = intent.getStringExtra("SCAN_RESULT");
			
			splitScanResult(result); //split scan result
			
			direction = "Here is your scan result ["+result+"]";
//			compileDirection();	//compile direction
			compileDirection();

			//send result to new fragment.
			FragmentManager fm = getFragmentManager();
			InstructionsFragment newFrame = InstructionsFragment.newInstance();
			fm.beginTransaction().replace(R.id.fragmentContainer, newFrame).commit();
		}
	}
	
	public void compileDirection()
	{
		direction = "Test Result:\n\n";
		// is building input/scan the same
		if(scanBuild.equals(inputBuild)){
			// is the input/scan floor the same
			if(inputFloor == scanFloor){
				//determine room direction
				RoomDir();
				//also determine is looking for room locate special location
				compileSpecialDir();
			}else{
				//determine Floor to Floor direction
				floorDirection();

				//also brief determine room direction as well
				basicRoomDirection();
			}			
		}else{
			//determine Building to Building direction
			if((scanBuild.equals("CC1") && inputBuild.equals("CC2")) || (scanBuild.equals("CC2") && inputBuild.equals("CC1")))
			{
				//determine Floor to Floor direction
				floorDirection();

				//user inside building CC1 and looking for room in building CC2 and by versa
				switch(scanBuild){
					case "CC1":	// user location in CC1 building and want to go to CC2 building
						if(scanSide == 1)
							direction += getResources().getString(R.string.roomDir, "RIGHT", myRoomLocation("right"));
						else if(scanSide == 0)
							direction += getResources().getString(R.string.roomDir, "LEFT", myRoomLocation("right"));
						break;
					case "CC2": // user location in CC2 building and want to go to CC1 building
						direction += "\nGo forward down the hall, the room is locate on your " + myRoomLocation("left") + ".";
						break;
					default:
						direction += "Building is unkown. Please rescan bar code again.";
						break;
				}				
			}else{
				if(inputBuild.equals("CC3")){
					if(scanBuild.equals("CC1") || scanBuild.equals("CC2")){
						//user from CC3 and want to go to CC1 or CC2
						if(scanFloor != 1){
							floorDirection();
						}
						direction += getResources().getString(R.string.exitCC1, inputBuild, "RIGHT");

					}else if(scanBuild.equals("some other building here")){
						//user from CC3 and want to go to other building
					}
				}else if(inputBuild.equals("CC1") || inputBuild.equals("CC2")){
					if(scanBuild.equals("CC3")){
						//user from building CC1 or CC2 want to go to CC3 building
						if(scanFloor != 1){
							floorDirection();
						}
						direction += getResources().getString(R.string.exitCC3, inputBuild, "LEFT");
					}else if(scanBuild.equals("some other building here such library or bookstore")){
						//user from CC1 or CC2 and want to go to other building
					}
				}				
			}
		}
	}
	
	//determine Floor to Floor direction
	public void floorDirection()
	{
		if(inputBuild.equals("CC1") || inputBuild.equals("CC2")){
			if(inputFloor > scanFloor)
				direction += getResources().getString(R.string.floorDir, "UP", stringFloor(inputFloor))+"\n";
			else if(inputFloor < scanFloor)
				direction += getResources().getString(R.string.floorDir, "DOWN", stringFloor(inputFloor))+"\n";
		}else{
			if(scanFloor < 1)
				direction += getResources().getString(R.string.floorDir, "UP", stringFloor(1))+"\n";
			else if(scanFloor > 1)
				direction += getResources().getString(R.string.floorDir, "DOWN", stringFloor(1))+"\n";			
		}
	}
	

	//provide basic room direction when user from different floor
	public void basicRoomDirection()
	{
		if(inputBuild.equals("CC1") || inputBuild.equals("CC2"))
		{
			if(inputLocation <= 3 && inputLocation >= 0)
				direction += getResources().getString(R.string.cc1FloorDir, stringFloor(inputFloor), "LEFT", myRoomLocation("left"));
			else if(inputLocation >= 4 && inputLocation <= 8)
				direction += getResources().getString(R.string.cc1FloorDir, stringFloor(inputFloor), "RIGHT", myRoomLocation("right"));
		}
		else if(inputBuild.equals("CC3"))
		{
			if(inputLocation <= 3 && inputLocation >= 2)
				direction += getResources().getString(R.string.cc3FloorDir, stringFloor(inputFloor), "RIGHT", myRoomLocation("left"));
			else if(inputLocation >= 0 && inputLocation <= 1)
				direction += getResources().getString(R.string.cc3FloorDir, stringFloor(inputFloor), "LEFT", myRoomLocation("right"));			
		}
	}
	
	// which side is room locate
	public String myRoomLocation(String str)
	{
		int tempRoom = Integer.parseInt((inputRoom.replaceAll("[\\D]", "")));
		if(str.equals("left"))
		{
			if(tempRoom % 2 == 0) return "LEFT";
			else return "RIGHT";		
		}else{
			if(tempRoom % 2 == 0) return "RIGHT";
			else return "LEFT";		
		}
	}
	
	// determine floor level and return text value
	public String stringFloor(int flr)
	{
		switch(flr)
		{
			case 0: return "Level 0";
			case 1: return "1st";
			case 2: return "2nd";
			case 3: return "3rd";
			case 4: return "4th";
			case 5: return "5th";
			case 6: return "6th";
			case 7: return "7th";
			default: return "";
		}
	}

	
	// return index value (integer) from an array where room is match.
	public int getRoomIndex()
	{
		Resources res = getResources();
		TypedArray tempBld = null;
		switch(inputBuild){
	   		case "CC1":
	   			tempBld = res.obtainTypedArray(R.array.CC1);
	   			break;
	   		case "CC2":
	   			tempBld = res.obtainTypedArray(R.array.CC2);
	   			break;
	   		case "CC3":
	   			tempBld = res.obtainTypedArray(R.array.CC3);
	   			break;
	   		default:
	   			return -1; // invalid build 
		}
		//return index value(integer) of room location in an array base
		//from user input building and room number
		return Arrays.asList(res.getStringArray(tempBld.getResourceId(inputFloor, 0))).indexOf(inputRoom);
	}

	// compile direction if the room is locate in hiding location.
	public void compileSpecialDir()
	{
		
	}
	
	// determine room direction on the same floor.
	public void RoomDir()
	{
		int current = Integer.parseInt(scanRoom.replaceAll("[\\D]", ""));      //here we are making our strings for rooms into integers
		int destination = Integer.parseInt(inputRoom.replaceAll("[\\D]", ""));
		
		if (current == destination+1 || current == destination-1 ){             //if the destination is only +-1 from your location it is behind you.
//			direction += "Turn around to find your destination";
			direction += getResources().getString(R.string.destination, inputRoom);
		}
		else if (scanIndex < getRoomIndex()){
			if (scanSide == 1){
				//This should be made the only text that displays on the fragment
				direction += getResources().getString(R.string.roomDir, "RIGHT", myRoomLocation("right"));
			}
			else if (scanSide ==0){
				direction += getResources().getString(R.string.roomDir, "LEFT", myRoomLocation("right"));
			}
		}
		else if (scanIndex > getRoomIndex()){
			if (scanSide == 1){
				direction += getResources().getString(R.string.roomDir, "LEFT", myRoomLocation("left"));
			}
			else if (scanSide ==0){
				direction += getResources().getString(R.string.roomDir, "RIGHT", myRoomLocation("left"));
			}
		}
//		direction += "\n\n"+ getResources().getString(R.string.testDir, scanBuild, String.valueOf(scanFloor), scanRoom, String.valueOf(scanSide), String.valueOf(scanIndex), scanName); 
	}
	
	// determine split the scan result content
	public void splitScanResult(String str)
	{
		String[] tempStr = str.split("-");
		scanBuild = tempStr[0].trim();
		scanFloor = Integer.parseInt(tempStr[1]);
		scanSide = Integer.parseInt(tempStr[2]);
		scanIndex = Integer.parseInt(tempStr[3]);
		scanRoom = tempStr[4].trim();
		//check to see if location name giving from QR code
		if(tempStr.length == 6)
			scanName = tempStr[5].trim();
		else
			scanName = "";
	}
	
	// determine user input
	public static void setSplitInput(String building, String room, int flr, int loc)
	{
		inputBuild = building;
		inputRoom = room;
		inputFloor = flr;
		inputLocation = loc;
	}
	
	public void resetResult()
	{
		scanBuild = null;
		scanFloor = -1;
		scanSide = -1;
		scanIndex = -1;
		scanRoom = null;
		scanName = null;
		scanExit = null;
	}
}
