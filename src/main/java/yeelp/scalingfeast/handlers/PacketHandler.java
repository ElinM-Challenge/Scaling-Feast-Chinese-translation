package yeelp.scalingfeast.handlers;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import yeelp.scalingfeast.ModConsts;
import yeelp.scalingfeast.network.FoodCapMessage;
import yeelp.scalingfeast.network.FoodCapModifierMessage;
import yeelp.scalingfeast.network.StarvationTrackerMessage;

public class PacketHandler 
{
	public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(ModConsts.MOD_ID);
	public static  int id = 0;
	public static void init()
	{
		INSTANCE.registerMessage(FoodCapMessage.Handler.class, FoodCapMessage.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(StarvationTrackerMessage.Handler.class, StarvationTrackerMessage.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(FoodCapModifierMessage.Handler.class, FoodCapModifierMessage.class, id++, Side.CLIENT);
	}
}
