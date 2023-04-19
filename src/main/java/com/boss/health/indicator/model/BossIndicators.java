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

    public boolean hasAnyNull() {
        boolean hasNull = false;
        for(int i = 0; i < entries.size() && !hasNull; i++) {
            if(entries.get(i).hasAnyNull()) {
                hasNull = true;
            }
        }
        return (bossName == null) || hasNull;
    }
}
