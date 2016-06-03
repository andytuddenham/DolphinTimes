package com.tudders.dolphin.times;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Race {
	private String race;
	private String event;
	private String heat;
	private StringBuilder fileData = new StringBuilder();
	private List<Result> results;
	private Pattern pattern = Pattern.compile("^(\\d{1,2});([0-9.]+);([^;]*);([^;]*)$");
	private static final Logger logger = Logger.getLogger(MeetPanel.class.getName());

	// TODO implement more logging 

	public Race(File file) {
		logger.setLevel(Application.getLoggingLevel(Race.class.getName()));
		this.race = DolphinFile.getRaceFromFile(file);
		this.event = DolphinFile.getEventFromFile(file);
		this.heat = DolphinFile.getHeatFromFile(file);
		logger.fine("file="+file.getAbsolutePath());
		results = new ArrayList<Result>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null) {
				fileData.append(line+"\n");
				Matcher matcher = pattern.matcher(line);
				boolean matched = matcher.matches();
				logger.finer("line: '"+line+"' matches="+matched);
				if (matched) {
					Integer lane = Integer.valueOf(matcher.group(1));
					String time = matcher.group(2);
					if (lane == 0 && "0".equals(time) && "1".equals(matcher.group(3)) && "A".equals(matcher.group(4))) {
						logger.finest("eliminating '0;0;1;A'");
					} else {
						Result result = new Result(lane, time);
						results.add(result);
						logger.finest("Added result [lane "+result.getLaneNumber()+" : "+result.getTime()+"]");
					}
				} else {
					logger.finest("line not matched");
				}
			}
			if (!results.isEmpty()) {
				Collections.sort(results, new ResultComparator());
			}
		} catch (FileNotFoundException fnfe) {
			if (results!= null){
				results.clear();
			}
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			if (results!= null){
				results.clear();
			}
			ioe.printStackTrace();
		} finally {
			if (br != null) try {br.close();} catch(IOException ioe) {}
		}
	}

	public String getRaceNumber() {
		return race;
	}

	public String getEventNumber() {
		return event;
	}

	public String getHeatNumber() {
		return heat;
	}

	public String getFileData() {
		return fileData.toString();
	}

	public List<Result> getRaceResults() {
		return results;
	}

	public boolean isValid() {
		return fileData.length() > 0 && !results.isEmpty();
	}

	public class Result {
		private Integer laneNumber;
		private String time;
		Result(Integer laneNumber, String time) {
			this.laneNumber = laneNumber;
			this.time = time;
		}
		public Integer getLaneNumber() {
			return laneNumber;
		}
		public String getTime() {
			return time;
		}
	}

	private class ResultComparator implements Comparator<Result> {
		@Override
		public int compare(Result a, Result b) {
			// sort in time increasing order
			return Double.valueOf(a.getTime()).compareTo(Double.valueOf(b.getTime()));
		}		
	}
}
