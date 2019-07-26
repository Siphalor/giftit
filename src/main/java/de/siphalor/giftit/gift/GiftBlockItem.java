package de.siphalor.giftit.gift;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class GiftBlockItem extends BlockItem implements DyeableGift {
	public GiftBlockItem(Block block_1) {
		super(block_1, new Settings().maxCount(1));
	}
}
