package de.siphalor.giftit.gift;

import de.siphalor.giftit.GiftIt;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class GiftPaperItem extends Item implements DyeableGift {
	public GiftPaperItem() {
		super(new Settings().maxDamage(4).group(ItemGroup.MISC));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext itemUsageContext) {
		World world = itemUsageContext.getWorld();
		if (!world.isClient()) {
			BlockPos blockPos = itemUsageContext.getBlockPos();
			if (tryWrapBlock(itemUsageContext.getStack(), world, blockPos) && itemUsageContext.getPlayer() != null) {
				itemUsageContext.getPlayer().incrementStat(Stats.USED.getOrCreateStat(this));
				itemUsageContext.getStack().decrement(1);
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		World world = entity.world;
		if (!world.isClient()) {
			if (tryWrapEntity(stack, world, entity)) {
				if (user != null) {
					user.incrementStat(Stats.USED.getOrCreateStat(this));
				}
				stack.decrement(1);
			}
		}
		return ActionResult.SUCCESS;
	}

	public boolean tryWrapBlock(ItemStack itemStack, World world, BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);
		if (GiftIt.NONWRAPPABLE_BLOCKS.contains(blockState.getBlock())) return false;
		if (blockState.getBlock() == GiftIt.GIFT_BLOCK) return false;

		CompoundTag data = null;
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (GiftIt.CONFIG.restrictToInventories) {
			if (!(blockEntity instanceof Inventory)) {
				return false;
			}
		}

		if (blockEntity != null) {
			data = blockEntity.toTag(new CompoundTag());
		}
		Clearable.clear(blockEntity);
		world.setBlockState(blockPos, GiftIt.GIFT_BLOCK.getDefaultState());
		world.setBlockEntity(blockPos, new GiftBlockEntity(blockState, data, itemStack.getDamage(), getColor(itemStack), itemStack.hasCustomName() ? itemStack.getName() : null));

		world.playSound(null, blockPos, GiftIt.GIFT_WRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
		return true;
	}

	public boolean tryWrapEntity(ItemStack itemStack, World world, LivingEntity entity) {
		if (GiftIt.NONWRAPPABLE_ENTITIES.contains(entity.getType())) return false;

		Box box = entity.getBoundingBox(entity.getPose());
		if (GiftIt.CONFIG.forbidLargeEntities) {
			if (box.getXLength() > 1.5 || box.getYLength() > 3.0 || box.getZLength() > 1.5) {
				return false;
			}
		}

		BlockPos blockPos = new BlockPos(entity.getX() + box.getXLength() / 2D, entity.getY(), entity.getZ() + box.getZLength() / 2D);
		if (world.getBlockState(blockPos).getMaterial().isReplaceable()) {
			entity.stopRiding();
			entity.removeAllPassengers();

			GiftBlockEntity blockEntity = new GiftBlockEntity(entity, itemStack.getDamage(), getColor(itemStack), itemStack.hasCustomName() ? itemStack.getName() : null);
			entity.removed = true;

			world.setBlockState(blockPos, GiftIt.GIFT_BLOCK.getDefaultState());
			world.setBlockEntity(blockPos, blockEntity);

			world.playSound(null, blockPos, GiftIt.GIFT_WRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return true;
		}
		return false;
	}
}
