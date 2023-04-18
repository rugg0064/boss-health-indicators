package com.boss.health.indicator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BossHealthIndicatorTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(com.boss.health.indicator.BossHealthIndicatorPlugin.class);
		RuneLite.main(args);
	}
}