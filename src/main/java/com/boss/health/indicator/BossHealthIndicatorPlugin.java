package com.boss.health.indicator;

import javax.inject.Inject;

import com.boss.health.indicator.model.BossIndicators;
import com.boss.health.indicator.model.Indicator;
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
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Boss Health Indicators",
	description = "",
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
		bossDatabase = new ArrayList<BossIndicators>();
		//saveToConfig();
		//loadFromConfig();
		createDummyData();
		makeDatabaseMap();

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/bosshealthindicator_icon.png");
		panel = new BossHealthIndicatorPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Boss Health Indicators")
			.icon(icon)
			.priority(3)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		activeBars = new ArrayList<Widget>();
		activeBoss = null;
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

	void clearBars() {
		for(Widget widget : activeBars) {
			widget.setHidden(true);
			widget.revalidate();
		}
		activeBars.clear();
	}

	void createBars() {
		Widget parent = client.getWidget(303, 10);
		int i = 0;
		for(Indicator indicator : activeBoss.getEntries()) {
			Widget bar = createBarWidget(parent, indicator.getColor(), indicator.getPercentage());
			activeBars.add(bar);
		}
	}

	private Widget createBarWidget(Widget parent, Color color, double percent) {
		Widget bar = parent.createChild(WidgetType.RECTANGLE);
		bar.setOriginalWidth(2);
		bar.setWidthMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalHeight(parent.getOriginalHeight());
		bar.setHeightMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalX((int) (parent.getWidth() * percent));
		bar.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);

		bar.setTextColor(color.getRGB());
		bar.setOpacity(color.getTransparency());
		//bar.setOpacity(127);

		bar.revalidate();

		return bar;
	}

	private void loadFromConfig() {
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		Type type = new TypeToken<List<BossIndicators>>(){}.getType();
		System.out.println("Loading config: " + json);
		bossDatabase = gson.fromJson(json, type);
	}

	private void saveToConfig() {
		configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
		String json = gson.toJson(bossDatabase);
		System.out.println("Writing to config: " + json);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	private void makeDatabaseMap() {
		mapping = new HashMap<String, BossIndicators>();
		for(BossIndicators data : bossDatabase) {
			mapping.put(data.getBossName(), data);
		}
	}

	public void updateFromPanel() {
		bossDatabase = panel.getBossDatabase();
		makeDatabaseMap();
		activeBoss = null;
	}

	public List<BossIndicators> getBossDatabase() {
		return bossDatabase;
	}
}
