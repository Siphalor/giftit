package de.siphalor.giftit;

import de.siphalor.giftit.gift.GiftBlock;
import de.siphalor.giftit.gift.GiftBlockEntity;
import de.siphalor.giftit.gift.GiftBlockItem;
import de.siphalor.giftit.gift.GiftPaperItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.registry.Registry;

public class GiftIt implements ModInitializer {
	public static final String MOD_ID = "giftit";

	public static final Block GIFT_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gift"), new GiftBlock());
	public static final GiftBlockItem GIFT_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift"), new GiftBlockItem(GIFT_BLOCK));
    public static final BlockEntityType<GiftBlockEntity> GIFT_BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY, new Identifier(MOD_ID, "gift"), BlockEntityType.Builder.create(GiftBlockEntity::new, GIFT_BLOCK).build(null));

    public static final GiftPaperItem GIFT_PAPER = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift_paper"), new GiftPaperItem());

    public static final SoundEvent GIFT_WRAP_SOUND = registerSound("gift.wrap");
    public static final SoundEvent GIFT_UNWRAP_SOUND = registerSound("gift.unwrap");

    public static final Tag<Block> NONWRAPPABLE_BLOCKS = TagRegistry.block(new Identifier(MOD_ID, "nonwrappable"));

	@Override
	public void onInitialize() {
		DispenserBlock.registerBehavior(GIFT_PAPER, new FallibleItemDispenserBehavior() {
			@Override
			public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
				success = GIFT_PAPER.tryWrapBlock(itemStack, blockPointer.getWorld(), blockPointer.getBlockPos().offset(blockPointer.getBlockState().get(DispenserBlock.FACING)));
				itemStack.split(1);
				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(GIFT_BLOCK_ITEM, new BlockPlacementDispenserBehavior());
	}

	private static SoundEvent registerSound(String name) {
		Config.initialize();

    	Identifier identifier = new Identifier(MOD_ID, name);
    	return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
	}
}