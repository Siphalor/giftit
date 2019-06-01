package de.siphalor.giftit;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class GiftBlock extends Block implements BlockEntityProvider {
	protected GiftBlock() {
		super(FabricBlockSettings.of(Material.WOOL).breakByHand(true).build());
	}

	@Override
	public void onBreak(World world, BlockPos blockPos, BlockState blockState, PlayerEntity playerEntity) {
		if(!world.isClient()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if(blockEntity instanceof GiftBlockEntity) {
				ItemStack itemStack = new ItemStack(this);
				itemStack.setChildTag("BlockEntityTag", ((GiftBlockEntity) blockEntity).toItemTag(new CompoundTag()));
				ItemEntity itemEntity = new ItemEntity(world.getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
				itemEntity.setToDefaultPickupDelay();
				world.spawnEntity(itemEntity);
			}
		}

		super.onBreak(world, blockPos, blockState, playerEntity);
	}

	@Override
	public boolean activate(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
		if(!world.isClient()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if(blockEntity instanceof GiftBlockEntity) {
				if(((GiftBlockEntity) blockEntity).getPaperDamage() < Core.GIFT_PAPER.getDurability() - 1) {
                    ItemStack itemStack = new ItemStack(Core.GIFT_PAPER);
                    itemStack.setDamage(((GiftBlockEntity) blockEntity).getPaperDamage() + 1);
                    Core.GIFT_PAPER.setColor(itemStack, ((GiftBlockEntity) blockEntity).getColor());
                    ItemEntity itemEntity = new ItemEntity(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                    world.spawnEntity(itemEntity);
				}

				CompoundTag blockData = ((GiftBlockEntity) blockEntity).wrappedBlockData;
				BlockState newBlockState = ((GiftBlockEntity) blockEntity).getWrappedBlockState();
				Clearable.clear(blockEntity);

				world.setBlockState(blockPos, newBlockState, 2);
				if(blockData != null) {
					BlockEntity newBlockEntity = world.getBlockEntity(blockPos);
					if(newBlockEntity != null) {
						blockData.putInt("x", blockPos.getX());
						blockData.putInt("y", blockPos.getY());
						blockData.putInt("z", blockPos.getZ());
						newBlockEntity.fromTag(blockData);
						newBlockEntity.markDirty();
					}
				}
				for(Direction direction : Direction.values()) {
					BlockPos pos = blockPos.offset(direction);
                    newBlockState = world.getBlockState(blockPos);
                    world.setBlockState(blockPos, newBlockState.getBlock().getStateForNeighborUpdate(newBlockState, direction, world.getBlockState(pos), world, blockPos, pos));
				}

				world.playSound(null, blockPos, Core.GIFT_UNWRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}

		return true;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView blockView) {
		return new GiftBlockEntity();
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isOpaque(BlockState blockState_1) {
		return true;
	}
}
