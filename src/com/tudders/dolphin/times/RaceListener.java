package com.tudders.dolphin.times;

import java.util.EventListener;

public interface RaceListener extends EventListener {
	void selectRaceEvent(Race race);
	void clearRaceEvent();
}
