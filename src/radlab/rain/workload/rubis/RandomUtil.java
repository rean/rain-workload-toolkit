/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
/*
Modifications by: Rean Griffith 
1) Changed package to include file in Rain harness
*/

//package org.apache.olio.workload.util;
package radlab.rain.workload.rubis;

//import com.sun.faban.driver.util.Random;
import radlab.rain.workload.rubis.Random;

import java.io.FileReader;
import java.util.HashSet;
import java.util.logging.Logger;

public class RandomUtil {

    public static final String[] TIMEZONES = { "ACT", "AET", "AGT", "ART",
        "AST", "Africa/Abidjan", "Africa/Accra", "Africa/Addis_Ababa",
        "Africa/Algiers", "Africa/Asmera", "Africa/Bamako", "Africa/Bangui",
        "Africa/Banjul", "Africa/Bissau", "Africa/Blantyre",
        "Africa/Brazzaville", "Africa/Bujumbura", "Africa/Cairo",
        "Africa/Casablanca", "Africa/Ceuta", "Africa/Conakry", "Africa/Dakar",
        "Africa/Dar_es_Salaam", "Africa/Djibouti", "Africa/Douala",
        "Africa/El_Aaiun", "Africa/Freetown", "Africa/Gaborone",
        "Africa/Harare", "Africa/Johannesburg", "Africa/Kampala",
        "Africa/Khartoum", "Africa/Kigali", "Africa/Kinshasa", "Africa/Lagos",
        "Africa/Libreville", "Africa/Lome", "Africa/Luanda",
        "Africa/Lubumbashi", "Africa/Lusaka", "Africa/Malabo", "Africa/Maputo",
        "Africa/Maseru", "Africa/Mbabane", "Africa/Mogadishu",
        "Africa/Monrovia", "Africa/Nairobi", "Africa/Ndjamena", "Africa/Niamey",
        "Africa/Nouakchott", "Africa/Ouagadougou", "Africa/Porto-Novo",
        "Africa/Sao_Tome", "Africa/Timbuktu", "Africa/Tripoli", "Africa/Tunis",
        "Africa/Windhoek", "America/Adak", "America/Anchorage",
        "America/Anguilla", "America/Antigua", "America/Araguaina",
        "America/Argentina/Buenos_Aires", "America/Argentina/Catamarca",
        "America/Argentina/ComodRivadavia", "America/Argentina/Cordoba",
        "America/Argentina/Jujuy", "America/Argentina/La_Rioja",
        "America/Argentina/Mendoza", "America/Argentina/Rio_Gallegos",
        "America/Argentina/San_Juan", "America/Argentina/Tucuman",
        "America/Argentina/Ushuaia", "America/Aruba", "America/Asuncion",
        "America/Atikokan", "America/Atka", "America/Bahia", "America/Barbados",
        "America/Belem", "America/Belize", "America/Blanc-Sablon",
        "America/Boa_Vista", "America/Bogota", "America/Boise",
        "America/Buenos_Aires", "America/Cambridge_Bay", "America/Campo_Grande",
        "America/Cancun", "America/Caracas", "America/Catamarca",
        "America/Cayenne", "America/Cayman", "America/Chicago",
        "America/Chihuahua", "America/Coral_Harbour", "America/Cordoba",
        "America/Costa_Rica", "America/Cuiaba", "America/Curacao",
        "America/Danmarkshavn", "America/Dawson", "America/Dawson_Creek",
        "America/Denver", "America/Detroit", "America/Dominica",
        "America/Edmonton", "America/Eirunepe", "America/El_Salvador",
        "America/Ensenada", "America/Fort_Wayne", "America/Fortaleza",
        "America/Glace_Bay", "America/Godthab", "America/Goose_Bay",
        "America/Grand_Turk", "America/Grenada", "America/Guadeloupe",
        "America/Guatemala", "America/Guayaquil", "America/Guyana",
        "America/Halifax", "America/Havana", "America/Hermosillo",
        "America/Indiana/Indianapolis", "America/Indiana/Knox",
        "America/Indiana/Marengo", "America/Indiana/Petersburg",
        "America/Indiana/Vevay", "America/Indiana/Vincennes",
        "America/Indianapolis", "America/Inuvik", "America/Iqaluit",
        "America/Jamaica", "America/Jujuy", "America/Juneau",
        "America/Kentucky/Louisville", "America/Kentucky/Monticello",
        "America/Knox_IN", "America/La_Paz", "America/Lima",
        "America/Los_Angeles", "America/Louisville", "America/Maceio",
        "America/Managua", "America/Manaus", "America/Martinique",
        "America/Mazatlan", "America/Mendoza", "America/Menominee",
        "America/Merida", "America/Mexico_City", "America/Miquelon",
        "America/Moncton", "America/Monterrey", "America/Montevideo",
        "America/Montreal", "America/Montserrat", "America/Nassau",
        "America/New_York", "America/Nipigon", "America/Nome",
        "America/Noronha", "America/North_Dakota/Center",
        "America/North_Dakota/New_Salem", "America/Panama",
        "America/Pangnirtung", "America/Paramaribo", "America/Phoenix",
        "America/Port-au-Prince", "America/Port_of_Spain", "America/Porto_Acre",
        "America/Porto_Velho", "America/Puerto_Rico", "America/Rainy_River",
        "America/Rankin_Inlet", "America/Recife", "America/Regina",
        "America/Rio_Branco", "America/Rosario", "America/Santiago",
        "America/Santo_Domingo", "America/Sao_Paulo", "America/Scoresbysund",
        "America/Shiprock", "America/St_Johns", "America/St_Kitts",
        "America/St_Lucia", "America/St_Thomas", "America/St_Vincent",
        "America/Swift_Current", "America/Tegucigalpa", "America/Thule",
        "America/Thunder_Bay", "America/Tijuana", "America/Toronto",
        "America/Tortola", "America/Vancouver", "America/Virgin",
        "America/Whitehorse", "America/Winnipeg", "America/Yakutat",
        "America/Yellowknife", "Antarctica/Casey", "Antarctica/Davis",
        "Antarctica/DumontDUrville", "Antarctica/Mawson", "Antarctica/McMurdo",
        "Antarctica/Palmer", "Antarctica/Rothera", "Antarctica/South_Pole",
        "Antarctica/Syowa", "Antarctica/Vostok", "Arctic/Longyearbyen",
        "Asia/Aden", "Asia/Almaty", "Asia/Amman", "Asia/Anadyr", "Asia/Aqtau",
        "Asia/Aqtobe", "Asia/Ashgabat", "Asia/Ashkhabad", "Asia/Baghdad",
        "Asia/Bahrain", "Asia/Baku", "Asia/Bangkok", "Asia/Beirut",
        "Asia/Bishkek", "Asia/Brunei", "Asia/Calcutta", "Asia/Choibalsan",
        "Asia/Chongqing", "Asia/Chungking", "Asia/Colombo", "Asia/Dacca",
        "Asia/Damascus", "Asia/Dhaka", "Asia/Dili", "Asia/Dubai",
        "Asia/Dushanbe", "Asia/Gaza", "Asia/Harbin", "Asia/Hong_Kong",
        "Asia/Hovd", "Asia/Irkutsk", "Asia/Istanbul", "Asia/Jakarta",
        "Asia/Jayapura", "Asia/Jerusalem", "Asia/Kabul", "Asia/Kamchatka",
        "Asia/Karachi", "Asia/Kashgar", "Asia/Katmandu", "Asia/Krasnoyarsk",
        "Asia/Kuala_Lumpur", "Asia/Kuching", "Asia/Kuwait", "Asia/Macao",
        "Asia/Macau", "Asia/Magadan", "Asia/Makassar", "Asia/Manila",
        "Asia/Muscat", "Asia/Nicosia", "Asia/Novosibirsk", "Asia/Omsk",
        "Asia/Oral", "Asia/Phnom_Penh", "Asia/Pontianak", "Asia/Pyongyang",
        "Asia/Qatar", "Asia/Qyzylorda", "Asia/Rangoon", "Asia/Riyadh",
        "Asia/Riyadh87", "Asia/Riyadh88", "Asia/Riyadh89", "Asia/Saigon",
        "Asia/Sakhalin", "Asia/Samarkand", "Asia/Seoul", "Asia/Shanghai",
        "Asia/Singapore", "Asia/Taipei", "Asia/Tashkent", "Asia/Tbilisi",
        "Asia/Tehran", "Asia/Tel_Aviv", "Asia/Thimbu", "Asia/Thimphu",
        "Asia/Tokyo", "Asia/Ujung_Pandang", "Asia/Ulaanbaatar",
        "Asia/Ulan_Bator", "Asia/Urumqi", "Asia/Vientiane", "Asia/Vladivostok",
        "Asia/Yakutsk", "Asia/Yekaterinburg", "Asia/Yerevan", "Atlantic/Azores",
        "Atlantic/Bermuda", "Atlantic/Canary", "Atlantic/Cape_Verde",
        "Atlantic/Faeroe", "Atlantic/Jan_Mayen", "Atlantic/Madeira",
        "Atlantic/Reykjavik", "Atlantic/South_Georgia", "Atlantic/St_Helena",
        "Atlantic/Stanley", "Australia/ACT", "Australia/Adelaide",
        "Australia/Brisbane", "Australia/Broken_Hill", "Australia/Canberra",
        "Australia/Currie", "Australia/Darwin", "Australia/Hobart",
        "Australia/LHI", "Australia/Lindeman", "Australia/Lord_Howe",
        "Australia/Melbourne", "Australia/NSW", "Australia/North",
        "Australia/Perth", "Australia/Queensland", "Australia/South",
        "Australia/Sydney", "Australia/Tasmania", "Australia/Victoria",
        "Australia/West", "Australia/Yancowinna", "BET", "BST", "Brazil/Acre",
        "Brazil/DeNoronha", "Brazil/East", "Brazil/West", "CAT", "CET", "CNT",
        "CST", "CST6CDT", "CTT", "Canada/Atlantic", "Canada/Central",
        "Canada/East-Saskatchewan", "Canada/Eastern", "Canada/Mountain",
        "Canada/Newfoundland", "Canada/Pacific", "Canada/Saskatchewan",
        "Canada/Yukon", "Chile/Continental", "Chile/EasterIsland", "Cuba",
        "EAT", "ECT", "EET", "EST", "EST5EDT", "Egypt", "Eire", "Etc/GMT",
        "Etc/GMT+0", "Etc/GMT+1", "Etc/GMT+10", "Etc/GMT+11", "Etc/GMT+12",
        "Etc/GMT+2", "Etc/GMT+3", "Etc/GMT+4", "Etc/GMT+5", "Etc/GMT+6",
        "Etc/GMT+7", "Etc/GMT+8", "Etc/GMT+9", "Etc/GMT-0", "Etc/GMT-1",
        "Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12", "Etc/GMT-13", "Etc/GMT-14",
        "Etc/GMT-2", "Etc/GMT-3", "Etc/GMT-4", "Etc/GMT-5", "Etc/GMT-6",
        "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT0", "Etc/Greenwich",
        "Etc/UCT", "Etc/UTC", "Etc/Universal", "Etc/Zulu", "Europe/Amsterdam",
        "Europe/Andorra", "Europe/Athens", "Europe/Belfast", "Europe/Belgrade",
        "Europe/Berlin", "Europe/Bratislava", "Europe/Brussels",
        "Europe/Bucharest", "Europe/Budapest", "Europe/Chisinau",
        "Europe/Copenhagen", "Europe/Dublin", "Europe/Gibraltar",
        "Europe/Guernsey", "Europe/Helsinki", "Europe/Isle_of_Man",
        "Europe/Istanbul", "Europe/Jersey", "Europe/Kaliningrad", "Europe/Kiev",
        "Europe/Lisbon", "Europe/Ljubljana", "Europe/London",
        "Europe/Luxembourg", "Europe/Madrid", "Europe/Malta",
        "Europe/Mariehamn", "Europe/Minsk", "Europe/Monaco", "Europe/Moscow",
        "Europe/Nicosia", "Europe/Oslo", "Europe/Paris", "Europe/Prague",
        "Europe/Riga", "Europe/Rome", "Europe/Samara", "Europe/San_Marino",
        "Europe/Sarajevo", "Europe/Simferopol", "Europe/Skopje", "Europe/Sofia",
        "Europe/Stockholm", "Europe/Tallinn", "Europe/Tirane",
        "Europe/Tiraspol", "Europe/Uzhgorod", "Europe/Vaduz", "Europe/Vatican",
        "Europe/Vienna", "Europe/Vilnius", "Europe/Volgograd", "Europe/Warsaw",
        "Europe/Zagreb", "Europe/Zaporozhye", "Europe/Zurich", "GB", "GB-Eire",
        "GMT", "GMT0", "Greenwich", "HST", "Hongkong", "IET", "IST", "Iceland",
        "Indian/Antananarivo", "Indian/Chagos", "Indian/Christmas",
        "Indian/Cocos", "Indian/Comoro", "Indian/Kerguelen", "Indian/Mahe",
        "Indian/Maldives", "Indian/Mauritius", "Indian/Mayotte",
        "Indian/Reunion", "Iran", "Israel", "JST", "Jamaica", "Japan",
        "Kwajalein", "Libya", "MET", "MIT", "MST", "MST7MDT",
        "Mexico/BajaNorte", "Mexico/BajaSur", "Mexico/General",
        "Mideast/Riyadh87", "Mideast/Riyadh88", "Mideast/Riyadh89", "NET",
        "NST", "NZ", "NZ-CHAT", "Navajo", "PLT", "PNT", "PRC", "PRT", "PST",
        "PST8PDT", "Pacific/Apia", "Pacific/Auckland", "Pacific/Chatham",
        "Pacific/Easter", "Pacific/Efate", "Pacific/Enderbury",
        "Pacific/Fakaofo", "Pacific/Fiji", "Pacific/Funafuti",
        "Pacific/Galapagos", "Pacific/Gambier", "Pacific/Guadalcanal",
        "Pacific/Guam", "Pacific/Honolulu", "Pacific/Johnston",
        "Pacific/Kiritimati", "Pacific/Kosrae", "Pacific/Kwajalein",
        "Pacific/Majuro", "Pacific/Marquesas", "Pacific/Midway",
        "Pacific/Nauru", "Pacific/Niue", "Pacific/Norfolk", "Pacific/Noumea",
        "Pacific/Pago_Pago", "Pacific/Palau", "Pacific/Pitcairn",
        "Pacific/Ponape", "Pacific/Port_Moresby", "Pacific/Rarotonga",
        "Pacific/Saipan", "Pacific/Samoa", "Pacific/Tahiti", "Pacific/Tarawa",
        "Pacific/Tongatapu", "Pacific/Truk", "Pacific/Wake", "Pacific/Wallis",
        "Pacific/Yap", "Poland", "Portugal", "ROK", "SST", "Singapore",
        "SystemV/AST4", "SystemV/AST4ADT", "SystemV/CST6", "SystemV/CST6CDT",
        "SystemV/EST5", "SystemV/EST5EDT", "SystemV/HST10", "SystemV/MST7",
        "SystemV/MST7MDT", "SystemV/PST8", "SystemV/PST8PDT", "SystemV/YST9",
        "SystemV/YST9YDT", "Turkey", "UCT", "US/Alaska", "US/Aleutian",
        "US/Arizona", "US/Central", "US/East-Indiana", "US/Eastern",
        "US/Hawaii", "US/Indiana-Starke", "US/Michigan", "US/Mountain",
        "US/Pacific", "US/Pacific-New", "US/Samoa", "UTC", "Universal", "VST",
        "W-SU", "WET", "Zulu"};


    // Phone is 0018889990000 or 0077669990000 for non-US (no spaces for Rails app)
    // 50% of the time, do US, 50% non-us.
    public static String randomPhone(Random r, StringBuilder b) {
        String v = r.makeNString(1, 2);
        if (v.length() == 1) {
            b.append("001"); // removed space
            v = r.makeNString(3, 3);
            b.append(v); // removed space
        } else {
            b.append("00").append(v);
            v = r.makeNString(2, 2);
            b.append(v); //removed spaces
        }
        v = r.makeNString(3, 3);
        b.append(v); // removed space
        v = r.makeNString(4, 4);
        b.append(v);
        return b.toString();
    }

    public static String randomTimeZone(Random r) {
        return TIMEZONES[r.random(0, TIMEZONES.length - 1)];
    }

    public static StringBuilder randomName(Random r, StringBuilder b,
                                           int minLength, int maxLength) {
        if (minLength < 1 || maxLength < minLength)
            throw new IllegalArgumentException();
        b.append(r.makeCString(1, 1).toUpperCase());
        b.append(r.makeCString(minLength - 1, maxLength - 1).toLowerCase());
        return b;
    }

    /**
     * From http://tagsonomy.com/index.php/dynamic-growth-of-tag-clouds/

     * "It took only 10 users (not quite 1.5% of the current total) before
     * the top 3 tags were tagging ontology folksonomy, conveying much
     * the same sense, with only the use of tagging instead of tags
     * making this different from the current set of 3."<br>
     *
     * In order to achieve a high concentration on the first tags added
     * to the system, we use a negative exponential distribution to select
     * the tags.
     *
     * @param r The random value generator
     * @param meanRatio The point of mean in the range from 0 to 1
     * @return The randomly selected tag id starting with 1, with a
     *         high probability of the first tags being selected.
     */
    public static int randomTagId(Random r, double meanRatio) {
        Logger logger = Logger.getLogger(RandomUtil.class.getName());
        double mean = ScaleFactors.tags * meanRatio;

        int selected;
        int loops = 0;

        do {
            double x = r.drandom(0.0, 1.0);
            if (x == 0)
                x = 0.05;

            // We use a negative exponential distribution to select the tags.
            // The first tags tend to be the most often used.
            selected = (int)(mean * -Math.log(x));

            // if (selected >= ScaleFactors.tagCount)
            //    logger.warning("Selected: " + selected);

            // However, if we exceed the limit, we do not select the last one.
            // We redo the selection instead.
        } while (selected >= ScaleFactors.tags && loops++ < 10);

        if (loops >= 10)
            logger.severe("Exceeded loop limit. Selected:" +
                            selected + " TagCount: " + ScaleFactors.tags);

        // We use the user name mechanism to create the tag names
        return ++selected;
    }

    /**
     * Returns a random tag name. Implicitly calls randomTagId.
     * @param r The random value generator
     * @return The randomly selected tag name.
     */
    public static String randomTagName(Random r) {
        return UserName.getUserName(r.random(1, ScaleFactors.tags));
        //return UserName.getUserName(randomTagId(r, 0.1));
    }

    /**
     * Randomly creates text between length x and y, inclusive that is
     * separated by spaces. The words are between 1 and 12 characters long.
     */
    public static String randomText(Random r, int x, int y) {
        int length = r.random(x, y);
        StringBuilder buffer = new StringBuilder(length);
        int leftover = length;
        while (leftover > 0) {
            String word = r.makeCString(1, leftover < 12 ? leftover : 12);
            buffer.append(word).append(' ');
            leftover -= word.length() + 1;
        }
        return buffer.toString();
    }

    /**
     * Randomly selects an event from the events page.
     * @param r The random value generator
     * @param eventListPage The page from the response buffer
     * @return The selected event id, as a string
     */
    public static String randomEvent(Random r, StringBuilder eventListPage) {
        String search1 = "<a href=\"/events/";
        int idx = 0;
        HashSet<String> eventIdSet = new HashSet<String>();
        String eventItem = "";
        for (;;) {
            idx = eventListPage.indexOf(search1, idx);
            if (idx == -1)
                break;
            // We skip this the php or jsp, just knowing it is 3 chars
            idx += search1.length();
            int endIdx = eventListPage.indexOf("\"", idx);
            if (endIdx == -1)
                break;

            eventItem = eventListPage.substring(idx, endIdx).trim();
            if (!eventItem.contains("tagged") && !eventItem.contains("new") )
            eventIdSet.add(eventListPage.substring(idx, endIdx).trim());
            idx = endIdx;
        }
        int size = eventIdSet.size();
        if (size == 0)
            return null;

        String[] eventIds = new String[size];
        eventIds = eventIdSet.toArray(eventIds);
        return eventIds[r.random(0, size - 1)];
    }

    // Main for testing RandomEvent.
    public static void main(String[] args) throws Exception {
        FileReader reader = new FileReader(args[0]);
        StringBuilder page = new StringBuilder();
        char[] readBuffer = new char[8192];
        int readSize;
        while ((readSize = reader.read(readBuffer, 0, readBuffer.length))
                != -1)
            page.append(readBuffer, 0, readSize);
        Random r = new Random();
        String selected = randomEvent(r, page);
        System.out.println("Selected: " + selected);
    }
}
