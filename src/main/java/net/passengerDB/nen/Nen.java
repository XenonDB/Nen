package net.passengerDB.nen;

import net.minecraftforge.fml.common.Mod;
import net.passengerDB.nen.entityparts.PartsHandler;
import net.passengerDB.nen.registry.Entities;
import net.passengerDB.nen.utils.NenLogger;


@Mod(value = Nen.MODID)
public class Nen
{
    public static final String MODID = "nen";
    public static final String NAME = "Nen";
    public static final String VERSION = "forge1.16.5";
    private static Nen instance = null;
    
    //@SidedProxy(clientSide = "net.passengerDB.nen.network.client.ClientProxy", serverSide = "net.passengerDB.nen.network.server.ServerProxy")
    //public static CommonProxy proxy;
    
    public static Nen getInstence() {
    	return instance;
    }
    
    
    //ClientCustomPacketEvent
    
    public Nen() {
    	NenLogger.info("mod instance is created.");
    	instance = this;
    }
    
}
