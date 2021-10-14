package net.passengerDB.nen;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.passengerDB.nen.config.NenConfig;
import net.passengerDB.nen.utils.NenLogger;


@Mod(value = Nen.MODID)
public class Nen
{
    public static final String MODID = "nen";
    public static final String NAME = "Nen";
    public static final String VERSION = "forge1.16.5";
    private static Nen INSTANCE = null;
    
    //@SidedProxy(clientSide = "net.passengerDB.nen.network.client.ClientProxy", serverSide = "net.passengerDB.nen.network.server.ServerProxy")
    //public static CommonProxy proxy;
    
    public static Nen getInstence() {
    	return INSTANCE;
    }
    
    
    //ClientCustomPacketEvent
    
    public Nen() {
    	
    	synchronized(Nen.class) {
    		if(INSTANCE != null) throw new RuntimeException("Someone try to instance Nen mod twice or more!");
    		
    		ModLoadingContext.get().registerConfig(Type.COMMON, NenConfig.SPEC, NenConfig.CONFIG_NAME);
    		
    		NenLogger.info("mod instance is created.");
    		INSTANCE = this;
    	}
    	
    	
    }
    
}
