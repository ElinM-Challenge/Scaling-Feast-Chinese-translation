package yeelp.scalingfeast.handlers;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.hunger.ExhaustionEvent;
import yeelp.scalingfeast.init.SFEnchantments;

public class EnchantmentHandler 
{
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onExhaustionAddition(ExhaustionEvent.ExhaustionAddition evt)
	{
		int level = EnchantmentHelper.getMaxEnchantmentLevel(SFEnchantments.fasting, evt.player);
		if(level != 0)
		{
			float mod = (1-0.1f*level);
			evt.deltaExhaustion*=mod;
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOW, receiveCanceled=true)
	public void getPlayerSpecificFoodValues(FoodEvent.GetPlayerFoodValues evt)
	{
		int level = EnchantmentHelper.getMaxEnchantmentLevel(SFEnchantments.gluttony, evt.player);
		if(level != 0 && evt.player != null) //Player can apparently be null, according to AppleCore.
		{
			float mod = (1 + 0.5f*level);
			FoodValues foodvals = evt.foodValues;
			int newHunger = (int)(foodvals.hunger*mod);
			float newSat = foodvals.saturationModifier*mod;
			evt.foodValues = new FoodValues(newHunger, newSat);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(LivingAttackEvent evt)
	{
		EntityLivingBase entity = evt.getEntityLiving();
		if(evt.getSource().getTrueSource() instanceof EntityLivingBase)
		{
			int level = EnchantmentHelper.getMaxEnchantmentLevel(SFEnchantments.famine, (EntityLivingBase) evt.getSource().getTrueSource());
			if(level != 0)
			{
				//20 ticks a second, so duration should be multiplied by 20
				int hduration = level*20;
				int wlevel = (level < 3 ? 0 : 1);
				//level is also zero indexed.
				if(entity instanceof EntityPlayer)
				{
					entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, hduration, 50));
				}
				entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 15*20,  wlevel));
			}
		}
	}
}
