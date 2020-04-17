package net.passengerDB.nen.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*;
import java.util.Map;


@TransformerExclusions("net.passengerDB.nen.asm")
@MCVersion("1.12.2")
@SortingIndex(1100)
public class NenASM implements IFMLLoadingPlugin {

	public String[] getASMTransformerClass() {
		return new String[] {TransformerAssignment.class.getName()};
	}
	
	public String getModContainerClass() {
		return null;
	}
	
	public String getSetupClass() {
		return null;
	}
	
	public void injectData(Map<String, Object> paramMap) {
		
	}
	
	public String getAccessTransformerClass() {
		return null;
	}
	
}
