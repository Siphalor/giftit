package de.siphalor.giftit.gift;

import de.siphalor.giftit.Config;
import de.siphalor.giftit.GiftIt;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class GiftBlock extends Block implements BlockEntityProvider {
	public GiftBlock() {
		super(FabricBlockSettings.of(Material.WOOL).breakByHand(true));
	}

	@Override
	public void onPlaced(World world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomName()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity instanceof GiftBlockEntity) {
				((GiftBlockEntity) blockEntity).setCustomName(itemStack.getName());
			}
		}

		super.onPlaced(world, blockPos, blockState, livingEntity, itemStack);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient()) {
			unwrap(world, pos, hit.getSide(), player);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState blockState_1) {
		return PistonBehavior.DESTROY;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GiftBlockEntity(pos, state);
	}

	public void unwrap(World world, BlockPos blockPos, Direction activationDirection, PlayerEntity playerEntity) {
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (blockEntity instanceof GiftBlockEntity giftBlockEntity) {
			if (playerEntity instanceof ServerPlayerEntity && giftBlockEntity.hasCustomName()) {
				((ServerPlayerEntity) playerEntity).networkHandler.sendPacket(new TitleS2CPacket(giftBlockEntity.getCustomName()));
			}

			boolean dropAround = false;
			switch (giftBlockEntity.getWrappedType()) {
				case BLOCK -> {
					NbtCompound blockData = giftBlockEntity.getWrappedData();
					BlockState newBlockState = giftBlockEntity.getWrappedBlockState();
					Clearable.clear(blockEntity);

					world.removeBlock(blockPos, false);
					world.setBlockState(blockPos, newBlockState, 2);
					if (blockData != null) {
						BlockEntity newBlockEntity = world.getBlockEntity(blockPos);
						if (newBlockEntity != null) {
							blockData.putInt("x", blockPos.getX());
							blockData.putInt("y", blockPos.getY());
							blockData.putInt("z", blockPos.getZ());
							newBlockEntity.readNbt(blockData);
							newBlockEntity.markDirty();
						}
					}

					for (Direction direction : Direction.values()) {
						BlockPos pos = blockPos.offset(direction);
						newBlockState = world.getBlockState(blockPos);
						world.setBlockState(blockPos, newBlockState.getBlock().getStateForNeighborUpdate(newBlockState, direction, world.getBlockState(pos), world, blockPos, pos));
					}
					dropAround = newBlockState.isFullCube(world, blockPos);
				}
				case STACK -> {
					ItemStack stack = giftBlockEntity.getWrappedStack();
					Clearable.clear(blockEntity);

					world.removeBlock(blockPos, false);

					ItemEntity itemEntity = new ItemEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, stack);
					world.spawnEntity(itemEntity);
				}
				case ENTITY -> {
					Entity entity = giftBlockEntity.getWrappedEntity(world);
					Clearable.clear(blockEntity);

					world.removeBlock(blockPos, false);

					if (entity != null) {
						entity.setVelocity(Vec3d.ZERO);
						entity.setOnGround(false);
						entity.refreshPositionAndAngles(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, entity.getYaw(), entity.getPitch());
						world.spawnEntity(entity);
					}
				}
			}

			if (Config.unbreakableGiftPaper || giftBlockEntity.getPaperDamage() < GiftIt.GIFT_PAPER.getMaxDamage() - 1 && (playerEntity == null || !playerEntity.isCreative())) {
				ItemStack itemStack = new ItemStack(GiftIt.GIFT_PAPER);
				if (!Config.unbreakableGiftPaper)
					itemStack.setDamage(giftBlockEntity.getPaperDamage() + 1);
				GiftIt.GIFT_PAPER.setColor(itemStack, giftBlockEntity.color);
				BlockPos.Mutable itemPos = blockPos.mutableCopy();
				if (dropAround) {
					itemPos.move(0, 1, 0);
					if (isFullCube(world.getBlockState(itemPos), world, itemPos)) {
						itemPos.move(0, -2, 0);
						if (isFullCube(world.getBlockState(itemPos), world, itemPos)) {
							itemPos.move(0, 1, 0);
						}
					}
				}
				ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5, itemPos.getY(), itemPos.getZ() + 0.5, itemStack);
				world.spawnEntity(itemEntity);
			}

			world.playSound(null, blockPos, GiftIt.GIFT_UNWRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	private boolean isFullCube(BlockState blockState, World world, BlockPos blockPos) {
		return blockState.isFullCube(world, blockPos);
	}
}
