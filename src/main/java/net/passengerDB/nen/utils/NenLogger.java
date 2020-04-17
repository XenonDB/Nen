package net.passengerDB.nen.utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class NenLogger {

	private static Logger logger = LogManager.getLogger("Nen");
	
	public static void info(String s) {
		logger.info(s);
	}
	
}
