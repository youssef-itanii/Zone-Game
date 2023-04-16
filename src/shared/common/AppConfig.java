package shared.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppConfig {
	
	

	static String fileName = "app.config";
	
	public static int getNumberOfZones() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		
		return Integer.parseInt(prop.getProperty("NUMBER_OF_ZONES"));
	}
    //===========================================================================
	public static int getZoneSize() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		
		return Integer.parseInt(prop.getProperty("ZONE_SIZE"));
	}
    //===========================================================================
	public static int getZonePerRow() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		
		return Integer.parseInt(prop.getProperty("ZONES_PER_ROW"));
	}
	//==============================================================================
	public static String getServerAddress() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		
		return prop.getProperty("SERVER_ADDRESS");
	}
	//==============================================================================
	public static int getPort() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		
		return Integer.parseInt(prop.getProperty("PORT"));
	}
	
	//==============================================================================
	public static List<String> getMovementKeys() {
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(fileName)){
			prop.load(fis);
		}
		catch(FileNotFoundException ex) {
			
		}
		catch (IOException e) {
			// TODO: handle exception
		}
		List<String> movementKeys = new ArrayList<String>();
		movementKeys.add(prop.getProperty("UP"));
		movementKeys.add(prop.getProperty("DOWN"));
		movementKeys.add(prop.getProperty("LEFT"));
		movementKeys.add(prop.getProperty("RIGHT"));
		return movementKeys;
	}
}
