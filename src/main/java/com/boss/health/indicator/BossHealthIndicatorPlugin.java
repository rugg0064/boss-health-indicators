package com.boss.health.indicator;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.inject.Inject;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.model.Indicator;
import com.boss.health.indicator.ui.BossIndicatorCreator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.*;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Boss Health Indicators",
	description = "Shows indicators for certain health percentages on boss health bars.",
	tags = {"Health Bar"}
)
public class BossHealthIndicatorPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientToolbar clientToolbar;
	@Inject private WorldService worldService;
	@Inject private ClientThread clientThread;
	@Inject private ConfigManager configManager;
	@Inject private Gson gson;
	@Inject private ColorPickerManager colorPickerManager;
	@Inject private BossHealthIndicatorConfig config;
	@Inject private Notifier notifier;

	private static final DecimalFormat percentageFormat = new DecimalFormat("##.##%");
	private static final String CONFIG_GROUP = "bosshealthindicators";
	private static final String CONFIG_KEY = "indicators";

	// The Theatre of Blood raid HUD (InterfaceID.TobHud) shows a percentage only,
	// so the boss is identified from the NPC in the room.
	private static final Set<String> TOB_BOSS_NAMES = new HashSet<String>(Arrays.asList(
		"The Maiden of Sugadinti",
		"Pestilent Bloat",
		"Nylocas Vasilias",
		"Sotetseg",
		"Xarpus",
		"Verzik Vitur"
	));

	private NavigationButton navButton;
	private BossHealthIndicatorPanel panel;

	public ColorPickerManager getColorPickerManager() {
		return colorPickerManager;
	}

	private List<BossIndicators> bossDatabase;
	Map<String, BossIndicators> mapping;

	// The health of the boss on the last tick of execution
	private Double lastBossHealthPercentage;

	List<Widget> activeBars;
	List<BossIndicators> activeBoss;
	private Widget activeBarWidget;

	@Provides
	BossHealthIndicatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossHealthIndicatorConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if(config.showPanel()) {
			clientToolbar.addNavigation(navButton);
		} else {
			clientToolbar.removeNavigation(navButton);
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		loadFromConfig();
		makeDatabaseMap();

		activeBars = new ArrayList<Widget>();
		activeBoss = null;
		activeBarWidget = null;
		lastBossHealthPercentage = null;

		// Set up side panel
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/bosshealthindicator_icon.png");
		panel = new BossHealthIndicatorPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Boss Health Indicators")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();
		if(config.showPanel()) {
			clientToolbar.addNavigation(navButton);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		activeBoss = null;
		activeBarWidget = null;
		clientThread.invoke(() -> clearBars());
	}

	private void handleHealthNotification(HealthBarTarget target) {
		if(activeBoss == null) {
			return;
		}
		Double percentHealth = readHealthFraction(target);
		if(percentHealth == null) {
			return;
		}
		final boolean forceCheck = lastBossHealthPercentage == null || percentHealth > lastBossHealthPercentage;
		if (forceCheck) {
			lastBossHealthPercentage = percentHealth;
		}

		final double healthFraction = percentHealth;
		activeBoss.forEach((indicatorSet -> {
			indicatorSet.getEntries().forEach(indicator -> {
				if(!indicator.getNotify()) {
					return;
				}
				if(
					((forceCheck) && healthFraction == indicator.getPercentage()) ||
						(healthFraction <= indicator.getPercentage() && indicator.getPercentage() < lastBossHealthPercentage))
				{
					notifier.notify(String.format("%s's health has reached %s!", indicatorSet.getBossName(), percentageFormat.format(indicator.getPercentage())));
				}
			});
		}));

		lastBossHealthPercentage = healthFraction;
	}

	private Double readHealthFraction(HealthBarTarget target) {
		if(target.theatreOfBlood) {
			int max = client.getVarbitValue(VarbitID.TOB_CLIENT_WAVEPROGRESS_MAX);
			if(max <= 0) {
				return null;
			}
			return client.getVarbitValue(VarbitID.TOB_CLIENT_WAVEPROGRESS_VAL) / (double) max;
		}

		Widget healthBarHealthTextWidget = client.getWidget(InterfaceID.HpbarHud.HP_BAR_TEXT);
		if(healthBarHealthTextWidget == null || healthBarHealthTextWidget.isHidden()) {
			return null;
		}
		String[] numbers = healthBarHealthTextWidget.getText().split(" / ");
		try {
			int numerator = Integer.parseInt(numbers[0]);
			int denominator = Integer.parseInt(numbers[1].contains("%") ? (numbers[1].split(" "))[0] : numbers[1]);
			return ((double) numerator) / denominator;
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	private List<BossIndicators> getMatchingIndicators(String bossName) {
		ArrayList<BossIndicators> returnList = new ArrayList<>();
		if(bossName == null || mapping == null) {
			return returnList;
		}
		mapping.forEach((name, indicator) -> {
			if(nameMatches(name, bossName)) {
				returnList.add(indicator);
			}
		});
		return returnList;
	}

	private static boolean nameMatches(String patternText, String bossName) {
		try {
			Pattern pattern = Pattern.compile(patternText);
			Matcher matcher = pattern.matcher(bossName);
			return matcher.matches();
		} catch(PatternSyntaxException e) {
			return false;
		}
	}

	/**
	 * Picks the Theatre of Blood boss whose health the raid bar is showing.
	 * Adds such as Nylocas Matomenos are ignored. If two different bosses are
	 * alive and the player is not attacking one of them, no name is returned.
	 */
	private static String selectTobBossName(Iterable<String> aliveNpcNames, String interactingName) {
		if(interactingName != null && TOB_BOSS_NAMES.contains(interactingName)) {
			return interactingName;
		}
		String match = null;
		for(String name : aliveNpcNames) {
			if(name == null || !TOB_BOSS_NAMES.contains(name)) {
				continue;
			}
			if(match == null) {
				match = name;
			} else if(!match.equals(name)) {
				return null;
			}
		}
		return match;
	}

	private boolean areBossListsIdentical(List<BossIndicators> a, List<BossIndicators> b) {
		if(a.size() != b.size()) {
			return false;
		}
		for(int i = 0; i < a.size(); i++) {
			BossIndicators elementA = a.get(i);
			BossIndicators elementB = b.get(i);
			if(!a.equals(b)) {
				return false;
			}
		}
		return true;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		HealthBarTarget target = findHealthBarTarget();
		if(target == null) {
			if(activeBoss != null) {
				clearBars();
				clearActiveTarget();
			}
			return;
		}

		List<BossIndicators> newIndicators = getMatchingIndicators(target.bossName);
		boolean targetChanged = activeBoss == null
			|| activeBarWidget != target.barWidget
			|| !areBossListsIdentical(activeBoss, newIndicators);
		if(targetChanged) {
			clearBars();
			lastBossHealthPercentage = null;
			if(newIndicators.size() > 0) {
				activeBoss = newIndicators;
				activeBarWidget = target.barWidget;
				createBars(target);
			} else {
				clearActiveTarget();
			}
		}
		if(activeBoss != null) {
			handleHealthNotification(target);
		}
	}

	private HealthBarTarget findHealthBarTarget() {
		HealthBarTarget standard = standardHealthBar();
		if(standard != null && getMatchingIndicators(standard.bossName).size() > 0) {
			return standard;
		}
		HealthBarTarget theatre = theatreHealthBar();
		if(theatre != null && getMatchingIndicators(theatre.bossName).size() > 0) {
			return theatre;
		}
		return standard;
	}

	private HealthBarTarget standardHealthBar() {
		Widget hp = client.getWidget(InterfaceID.HpbarHud.HP);
		Widget nameWidget = client.getWidget(InterfaceID.HpbarHud.CREATURE_NAME);
		// The layer holding the coloured health fill
		Widget inner = client.getWidget(InterfaceID.HpbarHud.INNER);
		if(hp == null || hp.isHidden() || nameWidget == null || inner == null) {
			return null;
		}
		String bossName = normalizeName(nameWidget.getText());
		if(bossName.isEmpty() || "-".equals(bossName)) {
			return null;
		}
		return new HealthBarTarget(bossName, false, inner);
	}

	private HealthBarTarget theatreHealthBar() {
		Widget container = client.getWidget(InterfaceID.TobHud.PROGRESS_CONTAINER);
		Widget bar = client.getWidget(InterfaceID.TobHud.PROGRESS_BAR);
		if(container == null || container.isHidden() || bar == null || client.getVarbitValue(VarbitID.TOB_CLIENT_WAVEPROGRESS_TYPE) == 0) {
			return null;
		}
		String bossName = selectTobBossName(aliveNpcNames(), interactingNpcName());
		if(bossName == null) {
			return null;
		}
		return new HealthBarTarget(bossName, true, bar);
	}

	private List<String> aliveNpcNames() {
		List<String> names = new ArrayList<String>();
		if(client.getNpcs() == null) {
			return names;
		}
		for(NPC npc : client.getNpcs()) {
			if(npc == null || npc.isDead()) {
				continue;
			}
			String name = normalizeName(npc.getName());
			if(!name.isEmpty()) {
				names.add(name);
			}
		}
		return names;
	}

	private String interactingNpcName() {
		if(client.getLocalPlayer() == null) {
			return null;
		}
		Actor interacting = client.getLocalPlayer().getInteracting();
		if(!(interacting instanceof NPC) || ((NPC) interacting).isDead()) {
			return null;
		}
		String name = normalizeName(((NPC) interacting).getName());
		return name.isEmpty() ? null : name;
	}

	private static String normalizeName(String name) {
		if(name == null) {
			return "";
		}
		return Text.removeTags(name).trim();
	}

	private void clearActiveTarget() {
		activeBoss = null;
		activeBarWidget = null;
		lastBossHealthPercentage = null;
	}

	// Deletes all active bar widgets
	void clearBars() {
		for(Widget widget : activeBars) {
			widget.setHidden(true);
			widget.revalidate();
		}
		activeBars.clear();
	}

	// Creates the appropriate indicator bars as children of the healthbar widget
	// Assumes activeBoss is set and not null
	void createBars(HealthBarTarget target) {
		for(BossIndicators bossIndicators : activeBoss) {
			for(Indicator indicator : bossIndicators.getEntries()) {
				Widget bar = createBarWidget(target.barWidget, indicator.getColor(), indicator.getPercentage());
				activeBars.add(bar);
			}
		}
	}

	// Creates a bar widget, does not add to parent
	private Widget createBarWidget(Widget parent, Color color, double percent) {
		Widget bar = parent.createChild(WidgetType.RECTANGLE);
		bar.setOriginalWidth(2);
		bar.setWidthMode(WidgetSizeMode.ABSOLUTE);

		// Fill the parent's height
		bar.setOriginalHeight(0);
		bar.setHeightMode(WidgetSizeMode.MINUS);

		// Position in 16384ths of the parent's width so the bar stays in place if the health bar is resized
		bar.setOriginalX((int) (16384 * percent));
		bar.setXPositionMode(WidgetPositionMode.LEFT_16384THS);

		bar.setTextColor(color.getRGB());
		bar.setOpacity(color.getTransparency());
		//bar.setOpacity(127);

		bar.revalidate();

		return bar;
	}

	// Lodas config, merges by boss name, and saves to bossDatabase
	private void loadFromConfig() {
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		bossDatabase = stringToBossIndicators(json);
		saveToConfig();
	}

	// Saves the current bossDatabase to config
	private void saveToConfig() {
		configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
		String json = gson.toJson(bossDatabase);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	private void makeDatabaseMap() {
		List<BossIndicators> mergedIndicators = mergeIndicatorList(bossDatabase);

		mapping = new HashMap<String, BossIndicators>();
		for(BossIndicators data : mergedIndicators) {
			mapping.put(data.getBossName(), data);
		}
	}

	private ArrayList<BossIndicators> stringToBossIndicators(String string) {
		ArrayList<BossIndicators> returnList = new ArrayList<>();
		try {
			Type type = new TypeToken<List<BossIndicators>>() {}.getType();
			returnList = gson.fromJson(string, type);
			boolean hasNull = false;
			for(int i = 0; i < returnList.size() && !hasNull; i++) {
				if(returnList.get(i).hasAnyNull()) {
					hasNull = true;
				}
			}
			if(hasNull) {
				returnList = null;
			}
		} catch (Exception e) {
			// If there was any error, we really don't care what it was.
			// Keep the plugin going and return null.
			returnList = null;
		} finally {
			if(returnList == null) {
				returnList = new ArrayList<BossIndicators>();
			}
		}
		return returnList;
	}

	public void updateFromPanel() {
		bossDatabase = panel.getBossDatabase();
		saveToConfig();

		makeDatabaseMap();
		activeBoss = null;
	}

	// Merges entries of bossDatabase where the name is the same into a single entry
	// Be careful not to save merged data into config
	private List<BossIndicators> mergeIndicatorList(List<BossIndicators> indicators) {
		// Incase the user made multiple entries with the same name, we will squash that into one entry here.
		HashMap<String, BossIndicators> mergeMap = new HashMap<>();
		for(BossIndicators indicator : indicators) {
			String bossName = indicator.getBossName();
			if(!mergeMap.containsKey(bossName)) {
				// Not in table
				mergeMap.put(bossName, indicator);
			} else {
				// Already exists
				BossIndicators oldIndicator = mergeMap.get(bossName);
				ArrayList<Indicator> mergedIndicators = new ArrayList<>();
				mergedIndicators.addAll(oldIndicator.getEntries());
				mergedIndicators.addAll(indicator.getEntries());
				mergeMap.put(bossName, new BossIndicators(bossName, mergedIndicators));
			}
		}
		return new ArrayList<BossIndicators>(mergeMap.values());
	}

	public List<BossIndicators> getBossDatabase() {
		return bossDatabase;
	}

	public void exportToClipboard() {
		String json = gson.toJson(bossDatabase);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection selection = new StringSelection(json);
		clipboard.setContents(selection, null);
	}

	public void importFromClipboard() {
		String clipboardText = getClipboard();

		ArrayList<BossIndicators> originalIndicators = stringToBossIndicators(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY));
		ArrayList<BossIndicators> newIndicators = stringToBossIndicators(clipboardText);

		ArrayList<BossIndicators> combined = new ArrayList<>();
		combined.addAll(originalIndicators);
		combined.addAll(newIndicators);

		bossDatabase = combined;
		makeDatabaseMap();
		panel.rebuild();

		saveToConfig();
		activeBoss = null;
	}

	private String getClipboard() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				String text = (String) clipboard.getData(DataFlavor.stringFlavor);
				return text;
			} catch (Exception e) {
				return "";
			}
		} else {
			return "";
		}
	}

	public void moveCreator(BossIndicatorCreator creator, int amount) {
		panel.moveCreator(creator, amount);
	}

	private static final class HealthBarTarget {
		private final String bossName;
		private final boolean theatreOfBlood;
		private final Widget barWidget;

		private HealthBarTarget(String bossName, boolean theatreOfBlood, Widget barWidget) {
			this.bossName = bossName;
			this.theatreOfBlood = theatreOfBlood;
			this.barWidget = barWidget;
		}
	}
}
