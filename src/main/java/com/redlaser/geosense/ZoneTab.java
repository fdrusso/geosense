/**
 * ZoneTab.java
 * 
 * Copyright (c) 2013 eBay Software Foundation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.redlaser.geosense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * Access wrapper for public domain zone.tab file
 *
 * @author Frank D. Russo
 */
public class ZoneTab {
	private Map<String, List<TimeZone>> tzByCountry;
	private Map<TimeZone, List<String>> countryByTz;
	private Map<TimeZone, TimeZone> tzLinks;

	public ZoneTab(InputStream zoneTabIn, InputStream tzLinksIn) throws IOException {
		BufferedReader linksReader = new BufferedReader(new InputStreamReader(tzLinksIn));
		String line;
		tzLinks = new HashMap<TimeZone, TimeZone>();
		while ((line = linksReader.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			String[] cols = line.split("\\s+");
			if (cols.length >= 3) {
				TimeZone tz = TimeZone.getTimeZone(cols[1]);
				TimeZone linkedTz = TimeZone.getTimeZone(cols[2]);
				tzLinks.put(linkedTz, tz);
			}
		}
		tzByCountry = new HashMap<String, List<TimeZone>>();
		countryByTz = new HashMap<TimeZone, List<String>>();
		HashMap<String, TimeZone> mainTzByCountry = new HashMap<String, TimeZone>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(zoneTabIn));
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}

			String[] cols = line.split("\t");
			if (cols.length >= 3) {
				String[] zoneCountriesList = cols[0].split(",");
				for (int i = 0; i < zoneCountriesList.length; i++) {
					String country = zoneCountriesList[i];
					String tzName = cols[2];
					TimeZone tz = TimeZone.getTimeZone(tzName);

					List<TimeZone> tzs = tzByCountry.get(country);
					if (tzs == null) {
						tzs = new ArrayList<TimeZone>();
						tzByCountry.put(country, tzs);
					}
					if (i == 0 && !mainTzByCountry.containsKey(country)) {
						mainTzByCountry.put(country, tz);
					} else {
						tzs.add(tz);
					}

					List<String> countries = countryByTz.get(tz);
					if (countries == null) {
						countries = new ArrayList<String>();
						countryByTz.put(tz, countries);
					}
					countries.add(country);
				}
			}
		}

		for (Entry<String, TimeZone> entry : mainTzByCountry.entrySet()) {
			tzByCountry.get(entry.getKey()).add(0, entry.getValue());
		}
	}

	public List<TimeZone> getTimeZones(String country) {
		return tzByCountry.get(country);
	}

	public TimeZone getATimeZone(String country) {
		if (!tzByCountry.containsKey(country))
			return null;

		return tzByCountry.get(country).get(0);
	}

	public List<TimeZone> getTimeZones(Locale locale) {
		return getTimeZones(locale.getCountry());
	}

	public TimeZone getATimeZone(Locale locale) {
		return getATimeZone(locale.getCountry());
	}

	public List<String> getCountries(TimeZone tz) {
		if (tzLinks.containsKey(tz)) {
			return getCountries(tzLinks.get(tz));
		}
		return countryByTz.get(tz);
	}

	public String getACountry(TimeZone tz) {
		if (!countryByTz.containsKey(tz)) {
			if (tzLinks.containsKey(tz)) {
				return getACountry(tzLinks.get(tz));
			}
			return null;
		}

		return countryByTz.get(tz).get(0);
	}

	public Collection<String> getCountries() {
		return tzByCountry.keySet();
	}

	public Collection<TimeZone> getTimeZones() {
		return countryByTz.keySet();
	}
}
