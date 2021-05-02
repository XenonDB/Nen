package net.passengerDB.nen.config;

import net.minecraftforge.common.config.Config;
import net.passengerDB.nen.Nen;

@Config(modid = Nen.MODID)
public class NenConfig {

	@Config.Comment("True to enable debug mode for this mod.")
	public static boolean debugMode = false;
	
}
