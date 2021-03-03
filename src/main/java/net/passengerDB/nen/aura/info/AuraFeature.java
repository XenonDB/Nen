package net.passengerDB.nen.aura.info;

import net.passengerDB.nen.utils.MathUtils;
import java.lang.IllegalArgumentException;

/**
 * 表示氣的特性。需要帶有兩個參數:變異數以及期望值。
 * 是的，這正是常態分布。一個AuraFeature的實例表示一種常態分布。
 * AuraFeature的鋒值(也就是期望值)決定了這個氣是哪一種氣(操作、放出、強化、變化、具現化)，
 * 鋒值的區間為[-2,2]，在此區間的整數，依序表示操作(-2)、放出(-1)、強化(0)、變化(1)、具現化(2)的屬性。
 * 變異數表示分布的範圍。這決定了氣使用5系能力的效率。變異數越小，讓氣發揮在其鋒值的能力會比讓它發揮非鋒值能力強的多；變異數越大，則發揮非鋒值的能力也有一定的威力，但相較於變異數小的氣，發揮屬於其鋒值能力的威力相對就比較小。
 * TODO:變異數預設為2.2，可能的範圍為預設值的50%~200%(即:[1.1~4.4])，可根據設定檔調整，但不得低於1/(2*pi)^0.5(取0.4為此標準)。
 * 根據能力覺醒的過程、或是後天的影響，變異數的具體數值可能變動。
 * 但要注意，一個氣的實例可能帶有2個或以上的AuraFeature(即特質系的氣)，
 * **/

public class AuraFeature {
	
	public static final float DEFAULT_VAR = 2.2f;
	public static final float DEFAULT_VAR_RANGEMULTIPLE = 2.0f;
	
	private static final String EXCEPTIONMSG_TEMPLATE = "NaN was passed to %s during constructing AuraFeature. This shouldn't happen.";
	
	private static final double const1 = Math.sqrt(2*Math.PI);
	
	private final float mainFeature;
	private final float var;
	
	public AuraFeature(float mainFeature, float var) {
		if(Float.isNaN(mainFeature)) throw new IllegalArgumentException(String.format(EXCEPTIONMSG_TEMPLATE, "mainFeature"));
		if(Float.isNaN(var)) throw new IllegalArgumentException(String.format(EXCEPTIONMSG_TEMPLATE, "var"));
		
		this.mainFeature = MathUtils.clamp(mainFeature, -2, 2);
		this.var = MathUtils.clamp(var, Math.max(DEFAULT_VAR/DEFAULT_VAR_RANGEMULTIPLE, 0.4f), DEFAULT_VAR*DEFAULT_VAR_RANGEMULTIPLE);
	}
	
	/**
	 * 取得該氣在指定氣的屬性x時，發揮該屬性的效率。例如，要取得該氣發揮強化系能力的效率，則指定x=0，變化系則是1，操作系是-2。
	 * **/
	public double getAuraEfficiencyAt(EnumMainFeature x) {
		return getAuraEfficiencyAt(x.getValue());
	}
	
	public double getAuraEfficiencyAt(double x) {
		double tmp = x-mainFeature;
		return (1/var/const1)*Math.exp(-tmp*tmp/var/var/2);
	}
	
	/**
	 * 取得該特性的鋒值最接近哪一種主要特性。假如與兩個特性一樣接近，則優先回傳value較大的特性。
	 * 例：mainFeature=1，回傳變化系；mainFeature=0.3；回傳強化系；mainFeature=-1.6，回傳操作系；mainFeature=-1.5，回傳放出系。
	 * **/
	public EnumMainFeature getMainFeature() {
		return EnumMainFeature.getCorrespondingFeature(Math.round(mainFeature));
	}
	
}
