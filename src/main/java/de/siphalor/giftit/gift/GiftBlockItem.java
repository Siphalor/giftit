package de.siphalor.giftit.gift;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;

public class GiftBlockItem extends BlockItem implements DyeableGift {
	public GiftBlockItem(Block block) {
		super(block, new Settings().maxCount(1).group(ItemGroup.MISC));
	}
}
