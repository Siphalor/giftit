package de.siphalor.giftit.gift;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface DyeableGift extends DyeableItem {
	@Override
	default boolean hasColor(ItemStack itemStack) {
		NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
		return compoundTag != null && compoundTag.contains("color", 99);
	}

	@Override
	default int getColor(ItemStack itemStack) {
		NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
		return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : getDefaultValue();
	}

	static int getDefaultValue() {
		return 0xfff9d4;
	}

	@Override
	default void removeColor(ItemStack itemStack) {
		NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
		if(compoundTag != null && compoundTag.contains("color"))
			compoundTag.remove("color");
	}

	@Override
	default void setColor(ItemStack itemStack, int color) {
		itemStack.getOrCreateSubNbt("BlockEntityTag").putInt("color", color);
	}
}
