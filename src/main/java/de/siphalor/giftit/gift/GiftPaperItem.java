package de.siphalor.giftit.gift;

import de.siphalor.giftit.GiftIt;
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
		super(new Settings().maxDamage(4));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext itemUsageContext) {
		World world = itemUsageContext.getWorld();
		BlockPos blockPos = itemUsageContext.getBlockPos();
		if(!world.isClient()) {
			if(tryWrapBlock(itemUsageContext.getStack(), world, blockPos) && itemUsageContext.getPlayer() != null)
				itemUsageContext.getStack().decrement(1);
		}

		return ActionResult.SUCCESS;
	}

	public boolean tryWrapBlock(ItemStack itemStack, World world, BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);
		if(GiftIt.NONWRAPPABLE_BLOCKS.contains(blockState.getBlock())) return false;
		if(blockState.getBlock() == GiftIt.GIFT_BLOCK) return false;
		CompoundTag data = null;
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if(blockEntity != null) {
			data = blockEntity.toTag(new CompoundTag());
		}
		Clearable.clear(blockEntity);
		world.setBlockState(blockPos, GiftIt.GIFT_BLOCK.getDefaultState());
		world.setBlockEntity(blockPos, new GiftBlockEntity(blockState, data, itemStack.getDamage(), getColor(itemStack), itemStack.hasCustomName() ? itemStack.getName() : null));

		world.playSound(null, blockPos, GiftIt.GIFT_WRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
		return true;
	}
}
