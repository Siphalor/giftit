package de.siphalor.giftit.client;

import de.siphalor.giftit.GiftBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;

import static de.siphalor.giftit.Core.*;

public class ClientCore implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
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
}
