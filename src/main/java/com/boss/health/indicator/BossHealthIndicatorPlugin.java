package com.boss.health.indicator;

import javax.inject.Inject;

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
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.*;

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

	private static final String CONFIG_GROUP = "bosshealthindicators";
	private static final String CONFIG_KEY = "indicators";

	private NavigationButton navButton;
	private BossHealthIndicatorPanel panel;

	private class HealthBarIndicator {
		private double percentage;
		private int color;

		public HealthBarIndicator(double percentage, int color) {
			this.percentage = percentage;
			this.color = color;
		}
	}

	private class HealthBarData {
		private String bossName;
		private List<HealthBarIndicator> entries;

		public HealthBarData(String bossName, List<HealthBarIndicator> entries) {
			this.bossName = bossName;
			this.entries = entries;
		}

		public String getBossName() {
			return bossName;
		}

		public List<HealthBarIndicator> getEntries() {
			return entries;
		}
	}

	private List<HealthBarData> bossDatabase;
	Map<String, HealthBarData> mapping;

	List<Widget> activeBars;
	HealthBarData activeBoss;

	@Override
	protected void startUp() throws Exception
	{
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/bosshealthindicator_icon.png");
		panel = new BossHealthIndicatorPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("qwe")
			.icon(icon)
			.priority(3)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		bossDatabase = new ArrayList<HealthBarData>();
		//saveToConfig();
		loadFromConfig();
		createDummyData();
		makeDatabaseMap();

		activeBars = new ArrayList<Widget>();
		activeBoss = null;
	}

	void createDummyData() {
		bossDatabase = new ArrayList<HealthBarData>();
		bossDatabase.add(
			new HealthBarData(
		"Galvek",
				Arrays.asList(
				new HealthBarIndicator[] {
					new HealthBarIndicator(0.25, 0x0000FF),
					new HealthBarIndicator(0.50, 0x00FF00),
					new HealthBarIndicator(0.75, 0xFF0000)
				})));
		bossDatabase.add(
			new HealthBarData(
	"Vorkath",
			Arrays.asList(
			new HealthBarIndicator[] {
				new HealthBarIndicator(0.33, 0xFFFFFF),
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
					HealthBarData data = mapping.get(text);
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
		for(HealthBarIndicator indicator : activeBoss.getEntries()) {
			Widget bar = createBarWidget(parent, indicator.color, indicator.percentage);
			activeBars.add(bar);
		}
	}

	Widget createBarWidget(Widget parent, int color, double percent) {
		Widget bar = parent.createChild(3);
		bar.setOriginalWidth(2);
		bar.setWidthMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalHeight(parent.getOriginalHeight());
		bar.setHeightMode(WidgetSizeMode.ABSOLUTE);

		bar.setOriginalX((int) (parent.getWidth() * percent));
		bar.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);

		bar.setTextColor(color);
		bar.setOpacity(127);

		bar.revalidate();

		return bar;
	}

	void loadFromConfig() {
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		Type type = new TypeToken<List<HealthBarData>>(){}.getType();
		System.out.println("Loading config: " + json);
		bossDatabase = gson.fromJson(json, type);
	}

	void saveToConfig() {
		configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
		String json = gson.toJson(bossDatabase);
		System.out.println("Writing to config: " + json);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	void makeDatabaseMap() {
		mapping = new HashMap<String, HealthBarData>();
		for(HealthBarData data : bossDatabase) {
			mapping.put(data.getBossName(), data);
		}
	}
}
