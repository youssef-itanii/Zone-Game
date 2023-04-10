package shared.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
	public static int getNumberOfZones() {
		Properties prop = new Properties();
		String fileName = "app.config";
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
	
	public static int getZoneSize() {
		Properties prop = new Properties();
		String fileName = "app.config";
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
	
	public static int getZonePerRow() {
		Properties prop = new Properties();
		String fileName = "app.config";
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

}
