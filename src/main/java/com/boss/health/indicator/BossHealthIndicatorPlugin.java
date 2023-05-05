package com.boss.health.indicator;

import javax.inject.Inject;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.model.Indicator;
import com.boss.health.indicator.ui.BossIndicatorCreator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
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

	private static final String CONFIG_GROUP = "bosshealthindicators";
	private static final String CONFIG_KEY = "indicators";

	private NavigationButton navButton;
	private BossHealthIndicatorPanel panel;

	public ColorPickerManager getColorPickerManager() {
		return colorPickerManager;
	}

	private List<BossIndicators> bossDatabase;
	Map<String, BossIndicators> mapping;

	List<Widget> activeBars;
	BossIndicators activeBoss;

	@Override
	protected void startUp() throws Exception
	{
		loadFromConfig();
		makeDatabaseMap();

		activeBars = new ArrayList<Widget>();
		activeBoss = null;

		// Set up side panel
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/bosshealthindicator_icon.png");
		panel = new BossHealthIndicatorPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Boss Health Indicators")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
	}

	void createDummyData() {
		bossDatabase = new ArrayList<BossIndicators>();
		bossDatabase.add(
			new BossIndicators(
		"Galvek",
				Arrays.asList(
				new Indicator[] {
					new Indicator(0.25, Color.BLUE),
					new Indicator(0.50, Color.GREEN),
					new Indicator(0.75, Color.RED)
				})));
		bossDatabase.add(
			new BossIndicators(
	"Vorkath",
			Arrays.asList(
			new Indicator[] {
				new Indicator(0.33, Color.WHITE),
			})));
		bossDatabase.add(
				new BossIndicators(
						"Ba-Ba",
						Arrays.asList(
								new Indicator[] {
										new Indicator(0.33, Color.WHITE),
										new Indicator(0.66, Color.WHITE),
								})));
		bossDatabase.add(
				new BossIndicators(
						"Akkha",
						Arrays.asList(
								new Indicator[] {
										new Indicator(0.8, Color.BLUE),
										new Indicator(0.6, Color.BLUE),
										new Indicator(0.4, Color.BLUE),
										new Indicator(0.2, Color.BLUE),
								})));
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		activeBoss = null;
		clientThread.invoke(() -> clearBars());
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Widget healthBarTextWidget = client.getWidget(303, 9);
		// TODO: this might not be necessary
		if(healthBarTextWidget != null) {
			String text = healthBarTextWidget.getText();
			// TODO: this will loop on null over and over again
			if((activeBoss == null || !activeBoss.getBossName().equals(text))) {
				clearBars();
				if(mapping.containsKey(text)) {
					BossIndicators data = mapping.get(text);
					activeBoss = data;
					createBars();
				} else {
					activeBoss = null;
				}
			}
		}
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
	void createBars() {
		//Widget parent = client.getWidget(303, 10);
		Widget parent = client.getWidget(303, 12);
		int height = client.getWidget(303, 10).getOriginalHeight();

		for(Indicator indicator : activeBoss.getEntries()) {
			Widget bar = createBarWidget(parent, indicator.getColor(), indicator.getPercentage(), height);
			activeBars.add(bar);
		}
	}

	// Creates a bar widget, does not add to parent
	private Widget createBarWidget(Widget parent, Color color, double percent, int height) {
		Widget bar = parent.createChild(WidgetType.RECTANGLE);
		bar.setOriginalWidth(2);
		bar.setWidthMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalHeight(height);
		bar.setHeightMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalX((int) (parent.getWidth() * percent));
		bar.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);

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
}