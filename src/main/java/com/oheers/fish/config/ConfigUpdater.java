package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

import java.io.*;

public class ConfigUpdater {

	public static void updateMessages(int version) throws IOException {
		File messagesFile = new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder().getPath() + "\\messages.yml");
		if (messagesFile.exists()) {
			try (BufferedReader file = new BufferedReader(new FileReader(messagesFile))) {

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
				// write the new string with the replaced line OVER the same file
				FileOutputStream fileOut = new FileOutputStream(messagesFile);
				fileOut.write(inputBuffer.toString().getBytes());
			}
		}
	}

	public static void updateConfig(int version) throws IOException {
		File messagesFile = new File(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class).getDataFolder().getPath() + "\\config.yml");
		if (messagesFile.exists()) {
			try (BufferedReader file = new BufferedReader(new FileReader(messagesFile))) {

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
				// write the new string with the replaced line OVER the same file
				FileOutputStream fileOut = new FileOutputStream(messagesFile);
				fileOut.write(inputBuffer.toString().getBytes());
			}
		}
	}

	private static String getMessageUpdates(int version) {
		StringBuilder update = new StringBuilder();
		update.append(UPDATE_ALERT);
		switch (version) {
			case 7: update.append(MSG_UPDATE_8);
			case 8: update.append(MSG_UPDATE_9);
			case 9: update.append(MSG_UPDATE_10);
			case 10: update.append(MSG_UPDATE_11);
		}

		update.append(UPDATE_ALERT);

		return update.toString();
	}

	private static String getConfigUpdates(int version) {
		StringBuilder update = new StringBuilder();
		update.append(UPDATE_ALERT);
		switch (version) {
			case 7: update.append(CONFIG_UPDATE_8);
			case 8: update.append(CONFIG_UPDATE_9);
			case 9: update.append(CONFIG_UPDATE_10);
		}

		update.append(UPDATE_ALERT);

		return update.toString();
	}

	/**
	 * Removes all data from variables storing update strings to reduce memory usage in server memory.
	 */
	public static void clearUpdaters() {
		MSG_UPDATE_8 = null;
		MSG_UPDATE_9 = null;
		MSG_UPDATE_10 = null;
		MSG_UPDATE_11 = null;

		CONFIG_UPDATE_8 = null;
		CONFIG_UPDATE_9	= null;
		CONFIG_UPDATE_10 = null;
	}

	private final static String UPDATE_ALERT = "\n###################### THIS IS AUTOMATICALLY UPDATED BY THE PLUGIN, IT IS RECOMMENDED TO MOVE THESE VALUES TO THEIR APPROPRIATE PLACES. ######################\n";

	private static String MSG_UPDATE_11 = "invalid-type: \"&rThat isn't a type of competition type, available types: MOST_FISH, LARGEST_FISH, SPECIFIC_FISH\"\n" +
			"# Sent to all online players when not enough players are on to start a competition\n# How should the %emf_competition_place_fish_*% be formatted when there's no length on the fish?\n" +
			"emf-lengthless-fish-format: \"{rarity_colour}&l{rarity} {fish}\"\n" +
			"# How should %emf_competition_place_fish_*% be formatted during MOST/SPECIFIC_FISH competitions?\n" +
			"emf-most-fish-format: \"{amount} fish\"\n" +
			"# What should be displayed for %emf_competition_place_size_*% during the MOST_FISH competition?\n" +
			"emf-size-during-most-fish: \"This is a competition for the most fish.\"\n" +
			"# How the %emf_competition_time_left% variable should be formatted.\n" +
			"emf-time-remaining: \"Time left until next competition: {days}d, {hours}h, {minutes}m.\"\n" +
			"# Replaces the %emf_competition_time_left% variable when there's a competition already running.\n" +
			"emf-time-remaining-during-comp: \"There is a competition running right now.\"\n# Sent when someone tries to use a bait in creative\n" +
			"bait-survival-limited: \"&rYou must be in &nsurvival&r to apply baits to fishing rods.\"\n# When trying to place a skull-fish when this is blocked in config.yml\n" +
			"place-fish-blocked: \"&rYou cannot place this fish.\"\n" +
			"\n" +
			"admin:\n" +
			"  # Opens an /emf shop for another player\n" +
			"  open-fish-shop: \"&rOpened a shop inventory for {player}.\"\n" +
			"  # When a fish is given to a player\n" +
			"  given-player-fish: \"&rYou have given {player} a {fish}.\"\n" +
			"  # When a bait is given to a player\n" +
			"  given-player-bait: \"&rYou have given {player} a {bait}.\"\n" +
			"  # When an admin runs /emf admin bait without a bait name.\n" +
			"  no-bait-specified: \"&rYou must specify a bait name.\"\n" +
			"\n" +
			"  # When economy is disabled for the plugin\n" +
			"  economy-disabled: \"&rEvenMoreFish's economy features are disabled.\"\n" +
			"\n" +
			"  # When the specified player can't be found when using -p: parameter.\n" +
			"  player-not-found: \"&r{player} could not be found.\"\n" +
			"  # When the specified number in -q: is not a number.\n" +
			"  number-format-error: \"&r{amount} is not a valid number.\"\n" +
			"  # When the specified number in -q: is not within the minecraft stack range (1-64)\n" +
			"  number-range-error: \"&r{amount} is not a number between 1-64.\"\n" +
			"  # When a command cannot be run from the console\n" +
			"  cannot-run-on-console: \"&rCommand cannot be run from console.\"\n" +
			"\n" +
			"  # Sent when a competition is already running when using /emf admin competition start\n" +
			"  competition-already-running: \"&rThere's already a competition running.\"\n" +
			"  # When an invalid competition type is tried to be started\n" +
			"  competition-type-invalid: \"&rThat isn't a type of competition type, available types: MOST_FISH, LARGEST_FISH, SPECIFIC_FISH\"\n" +
			"\n" +
			"  # When there's a spigot update available, don't translate the URL otherwise it won't direct to the correct page.\n" +
			"  update-available: \"&rThere is an update available: https://www.spigotmc.org/resources/evenmorefish.91310/updates\"\n" +
			"  # When the plugin is reloaded\n" +
			"  reload: \"&rSuccessfully reloaded the plugin.\"";

	private static String MSG_UPDATE_10 = "# Shown when /emf toggle is run, to turn off and on respectively.\n" +
			"toggle-off: \"You will no longer catch custom fish.\"\n" +
			"toggle-on: \"You will now catch custom fish.\"";

	private static String MSG_UPDATE_9 = "# This is the format of the lore given to fish when they're caught.\n" +
			"# {custom-lore} is specified in the fish.yml under the lore: section, if the fish has a lore, the lore's lines will\n" +
			"# replace the {fish_lore}, however if it's empty the line will be removed. DO NOT ADD ANYTHING OTHER THAN THIS VARIABLE\n" +
			"# TO THAT LINE.\n" +
			"fish-lore:\n" +
			"  - \"&fCaught by {player}\"\n" +
			"  - \"&fMeasures {length}cm\"\n" +
			"  - \"\"\n" +
			"  - \"{fish_lore}\"\n" +
			"  - \"{rarity_colour}&l{rarity}\"\n" +
			"# Sent when a player tries to apply too many types of baits to a fishing rod, set in the general section of baits.yml\n" +
			"# Sent when a player tries to apply too many types of baits to a fishing rod, set in the general section of baits.yml\n" +
			"max-baits-reached: \"You have reached the maximum number of types of baits for this fishing rod.\"\n" +
			"# Sent when a player catches a bait from fishing (this can be disabled by setting catch-percentage to 0 in baits.yml\n" +
			"bait-catch: \"&l{player} &rhas caught a {bait_theme}&l{bait} &rbait!\"\n" +
			"# Sent when a bait is applied and a fish is caught.\n" +
			"bait-use: \"You have used one of your rod's {bait_theme}&l{bait} &rbait.\"";

	private static String CONFIG_UPDATE_9 = "\n" +
			"# The locale of the message file\n" +
			"# Currently: en, de, es, fr, nl, pt-br, ru, tr, vn\n" +
			"# Delete messages.yml before changing this\n" +
			"locale: en";

	private static String CONFIG_UPDATE_10 = "\n" +
			"# Setting this to change the boosbar sytle\n" +
			"# you can use like: SEGMENTED_6 SEGMENTED_10 SEGMENTED_12 SEGMENTED_20 SOLID\n" +
			"barstyle: 'SEGMENTED_10'";

	private static String MSG_UPDATE_8 =
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


	private static String CONFIG_UPDATE_8 =
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
