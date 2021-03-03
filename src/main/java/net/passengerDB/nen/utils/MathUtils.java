package net.passengerDB.nen.utils;

public class MathUtils {

	public static float clamp(float f, float bound1, float bound2) {
		if(bound1 == bound2) return bound1;
		if(bound1 > bound2) return Math.min(Math.max(bound2, f), bound1);
		return Math.min(Math.max(bound1, f), bound2);
	}
	
}
