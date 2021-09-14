package yeelp.scalingfeast.proxy;

public abstract class AbstractProxy {

	/**
	 * pre initialization
	 */
	public abstract void preInit();
	
	/**
	 * initialization
	 */
	public abstract void init();
	
	/**
	 * post initialization
	 */
	public abstract void postInit();
}
