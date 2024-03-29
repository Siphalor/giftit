package de.siphalor.giftit.mixin;

import de.siphalor.giftit.gift.GiftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"WeakerAccess"})
@Mixin(ShearsDispenserBehavior.class)
public abstract class MixinShearsDispenserBehavior extends FallibleItemDispenserBehavior {
	@Inject(method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
	public void onDispenseSilently(BlockPointer blockPointer, ItemStack itemStack, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		World world = blockPointer.getWorld();
		if(!world.isClient()) {
			Direction dispenserDirection = blockPointer.getBlockState().get(DispenserBlock.FACING);
			BlockPos frontPos = blockPointer.getPos().offset(dispenserDirection);
			Block block = world.getBlockState(frontPos).getBlock();
			if (block instanceof GiftBlock) {
				((GiftBlock) block).unwrap(world, frontPos, dispenserDirection.getOpposite(), null);
				setSuccess(true);
				if(itemStack.damage(1, world.random, null))
					itemStack.setCount(0);
				callbackInfoReturnable.setReturnValue(itemStack);
			}
		}
	}
}
