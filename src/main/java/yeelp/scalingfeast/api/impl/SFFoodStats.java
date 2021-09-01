package yeelp.scalingfeast.api.impl;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import squeek.applecore.api.AppleCoreAPI;
import yeelp.scalingfeast.api.IBloatedHungerOperations;
import yeelp.scalingfeast.api.IFoodEfficiencyChanger;
import yeelp.scalingfeast.api.IMaxHungerChanger;
import yeelp.scalingfeast.api.IStarvationOperations;
import yeelp.scalingfeast.api.IStarveExhaustTrackerOperations;
import yeelp.scalingfeast.api.ScalingFeastAPI;
import yeelp.scalingfeast.capability.IBloatedHunger;
import yeelp.scalingfeast.capability.IStarvationStats;
import yeelp.scalingfeast.capability.IStarvationStats.ICountable;
import yeelp.scalingfeast.capability.IStarveExhaustionTracker;
import yeelp.scalingfeast.capability.impl.BloatedHunger;
import yeelp.scalingfeast.capability.impl.StarvationStats;
import yeelp.scalingfeast.capability.impl.StarveExhaustionTracker;
import yeelp.scalingfeast.config.ModConfig;
import yeelp.scalingfeast.init.SFAttributes;
import yeelp.scalingfeast.lib.SFBuiltInModifiers;

/**
 * A wrapper for Scaling Feast stats with convenience methods that perform operations and sync with the server.
 * @author Yeelp
 *
 */
public class SFFoodStats implements IMaxHungerChanger, IFoodEfficiencyChanger, IBloatedHungerOperations, IStarvationOperations, IStarveExhaustTrackerOperations {
	
	private static final class Caps {
		private IBloatedHunger bloat;
		private IStarveExhaustionTracker starveExhaustTracker;
		private IStarvationStats starveStats;
		private final EntityPlayer player;
		
		Caps(EntityPlayer player) {
			this.player = player;
		}
		
		IBloatedHunger getBloatedHungerCapability() {
			return this.bloat == null ? this.bloat = this.player.getCapability(BloatedHunger.cap, null) : this.bloat;
		}
		
		IStarveExhaustionTracker getStarvationExhaustionTrackerCapability() {
			return this.starveExhaustTracker == null ? this.starveExhaustTracker = this.player.getCapability(StarveExhaustionTracker.cap, null) : this.starveExhaustTracker;
		}
		
		IStarvationStats getStarvationStatsCapability() {
			return this.starveStats == null ? this.starveStats = this.player.getCapability(StarvationStats.cap, null) : this.starveStats;
		}
	}
	private IAttributeInstance maxHunger, foodEfficiency;
	private final EntityPlayer player;
	private final Caps caps;
	
	public SFFoodStats(EntityPlayer player) {
		this.player = player;
		this.caps = new Caps(this.player);
	}
	
	@Override
	public IAttributeInstance getMaxHungerAttribute() {
		return this.maxHunger == null ? this.maxHunger = this.player.getEntityAttribute(SFAttributes.MAX_HUNGER_MOD) : this.maxHunger;
	}
	
	@Override
	public IAttributeInstance getFoodEfficiencyModifierAttribute() {
		return this.foodEfficiency == null ? this.foodEfficiency = this.player.getEntityAttribute(SFAttributes.FOOD_EFFICIENCY) : this.foodEfficiency;
	}
	
	@Override
	public void applyMaxHungerModifier(AttributeModifier modifier) {
		IMaxHungerChanger.super.applyMaxHungerModifier(modifier);
		this.capStats();
	}

	private void capStats() {
		ScalingFeastAPI.mutator.capPlayerHunger(this.player);
		ScalingFeastAPI.mutator.capPlayerSaturation(this.player);
	}

	@Override
	public void setBloatedHungerAmount(short amount) {
		IBloatedHunger bloat = this.caps.getBloatedHungerCapability();
		bloat.setVal(amount);
		bloat.sync(this.player);
	}

	@Override
	public short deductBloatedAmount(short amount) {
		IBloatedHunger bloat = this.caps.getBloatedHungerCapability();
		short leftover = bloat.deductBloatedAmount(amount);
		bloat.sync(this.player);
		return leftover;
	}

	@Override
	public void addBloatedAmount(short amount) {
		IBloatedHunger bloat = this.caps.getBloatedHungerCapability();
		bloat.inc(amount);
		bloat.sync(this.player);
	}

	@Override
	public short getBloatedHungerAmount() {
		return this.caps.getBloatedHungerCapability().getVal();
	}

	@Override
	public void revokeAllBloatedHunger() {
		IBloatedHunger bloat = this.caps.getBloatedHungerCapability();
		bloat.reset();
		bloat.sync(this.player);
	}
	
	@Override
	public void tickStarvation(int amount) {
		int hunger = this.player.getFoodStats().getFoodLevel();
		int threshold = ModConfig.features.starve.tracker.lossFreq;
		int lowerBound = ModConfig.features.starve.tracker.starveLowerCap;
		int lossAmount = ModConfig.features.starve.tracker.starveLoss;
		if(hunger == 0 || threshold == 0) {
			return;
		}
		ICountable tracker = this.caps.getStarvationStatsCapability().getTracker();
		ICountable counter = this.caps.getStarvationStatsCapability().getCounter();
		counter.inc((short) 1);
		for(int i = 0; i < amount; i++) {
			tracker.inc((short) 1);
			int maxHunger = AppleCoreAPI.accessor.getMaxHunger(this.player);
			if(tracker.get() >= threshold) { 
				if(maxHunger > lowerBound) {
					double base = SFBuiltInModifiers.MaxHungerModifiers.PENALTY.getModifierValueForPlayer(this.player);
					double penalty = MathHelper.clamp(base - lossAmount, maxHunger - base - lowerBound, 0.0);
					this.applyMaxHungerModifier(SFBuiltInModifiers.MaxHungerModifiers.PENALTY.createModifier(penalty));
				}
				if(ModConfig.features.starve.tracker.doesFreqResetOnStarve) {
					tracker.reset();
				}
				else {
					tracker.set((short) (threshold - 1));
				}
			}
		}
		this.caps.getStarvationStatsCapability().sync(this.player);
	}

	@Override
	public void resetStarvationTracker() {
		IStarvationStats stats = this.caps.getStarvationStatsCapability();
		stats.getTracker().reset();
		stats.sync(this.player);
	}

	@Override
	public short getStarvationTrackerCount() {
		return this.caps.getStarvationStatsCapability().getTracker().get();
	}
	
	@Override
	public short getStarvationCountAllTime() {
		return this.caps.getStarvationStatsCapability().getCounter().get();
	}
	
	@Override
	public void resetStarvationCountAllTime() {
		IStarvationStats stats = this.caps.getStarvationStatsCapability();
		stats.getCounter().reset();
		stats.sync(this.player);
	}

	@Override
	public void resetStarvationStats() {
		IStarvationStats stats = this.caps.getStarvationStatsCapability();
		stats.getCounter().reset();
		stats.getTracker().reset();
		stats.sync(this.player);
	}

	@Override
	public void addExhaustionIfAtZeroHunger(float amount) {
		if(this.player.getFoodStats().getFoodLevel() == 0) {
			IStarveExhaustionTracker exhaustTracker = this.caps.getStarvationExhaustionTrackerCapability();
			exhaustTracker.inc(amount);
			exhaustTracker.sync(this.player);
		}
	}

	@Override
	public float getTotalExhaustionAtZeroHunger() {
		return this.caps.getStarvationExhaustionTrackerCapability().getVal();
	}

	@Override
	public int getTotalBonusDynamicStarvationDamage() {
		return (int) Math.floor(this.getTotalExhaustionAtZeroHunger() / AppleCoreAPI.accessor.getMaxExhaustion(this.player));
	}

	@Override
	public void resetStarvationExhaustionTracker() {
		IStarveExhaustionTracker exhaustTracker = this.caps.getStarvationExhaustionTrackerCapability();
		exhaustTracker.reset();
		exhaustTracker.sync(this.player);
	}
}
