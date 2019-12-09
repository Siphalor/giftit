package de.siphalor.giftit.gift;

import de.siphalor.giftit.Config;
import de.siphalor.giftit.GiftIt;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.network.packet.TitleS2CPacket;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class GiftBlock extends Block implements BlockEntityProvider {
	public GiftBlock() {
		super(FabricBlockSettings.of(Material.WOOL).breakByHand(true).build());
	}

	@Override
	public void onPlaced(World world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if(itemStack.hasCustomName()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if(blockEntity instanceof GiftBlockEntity) {
				((GiftBlockEntity) blockEntity).setCustomName(itemStack.getName());
			}
		}

		super.onPlaced(world, blockPos, blockState, livingEntity, itemStack);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(!world.isClient()) {
			unwrap(world, pos, hit.getSide(), player);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState blockState_1) {
		return PistonBehavior.DESTROY;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView blockView) {
		return new GiftBlockEntity();
	}

	public void unwrap(World world, BlockPos blockPos, Direction activationDirection, PlayerEntity playerEntity) {
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if(blockEntity instanceof GiftBlockEntity) {
			if(Config.unbreakableGiftPaper || ((GiftBlockEntity) blockEntity).getPaperDamage() < GiftIt.GIFT_PAPER.getMaxDamage() - 1 && !playerEntity.isCreative()) {
				ItemStack itemStack = new ItemStack(GiftIt.GIFT_PAPER);
				if(!Config.unbreakableGiftPaper)
					itemStack.setDamage(((GiftBlockEntity) blockEntity).getPaperDamage() + 1);
				GiftIt.GIFT_PAPER.setColor(itemStack, ((GiftBlockEntity) blockEntity).color);
				BlockPos itemPos = blockPos.offset(activationDirection);
				ItemEntity itemEntity = new ItemEntity(world, itemPos.getX(), itemPos.getY(), itemPos.getZ(), itemStack);
				world.spawnEntity(itemEntity);
			}

			if(playerEntity instanceof ServerPlayerEntity && ((GiftBlockEntity) blockEntity).hasCustomName()) {
				((ServerPlayerEntity) playerEntity).networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, ((GiftBlockEntity) blockEntity).getCustomName()));
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

			world.playSound(null, blockPos, GiftIt.GIFT_UNWRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}
}
