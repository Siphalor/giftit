package de.siphalor.giftit.gift;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public interface DyeableGift extends DyeableItem {
	@Override
	default boolean hasColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
		return compoundTag != null && compoundTag.containsKey("color", 99);
	}

	@Override
	default int getColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
		return compoundTag != null && compoundTag.containsKey("color", 99) ? compoundTag.getInt("color") : getDefaultValue();
	}

	static int getDefaultValue() {
		return 0x00fff9d4;
	}

	@Override
	default void removeColor(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
		if(compoundTag != null && compoundTag.containsKey("color"))
			compoundTag.remove("color");
	}

	@Override
	default void setColor(ItemStack itemStack, int color) {
		itemStack.getOrCreateSubTag("BlockEntityTag").putInt("color", color);
	}
}
