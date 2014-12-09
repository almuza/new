//MainActivity Campus Direction

package edu.cascadia.campusdirections;

import java.util.Arrays;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends Activity implements SearchFragment.SearchFragmentListener{

	SearchFragment searchFragment;
	public static String lookFor = "Kodiac Corner";
	public static String direction = "", specialDirection = "";
	// Determine QR Code string
	public static String scanBuild, scanRoom, scanName, scanExit, inputBuild, inputRoom;
	public static int scanFloor, scanSide, scanIndex, inputFloor, inputLocation, scanLocation;
	public static boolean searchClick = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(savedInstanceState != null)
			return;
		
		resetResult();
		
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
			//result string return from scanner
			String result = intent.getStringExtra("SCAN_RESULT");
			
			if(splitScanResult(result)) //split string value from qr code scan result				
				compileDirection(); //begin compile direction from result
			else
				direction = "---> ERROR <---\n" + getResources().getString(R.string.invalidQRCode, result);

			//send result to new fragment.
			FragmentManager fm = getFragmentManager();
			InstructionsFragment newFrame = InstructionsFragment.newInstance();
			fm.beginTransaction().replace(R.id.fragmentContainer, newFrame).commit();
		}
	}
	
	// compile direction output text string display to user
	public void compileDirection()
	{
		direction = "---> Direction <---\n";
		// is building input/scan the same
		if(scanBuild.equals(inputBuild)){
			//only if user inside LBA building and look for room in there
			if(inputBuild.equals("LBA") && scanBuild.equals("LBA")){
				direction += getResources().getString(R.string.roomLBA);
				//also determine is looking for room locate special location
				if(isSpecialRoom()) compileSpecialDir();
				return;
			}//end brief room direction inside LBA building				
				
			// is the input/scan floor the same
			if(inputFloor == scanFloor){
				//determine room direction
				RoomDir();
			}else{
				//determine Floor to Floor direction
				floorDirection();
				//also brief determine room direction as well
				basicRoomDirection();
			}			
			//also determine is looking for room locate special location
			if(isSpecialRoom()) compileSpecialDir();
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
							direction += getResources().getString(R.string.roomDirBuilding, "RIGHT", inputBuild, myRoomLocation("right"));
						else if(scanSide == 0)
							direction += getResources().getString(R.string.roomDirBuilding, "LEFT", inputBuild,  myRoomLocation("right"));
						break;
					case "CC2": // user location in CC2 building and want to go to CC1 building
						if(scanSide == 1)
							direction += getResources().getString(R.string.roomDirBuilding, "LEFT", inputBuild, myRoomLocation("left"));
						else if(scanSide == 0)
							direction += getResources().getString(R.string.roomDirBuilding, "RIGHT", inputBuild,  myRoomLocation("left"));
						break;
					default:
						direction += "Building unkown. Please rescan QR code again.";
						break;
				}				
			}else{
				if(inputBuild.equals("CC3")){ //user want to go to building CC3
					if(scanBuild.equals("CC1") || scanBuild.equals("CC2")){ //start from CC1 or CC2 building
						//user from CC1 or CC2 and want to go to CC3
						if(scanFloor != 1) floorDirection();
						direction += getResources().getString(R.string.exitCC1, inputBuild, "RIGHT");
					}else if(scanBuild.equals("LBA")){ //start from LBA share library
						//user from LBA and want to go to CC3 building
						if(scanFloor != 1) floorDirection();
						direction += getResources().getString(R.string.exitLBA, inputBuild, "LEFT pass CC1 building");
					}
				}else if(inputBuild.equals("CC1") || inputBuild.equals("CC2")){ //user want to go to building CC1 or CC2
					if(scanBuild.equals("CC3")){ //start from CC3 building
						//user from building CC3 want to go to CC1 or CC2 building
						if(scanFloor != 1) floorDirection();
						direction += getResources().getString(R.string.exitCC3, inputBuild, "LEFT");
					}else if(scanBuild.equals("LBA")){ //start from LBA share library building
						//user from LBA and want to go to CC1 or CC2 building
						if(scanFloor != 1) floorDirection();
						direction += getResources().getString(R.string.exitLBA, inputBuild, "RIGHT");
					}
				}else if(inputBuild.equals("LBA")){ //user want to go to LBA share library building
					if(scanBuild.equals("CC3")){ //start from CC3 building
						//user from building CC3 want to go to LBA building
						if(scanFloor != 1) floorDirection(); 
						direction += getResources().getString(R.string.exitCC3, inputBuild, "LEFT pass CC1 building");
					}else if(scanBuild.equals("CC1") || scanBuild.equals("CC2")){ //start from CC1 or CC2 building
						//user from CC1 or CC2 building and want to go to LBA building
						if(scanFloor != 1) floorDirection(); 
						direction += getResources().getString(R.string.exitCC1, inputBuild, "LEFT pass the Bookstore");
					}					
				}//add another else if here if user want to go to other different building
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
		specialDirection = "[SPECIAL DIRECTION HERE]";
	}
	
	// determine room direction on the same floor.
	public void RoomDir()
	{
		int currentRm = Integer.parseInt(scanRoom.replaceAll("[\\D]", ""));      //here we are making our strings for rooms into integers
		int destinationRm = Integer.parseInt(inputRoom.replaceAll("[\\D]", ""));
		
		//quick check to see if user already at destination
		if(currentRm == destinationRm){
			direction += getResources().getString(R.string.roomFound, inputRoom);
			return;
		}
		
		if (currentRm == destinationRm+1 || currentRm == destinationRm-1 ){             //if the destination is only +-1 from your location it is behind you.
			direction += getResources().getString(R.string.destination, inputRoom);
		}else if (scanIndex < getRoomIndex()){
			if (scanSide == 1){	// user facing odd side room number
				//This should be made the only text that displays on the fragment
				direction += getResources().getString(R.string.roomDir, "RIGHT", lookFor, myRoomLocation("right"));
			}
			else if (scanSide == 0){ // user facing even side room number
				direction += getResources().getString(R.string.roomDir, "LEFT", lookFor, myRoomLocation("right"));
			}
		}else if (scanIndex > getRoomIndex()){
			if (scanSide == 1){ // user facing odd side room number
				direction += getResources().getString(R.string.roomDir, "LEFT", lookFor, myRoomLocation("left"));
			}
			else if (scanSide == 0){ // user facing even side room number
				direction += getResources().getString(R.string.roomDir, "RIGHT", lookFor, myRoomLocation("left"));
			}
		}
	}
	
	// determine split the scan result content
	public boolean splitScanResult(String str)
	{
		String[] tempStr = str.split("-");

		//check to see if QR code is valid string format
		if(tempStr.length != 6) return false;

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
		
		//determine scan location of room number by second digit.
		scanLocation = Integer.parseInt((scanRoom.replaceAll("[\\D]", "")).substring(1, 2));
		
		//if qr code string format is correct, check whether string contain right value or not.
		if(scanFloor < 0 || scanIndex < 0 || scanBuild.equals("") || scanRoom.equals(""))
			return false;

		return true;
	}
	
	// determine user input
	public static void setSplitInput(String building, String room, int flr, int loc)
	{
		inputBuild = building;
		inputRoom = room;
		inputFloor = flr;
		inputLocation = loc;
	}
	
	// reset all static variable
	public static void resetResult()
	{
		scanBuild = null;
		scanFloor = -1;
		scanSide = -1;
		scanIndex = -1;
		scanRoom = null;
		scanName = null;
		scanExit = null;
		scanLocation = -1;
		searchClick = false;
	}
	
	// determine where user inside building approximate location
	public static String scanLocation()
	{
		String loc = "";
		switch(scanLocation){
			case 0:
				if(scanBuild.equals("CC1") || scanBuild.equals("CC2"))
					loc = "Center of Building";
				else if(scanBuild.equals("CC3"))
					loc = "North End Building";
				break;
			case 1:
				if(scanBuild.equals("CC1") || scanBuild.equals("CC2"))
					loc = "Center of Building";				
				else if(scanBuild.equals("CC3"))
					loc = "North End Building";
				break;
			case 2:
				if(scanBuild.equals("CC1") || scanBuild.equals("CC2"))
					loc = "South End Building";				
				else if(scanBuild.equals("CC3"))
					loc = "Center of Building";				
				break;				
			case 3:
				loc = "South End Building";				
				break;
			case 4:
				loc = "North End Building";				
				break;
			case 5:
				loc = "North End Building";								
				break;
			case 6:
				loc = "South End Building";				
				break;
			case 7:
				loc = "Center of Building";								
				break;
			case 8:
				loc = "North End Building";								
				break;
			default:
				loc = "Location Unknown";
				break;
		}
		return loc;
	}
	
	//check to see if the room user enter existed in that building/floor Special location
	public boolean isSpecialRoom()
	{
		Resources res = getResources();
		TypedArray tempBld;
		switch(inputBuild){
		  	case "CC1":
		  		if(inputFloor >= 0 && inputFloor < 4){
		  			tempBld = res.obtainTypedArray(R.array.CC1_Special);
		   			return verifySpecRoom(res.getStringArray(tempBld.getResourceId(inputFloor, 0)));
		   		}else return false;
		   	case "CC2":
		   		if(inputFloor >= 0 && inputFloor < 4){
		   			tempBld = res.obtainTypedArray(R.array.CC2_Special);
		   			return verifySpecRoom(res.getStringArray(tempBld.getResourceId(inputFloor, 0)));
		   		}else return false;
		   	case "CC3":
		   		if(inputFloor > 0 && inputFloor < 3){
		   			tempBld = res.obtainTypedArray(R.array.CC3_Special);
		   			return verifySpecRoom(res.getStringArray(tempBld.getResourceId(inputFloor, 0)));
		   		}else return false;
		   	case "LBA": //Share building: Library Annex
		   		if(inputFloor == 1){
		   			tempBld = res.obtainTypedArray(R.array.LBA_Special);
		   			return verifySpecRoom(res.getStringArray(tempBld.getResourceId(inputFloor, 0)));		   			
		   		}else return false;		   		
		   	default:
		   		return false; //building is invalid
		}
	}

	// check existing room per floor plan
	public boolean verifySpecRoom(String[] arr)
	{
		return Arrays.asList(arr).contains(inputRoom);
	}	
}
