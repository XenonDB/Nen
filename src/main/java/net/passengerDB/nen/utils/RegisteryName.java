package net.passengerDB.nen.utils;

import net.minecraft.util.ResourceLocation;
import net.passengerDB.nen.Nen;

public class RegisteryName {

	public static ResourceLocation getModResourceLocation(String name) {
		return new ResourceLocation(Nen.MODID,name);
	}
	
	public static String getModRegisteryName(String name) {
		return String.format("%s:%s", Nen.MODID, name);
	}
}
