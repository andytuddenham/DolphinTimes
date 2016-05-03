package com.tudders.dolphin.times;

import java.util.EventListener;

public interface MeetListener extends EventListener {
	void selectMeetEvent(String meet);
	void clearMeetEvent();
}
