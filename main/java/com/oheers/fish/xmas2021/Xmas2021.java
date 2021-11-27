package com.oheers.fish.xmas2021;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.FishReport;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Xmas2021 {

	// Slot registering for which slots will contain items and won't
	private final static int[] FILLER_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 14, 19, 21, 23, 25, 28, 30, 32, 34, 39, 43, 45, 46, 47, 48, 49, 50, 51, 52, 53 };
	private final static int[] ACTIVE_SLOTS = { 9, 18, 27, 36, 37, 38, 29, 20, 11, 12, 13, 22, 31, 40, 41, 42, 33, 24, 15, 16, 17, 26, 35, 44 };

	private static final List<UUID> FOCUSED_PLAYERS = new ArrayList<>();
	private static final Map<Integer, Fish> REGISTERED_FISH = new HashMap<>();

	public static void generateGUI(Player player) {

		FOCUSED_PLAYERS.add(player.getUniqueId());

		Inventory gui = Bukkit.createInventory(null, 54, FishUtils.translateHexColorCodes(FishUtils.translateHexColorCodes(EvenMoreFish.xmas2021Config.getGUIName())));
		loadFillers(gui);
		loadFish(gui, player);
		player.openInventory(gui);
	}

	private static void loadFillers(Inventory inventory) {

		ItemStack fillerStack = new ItemStack(EvenMoreFish.xmas2021Config.getFillerMaterial());
		ItemMeta meta = fillerStack.getItemMeta();

		meta.setDisplayName(ChatColor.RESET + "");

		fillerStack.setItemMeta(meta);

		for (int slotID : FILLER_SLOTS) {
			inventory.setItem(slotID, fillerStack);
		}
	}

	private static void loadFish(Inventory inventory, Player player) {

		for (Integer day : REGISTERED_FISH.keySet()) {
			boolean modified = false;
			if (day <= ACTIVE_SLOTS.length) {
				Calendar calendar = Calendar.getInstance();
				if (calendar.get(Calendar.DAY_OF_MONTH) >= day) {

					Fish fish = REGISTERED_FISH.get(day);

					if (EvenMoreFish.fishReports.containsKey(player.getUniqueId())) {
						List<FishReport> reports = EvenMoreFish.fishReports.get(player.getUniqueId());

						for (FishReport report : reports) {
							if (report.getRarity().equals(fish.getRarity().getValue()) && report.getName().equals(fish.getName())) {

								ItemStack fishIcon = REGISTERED_FISH.get(day).give().clone();
								fishIcon.setAmount(day);

								ItemMeta fishIconMeta = fishIcon.getItemMeta();
								List<String> lore = new ArrayList<>();

								LocalDateTime dateTime = LocalDateTime.ofEpochSecond(report.getTimeEpoch(), 0, ZoneOffset.UTC);
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale.ENGLISH);
								String formattedDate = dateTime.format(formatter);

								for (String line : EvenMoreFish.xmas2021Config.getAdventItemLore()) {
									lore.add(new Message()
											.setMSG(line)
											.setName(fish.getName())
											.setNumCaught(Integer.toString(report.getNumCaught()))
											.setLargestSize(EvenMoreFish.xmas2021Config.getLengthFormat())
											.setLength(Float.toString(report.getLargestLength()))
											.setFirstCaught(formattedDate)
											.setDay(Integer.toString(day))
											.toString());
								}

								fishIconMeta.setLore(lore);

								fishIconMeta.setDisplayName(new Message()
										.setMSG(EvenMoreFish.xmas2021Config.getAdventItemName())
										.setDay(Integer.toString(day))
										.setName(fish.getName())
										.toString());

								fishIcon.setItemMeta(fishIconMeta);

								inventory.setItem(ACTIVE_SLOTS[day - 1], fishIcon);
								modified = true;
								break;
							}
						}

						if (!modified) inventory.setItem(ACTIVE_SLOTS[day - 1], unCaughtFish(day)); modified = true;
					}
				}

				if (!modified) inventory.setItem(ACTIVE_SLOTS[day - 1], loadLockedFish(day));
			}
		}
	}

	public static List<UUID> getFocusedPlayers() {
		return FOCUSED_PLAYERS;
	}

	public static void unfocusPlayer(Player player) {
		FOCUSED_PLAYERS.remove(player.getUniqueId());
	}

	public static void setRegisteredFish(Fish f, int day) {
		f.init();
		REGISTERED_FISH.put(day, f);
	}

	public static ItemStack unCaughtFish(int day) {
		ItemStack uncaughtFish = new ItemStack(EvenMoreFish.xmas2021Config.getLockedFishMaterial());

		ItemMeta uncaughtMeta = uncaughtFish.getItemMeta();
		List<String> lore = new ArrayList<>();

		for (String line : EvenMoreFish.xmas2021Config.getAdventItemLore()) {
			lore.add(new Message()
					.setMSG(line)
					.setName("???")
					.setNumCaught("0")
					.setLargestSize("???")
					.setFirstCaught("???")
					.setDay(Integer.toString(day))
					.toString());
		}

		uncaughtMeta.setLore(lore);

		uncaughtMeta.setDisplayName(new Message()
				.setMSG(EvenMoreFish.xmas2021Config.getAdventItemName())
				.setDay(Integer.toString(day))
				.setName("???")
				.toString());

		uncaughtFish.setItemMeta(uncaughtMeta);
		uncaughtFish.setAmount(day);
		return uncaughtFish;
	}

	public static ItemStack loadLockedFish(int day) {
		ItemStack lockedFish = new ItemStack(EvenMoreFish.xmas2021Config.getLockedFishMaterial());

		ItemMeta lockedMeta = lockedFish.getItemMeta();

		List<String> lore = EvenMoreFish.xmas2021Config.getAdventLockedItemLore();
		for (int i=0; i<lore.size(); i++) {
			lore.set(i, new Message().setMSG(lore.get(i)).setTimeRemaining(timeFormat(getTimeRemaining(day))).toString());
		}

		lockedMeta.setLore(lore);
		lockedMeta.setDisplayName(new Message().setMSG(EvenMoreFish.xmas2021Config.getAdventLockedItemName()).setDay(Integer.toString(day)).toString());
		lockedFish.setItemMeta(lockedMeta);

		lockedFish.setAmount(day);
		return lockedFish;
	}

	public static Fish getFish() {
		Calendar calendar = new GregorianCalendar();
		return REGISTERED_FISH.get(calendar.get(Calendar.DAY_OF_MONTH));
	}

	public static boolean hiddenCheck() {
		Calendar calendar = new GregorianCalendar();
		return calendar.get(Calendar.DAY_OF_MONTH) <= 24 && calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.YEAR) == 2021;
	}

	public static int getTimeRemaining(int day) {
		Calendar calendar = new GregorianCalendar();
		int timeLeft = (day - calendar.get(Calendar.DAY_OF_MONTH)) * 86400;
		timeLeft -= (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
		return timeLeft;
	}

	public static String timeFormat(int timeLeft) {
		String returning = "";

		if (timeLeft >= 86400) {
			returning += timeLeft/86400 + EvenMoreFish.xmas2021Config.getTimeUnitDay() + " ";
		}

		if (timeLeft >= 3600) {
			returning += ((timeLeft%86400)/3600) + EvenMoreFish.xmas2021Config.getTimeUnitHour() + " ";
		}

		if (timeLeft >= 60) {
			returning += ((timeLeft%3600)/60) + EvenMoreFish.xmas2021Config.getTimeUnitMinute() + " ";
		}

		// Remaining seconds to always show, e.g. "1 minutes and 0 seconds left" and "5 seconds left"
		returning += (timeLeft%60) + EvenMoreFish.xmas2021Config.getTimeUnitSecond();
		return returning;
	}
}