package de.siphalor.giftit;

import com.google.common.base.CaseFormat;
import de.siphalor.giftit.util.IItem;
import de.siphalor.tweed4.annotated.*;
import de.siphalor.tweed4.config.ConfigEnvironment;
import de.siphalor.tweed4.config.ConfigScope;
import de.siphalor.tweed4.config.constraints.RangeConstraint;

import java.util.ArrayList;
import java.util.List;

@ATweedConfig(scope = ConfigScope.SMALLEST, environment = ConfigEnvironment.UNIVERSAL, tailors = "tweed4:coat", casing = CaseFormat.LOWER_HYPHEN)
public class Config {
	@AConfigEntry(
			environment = ConfigEnvironment.SYNCED,
			constraints = @AConfigConstraint(value = RangeConstraint.class, param = "-1.."),
			comment = """
					The amount of uses for the gift paper.
					Use 0 for infinite uses.
					1 or infinite uses will change the stack size to 64."""
	)
	public static int maxPaperDamage = 4;

	@AConfigEntry(
			comment = "Restrict wrapping blocks completely to blocks with inventories"
	)
	public static boolean restrictToInventories = false;

	@AConfigEntry(
			comment = "Disallow certain blocks from being wrapped.\n" +
					          "This can be used additionally to the block tag \"giftit:nonwrappable\"."
	)
	public static List<String> forbiddenBlocks = new ArrayList<>();

	@AConfigEntry(
			comment = "Rotates blocks that are direction aware when they are unwrapped."
	)
	public static boolean enableBlockUnwrapRotation = true;

	@AConfigEntry(
			comment = "Allow gift recursion (packing gifts into gifts)."
	)
	public static boolean allowGiftRecursion = true;

	@AConfigEntry(
			comment = "Enable wrapping entities by clicking on them with gift paper."
	)
	public static boolean enableEntityWrapping = false;

	@AConfigEntry(
			comment = "Forbid large entities (> 1.5x3x1.5)"
	)
	public static boolean forbidLargeEntities = true;

	@AConfigEntry(
			comment = "Disallow certain entities from being wrapped.\n" +
					          "This can be used additionally to the entity tag \"giftit:nonwrappable\"."
	)
	public static List<String> forbiddenEntities = new ArrayList<>();

	@AConfigExclude
	public static boolean unbreakableGiftPaper;

	@AConfigListener()
	public static void onPaperDamageReload() {
		((IItem) GiftIt.GIFT_PAPER).setMaxDamage(maxPaperDamage);
		unbreakableGiftPaper = maxPaperDamage == 0;
		((IItem) GiftIt.GIFT_PAPER).setMaxCount(maxPaperDamage <= 1 ? 64 : 1);
	}
}
