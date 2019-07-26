package de.siphalor.giftit.mixin;

import de.siphalor.giftit.util.IItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class MixinItem implements IItem {

	@Mutable
	@Shadow @Final private int maxDamage;

	@Mutable
	@Shadow @Final private int maxCount;

	@Override
	public void setMaxDamage(int maxDamage) {
		this.maxDamage = maxDamage;
	}

	@Override
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
}
