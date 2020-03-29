package yeelp.scalingfeast.util;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * A FoodStat map for storing the individual ExtendedFoodStats for different players.
 * All modifications to this map respect the caps specified by each individual player's ExtendedFoodStats and Minecraft's basic hunger mechanics
 * @author Yeelp
 * @see FoodCap
 * @see <a href="https://minecraft.gamepedia.com/Hunger#Mechanics">Minecraft's basic hunger mechanics</a>
 * @deprecated No longer needed
 */
public class FoodStatsMap 
{
	private static final Map<UUID, FoodCap> map = Collections.synchronizedMap(new HashMap<UUID, FoodCap>());
	private static short cap;
	private static boolean noCap = false;
	private static short incInterval;
	
	/**
	 * Initialize the FoodStatsMap
	 * @param cap The hard upper limit to the maximum food a player can ever have. -1 if there shouldn't be a cap
	 * @param increaseAmount The amount a player's max food level increased when they eat a Hearty Shank.
	 */
	public static void init(short setCap, short increaseAmount)
	{
		if(setCap == -1)
		{
			noCap = true;
		}
		else
		{
			cap = setCap;
		}
		incInterval = increaseAmount;
	}
	/**
	 * Add a player to the map, with the default starting values
	 * @param player the UUID of the player to add
	 */
	public static void addPlayer(UUID player)
	{
		FoodCap fs = new FoodCap();
		map.put(player, fs);
	}
	/**
	 * Add a player to the map with specified values
	 * @param player The UUID of the player to add
	 * @param foodLevel the food level this player starts with
	 * @param satLevel the saturation level this player starts with
	 * @param max the maximum food level this player can have.
	 */
	public static void addPlayer(UUID player, short max)
	{
		FoodCap fs = new FoodCap(max);
		map.put(player, fs);
	}
	/**
	 * Remove a player from the map
	 * @param player The UUID of the player to remove
	 */
	public static void removePlayer(UUID player)
	{
		map.remove(player);
	}
	/**
	 * Does the map contain a specified player?
	 * @param player The UUID of the player to check for
	 * @return true if the player is tracked in the map, false otherwise.
	 */
	public static boolean hasPlayer(UUID player)
	{
		return map.containsKey(player);
	}

	/**
	 * Gets the maximum food level this player has
	 * @param player The UUID of the player
	 * @return the maximum food level this player can have
	 */
	public static short getMaxFoodLevel(UUID player)
	{
		return map.get(player).getMaxFoodLevel();
	}
	/**
	 * Get the ExtendedFoodStats container for this player
	 * @param player the UUID of the player
	 * @return An ExtendedFoodStats container for this player
	 */
	public static FoodCap getExtendedFoodStats(UUID player)
	{
		return map.get(player);
	}
	/**
	 * Get the maximum extra food level any player can ever have. This method returns -1 if there is no such cap.
	 * Thus, checking {@code FoodStatsMap.getCap() == -1} is equivalent to checking {@code FoodStatsMap.noCap() == true}. 
	 * @return The maximum food level a player can ever have, or -1 if no such cap is in place
	 * @see FoodStatsMap#noCap()
	 */
	public static short getCap()
	{
		return cap;
	}
	/**
	 * Is there no maximum cap for the food map? This works the same as checking {@code FoodStatsMap.getCap() == -1}
	 * @return true if there is no cap for the food level a player can have, false otherwise.
	 * @see FoodStatsMap#getCap()
	 */
	public static boolean noCap()
	{
		return noCap;
	}
	/**
	 * Get the amount that a player's max food level will increase by when they eat a Hearty Shank
	 * @return The increase amount
	 */
	public static short getIncreaseInterval()
	{
		return incInterval;
	}
	/**
	 * Set the maximum extra food level a player can ever have. When this is called, all players with more hunger than this cap are set to the new cap
	 * If set to -1, no cap will be enforced
	 * @param newCap The new cap
	 */
	public static void setCap(short newCap)
	{
		cap = newCap;
		if(cap != -1)
		{
			for(UUID player : map.keySet())
			{
				//If a player's food cap is greater than their food cap, 
				if(map.get(player).getMaxFoodLevel() > cap)
				{
					map.get(player).setMax(cap);
				}
			}
		}
	}
	/**
	 * Set the amount that a player's max food level will increase by when they eat a Hearty Shank.
	 * @param increaseAmount the increase interval.
	 */
	public static void setIncreaseAmount(short increaseAmount)
	{
		incInterval = increaseAmount;
	}

	/**
	 * Set the maximum food level a specific player can have
	 * @param player the player to target
	 * @param max the max level to set
	 */
	public static void setMaxFoodLevel(UUID player, short max)
	{
		map.get(player).setMax(max);
	}
	/**
	 * Increase a player's max food level by the amount specified by {@code FoodStatsMap.getIncreaseInterval()}
	 * Will not exceed the amount specified by {@code FoodStatsMap.getCap()}
	 * @param player The player to target
	 */
	public static void increaseMax(UUID player)
	{
		if(map.get(player).getMaxFoodLevel() == cap)
		{
			return;
		}
		else if(map.get(player).getMaxFoodLevel() + incInterval > cap)
		{
			map.get(player).setMax(cap);
		}
		else
		{
			map.get(player).setMax((short) (map.get(player).getMaxFoodLevel() + incInterval));
		}
	}
}
