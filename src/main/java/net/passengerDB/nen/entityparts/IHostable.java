package net.passengerDB.nen.entityparts;

/**
 * Mixin用類別。此類別會透過Mixin合併給Entity使用。<br>
 * 然而因為編譯器不知道這件事，所以操作這些方法時，請自行cast成這個interface，或透過GeneralEntityPartsAPI來操作。<br>
 * 當然，如果你有自己的實體類別且自己實做了這個介面，也是可以直接透過這個介面來操作。
 * */
public interface IHostable {

	public EntityPartsManager getManager();
	
	public void setManager(EntityPartsManager manager);

}
