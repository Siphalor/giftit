package de.siphalor.giftit.client;

import de.siphalor.giftit.gift.GiftBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;

import static de.siphalor.giftit.GiftIt.*;

@Environment(EnvType.CLIENT)
public class ClientCore implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		GIFT_BLOCK_ITEM.appendBlocks(Item.BLOCK_ITEMS, GIFT_BLOCK_ITEM);

		ColorProviderRegistry.ITEM.register((itemStack, layer) -> {
			int baseColor = ((DyeableItem) itemStack.getItem()).getColor(itemStack);
			if(layer == 1) {
				return baseColor ^ 0xffffff;
			} else {
				return baseColor;
			}
		}, GIFT_BLOCK_ITEM, GIFT_PAPER);
		ColorProviderRegistry.BLOCK.register((blockState, blockView, blockPos, tintIndex) -> {
			if(blockView != null) {
				BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
				if (blockEntity instanceof GiftBlockEntity) {
					int baseColor = ((GiftBlockEntity) blockEntity).getColor();
					if (tintIndex == 1) {
						return baseColor ^ 0xffffff;
					} else {
						return baseColor;
					}
				}
			}
			return -1;
		}, GIFT_BLOCK);

		BlockRenderLayerMap.INSTANCE.putBlock(GIFT_BLOCK, RenderLayer.getCutoutMipped());
	}
}
