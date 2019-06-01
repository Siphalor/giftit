package de.siphalor.giftit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Core implements ModInitializer {
	public static final String MOD_ID = "giftit";

	public static final Block GIFT_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gift"), new GiftBlock());
	public static final GiftBlockItem GIFT_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift"), new GiftBlockItem(GIFT_BLOCK));
    public static final BlockEntityType<GiftBlockEntity> GIFT_BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY, new Identifier(MOD_ID, "gift"), BlockEntityType.Builder.create(GiftBlockEntity::new, GIFT_BLOCK).build(null));

    public static final GiftPaperItem GIFT_PAPER = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift_paper"), new GiftPaperItem());

    public static final SoundEvent GIFT_WRAP_SOUND = registerSound("gift.wrap");
    public static final SoundEvent GIFT_UNWRAP_SOUND = registerSound("gift.unwrap");

	@Override
	public void onInitialize() {
		GIFT_BLOCK_ITEM.registerBlockItemMap(Item.BLOCK_ITEM_MAP, GIFT_BLOCK_ITEM);

		ColorProviderRegistry.ITEM.register((itemStack, layer) -> {
			int baseColor = ((DyeableItem) itemStack.getItem()).getColor(itemStack);
			if(layer == 1) {
				return baseColor ^ 0x00ffffff;
			} else {
				return baseColor;
			}
		}, GIFT_BLOCK_ITEM, GIFT_PAPER);
		ColorProviderRegistry.BLOCK.register((blockState, blockView, blockPos, tintIndex) -> {
			BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
			if(blockEntity instanceof GiftBlockEntity) {
				int baseColor = ((GiftBlockEntity) blockEntity).getColor();
				if(tintIndex == 1) {
					return baseColor ^ 0x00ffffff;
				} else {
					return baseColor;
				}
			}
			return -1;
		}, GIFT_BLOCK);
	}

	public static SoundEvent registerSound(String name) {
    	Identifier identifier = new Identifier(MOD_ID, name);
    	return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
	}
}