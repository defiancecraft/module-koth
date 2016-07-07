package com.defiancecraft.modules.koth.utils;

import java.time.Duration;

public class DurationFormatter {

	/**
	 * Crudely formats a duration to H:M:S (no padding)
	 * @param dur Duration
	 * @return Formatted string
	 */
	public static String formatDuration(Duration dur) {
		long hours = dur.getSeconds() / (60 * 60);
		long minutes = (dur.getSeconds() % (60 * 60)) / 60;
		long seconds = (dur.getSeconds() % 60);
		
		StringBuilder builder = new StringBuilder();
		if (hours > 0)
			builder.append(hours)
			       .append(":");
		
		if (minutes > 0)
			builder.append(minutes)
				   .append(":");
		
		builder.append(seconds);
		
		return builder.toString();
	}
	
}
