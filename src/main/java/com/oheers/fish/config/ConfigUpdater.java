package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

import java.io.*;

public class ConfigUpdater {

	public static void updateMessages(int version) throws IOException {
		File messagesFile = new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder().getPath() + "\\messages.yml");
		if (messagesFile.exists()) {
			BufferedReader file = new BufferedReader(new FileReader(messagesFile));
			StringBuilder inputBuffer = new StringBuilder();
			String line;

			while ((line = file.readLine()) != null) {
				if (line.equals("config-version: " + version)) {
					line = "config-version: " + EvenMoreFish.MSG_CONFIG_VERSION; // replace the line here
				}
				inputBuffer.append(line);
				inputBuffer.append('\n');
			}

			inputBuffer.append(getMessageUpdates(version));

			file.close();

			// write the new string with the replaced line OVER the same file
			FileOutputStream fileOut = new FileOutputStream(messagesFile);
			fileOut.write(inputBuffer.toString().getBytes());
			fileOut.close();

		}
	}

	public static void updateConfig(int version) throws IOException {
		File messagesFile = new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder().getPath() + "\\config.yml");
		if (messagesFile.exists()) {
			BufferedReader file = new BufferedReader(new FileReader(messagesFile));
			StringBuilder inputBuffer = new StringBuilder();
			String line;

			while ((line = file.readLine()) != null) {
				if (line.equals("config-version: " + version)) {
					line = "config-version: " + EvenMoreFish.MAIN_CONFIG_VERSION; // replace the line here
				}
				inputBuffer.append(line);
				inputBuffer.append('\n');
			}

			inputBuffer.append(getConfigUpdates(version));

			file.close();

			// write the new string with the replaced line OVER the same file
			FileOutputStream fileOut = new FileOutputStream(messagesFile);
			fileOut.write(inputBuffer.toString().getBytes());
			fileOut.close();

		}
	}

	private static String getMessageUpdates(int version) {
		StringBuilder update = new StringBuilder();
		update.append(UPDATE_ALERT);
		switch (version) {
			case 7: {
				update.append(MSG_UPDATE_8);
			}
		}

		update.append(UPDATE_ALERT);

		return update.toString();
	}

	private static String getConfigUpdates(int version) {
		StringBuilder update = new StringBuilder();
		update.append(UPDATE_ALERT);
		switch (version) {
			case 7: {
				update.append(CONFIG_UPDATE_8);
			}
		}

		update.append(UPDATE_ALERT);

		return update.toString();
	}

	private final static String UPDATE_ALERT = "\n###################### THIS IS AUTOMATICALLY UPDATED BY THE PLUGIN, IT IS RECOMMENDED TO MOVE THESE VALUES TO THEIR APPROPRIATE PLACES. ######################\n";

	private static final String MSG_UPDATE_8 =
			"# Help messages\n" +
					"# General help (/emf help) - permission node dependant commands will only show if they are formatted with the forward-slash.\n" +
					"help-general:\n" +
					"  - \"&f&m &#f1ffed&m &#e2ffdb&m &#d3ffc9&m &#c3ffb7&m &#b2ffa5&m &#9fff92&m &#8bff7f&m &#73ff6b&m &a&m &f &a&lEvenMoreFish &a&m &#73ff6b&m&m &#8bff7f&m &#9fff92&m &#b2ffa5&m &#c3ffb7&m &#d3ffc9&m &#f1ffed&m &f&m &f\"\n" +
					"  - \"/emf top - Shows an ongoing competition's leaderboard.\"\n" +
					"  - \"/emf help - Shows you this page.\"\n" +
					"  - \"/emf shop - Opens a shop to sell your fish.\"\n" +
					"  - \"/emf admin - Admin command help page.\"\n" +
					"\n" +
					"# Competition help (/emf admin competition help)\n" +
					"help-competition:\n" +
					"  - \"&f&m &#f1ffed&m &#e2ffdb&m &#d3ffc9&m &#c3ffb7&m &#b2ffa5&m &#9fff92&m &#8bff7f&m &#73ff6b&m &a&m &f &a&lEvenMoreFish &a&m &#73ff6b&m&m &#8bff7f&m &#9fff92&m &#b2ffa5&m &#c3ffb7&m &#d3ffc9&m &#f1ffed&m &f&m &f\"\n" +
					"  - \"/emf admin competition start <duration> <type> - Starts a competition of a specified duration\"\n" +
					"  - \"/emf admin competition end - Ends the current competition (if there is one)\"\n" +
					"\n" +
					"# Admin help (/emf admin help)\n" +
					"help-admin:\n" +
					"  - \"&f&m &#f1ffed&m &#e2ffdb&m &#d3ffc9&m &#c3ffb7&m &#b2ffa5&m &#9fff92&m &#8bff7f&m &#73ff6b&m &a&m &f &a&lEvenMoreFish &a&m &#73ff6b&m&m &#8bff7f&m &#9fff92&m &#b2ffa5&m &#c3ffb7&m &#d3ffc9&m &#f1ffed&m &f&m &f\"\n" +
					"  - \"/emf admin competition <start/end> <duration> <type> - Starts or stops a competition\"\n" +
					"  - \"/emf admin reload - Reloads the plugin's config files\"\n" +
					"  - \"/emf admin version - Displays plugin information.\"\n\n" +
					"# The name found on the item to sell all fish in inventory in /emf shop\n" +
					"sell-all-name: \"&6&lSELL ALL\"\n" +
					"# The name found on the confirming item in /emf shop\n" +
					"confirm-sell-all-gui-name: \"&6&lCONFIRM\"\n" +
					"# The name found on the error item in /emf shop when the player's inventory contains no items of value.\n" +
					"error-sell-all-gui-name: \"&c&lCan't Sell\"\n" +
					"# The lore for the sell-all item in the GUI\n" +
					"sell-all-lore:\n" +
					"  - \"&8Inventory\"\n" +
					"  - \"\"\n" +
					"  - \"&7Total Value = &e${sell-price}\"\n" +
					"  - \"\"\n" +
					"  - \"&7Click this button to sell\"\n" +
					"  - \"&7the fish in your inventory to\"\n" +
					"  - \"&7make some extra money.\"\n" +
					"  - \"\"\n" +
					"  - \"&e» (Left-click) sell the fish.\"\n" +
					"# The lore below the error item in /emf shop when the gui contains no items of value.\n" +
					"error-sell-all-gui-lore:\n" +
					"  - \"&8Inventory\"\n" +
					"  - \"\"\n" +
					"  - \"&7Total Value = &c$0\"\n" +
					"  - \"\"\n" +
					"  - \"&7Click this button to sell\"\n" +
					"  - \"&7the fish in your inventory to\"\n" +
					"  - \"&7make some extra money.\"\n" +
					"  - \"\"\n" +
					"  - \"&c» (Left-click) sell the fish.\"" +
					"# By setting a fish's minimum-length to less than 0, you can create a lengthless fish. This is used when a player fishes a lengthless fish.\n" +
					"lengthless-fish-caught: \"&l{player} &rhas fished a {rarity_colour}&l{rarity} {rarity_colour}{fish}!\"";


	private static final String CONFIG_UPDATE_8 =
			"gui: \n" +
			"  # The slot to put the item in on the bottom row, accepts values 1-9 inclusive.\n" +
					"  sell-slot: 4\n" +
					"  # The item for the player to click to automatically sell all their fish\n" +
					"  sell-all-item: COD_BUCKET\n" +
					"  # The slot to put the item in on the bottom row, accepts values 1-9 inclusive.\n" +
					"  sell-all-slot: 6\n" +
					"  # The item for the player to click to confirm selling all of their fish\n" +
					"  sell-all-item-confirm: TROPICAL_FISH_BUCKET\n" +
					"  # The item shown to the player when an error occurs (trying to sell nothing of value from their inventory)\n" +
					"  sell-all-item-error: SALMON_BUCKET\n" +
					"  # How many rows the selling area of the GUI should be (max 5, min 1)\n" +
					"  size: 3\n" +
					"  # Should the items be dropped(false) or sold(true) when a player exits an inventory?\n" +
					"  sell-over-drop: false";
}
