package de.siphalor.giftit;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.World;

import java.util.List;

public class GiftBlockItem extends BlockItem implements DyeableGift {
	public GiftBlockItem(Block block_1) {
		super(block_1, new Settings().stackSize(1));
	}
}
