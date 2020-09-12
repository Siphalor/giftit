package de.siphalor.giftit;

import com.google.common.base.CaseFormat;
import de.siphalor.giftit.util.IItem;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.annotated.*;
import de.siphalor.tweed.config.constraints.RangeConstraint;

@ATweedConfig(scope = ConfigScope.SMALLEST, environment = ConfigEnvironment.UNIVERSAL, tailors = "tweed:cloth", casing = CaseFormat.LOWER_HYPHEN)
public class Config {
	@AConfigEntry(
			environment = ConfigEnvironment.SYNCED,
			constraints = @AConfigConstraint(value = RangeConstraint.class, param = "-1.."),
			comment =  "The amount of uses for the gift paper.\n" +
					"Use 0 for infinite uses.\n" +
					"1 or infinite uses will change the stack size to 64."
	)
	public int maxPaperDamage = 4;

	@AConfigEntry(
			comment = "Restrict wrapping blocks completely to blocks with inventories"
	)
	public boolean restrictToInventories = false;

	@AConfigExclude
	public boolean unbreakableGiftPaper;

	@AConfigListener()
	public void onPaperDamageReload() {
		((IItem) GiftIt.GIFT_PAPER).setMaxDamage(maxPaperDamage == 0 ? 0 : maxPaperDamage - 1);
		unbreakableGiftPaper = maxPaperDamage == 0;
		((IItem) GiftIt.GIFT_PAPER).setMaxCount(maxPaperDamage <= 1 ? 64 : 1);
	}
}
