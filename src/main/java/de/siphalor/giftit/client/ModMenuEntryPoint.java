package de.siphalor.giftit.client;

import de.siphalor.giftit.Config;
import de.siphalor.giftit.GiftIt;
import de.siphalor.tweed.client.TweedClothBridge;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenuEntryPoint implements ModMenuApi {
	private static TweedClothBridge tweedClothBridge = new TweedClothBridge(Config.FILE);

	@Override
	public String getModId() {
		return GiftIt.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> tweedClothBridge.buildScreen();
	}
}
