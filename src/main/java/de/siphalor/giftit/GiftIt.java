package de.siphalor.giftit;

import de.siphalor.giftit.client.ClientCore;
import de.siphalor.giftit.gift.GiftBlock;
import de.siphalor.giftit.gift.GiftBlockEntity;
import de.siphalor.giftit.gift.GiftBlockItem;
import de.siphalor.giftit.gift.GiftPaperItem;
import de.siphalor.giftit.recipe.StackWrappingRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

public class GiftIt implements ModInitializer {
	public static final String MOD_ID = "giftit";

	public static final Block GIFT_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gift"), new GiftBlock());
	public static final GiftBlockItem GIFT_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift"), new GiftBlockItem(GIFT_BLOCK));
    public static final BlockEntityType<GiftBlockEntity> GIFT_BLOCK_ENTITY_TYPE = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "gift"),
		    FabricBlockEntityTypeBuilder.create(GiftBlockEntity::new, GIFT_BLOCK).build()
    );

    public static final GiftPaperItem GIFT_PAPER = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gift_paper"), new GiftPaperItem());

    public static final SoundEvent GIFT_WRAP_SOUND = registerSound("gift.wrap");
    public static final SoundEvent GIFT_UNWRAP_SOUND = registerSound("gift.unwrap");

    public static final TagKey<Block> NONWRAPPABLE_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MOD_ID, "nonwrappable"));
    public static final TagKey<EntityType<?>> NONWRAPPABLE_ENTITIES = TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(MOD_ID, "nonwrappable"));

    public static final RecipeSerializer<StackWrappingRecipe> STACK_WRAPPING_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "stack_wrapping"), new SpecialRecipeSerializer<>(StackWrappingRecipe::new));

	@Override
	public void onInitialize() {
		DispenserBlock.registerBehavior(GIFT_PAPER, new FallibleItemDispenserBehavior() {
			@Override
			public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
				if (!blockPointer.getWorld().isClient()) {
					BlockPos targetPos = blockPointer.getPos().offset(blockPointer.getBlockState().get(DispenserBlock.FACING));
					setSuccess(
							GIFT_PAPER.tryWrapBlock(itemStack, blockPointer.getWorld(), targetPos)
									|| tryWrapEntity(blockPointer, itemStack, targetPos)
					);
					if (isSuccess()) {
						itemStack.decrement(1);
						blockPointer.getWorld().playSound(null, blockPointer.getPos(), GIFT_WRAP_SOUND, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
				}
				return itemStack;
			}

			public boolean tryWrapEntity(BlockPointer blockPointer, ItemStack itemStack, BlockPos targetPos) {
				if (blockPointer.getWorld().getBlockState(targetPos).isAir()) {
					List<Entity> entities = blockPointer.getWorld().getEntitiesByClass(Entity.class, new Box(targetPos), EntityPredicates.VALID_ENTITY);
					for (Entity entity : entities) {
						if (Config.enableEntityWrapping && entity instanceof LivingEntity) {
							if (GIFT_PAPER.tryWrapEntity(itemStack, blockPointer.getWorld(), entity)) {
								return true;
							}
						}
						if (entity instanceof ItemEntity) {
							World world = blockPointer.getWorld();
							entity.stopRiding();
							entity.removeAllPassengers();
							entity.remove(Entity.RemovalReason.KILLED);
							ItemStack stack = ((ItemEntity) entity).getStack();

							world.setBlockState(targetPos, GIFT_BLOCK.getDefaultState());
							GiftBlockEntity giftBlockEntity = new GiftBlockEntity(
									targetPos, GIFT_BLOCK.getDefaultState(),
									stack, itemStack.getDamage(), GIFT_PAPER.getColor(itemStack), itemStack.hasCustomName() ? itemStack.getName() : null
							);
							world.addBlockEntity(giftBlockEntity);
							return true;
						}
					}
				}
				return false;
			}
		});
		DispenserBlock.registerBehavior(GIFT_BLOCK_ITEM, new BlockPlacementDispenserBehavior());
	}

	private static SoundEvent registerSound(String name) {
    	Identifier identifier = new Identifier(MOD_ID, name);
    	return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
	}

	public static void proxyClientUpdateVisualState(World world, BlockPos blockPos, BlockState blockState) {
		ClientCore.updateVisualState(world, blockPos, blockState);
	}
}
