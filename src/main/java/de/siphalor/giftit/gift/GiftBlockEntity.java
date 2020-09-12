package de.siphalor.giftit.gift;

import de.siphalor.giftit.GiftIt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.world.World;

@SuppressWarnings("WeakerAccess")
public class GiftBlockEntity extends BlockEntity implements Nameable, BlockEntityClientSerializable {
	protected GiftWrappedType wrappedType;
	protected CompoundTag wrappedBlockState;
	protected CompoundTag wrappedData;
	protected int color;
	protected int paperDamage;

	protected Text customName;

	public GiftBlockEntity(BlockState blockState, CompoundTag blockData, int paperDamage, int color, Text customName) {
		this(paperDamage, color, customName);
        setWrappedBlockState(blockState, blockData);
	}

	public GiftBlockEntity(ItemStack itemStack, int paperDamage, int color, Text customName) {
		this(paperDamage, color, customName);
		setWrappedStack(itemStack);
	}

	public GiftBlockEntity(Entity entity, int paperDamage, int color, Text customName) {
		this(paperDamage, color, customName);
		setWrappedEntity(entity);
	}

	protected GiftBlockEntity(int paperDamage, int color, Text customName) {
		super(GiftIt.GIFT_BLOCK_ENTITY_TYPE);
		this.paperDamage = paperDamage;
		this.color = color;
		this.customName = customName;
	}

	public GiftBlockEntity() {
		this(Blocks.AIR.getDefaultState(), null, 0, -1, null);
	}

	public GiftWrappedType getWrappedType() {
		return wrappedType;
	}

	public void setWrappedBlockState(BlockState blockState, CompoundTag blockData) {
		wrappedType = GiftWrappedType.BLOCK;
		wrappedBlockState = NbtHelper.fromBlockState(blockState);
		wrappedData = blockData;
	}

	public BlockState getWrappedBlockState() {
		return NbtHelper.toBlockState(wrappedBlockState);
	}

	public void setWrappedStack(ItemStack stack) {
		wrappedType = GiftWrappedType.STACK;
		wrappedData = stack.toTag(new CompoundTag());
	}

	public ItemStack getWrappedStack() {
		return ItemStack.fromTag(wrappedData);
	}

	public void setWrappedEntity(Entity entity) {
		wrappedType = GiftWrappedType.ENTITY;
		wrappedData = new CompoundTag();
		entity.saveToTag(wrappedData);
	}

	public Entity getWrappedEntity(World world) {
		return EntityType.getEntityFromTag(wrappedData, world).orElse(null);
	}

	public CompoundTag getWrappedData() {
		return wrappedData;
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
	public void fromTag(BlockState blockState, CompoundTag compoundTag) {
	super.fromTag(blockState, compoundTag);
		if (compoundTag.contains("WrappedType", 3)) {
			int type = compoundTag.getInt("WrappedType");
			GiftWrappedType[] wrappedTypes = GiftWrappedType.values();
			if (type >= 0 && type < wrappedTypes.length) {
				wrappedType = wrappedTypes[type];
			} else {
				wrappedType = GiftWrappedType.BLOCK;
			}
		} else {
			wrappedType = GiftWrappedType.BLOCK;
		}

		switch (wrappedType) {
			default:
			case BLOCK:
				if (compoundTag.contains("WrappedState", 10)) {
					wrappedBlockState = compoundTag.getCompound("WrappedState");
					if (compoundTag.contains("WrappedData", 10)) {
						wrappedData = compoundTag.getCompound("WrappedData");
					}
				} else {
					setWrappedBlockState(Blocks.AIR.getDefaultState(), null);
				}
				break;
			case STACK:
			case ENTITY:
				if (compoundTag.contains("WrappedData", 10)) {
					wrappedData = compoundTag.getCompound("WrappedData");
				} else {
					setWrappedStack(ItemStack.EMPTY);
				}
				break;
		}

		color = compoundTag.getInt("color");
		if(!GiftIt.CONFIG.unbreakableGiftPaper) {
			if(compoundTag.contains("PaperDamage"))
				paperDamage = compoundTag.getInt("PaperDamage");
			else
				paperDamage = 0;
		}

		if(compoundTag.contains("CustomName"))
			customName = Text.Serializer.fromJson(compoundTag.getString("CustomName"));
	}

	@Override
	public CompoundTag toTag(CompoundTag compoundTag) {
		super.toTag(compoundTag);

		compoundTag.putInt("WrappedType", wrappedType.ordinal());
		switch (wrappedType) {
			case BLOCK:
				compoundTag.put("WrappedState", wrappedBlockState);
				if (wrappedData != null) {
					compoundTag.put("WrappedData", wrappedData);
				}
				break;
			case STACK:
			case ENTITY:
				compoundTag.put("WrappedData", wrappedData);
				break;
		}

		if(color >= 0) {
			compoundTag.putInt("color", color);
		}
		if(!GiftIt.CONFIG.unbreakableGiftPaper) {
			compoundTag.putInt("PaperDamage", paperDamage);
		}

		if(hasCustomName()) {
			compoundTag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		return compoundTag;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void fromClientTag(CompoundTag tag) {
		color = tag.getInt("color");
		if(world != null)
			MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, null, getCachedState(), 3);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		tag.putInt("color", color);
		return tag;
	}
}
