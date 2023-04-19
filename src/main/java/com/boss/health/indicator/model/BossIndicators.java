package com.boss.health.indicator.model;

import java.util.List;

public class BossIndicators {
    private String bossName;
    private List<Indicator> entries;

    public BossIndicators(String bossName, List<Indicator> entries) {
        this.bossName = bossName;
        this.entries = entries;
    }

    public String getBossName() {
        return bossName;
    }

    public List<Indicator> getEntries() {
        return entries;
    }
}
