package de.siphalor.giftit;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GiftPaperItem extends Item implements DyeableGift {
	public GiftPaperItem() {
		super(new Settings().stackSize(1).durability(4));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext itemUsageContext) {
		World world = itemUsageContext.getWorld();
		BlockPos blockPos = itemUsageContext.getBlockPos();
		if(!world.isClient()) {
			BlockState blockState = world.getBlockState(blockPos);
			if(blockState.getBlock() == Core.GIFT_BLOCK) return ActionResult.PASS;
			CompoundTag data = null;
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if(blockEntity != null) {
                data = blockEntity.toTag(new CompoundTag());
			}
			Clearable.clear(blockEntity);
			world.setBlockState(blockPos, Core.GIFT_BLOCK.getDefaultState());
			world.setBlockEntity(blockPos, new GiftBlockEntity(blockState, data, itemUsageContext.getItemStack().getDamage(), getColor(itemUsageContext.getItemStack())));

			itemUsageContext.getPlayer().setStackInHand(itemUsageContext.getHand(), ItemStack.EMPTY);

			world.playSound(null, blockPos, Core.GIFT_WRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}

		return ActionResult.SUCCESS;
	}
}
