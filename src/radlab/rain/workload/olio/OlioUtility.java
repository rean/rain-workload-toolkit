/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Shanti Subramanyam
 * Author: Rean Griffith 
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.GregorianCalendar;
import java.util.HashSet;
//import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import radlab.rain.workload.olio.model.OlioSocialEvent;
import radlab.rain.workload.olio.Random;


/**
 * Collection of utilities for the Olio workload.
 *
 * @author: Shanti Subramanyam
 * @author: Rean Griffith 
 * @author: <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class OlioUtility
{
	public static final int ANONYMOUS_PERSON_ID = -1;
	public static final int INVALID_EVENT_ID = -1;
	public static final int INVALID_OPERATION_ID = -1;
	//public static final int MILLISECS_PER_DAY = 1000*60*60*24;

	private static final char[] ALNUM_CHARS = { '0', '1', '2', '3', '4', '5',
												'6', '7', '8', '9', 'A', 'B',
												'C', 'D', 'E', 'F', 'G', 'H',
												'I', 'J', 'K', 'L', 'M', 'N',
												'O', 'P', 'Q', 'R', 'S', 'T',
												'U', 'V', 'W', 'X', 'Y', 'Z',
												'a', 'b', 'c', 'd', 'e', 'f',
												'g', 'h', 'i', 'j', 'k', 'l',
												'm', 'n', 'o', 'p', 'q', 'r',
												's', 't', 'u', 'v', 'w', 'x',
												'y', 'z'}; ///< The set of alphanumeric characters

	private static final char[] ALPHA_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F',
												'G', 'H', 'I', 'J', 'K', 'L',
												'M', 'N', 'O', 'P', 'Q', 'R',
												'S', 'T', 'U', 'V', 'W', 'X',
												'Y', 'Z', 'a', 'b', 'c', 'd',
												'e', 'f', 'g', 'h', 'i', 'j',
												'k', 'l', 'm', 'n', 'o', 'p',
												'q', 'r', 's', 't', 'u', 'v',
												'w', 'x', 'y', 'z'}; ///< The set of alphabetic characters


	private static final String[] COUNTRIES = {"Albania", "Algeria",
		"American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica",
		"Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Ascension",
		"Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain",
		"Bangladesh", "Barbados", "Belarus", "Belgium", "Belize",
		"Benin, Republic of", "Bermuda", "Bhutan", "Bolivia",
		"Bosnia and Herzegovina", "Botswana", "Brazil",
		"British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso",
		"Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde Islands",
		"Cayman Islands", "Central African Rep", "Chad Republic",
		"Chatham Island, NZ", "Chile", "China", "Christmas Island",
		"Cocos Islands", "Colombia", "Comoros", "Congo", "Cook Islands",
		"Costa Rica", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic",
		"Denmark", "Diego Garcia", "Djibouti", "Dominica", "Dominican Republic",
		"Easter Island", "Ecuador", "Egypt", "El Salvador", "Equitorial Guinea",
		"Eritrea", "Estonia", "Ethiopia", "Falkland Islands", "Faroe Islands",
		"Fiji Islands", "Finland", "France", "French Antilles", "French Guiana",
		"French Polynesia", "Gabon Republic", "Gambia", "Georgia", "Germany",
		"Ghana", "Gibraltar", "Greece", "Greenland", "Grenada and Carriacuou",
		"Grenadin Islands", "Guadeloupe", "Guam", "Guantanamo Bay", "Guatemala",
		"Guiana", "Guinea, Bissau", "Guinea, Rep", "Guyana", "Haiti",
		"Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia",
		"Inmarsat", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Ivory Coast",
		"Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati",
		"Korea, North", "Korea, South", "Kuwait", "Kyrgyzstan", "Laos",
		"Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",
		"Lithuania", "Luxembourg", "Macau", "Macedonia, FYROM", "Madagascar",
		"Malawi", "Malaysia", "Maldives", "Mali Republic", "Malta",
		"Mariana Islands", "Marshall Islands", "Martinique", "Mauritania",
		"Mauritius", "Mayotte Island", "Mexico", "Micronesia, Fed States",
		"Midway Islands", "Miquelon", "Moldova", "Monaco", "Mongolia",
		"Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru",
		"Nepal", "Neth. Antilles", "Netherlands", "Nevis", "New Caledonia",
		"New Zealand", "Nicaragua", "Niger Republic", "Nigeria", "Niue",
		"Norfolk Island", "Norway", "Oman", "Pakistan", "Palau", "Panama",
		"Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland",
		"Portugal", "Principe", "Puerto Rico", "Qatar", "Reunion Island",
		"Romania", "Russia", "Rwanda", "Saipan", "San Marino", "Sao Tome",
		"Saudi Arabia", "Senegal Republic", "Serbia, Republic of", "Seychelles",
		"Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
		"Somalia Republic", "South Africa", "Spain", "Sri Lanka", "St. Helena",
		"St. Kitts", "St. Lucia", "St. Pierre et Miquelon", "St. Vincent",
		"Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria",
		"Taiwan", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tokelau",
		"Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan",
		"Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine",
		"United Arab Emirates", "United Kingdom", "USA", "Uruguay",
		"US Virgin Islands", "Uzbekistan", "Vanuatu", "Vatican city",
		"Venezuela", "Vietnam, Soc Republic of", "Wake Island",
		"Wallis and Futuna Islands", "Western Samoa", "Yemen", "Yugoslavia",
		"Zaire", "Zambia", "Zanzibar", "Zimbabwe"};

	private static final String[] TIMEZONES = {"ACT", "AET", "AGT", "ART",
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

    private static final int MAX_NUM_TAGS = 7;

	private static final Date BASE_DATE = new Date(System.currentTimeMillis());

	private static final int MIN_PERSON_ID = 1; ///< Mininum value for person IDs
	private static final int MIN_EVENT_ID = 1; ///< Mininum value for event IDs
	private static AtomicInteger _personId;
	private static AtomicInteger _eventId;


    private DateFormat _dateFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	private Random _rng = null;
	private Logger _logger = Logger.getLogger(OlioUtility.class.getName());


	private static int nextPersonId()
	{
		return _personId.incrementAndGet();
	}

	private static int lastPersonId()
	{
		return _personId.get();
	}

	private static int nextEventId()
	{
		return _eventId.incrementAndGet();
	}

	private static int lastEventId()
	{
		return _eventId.get();
	}

	private static synchronized void initPersonId(int numPreloadPersons)
	{
		_personId = new AtomicInteger(MIN_PERSON_ID+numPreloadPersons-1);
	}

	private static synchronized void initEventId(int numPreloadEvents)
	{
		_eventId = new AtomicInteger(MIN_ITEM_ID+numPreloadEvents-1);
	}


	public OlioUtility()
	{
	}

	public OlioUtility(Random rng, OlioConfiguration cfg)
	{
		this._rng = rng;
		this._cfg = cfg;

		initPersonId(this._conf.getNumOfPreloadedPersons());
		initEventId(this._conf.getNumOfPreloadedEvents());
	}

    public void setRandomGenerator(Random rng)
    {
        this._rng = rng;
    }

    public Random getRandomGenerator()
    {
        return this._rng;
    }

    public void setConfiguration(OlioConfiguration conf)
    {
        this._conf = conf;

        initPersonsId(this._conf.getNumOfPreloadedPersons());
        initEventId(this._conf.getNumOfPreloadedEvents());
    }

    public OlioConfiguration getConfiguration()
    {
        return this._conf;
    }

    public boolean isAnonymousPerson(OlioPerson person)
    {
        return this.isValidPerson(person) && this.isAnonymousPerson(person.id);
    }

    public boolean isAnonymousPerson(int personId)
    {
        return ANONYMOUS_PERSON_ID == personId;
    }

	public boolean isValidPerson(OlioPerson person)
	{
		return null != person && (MIN_PERSON_ID <= person.id || this.isAnonymousPerson(person.id));
	}

	public boolean isValidEvent(OlioEvent event)
	{
		return null != event && MIN_EVENT_ID <= event.id;
	}

	public boolean checkHttpResponse(HttpTransport httpTransport, String response)
	{
		if (response == null
			|| response.length() == 0
			|| HttpStatus.SC_OK != httpTransport.getStatusCode())
		{
			return false;
		}

		return true;
	}

//	public boolean checkOliosResponse(String response)
//	{
//		if (response == null
//			|| response.length() == 0
//			|| -1 != response.indexOf("ERROR"))
//		{
//			return false;
//		}
//
//		return true;
//	}

	/**
	 * Create a new Olio person object.
	 *
	 * @return an instance of OlioPerson.
	 */
	public OlioPerson newPerson()
	{
		return this.getPerson(nextPersonId());
	}

	/**
	 * Generate a random Olio person among the ones already preloaded.
	 *
	 * @return an instance of OlioPerson.
	 */
	public OlioPerson generatePerson()
	{
		int personId = this._rng.nextInt(this._conf.getNumOfPreloadedPersons())+MIN_PERSON_ID;

		return this.getPerson(personId);
	}

	/**
	 * Get the Olio person associated to the given identifier.
	 *
	 * @param id The person identifier.
	 * @return an instance of OlioPerson.
	 */
	public OlioPerson getPerson(int id)
	{
	}

	/**
	 * Create a new Olio social event object.
	 *
	 * @return an instance of OlioSocialEvent.
	 */
	public OlioSocialEvent newSocialEvent()
	{
		return this.getSocialEvent(nextEventId());
	}

	/**
	 * Generate a random Olio social event among the ones already preloaded.
	 *
	 * @return an instance of OlioSocialEvent.
	 */
	public OlioSocialEvent generateSocialEvent()
	{
		int eventId = this._rng.nextInt(this._conf.getNumOfPreloadedEvents())+MIN_EVENT_ID;

		return this.getSocialEvent(eventId);
	}

	/**
	 * Get the Olio social event associated to the given identifier.
	 *
	 * @param id The social event identifier.
	 * @return an instance of OlioSocialEvent.
	 */
	public OlioSocialEvent getSocialEvent(int id)
	{
		OlioSocialEvent evt = new OlioSocialEvent();

		evt.id = id;
        evt.title = this.randomText(15, 20);
        evt.summary = this.randomText(50, 200);
        evt.description = this.randomText(100, 495);
        evt.telephone = this.randomPhone(new StringBuilder(256));
		Calendar cal = Calendar.getInstance();
		//cal.set(2008, 10, 20, 20, 10);
        evt.eventTimestamp = cal.getTime();
		//evt.imageUrl = this._cfg.getEventImageUrl();
		//evt.literatureUrl = this._cfg.getEventLiteratureUrl();
		evt.tags = this.generateTags();
		evt.address = this.generateAddressParts();
		evt.timezone = this.generateTimeZone();

		return evt;
	}

	/**
	 * Generates a random integer number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	public int generateInt(int x, int y)
	{
		// Swap x and y if y less than x.
		if (y < x)
		{
			int t = y;
			y = x;
			x = t;
		}

		return x + Math.abs(this._rng.nextInt() % (y-x+1));
	}

	/**
	 * Generates a random long number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	public long generateLong(long x, long y)
	{
		// Swap x and y if y less than x.
		if (y < x)
		{
			long t = y;
			y = x;
			x = t;
		}
		return x + Math.abs(this._rng.nextLong() % (y-x+1));
	}

	/**
	 * Generates a random double number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, exclusive.
	 */
	public double generateDouble(double x, double y)
	{
		return (x+(this._rng.nextDouble() * (y-x)));
	}

//	/**
//	 * NURand integer non-uniform random number generator.
//	 * TPC-C function NURand(A, x, y) =
//	 *      (((random(0,A) | random(x,y)) + C) % (y - x + 1)) + x
//	 * 
//	 * @param A     The A-value.
//	 * @param x     The x-value.
//	 * @param y     The y-value.
//	 * @return      The random value between x and y, inclusive.
//	 */
//	public int NURand( int A, int x, int y )
//	{
//		int C = 123; /* Run-time constant chosen between 0, A */
//		int nurand = ( ( ( random( 0, A ) | random( x, y ) ) + C ) % ( y - x + 1 ) ) + x;
//		
//		return nurand;
//	}

	/**
	 * Generates a random string of alphanumeric characters of random length of
	 * mininum x, maximum y and mean (x+y)/2
	 *
	 * @param x     The minimum length.
	 * @param y     The maximum length.
	 * @return      A random string of length between x and y.
	 */
	public String generateAlphaNumString(int x, int y)
	{
		int length = x;
		if (x != y)
		{
			length = this.generateInt(x, y);
		}
		
		char[] buffer = new char[length];
		
		for (int i = 0; i < length; ++i)
		{
			int j = this.generateInt(0, ALNUM_CHARS.length-1);
			buffer[i] = ALNUM_CHARS[j];
		}

		return new String(buffer);
	}

	/**
	 * Generates a random string of only alpahabet characters of random length
	 * of mininum x, maximum y and mean (x+y)/2
	 *
	 * @param x     The minimum length.
	 * @param y     The maximum length.
	 * @return      A random character string of length between x and y.
	 */
	public String generateAlphaString( int x, int y )
	{
		int length = x;
		if (x != y)
		{
			length = this.generateInt(x, y);
		}
		
		char[] buffer = new char[length];
		
		for (int i = 0; i < length; ++i)
		{
			int j = this.generateInt(0, ALPHA_CHARS.length-1);
			buffer[i] = ALPHA_CHARS[j];
		}
		return new String( buffer );
	}

	/**
	 * Generates a random string of only numeric characters of random length of
	 * mininum x, maximum y and mean (x+y)/2.
	 * 
	 * @param min       The minimum length.
	 * @param max       The maximum length.
	 * @return          A random character string of length between x and y..
	 */
	public String generateNumString(int x, int y)
	{
		int length = x;
		if (x != y)
		{
			length = this.generateInt(x, y);
		}
		
		char[] buffer = new char[length];
		
		for (int i = 0; i < length; ++i)
		{
			buffer[i] = (char) this.generateInt('0', '9');
		}
		
		return new String(buffer);
	}

 	/**
 	 * Generates a date within the range specified by (refDate + x) and
 	 * (refDate + y)
	 *
	 * @param refDate   The reference date.
	 * @param x         The minimum offset from the reference date.
	 * @param y         The maximum offset from the reference date.
	 * @return          A random date.
	 */
	public Date generateDateInInterval(Date refDate, int x, int y)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(refDate.getTime());
		
		int days = x;
		if (x != y)
		{
			days = this.generateInt(x, y);
		}
		
		calendar.add(Calendar.DATE, days);
		Date date = new Date(calendar.getTimeInMillis());
		
		return date;
	}

	/**
	 * Creates a random calendar between time (ref + min) and (ref + max).
	 * 
	 * @param refCal    The reference calendar.
	 * @param min       The lower time offset from the reference.
	 * @param max       The upper time offset from the reference.
	 * @param units     The units of minimum and maximum, referencing the
	 *                  fields of Calendar (e.g. Calendar.YEAR).
	 * @return          A random calendar.
	 */
	public Calendar generateCalendarInInterval(Calendar refCal, int min, int max, int units)
	{
		// We should not modify refCal; we clone it instead.
		Calendar baseCal = (Calendar) refCal.clone();
		baseCal.add( units, min );
		long minMs = baseCal.getTimeInMillis();
		
		Calendar maxCal = (Calendar) refCal.clone();
		maxCal.add( units, max );
		long maxMs = maxCal.getTimeInMillis();
		
		baseCal.setTimeInMillis(this.generateLong(minMs, maxMs));
		
		return baseCal;
	}

	/**
	 * Creates a random calendar between min and max.
	 * 
	 * @param min       The minimum time.
	 * @param max       The maximum time.
	 * @return          A random calendar.
	 */
	public Calendar generateCalendarInInterval(Calendar min, Calendar max)
	{
		long minMs = min.getTimeInMillis();
		long maxMs = max.getTimeInMillis();
		
		// We use cloning so Calendar type, timezone, locale, and stuff
		// stay the same as min.
		Calendar result = (Calendar) min.clone();
		result.setTimeInMillis(this.generateLong(minMs, maxMs));
		
		return result;
	}

	public String generateTimeZone()
	{
		return TIMEZONES[this.generateInt(0, TIMEZONES.length-1)];
	}

	// Phone is 0018889990000 or 0077669990000 for non-US 
	// 50% of the time, do US, 50% non-us.
	public static String generatePhone(StringBuilder b)
	{
		String v = this.generateNumString(1, 2);

		if (v.length() == 1)
		{
			b.append("001"); // removed space
			v = this.generateNumString(3, 3);
			b.append(v); // removed space
		}
		else
		{
			b.append("00").append(v);
			v = this.generateNumString(2, 2);
			b.append(v); //removed spaces
		}

		v = this.generateNumString(3, 3);
		b.append(v); // removed space
		v = this.generateNumString(4, 4);
		b.append(v);

		return b.toString();
	}

    public String generateCountry()
	{
        return COUNTRIES[this.generateInt(0, COUNTRIES.length-1)];
    }

	public String generateName(int minLength, int maxLength)
	{
		if (minLength < 1 || maxLength < minLength)
		{
			throw new IllegalArgumentException();
		}

		StringBuffer buf = new StringBuffer();
		buf.append(this.generateAlphaString(1, 1).toUpperCase());
		buf.append(this.generateAlphaString(minLength - 1, maxLength - 1).toLowerCase());

		return buf.toString();
	}

	/**
	 * Randomly creates text between length x and y, inclusive that is
	 * separated by spaces. The words are between 1 and 12 characters long.
	 */
	public String generateText(int x, int y)
	{
		int length = this.generateInt(x, y);
		StringBuilder buffer = new StringBuilder(length);
		int leftover = length;
		while (leftover > 0)
		{
			String word = this.generateAlphaString(1, leftover < 12 ? leftover : 12);
			buffer.append(word).append(' ');
			leftover -= word.length() + 1;
		}
		return buffer.toString();
	}

	public String[] generateAddressParts()
	{
		String[] addr = new String[6];
		addr[0] = this.generateStreet1();
		addr[1] = this.generateStreet2();
		addr[2] = this.generateAlphaString(4, 14);
		addr[3] = this.generateAlphaString(2, 2).toUpperCase();
		addr[4] = this.generateNumString(5, 5);
		addr[5] = this.generateCountry();
		return addr;
	}

	/**
	 * Generates the first line of a pseudorandom street address.
	 * 
	 * @return      A random street address.
	 */
	public String generateStreet1()
	{
		StringBuilder buffer = new StringBuilder(255);
		buffer.append(this.generateNumString(1, 5)).append(' '); // Number
		buffer.append(this.generateName(buffer, 1, 11)); // Street Name

		String[] STREETEXTS = {"Blvd", "Ave", "St", "Ln", ""};
		String streetExt = STREETEXTS[this.generateInt(0, STREETEXTS.length-1)];
		if (streetExt.length() > 0)
		{
			buffer.append(' ').append(stretExt);
		}

		return buffer.toString();
	}

	/**
	 * Generates the second line of a pseudorandom street address.
	 * 
	 * @return      Half the time, a second line of a random street address;
	 *              otherwise an empty string.
	 */
	public String generateStreet2()
	{
		String street = "";

		int toggle = this.generateInt(0, 1);
		if (toggle > 0)
		{
			street = this.generateAlphaString(5, 20);
		}

		return street;
	}

	/**
	 * Generates a pseudorandom country.
	 * 
	 * @return      Half the time, USA; otherwise, a random string.
	 */
	public String generateCountry()
	{
		String country = "USA";

		int toggle = this.generateInt(0, 1);
		if (toggle == 0)
		{
			StringBuilder buffer = new StringBuilder(255);
			country = this.generateName(buffer, 6, 16).toString();
		}

		return country;
	}

	/**
	 * Randomly selects an event from the events page.
	 * @param r The random value generator
	 * @param eventListPage The page from the response buffer
	 * @return The selected event id, as a string
	 */
/*
	public String generateEventId(StringBuilder eventListPage)
	{
		return this.generateEvent(eventListPage, null);
	}

	public String generateEventId(StringBuilder eventListPage, String pageType)
	{
		String search1 = null;
		switch (this._cfg.getIncarnation())
		{
			case JAVA_INCARNATION:
				search1 = "<a href=\"" + OlioGenerator.CONTEXT_ROOT + "/event/detail?socialEventID=";
				break;
			case RAILS_INCARNATION:
				search1 = "<a href=\"" + OlioGenerator.CONTEXT_ROOT + "/events/";
				break;
		}
		int idx = 0;
		Set<String> eventIdSet = new HashSet<String>();
		String eventItem = "";
		for (;;)
		{
			idx = eventListPage.indexOf(search1, idx);
			if (idx == -1)
			{
				break;
			}
			// We skip this the php or jsp, just knowing it is 3 chars
			idx += search1.length();
			int endIdx = eventListPage.indexOf("\"", idx);
			if (endIdx == -1)
			{
				break;
			}

			eventItem = eventListPage.substring(idx, endIdx).trim();
			if (!eventItem.contains("tagged") && !eventItem.contains("new"))
			{
				eventIdSet.add(eventListPage.substring(idx, endIdx).trim());
			}
			idx = endIdx;
		}
		int size = eventIdSet.size();
		if (size == 0)
		{
			if (pageType != null && !pageType.equals("TagSearch"))
			{
				this._logger.severe(search1 + " not found in " + pageType + " response! Response was:\n\n" + eventListPage + "\n\n\n");
			}
			return null;
		}

		String[] eventIds = new String[size];
		eventIds = eventIdSet.toArray(eventIds);
		return eventIds[this.generateInt(0, size-1)];
	}
*/

	/**
	 * Create a string of random tags.
	 *
	 * @return A list of tag names
	 *
	 * Note: the number of generated tags is randomly chosen between 1 and
	 * MAX_NUM_TAGS.
	 */
	private List<String> generateTags()
	{
    	Set<Integer> tagSet = new LinkedHashSet<Integer>(MAX_NUM_TAGS);
		int numTags = this.generateInt(1, MAX_NUM_TAGS);
		for (int i = 0; i < numTags; ++i)
		{
			tagSet.add(this.randomTagId(0.1D));
		}

		List<String> tags = new ArrayList<String>();
		for (int tagId : tagSet)
		{
			tags.add(UserName.getUserName(tagId));
		}
		return tags;
	}

	/**
	 * Create a random datetime.
	 * 
	 * @return The random date as an array of 5 fields: the year, the month,
	 *  the day, the hour, the minute, and the timezone.
	 */
	private String[] generateDateStr()
	{
		String[] date = new String[5];

		String strDate = this.dateFormat.format(this.getRandomUtil().makeDateInInterval(BASE_DATE, 0, 540));
		StringTokenizer tk = new StringTokenizer(strDate, "-");
		int counter = 0;
		while (tk.hasMoreTokens())
		{
			date[(counter++)] = tk.nextToken();
		}

		return date;
	}

	/**
	 * From http://tagsonomy.com/index.php/dynamic-growth-of-tag-clouds/
	 *
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
	private static int generateTagId(double meanRatio)
	{
		double mean = ScaleFactors.tags * meanRatio;

		int selected;
		int loops = 0;

		do {
			double x = this.generateDouble(0.0, 1.0);
			if (x == 0)
			{
				x = 0.05;
			}

			// We use a negative exponential distribution to select the tags.
			// The first tags tend to be the most often used.
			selected = (int)(mean * -Math.log(x));

//			if (selected >= ScaleFactors.tagCount)
//			{
//				this._logger.warning("Selected: " + selected);
//			}

			// However, if we exceed the limit, we do not select the last one.
			// We redo the selection instead.
		} while (selected >= ScaleFactors.tags && loops++ < 10);

		if (loops >= 10)
		{
			this._logger.severe("Exceeded loop limit. Selected:" + selected + " TagCount: " + ScaleFactors.tags);
		}

		// We use the user name mechanism to create the tag names
		return ++selected;
	}

	/**
	 * Returns a random tag name. Implicitly calls randomTagId.
	 * @param r The random value generator
	 * @return The randomly selected tag name.
	 */
	private String generateTagName(int tagId)
	{
		return UserName.getUserName(this.generateInt(1, ScaleFactors.tags));
		//return UserName.getUserName(randomTagId(r, 0.1));
	}
}
