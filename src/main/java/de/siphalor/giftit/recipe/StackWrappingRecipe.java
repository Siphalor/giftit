package de.siphalor.giftit.recipe;

import de.siphalor.giftit.Config;
import de.siphalor.giftit.GiftIt;
import de.siphalor.giftit.gift.GiftWrappedType;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StackWrappingRecipe extends SpecialCraftingRecipe {
	public StackWrappingRecipe(Identifier id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		boolean paper = false, other = false;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if (stack.isEmpty())
				continue;
			if (!paper && stack.getItem() == GiftIt.GIFT_PAPER) {
				paper = true;
				continue;
			}
			if (!other && stack.getItem() != GiftIt.GIFT_PAPER && !(stack.getItem() instanceof DyeItem)) {
				if (Config.allowGiftRecursion || stack.getItem() != GiftIt.GIFT_BLOCK_ITEM) {
					other = true;
					continue;
				}
			}
			if (paper || other)
				return false;
		}
		return paper && other;
	}

	@Override
	public ItemStack craft(CraftingInventory inv) {
		ItemStack paperStack = null;
		ItemStack otherStack = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			if (stack.isEmpty())
				continue;
			if (paperStack == null && stack.getItem() == GiftIt.GIFT_PAPER) {
				paperStack = stack;
				continue;
			}
			if (otherStack == null && stack.getItem() != GiftIt.GIFT_PAPER && !(stack.getItem() instanceof DyeItem)) {
				if (Config.allowGiftRecursion || stack.getItem() != GiftIt.GIFT_BLOCK_ITEM) {
					otherStack = stack;
					continue;
				}
			}
			if (paperStack != null || otherStack != null) {
				return ItemStack.EMPTY;
			}
		}
		if (paperStack == null || otherStack == null) {
			return ItemStack.EMPTY;
		}

		ItemStack result = new ItemStack(GiftIt.GIFT_BLOCK_ITEM);
		GiftIt.GIFT_BLOCK_ITEM.setColor(result, GiftIt.GIFT_PAPER.getColor(paperStack));
		NbtCompound blockEntityTag = result.getOrCreateSubNbt("BlockEntityTag");
		blockEntityTag.putInt("WrappedType", GiftWrappedType.STACK.ordinal());
		blockEntityTag.putInt("PaperDamage", paperStack.getDamage());
		NbtCompound otherTag = otherStack.writeNbt(new NbtCompound());
		blockEntityTag.put("WrappedData", otherTag);

		return result;
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GiftIt.STACK_WRAPPING_RECIPE_SERIALIZER;
	}
}
