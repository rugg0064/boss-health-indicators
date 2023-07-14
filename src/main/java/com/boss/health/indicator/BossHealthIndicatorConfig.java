package com.boss.health.indicator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bosshealthindicator")
public interface BossHealthIndicatorConfig extends Config {
    @ConfigItem(
            keyName = "showPanel",
            name = "Show panel",
            description = "Enable or disable the side panel",
            position = 0
    )
    default boolean showPanel() { return true; }
}
