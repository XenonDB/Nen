package net.passengerDB.nen.config;

import net.minecraftforge.common.ForgeConfigSpec;

//https://forums.minecraftforge.net/topic/102563-1165-how-can-i-create-config-for-my-mod/
public final class NenConfig {

	public static final String CONFIG_NAME = "nen.toml";
	
	private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	public static final ForgeConfigSpec.BooleanValue debugMode;
	
	static {
		
		CONFIG_BUILDER.push("General config");
		
		debugMode = CONFIG_BUILDER.comment("True to enable debug mode for this mod. Currently do nothing.").define("debugMode", false);
		
		CONFIG_BUILDER.pop();
		
		
		SPEC = CONFIG_BUILDER.build();
	}
	
}
