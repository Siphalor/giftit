package de.siphalor.giftit;

import com.mojang.datafixers.Dynamic;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class GiftBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
	protected Dynamic<Tag> wrappedBlockState;
	protected CompoundTag wrappedBlockData;
	protected int color;
	protected int paperDamage;

	public GiftBlockEntity(BlockState blockState, CompoundTag blockData, int paperDamage, int color) {
		super(Core.GIFT_BLOCK_ENTITY_TYPE);
        setWrappedBlockState(blockState);
		wrappedBlockData = blockData;
		this.paperDamage = paperDamage;
		this.color = color;
	}

	public GiftBlockEntity() {
		this(Blocks.AIR.getDefaultState(), null, 0, -1);
	}

	public void setWrappedBlockState(BlockState blockState) {
		this.wrappedBlockState = BlockState.serialize(NbtOps.INSTANCE, blockState);
	}

	public BlockState getWrappedBlockState() {
		return BlockState.deserialize(wrappedBlockState);
	}

	public void setWrappedBlockData(CompoundTag wrappedBlockData) {
		this.wrappedBlockData = wrappedBlockData;
	}

	public int getColor() {
		return color < 0 ? DyeableGift.getDefaultValue() : color;
	}

	public int getPaperDamage() {
		return paperDamage;
	}

	@Override
	public boolean onBlockAction(int type, int data) {
		if(type == 0) {
			this.color = data;
			return true;
		}
		return super.onBlockAction(type, data);
	}

	public CompoundTag toItemTag(CompoundTag compoundTag) {
        compoundTag.put("WrappedState", wrappedBlockState.getValue());
		if(wrappedBlockData != null)
			compoundTag.put("WrappedData", wrappedBlockData);
		if(color >= 0)
			compoundTag.putInt("color", color);
		compoundTag.putInt("PaperDamage", paperDamage);
		return compoundTag;
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		super.fromTag(compoundTag);
		wrappedBlockState = new Dynamic<>(NbtOps.INSTANCE, compoundTag.getTag("WrappedState"));

		if(compoundTag.containsKey("WrappedData")) {
			wrappedBlockData = (CompoundTag) compoundTag.getTag("WrappedData");
		}
		color = compoundTag.getInt("color");
		paperDamage = compoundTag.getInt("PaperDamage");
	}

	@Override
	public CompoundTag toTag(CompoundTag compoundTag) {
		super.toTag(compoundTag);
		return toItemTag(compoundTag);
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		color = tag.getInt("color");
		world.scheduleBlockRender(pos);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		tag.putInt("color", color);
		return tag;
	}
}
