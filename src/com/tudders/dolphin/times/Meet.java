package com.tudders.dolphin.times;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Meet {
	private String name;
	private Date date;
	private List<Race> raceList = Collections.synchronizedList(new ArrayList<Race>());

	Meet(String name, Date modifiedDate) {
		this.name = name;
		this.date = modifiedDate;
	}

	public String getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public void add(Race newRace) {
		raceList.add(newRace);
	}

	public void remove(Race race) {
		raceList.remove(race);
	}

	public int getRaceCount() {
		return raceList.size();
	}

	public List<Race> getRaceList() {
		return new ArrayList<Race>(raceList);
	}
}
