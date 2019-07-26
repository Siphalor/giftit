package de.siphalor.giftit.gift;

import com.mojang.datafixers.Dynamic;
import de.siphalor.giftit.Config;
import de.siphalor.giftit.GiftIt;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;

@SuppressWarnings("WeakerAccess")
public class GiftBlockEntity extends BlockEntity implements Nameable, BlockEntityClientSerializable {
	protected Dynamic<Tag> wrappedBlockState;
	protected CompoundTag wrappedBlockData;
	protected int color;
	protected int paperDamage;

	protected Text customName;

	public GiftBlockEntity(BlockState blockState, CompoundTag blockData, int paperDamage, int color, Text customName) {
		super(GiftIt.GIFT_BLOCK_ENTITY_TYPE);
        setWrappedBlockState(blockState);
		wrappedBlockData = blockData;
		this.paperDamage = paperDamage;
		this.color = color;

		this.customName = customName;
	}

	public GiftBlockEntity() {
		this(Blocks.AIR.getDefaultState(), null, 0, -1, null);
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
	public Text getName() {
		return getCustomName();
	}

	public Text getCustomName() {
		return customName;
	}

	public void setCustomName(Text customName) {
		this.customName = customName;
	}

	@Override
	public void fromTag(CompoundTag compoundTag) {
		super.fromTag(compoundTag);
		wrappedBlockState = new Dynamic<>(NbtOps.INSTANCE, compoundTag.getTag("WrappedState"));

		if(compoundTag.containsKey("WrappedData")) {
			wrappedBlockData = (CompoundTag) compoundTag.getTag("WrappedData");
		}
		color = compoundTag.getInt("color");
		if(!Config.unbreakableGiftPaper) {
			if(compoundTag.containsKey("PaperDamage"))
				paperDamage = compoundTag.getInt("PaperDamage");
			paperDamage = 0;
		}

		if(compoundTag.containsKey("CustomName"))
			customName = Text.Serializer.fromJson(compoundTag.getString("CustomName"));
	}

	@Override
	public CompoundTag toTag(CompoundTag compoundTag) {
		super.toTag(compoundTag);

		compoundTag.put("WrappedState", wrappedBlockState.getValue());
		if(wrappedBlockData != null)
			compoundTag.put("WrappedData", wrappedBlockData);
		if(color >= 0)
			compoundTag.putInt("color", color);
		if(!Config.unbreakableGiftPaper)
			compoundTag.putInt("PaperDamage", paperDamage);

		if(hasCustomName())
			compoundTag.putString("CustomName", Text.Serializer.toJson(customName));
		return compoundTag;
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		color = tag.getInt("color");
		if(world != null)
			world.scheduleBlockRender(pos, null, getCachedState());
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		tag.putInt("color", color);
		return tag;
	}
}
