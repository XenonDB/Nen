package net.passengerDB.nen;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.passengerDB.nen.entityparts.PartsHandler;
import net.passengerDB.nen.network.CommonProxy;
import net.passengerDB.nen.registry.Entities;
import net.passengerDB.nen.utils.NenLogger;


@Mod(modid = Nen.MODID, name = Nen.NAME, version = Nen.VERSION, dependencies = "required:forge@[14.23.5.2768,)")
public class Nen
{
    public static final String MODID = "nen";
    public static final String NAME = "Nen";
    public static final String VERSION = "forge1.12.2-1.0";
    public static Nen instance = null;
    
    @SidedProxy(clientSide = "net.passengerDB.nen.network.client.ClientProxy", serverSide = "net.passengerDB.nen.network.server.ServerProxy")
    public static CommonProxy proxy;
    
    //ClientCustomPacketEvent
    
    public Nen() {
    	NenLogger.info("mod instance is created.");
    	instance = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	//讓列舉類別載入
    	Entities.init();
    	PartsHandler.init(event.getSide());
    	
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        
    }
}
