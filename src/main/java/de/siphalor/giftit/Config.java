package de.siphalor.giftit;

import de.siphalor.giftit.util.IItem;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.constraints.RangeConstraint;
import de.siphalor.tweed.config.entry.IntEntry;

public class Config {
	public static final ConfigFile FILE = TweedRegistry.registerConfigFile(GiftIt.MOD_ID).setScope(ConfigScope.SMALLEST);

	public static final IntEntry MAX_PAPER_DAMAGE = FILE.register("max-paper-damage", new IntEntry(4))
		.setEnvironment(ConfigEnvironment.SYNCED)
		.addConstraint(new RangeConstraint<Integer>().greaterThan(-1))
		.setComment(
			"The amount of uses for the gift paper.\n" +
			"Use 0 for infinite uses.\n" +
			"1 or infinite uses will change the stack size to 64."
		)
		.setReloadListener(value -> {
			((IItem) GiftIt.GIFT_PAPER).setMaxDamage(value == 0 ? 0 : value - 1);
			unbreakableGiftPaper = value == 0;
			((IItem) GiftIt.GIFT_PAPER).setMaxCount(value <= 1 ? 64 : 1);
		});
	public static boolean unbreakableGiftPaper = false;

	static void initialize() {
	}
}
