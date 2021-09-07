package de.siphalor.giftit.gift;

import de.siphalor.giftit.Config;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("WeakerAccess")
public class GiftBlockEntity extends BlockEntity implements Nameable, BlockEntityClientSerializable {
	protected GiftWrappedType wrappedType;
	protected NbtCompound wrappedBlockState;
	protected NbtCompound wrappedData;
	protected int color;
	protected int paperDamage;

	protected Text customName;

	public GiftBlockEntity(BlockPos pos, BlockState state, BlockState wrappedState, NbtCompound blockData, int paperDamage, int color, Text customName) {
		this(pos, state, paperDamage, color, customName);
        setWrappedBlockState(wrappedState, blockData);
	}

	public GiftBlockEntity(BlockPos pos, BlockState state, ItemStack itemStack, int paperDamage, int color, Text customName) {
		this(pos, state, paperDamage, color, customName);
		setWrappedStack(itemStack);
	}

	public GiftBlockEntity(BlockPos pos, BlockState state, Entity entity, int paperDamage, int color, Text customName) {
		this(pos, state, paperDamage, color, customName);
		setWrappedEntity(entity);
	}

	protected GiftBlockEntity(BlockPos pos, BlockState state, int paperDamage, int color, Text customName) {
		super(GiftIt.GIFT_BLOCK_ENTITY_TYPE, pos, state);
		this.paperDamage = paperDamage;
		this.color = color;
		this.customName = customName;
	}

	public GiftBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, Blocks.AIR.getDefaultState(), null, 0, -1, null);
	}

	public GiftWrappedType getWrappedType() {
		return wrappedType;
	}

	public void setWrappedBlockState(BlockState blockState, NbtCompound blockData) {
		wrappedType = GiftWrappedType.BLOCK;
		wrappedBlockState = NbtHelper.fromBlockState(blockState);
		wrappedData = blockData;
	}

	public BlockState getWrappedBlockState() {
		return NbtHelper.toBlockState(wrappedBlockState);
	}

	public void setWrappedStack(ItemStack stack) {
		wrappedType = GiftWrappedType.STACK;
		wrappedData = stack.writeNbt(new NbtCompound());
	}

	public ItemStack getWrappedStack() {
		return ItemStack.fromNbt(wrappedData);
	}

	public void setWrappedEntity(Entity entity) {
		wrappedType = GiftWrappedType.ENTITY;
		wrappedData = new NbtCompound();
		entity.saveSelfNbt(wrappedData);
	}

	public Entity getWrappedEntity(World world) {
		return EntityType.getEntityFromNbt(wrappedData, world).orElse(null);
	}

	public NbtCompound getWrappedData() {
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
	public void readNbt(NbtCompound compoundTag) {
	super.readNbt(compoundTag);
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
		if(!Config.unbreakableGiftPaper) {
			if(compoundTag.contains("PaperDamage"))
				paperDamage = compoundTag.getInt("PaperDamage");
			else
				paperDamage = 0;
		}

		if(compoundTag.contains("CustomName"))
			customName = Text.Serializer.fromJson(compoundTag.getString("CustomName"));
	}

	@Override
	public NbtCompound writeNbt(NbtCompound compoundTag) {
		super.writeNbt(compoundTag);

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
		if(!Config.unbreakableGiftPaper) {
			compoundTag.putInt("PaperDamage", paperDamage);
		}

		if(hasCustomName()) {
			compoundTag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		return compoundTag;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void fromClientTag(NbtCompound tag) {
		color = tag.getInt("color");
		if(world != null)
			MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, null, getCachedState(), 3);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound tag) {
		tag.putInt("color", color);
		return tag;
	}
}
