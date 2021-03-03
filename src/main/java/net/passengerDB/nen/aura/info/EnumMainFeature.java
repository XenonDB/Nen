package net.passengerDB.nen.aura.info;

public enum EnumMainFeature {
	
	MANIPULATION(-2), EMISSION(-1), ENHANCEMENT(0), TRANSMUTATION(1), CONJURATION(2);
	
	private static final EnumMainFeature[] features = EnumMainFeature.values();
	
	private final int featureValue; 
	
	private EnumMainFeature(int value) {
		this.featureValue = value;
	}
	
	public int getValue() {
		return featureValue;
	}
	
	public static EnumMainFeature getCorrespondingFeature(int value) {
		for(EnumMainFeature feature : features) {
			if(value == feature.featureValue) return feature;
		}
		return null;
	}
}
