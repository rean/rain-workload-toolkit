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
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.http.HttpStatus;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.olio.model.OlioPerson;
import radlab.rain.workload.olio.model.OlioSocialEvent;
import radlab.rain.workload.olio.model.OlioTag;


/**
 * Collection of utilities for the Olio workload.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author: Shanti Subramanyam
 * @author: Rean Griffith 
 * @author: <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class OlioUtility
{
	public static final int ANONYMOUS_PERSON_ID = -1;
	public static final int INVALID_EVENT_ID = -1;
	public static final int INVALID_TAG_ID = -1;
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
			"Zaire", "Zambia", "Zanzibar", "Zimbabwe"
		};

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
			"W-SU", "WET", "Zulu"
		};

	private static final char[][][] SCRAMBLE = {
			{{'0'}}, {{'0'}}, {{'0'}}, {{'0'}}, {{'0'}},
			{{'r', 'b', 'd', 'm', 'f', 't', 'x', 'e', 'i', 'o', 's', 'p', 'a', 'l',
			'g', 'h', 'n', 'w', 'z', 'q', 'u', 'v', 'k', 'j', 'c', 'y'},
			{'h', 'i', 'b', 'f', '2', 'o', 'd', 'u', '7', '9', 'w', 'v', 'j', '3', '6',
			'g', 'z', 'p', 'n', '8', 'y', 'k', 'x', 'q', '5', 's', 't', 'a', '1', '4',
			'0', 'e', 'c', 'm', 'r', 'l', '_'},
			{'y', '3', 'w', 'h', 'v', 'u', 'e', 'q', 'm', 'z', '9', 'x', 'k', '7', 'p',
			'r', 't', 'n', '4', 'f', 'o', '2', '5', 'i', 'l', 'a', '6', '8', 'g', '1',
			'_', 'b', 'd', '0', 's', 'j', 'c'},
			{'6', 'c', 'f', '3', '2', 'v', 'm', 'l', 'x', 'k', 'e', '_', '7', 'a', 's',
			'0', 'j', 'n', 'd', 'z', 'u', '4', 't', 'o', 'g', '5', 'w', 'h', 'p', 'b',
			'i', '1', 'q', '8', 'r', 'y', '9'},
			{'i', 'e', 'p', 'g', 'h', 'b', '1', 'a', 'x', 'm', 'o', '7', 'l', 'u', 'z',
			'w', '0', '8', '9', 'd', 'f', '2', '_', '5', 'c', 'n', 'r', '6', 'j', 'v',
			'q', 't', 's', '4', 'k', '3', 'y'},
			{'t', 'n', 'v', 'g', 's', 'j', 'p', 'l', 'b', '8', 'd', '_', 'q', 'u', 'z',
			'2', '5', '7', 'x', 'i', 'o', 'r', '9', '0', 'f', 'w', 'k', '6', '4', '3',
			'e', 'c', 'a', 'h', 'm', '1', 'y'}},
			{{'q', 'o', 'x', 'j', 'g', 'r', 'k', 'p', 'a', 'e', 'i', 'w', 'u', 'n',
			's', 'f', 'c', 'b', 'z', 'y', 't', 'v', 'm', 'h', 'l', 'd'},
			{'z', 'g', 'm', '7', 'r', 'l', 'o', 'q', 't', '0', '9', 'b', 'w', '3', '2',
			'y', '1', 'e', 'p', 's', '6', 'x', '5', 'v', 'i', 'n', '_', '4', 'a', 'k',
			'u', 'f', 'c', 'd', 'h', 'j', '8'},
			{'1', '9', 'c', 't', 'p', 'm', 'e', '5', 'f', 'y', 'r', 'g', 'w', 'j', 'i',
			'x', '3', 'u', '8', '6', 'd', 'k', 's', '4', 'b', 'l', 'h', 'q', 'n', '7',
			'2', 'z', 'a', 'o', '0', 'v', '_'},
			{'g', 'v', 'r', 'l', 'h', 'a', '4', '0', 'k', '_', '2', 'j', 'b', 't', 'p',
			'i', 'z', '5', '7', 'm', '3', '1', 'w', 'e', '6', 'u', 'f', 'y', '9', 'n',
			'x', 'o', 'c', 'd', 's', 'q', '8'},
			{'q', 'u', '0', '4', 'f', 'j', 'r', 'w', '8', '9', 't', 'k', 'h', '2', 'i',
			'b', 'n', 'g', 'z', 'x', '3', 'd', 'e', 'a', 's', '6', '1', '5', 'v', 'o',
			'_', 'l', 'm', 'y', 'c', 'p', '7'},
			{'b', '7', 'x', 'r', 'a', '8', 'z', 'm', 'q', 'i', 't', 'v', 'c', 'd', '6',
			'j', 'k', 'y', '0', '9', 'f', '3', '2', 'h', 's', 'w', 'l', '1', 'u', '_',
			'g', 'o', 'e', 'n', 'p', '5', '4'},
			{'v', 'j', '1', 'n', '8', 's', '_', 'q', 'u', 'e', '3', 'c', 'o', '9', 'm',
			'h', 't', '0', 'f', '4', 'd', 'r', '5', 'x', '2', '6', 'w', 'a', 'i', 'z',
			'7', 'b', 'l', 'k', 'g', 'y', 'p'}},
			{{'o', 'g', 'l', 'k', 'e', 'q', 'r', 'p', 't', 'w', 'u', 'h', 'j', 'a',
			'i', 'v', 'd', 'y', 'z', 'b', 'c', 'm', 'x', 'n', 'f', 's'},
			{'3', 'y', 'f', 't', '6', 'q', 'z', 'r', 'b', '1', 'j', '0', '7', '2', '_',
			'a', 'g', '9', '4', 'l', 'v', 'd', 'c', 'm', 'o', 'i', 'k', '8', '5', 'x',
			'w', 'n', 'h', 'u', 'p', 'e', 's'},
			{'d', 'v', 'l', 'b', 'j', '5', 'y', '8', 'o', 'p', '0', 'q', 'x', 'u', 's',
			'w', '7', '3', 'h', 't', '_', 'n', '2', 'c', 'm', 'r', '6', 'g', 'k', 'z',
			'e', '1', 'f', '9', '4', 'i', 'a'},
			{'a', 'f', 'y', 'o', 'w', 'z', 'b', 'i', 'd', '7', '_', 'm', 's', 'p', '1',
			'4', 'x', 'l', 'r', '9', 'j', 'q', 'k', 'v', '6', 'g', '3', 't', 'h', 'e',
			'2', '0', 'n', '5', '8', 'u', 'c'},
			{'r', 'c', 'q', 'x', '1', 'a', 'u', '7', 'k', '8', 'p', '0', '9', 'f', 'j',
			'n', 'b', '3', 'z', 'o', '_', 'd', 'v', '4', 'g', 'i', 's', 'e', 'w', 'y',
			'2', 'm', 't', '5', 'h', '6', 'l'},
			{'s', '_', '2', 'v', 'z', 'f', '1', '7', 'k', 'o', 'd', '6', '8', 'j', 'i',
			'q', '0', 'x', 'a', 'm', 'r', 't', 'w', 'h', 'y', 'n', 'l', 'u', 'c', 'p',
			'g', '4', '9', '3', '5', 'e', 'b'},
			{'u', '5', 'h', 'x', 'y', 'a', '4', '8', 'z', 'i', 'g', 's', '2', 'n', 'p',
			'b', 'q', 'o', '6', '1', '0', 'w', 'e', '3', '_', 'j', 'v', '9', 'k', 'm',
			'd', 'r', 't', '7', 'f', 'l', 'c'},
			{'t', 'z', 'n', 'y', '6', 'm', 'i', 'w', 'c', '2', 'f', 'q', 'e', 'h', '_',
			'v', 'j', '9', '0', '8', 's', '5', 'g', 'd', 'p', 'l', '4', 'u', 'o', '1',
			'k', '3', 'r', 'b', 'a', 'x', '7'}},
			{{'n', 'w', 'e', 'q', 'k', 'p', 's', 'a', 'j', 'm', 'i', 'c', 'h', 'g',
			'z', 'u', 'd', 'l', 'y', 'v', 'f', 'o', 'b', 't', 'r', 'x'},
			{'l', 'x', 'd', 'k', 'u', 'q', '5', 'r', 'w', 's', '_', 'h', 'i', 'z', 'p',
			'g', 'a', 'c', 'y', 'v', 'e', 'f', '6', 'j', 'o', '4', '8', 't', '0', '9',
			'7', '3', 'b', 'n', '1', '2', 'm'},
			{'h', '_', 'y', 'm', 'a', 'v', 'r', 'k', 'j', 'i', 't', '3', '5', '2', 'f',
			'u', 'd', '4', 'n', 'b', 'e', '1', 'q', '0', 'c', 'p', '8', '7', 'x', '6',
			'9', 'o', 's', 'w', 'l', 'g', 'z'},
			{'k', 'y', 'a', '4', 'f', 'i', '0', '_', '1', 'b', 'p', '9', 'x', 'w', 'v',
			'n', 'd', 't', 'm', 'u', 'q', 'l', '6', 'o', '5', 'j', 's', '8', '7', 'h',
			'z', 'e', 'r', '3', 'c', '2', 'g'},
			{'y', 'q', '2', 'j', 'z', 'f', 'h', 'r', 'x', 'g', 'w', '9', '5', '0', '3',
			'4', 't', 'a', 'e', '7', 'b', 'p', 'd', 'c', 's', 'v', '_', '6', 'k', 'o',
			'i', 'l', 'n', 'u', '8', 'm', '1'},
			{'a', '0', 's', '5', 'h', '4', 'c', 'x', '8', 'w', 'r', '1', 't', '6', 'u',
			'7', 'e', 'b', 'd', 'j', 'i', '3', 'f', 'y', '2', 'v', 'n', 'o', '_', 'q',
			'9', 'm', 'g', 'k', 'p', 'l', 'z'},
			{'o', '7', 'n', 'r', 'g', 'v', 't', 'h', 'w', '_', '5', 'z', '1', '4', 'y',
			'a', '3', 'u', 'k', 'f', 'e', 'b', 'm', 'i', 'x', 'l', 'd', '0', 'c', '9',
			'p', '2', 'j', '8', 's', 'q', '6'},
			{'u', '2', 'o', 'a', 'k', '3', '_', 'i', 'z', 'r', 'e', 'x', '6', 'v', '4',
			'y', 'n', 'f', 'm', 'd', '7', '1', 'p', 'h', 't', 'l', '0', '8', '5', 'w',
			'c', '9', 'g', 's', 'b', 'q', 'j'},
			{'h', 'o', '3', '5', 'g', 'j', 'p', 'y', 'r', '4', 'q', '7', '9', '6', 't',
			'e', 'z', 'v', 'f', 'w', '1', 'i', 'b', '0', 'l', 's', 'a', 'x', '_', 'k',
			'u', 'c', 'd', 'n', '2', '8', 'm'}},
			{{'p', 'f', 'x', 'n', 'v', 'q', 'w', 'r', 'd', 'i', 'h', 'z', 'b', 't',
			'g', 'l', 'o', 'c', 'y', 's', 'u', 'm', 'e', 'k', 'a', 'j'},
			{'k', 'e', 'u', '0', 't', 'o', 'd', 'x', '_', '4', 'l', 'y', '7', '2', 'z',
			'9', 'a', 'm', 'r', 'b', 'f', '8', 'q', 's', 'n', 'g', '5', 'i', 'j', 'p',
			'6', '1', 'v', 'h', 'w', 'c', '3'},
			{'r', '4', '5', 'q', '_', 's', '2', 'p', 'z', 'n', 'o', 'm', 'g', 'a', 'c',
			'u', 'v', 'j', 'e', 'k', 'x', '7', 'l', 'd', 'w', '0', '8', 'i', '6', 'h',
			'3', '1', 'y', '9', 'f', 't', 'b'},
			{'q', 'x', 'i', 'g', 'z', 'v', '2', 'u', '4', 's', '1', 'd', 'k', 'n', 'a',
			'5', '7', 't', 'y', '9', 'm', 'r', '8', 'j', '0', 'b', 'c', 'o', 'l', 'h',
			'f', '6', 'e', 'p', '3', 'w', '_'},
			{'c', 'k', '1', 'd', '6', '_', 'o', '5', 'm', 'w', 'e', 'g', 'b', '0', 'q',
			't', '8', 'r', 'u', 'i', 'n', 'j', 'x', 's', '7', '2', 'z', 'f', 'l', '4',
			'v', 'y', 'a', 'p', 'h', '9', '3'},
			{'t', 'v', '_', 'j', 'w', '5', 'l', '8', 's', 'n', '9', 'e', '0', 'd', 'x',
			'r', 'c', '2', '6', 'a', 'k', '3', 'g', '1', 'u', 'y', '4', 'o', 'q', 'f',
			'm', 'p', 'b', 'z', '7', 'i', 'h'},
			{'k', 'm', 'v', 'p', 'w', '9', 'x', 'j', 't', '_', 'i', 'h', 'a', '5', 'd',
			'e', 'z', 'n', 'f', '4', 'o', '0', 'l', '6', 'c', '8', 's', 'b', 'g', '2',
			'1', '3', 'r', 'y', '7', 'u', 'q'},
			{'i', 'a', 'b', 'v', 'l', 'y', 't', 'f', 'z', '8', '3', 'e', '2', 'p', 'r',
			'u', '6', '7', 's', '5', 'c', 'k', '0', 'j', 'm', 'n', 'h', 'd', '1', 'g',
			'o', 'w', 'x', '4', 'q', '9', '_'},
			{'8', '9', 'x', 'f', 'h', '7', '0', '4', 'o', '2', 'g', 'v', 'j', 't', 'e',
			'1', '3', 'q', 'w', 'a', '_', 'n', 'd', 'l', 'i', '6', 'm', 's', 'c', 'b',
			'y', 'z', '5', 'u', 'k', 'p', 'r'},
			{'f', '5', '_', 's', 'p', 'o', 'r', 'w', '1', 'y', '4', 'g', 'e', '8', 'b',
			'a', 'c', '0', 'v', 'h', 'm', 'q', 't', '6', 'u', 'n', 'd', '3', 'x', 'i',
			'7', 'z', 'j', 'k', '9', 'l', '2'}},
			{{'o', 't', 'q', 'k', 'b', 'p', 'a', 'f', 'x', 'd', 'c', 's', 'w', 'e',
			'y', 'r', 'l', 'm', 'z', 'i', 'v', 'g', 'n', 'j', 'h', 'u'},
			{'1', 'f', '5', 'u', 'j', 'q', 'r', 'w', 'i', 'x', '2', 'h', 'o', '0', '6',
			'p', 'c', 'd', 'z', 't', 'l', 'n', '_', '7', '3', 'b', 'a', 'g', 'e', '9',
			'y', '4', 'v', 's', '8', 'k', 'm'},
			{'v', '9', 'u', '0', 'r', 'g', '_', '4', 'y', 'n', 'j', '7', 'p', 'k', '2',
			't', '1', 'o', 'e', 'b', 'f', 'i', 'x', 'z', 'c', 'q', '6', '8', 'w', 'm',
			'd', 'h', '5', 'a', 's', '3', 'l'},
			{'_', 'v', '8', 'y', 'g', 'e', 'o', 'b', 'r', 't', 'n', '1', 'm', '6', '5',
			'k', 'h', 'd', 'w', 's', 'x', 'q', 'l', '4', 'j', 'z', 'i', '7', '3', 'c',
			'p', 'a', '2', 'u', 'f', '0', '9'},
			{'j', 'z', 'd', '_', '3', '5', '9', 'p', '8', 'l', 'k', 'r', '7', 'q', '6',
			'a', 's', 'w', 'b', '0', 'f', 'x', 'e', 'v', 't', 'o', '4', 'h', '2', '1',
			'c', 'm', 'i', 'u', 'y', 'g', 'n'},
			{'a', 'f', '0', 'n', 'h', 'v', 'u', '7', '6', 'j', 'p', 'b', 'm', '_', '5',
			'9', 'q', 'd', '3', 'y', '4', 'i', '2', 'k', 'z', 't', 'w', 'g', '8', 'c',
			'r', '1', 'e', 'x', 'l', 'o', 's'},
			{'e', 'o', 'p', '9', 'f', 'u', 'j', 'y', '0', 'g', 's', 'r', '6', 't', 'n',
			'b', 'a', 'x', 'w', 'l', 'i', 'c', '1', 'k', 'q', '5', '7', 'd', 'h', '_',
			'2', 'z', '4', '3', 'v', 'm', '8'},
			{'5', 'f', 'b', 'g', 'v', 'a', '8', 'd', 'm', '_', 'k', 'o', 'u', '2', 'w',
			'x', 'c', '9', 'j', '7', 'n', 'e', '4', 'l', 'p', 'r', 'h', 'q', 'i', '1',
			'0', 't', '6', 's', 'z', 'y', '3'},
			{'s', 'j', 'h', 'z', '1', 'b', 'w', '2', 'y', 'l', 'u', '8', 'd', 'q', 'a',
			'5', 'x', 'k', '0', 'e', 't', 'v', '9', '7', 'n', 'i', 'p', 'm', 'r', '_',
			'3', '4', '6', 'o', 'c', 'g', 'f'},
			{'v', 'c', 'q', '1', 'a', '0', 'o', 'i', '3', '6', 'e', 'b', '9', 'w', 'x',
			'2', '5', 'l', 'd', 'z', 's', 'n', 'h', 'g', 'r', 'y', '8', 't', 'k', '4',
			'_', 'p', 'j', 'f', 'u', 'm', '7'},
			{'k', '4', 's', 'q', 'i', 'j', 'v', 'e', 'y', '8', 'l', 'u', '_', 'o', 'c',
			'g', 'd', 'r', 'x', 'b', 'a', 'w', '9', 'p', '3', '5', 'n', 'f', '1', '2',
			'0', 'h', 'z', 't', '7', '6', 'm'}}
		};
	
	// Note that these sum up to 100
	private static final int[] LENGTH_PERCENT = { 0, 0, 0, 0, 0, 5, 8, 17, 25, 24, 21 };
	
    private static final int MAX_NUM_TAGS = 7;

	private static final Date BASE_DATE = new Date(System.currentTimeMillis());

	private static final int MIN_PERSON_ID = 1; ///< Mininum value for person IDs
	private static final int MIN_EVENT_ID = 1; ///< Mininum value for event IDs

	private static final int MIN_TAG_ID = 1; ///< Mininum value for tag IDs

	private static int[] selector = new int[LENGTH_PERCENT.length];

	private static AtomicInteger _personId;
	private static AtomicInteger _eventId;

	static
	{
		selector[0] = LENGTH_PERCENT[0];
		for (int i = 1; i < selector.length; ++i)
		{
			selector[i] = selector[i-1] + LENGTH_PERCENT[i];
		}
	}


    private DateFormat _dateFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	private Random _rng = null;
	private OlioConfiguration _cfg = null;
	private Logger _logger = Logger.getLogger(OlioUtility.class.getName());


	/**
	 * Generate a new Olio person identifier
	 *
	 * @return A new Olio person identifier
	 */
	private static int nextPersonId()
	{
		return _personId.incrementAndGet();
	}

	/**
	 * Get the last generated Olio person identifier
	 *
	 * @return The last generated Olio person identifier
	 */
	private static int lastPersonId()
	{
		return _personId.get();
	}

	/**
	 * Generate a new Olio social event identifier
	 *
	 * @return A new Olio social event identifier
	 */
	private static int nextEventId()
	{
		return _eventId.incrementAndGet();
	}

	/**
	 * Get the last generated Olio social event identifier
	 *
	 * @return The last generated Olio social event identifier
	 */
	private static int lastEventId()
	{
		return _eventId.get();
	}

	/**
	 * Initialize the Olio person identifier generator
	 *
	 * @param numPreloadedPersons Number of already preloaded Olio persons.
	 */
	private static synchronized void initPersonId(int numPreloadPersons)
	{
		_personId = new AtomicInteger(MIN_PERSON_ID+numPreloadPersons-1);
	}

	/**
	 * Initialize the Olio social event identifier generator
	 *
	 * @param numPreloadedEvents Number of already preloaded Olio social events.
	 */
	private static synchronized void initEventId(int numPreloadEvents)
	{
		_eventId = new AtomicInteger(MIN_EVENT_ID+numPreloadEvents-1);
	}

	/**
	 * Obtains the unique user name for the given user id.
	 * 
	 * @param id    The user id.
	 * @return      The unique user name.
	 */
	private String generateUserName(int id)
	{
		// Since id starts with 1, we have to shift it to start with 0 for
		// our operations.
		--id;
		
		// We divide the ids into sets, each set has 100 users.
		int setId = id / 100;

		// Then we obtain the per-set id 0..99
		int psid = id % 100;
		
		// Here we reverse odd ids to ovid cluttering of shorter names
		// in the lower range and longer ones in the upper range of each
		// 100.
		if (psid % 2 == 1)
		{
			psid = 100 - psid;
		}

		// For selection, we do not want to make the same name lengths
		// contiguous. So we switch the digits on psid.
		psid = (psid % 10) * 10 + (psid / 10);

		// This outcoming psid is used for digit selection.

		// Next, choose the length.
		int lengthSequence = 0; // This is the sequence number for the psid
								// having this length within these 100 names.
		int len; // For now, pretend 0 is OK, but we'll shift is back to 1.
		for (len = 0; len < selector.length; ++len)
		{
			if (psid < selector[len])
			{
				if (len == 0)
				{
					lengthSequence = psid;
				}
				else
				{
					lengthSequence = psid - selector[len-1];
				}
				break;
			}
		}
		// Here we shift it back so len is from 1 to whatever.
		++len;

		// Now as we know the id, psid, and the name length to use,
		// we have to generate the name.
		char[] name = new char[len];
		int[] offset = new int[len];

		// The lengthId is the unique identifier for this length and is the
		// value we use to get the name.
		int lengthId = LENGTH_PERCENT[len-1]*setId + lengthSequence;

		// Now we calculate the initial offset into the scrambled chars
		// using last digit first.
		for (int i = 0; i < len; ++i)
		{
			offset[i] = lengthId % SCRAMBLE[len-1][i].length;
			lengthId /= SCRAMBLE[len-1][i].length;
		}

		// The first offset is now taken as is.
		name[0] = SCRAMBLE[len-1][0][offset[0]];

		for (int i = 1; i < len; ++i)
		{
			// We adjust the offset once again to avoid same name lenghts
			// to have many of the same characters. We use the previous
			// offset to step up the current offset.
			offset[i] = (offset[i] + offset[i-1]) % SCRAMBLE[len-1][i].length;
			// And finally we assign the rest of the name.
			name[i] = SCRAMBLE[len-1][i][offset[i]];
		}

		return new String(name);
	}


	public OlioUtility()
	{
	}

	public OlioUtility(Random rng, OlioConfiguration cfg)
	{
		this._rng = rng;
		this._cfg = cfg;

		initPersonId(this._cfg.getNumOfPreloadedPersons());
		initEventId(this._cfg.getNumOfPreloadedEvents());
		//initTagId(this._cfg.getNumOfPreloadedTags());
	}

    public void setRandomGenerator(Random rng)
    {
        this._rng = rng;
    }

    public Random getRandomGenerator()
    {
        return this._rng;
    }

    public void setConfiguration(OlioConfiguration cfg)
    {
        this._cfg = cfg;

        initPersonId(this._cfg.getNumOfPreloadedPersons());
        initEventId(this._cfg.getNumOfPreloadedEvents());
		//initTagId(this._cfg.getNumOfPreloadedTags());
    }

    public OlioConfiguration getConfiguration()
    {
        return this._cfg;
    }

    public boolean isAnonymousPerson(OlioPerson person)
    {
        return this.isValidPerson(person) && this.isAnonymousPerson(person.id);
    }

    public boolean isAnonymousPerson(int personId)
    {
        return ANONYMOUS_PERSON_ID == personId;
    }

    public boolean isRegisteredPerson(OlioPerson person)
    {
        return this.isValidPerson(person) && !this.isAnonymousPerson(person.id);
    }

    public boolean isRegisteredPerson(int personId)
    {
        return !this.isAnonymousPerson(personId);
    }

	public boolean isValidPerson(OlioPerson person)
	{
		return null != person && (MIN_PERSON_ID <= person.id || this.isAnonymousPerson(person.id));
	}

	public boolean isValidSocialEvent(OlioSocialEvent event)
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
		int personId = this._rng.nextInt(this._cfg.getNumOfPreloadedPersons())+MIN_PERSON_ID;

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
		OlioPerson person = new OlioPerson();

		person.id = id;
		person.userName = this.generateUserName(id);
		person.password = String.valueOf(id);
		person.firstName = this.generateName(2, 12);
		person.lastName  = this.generateName(5, 12);
		person.email = person.userName + "@" + this.generateAlphaString(3, 10) + ".com";
		person.telephone = this.generatePhone();
		person.summary = this.generateText(50, 200);
		person.timezone = this.generateTimeZone();
		person.address = this.generateAddressParts();
		person.imageUrl = "event.jpg";
		person.imageThumbUrl = "event_thumb.jpg";

		return person;
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
		int eventId = this._rng.nextInt(this._cfg.getNumOfPreloadedEvents())+MIN_EVENT_ID;

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
        evt.title = this.generateText(15, 20);
        evt.summary = this.generateText(50, 200);
        evt.description = this.generateText(100, 495);
        evt.telephone = this.generatePhone();
		Calendar cal = Calendar.getInstance();
		//cal.set(2008, 10, 20, 20, 10);
        evt.eventTimestamp = cal.getTime();
		evt.imageUrl = "event.jpg";
		evt.imageThumbUrl = "event_thumb.jpg";
		evt.literatureUrl = "event.pdf";
		evt.tags = this.generateTags();
		evt.address = this.generateAddressParts();
		evt.timezone = this.generateTimeZone();

		return evt;
	}

//	/**
//	 * Create a new Olio tag object.
//	 *
//	 * @return an instance of OlioTag.
//	 */
//	public OlioTag newTag()
//	{
//		return this.getTag(INVALID_TAG_ID);
//	}

	/**
	 * Generate a random Olio tag among the ones already preloaded.
	 *
	 * @return an instance of OlioTag.
	 */
	public OlioTag generateTag()
	{
		int tagId = this._rng.nextInt(this._cfg.getNumOfPreloadedTags())+MIN_TAG_ID;

		return this.getTag(tagId);
	}

	/**
	 * Get the Olio tag associated to the given identifier.
	 *
	 * @param id The tag identifier.
	 * @return an instance of OlioTag.
	 */
	public OlioTag getTag(int id)
	{
		OlioTag tag = new OlioTag();

		tag.id = id;
		tag.name = this.generateUserName(id);
		tag.refCount = this.generateInt(100, 150);

		return tag;
	}

	/**
	 * Parses the given HTML text to find a social event identifier.
	 *
	 * @param html The HTML string where to look for the event identifier.
	 * @return The found event identifier, or INVALID_EVENT_ID if
	 *  no event identifier is found. If more than one event is found, returns
	 *  the one picked at random.
	 */
	public int findEventIdInHtml(String html)
	{
		if (html == null)
		{
			return INVALID_EVENT_ID;
		}

		Set<String> eventIdSet = new HashSet<String>();
		switch (this._cfg.getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				{
					String search1 = "/event/detail?socialEventID=";
					int idx = 0;
					for (;;)
					{
						idx = html.indexOf(search1, idx);
						if (idx == -1)
						{
							break;
						}

						idx += search1.length();

						int endIdx = html.indexOf("\"", idx);
						if (endIdx == -1)
						{
							break;
						}
						//Javascript link for edit is defined as "/event/detail/socialEventID="
						// Ignore these
						if (idx != endIdx)
						{
							eventIdSet.add(html.substring(idx, endIdx).trim());
							idx = endIdx;
						}
					}
				}
				break;
			case OlioConfiguration.PHP_INCARNATION:
				{
					String search1 = "<a href=\"events.";
					String search2 = "?socialEventID=";
					int idx = 0;
					for (;;)
					{
						idx = html.indexOf(search1, idx);
						if (idx == -1)
						{
							break;
						}
						// We skip this the php or jsp, just knowing it is 3 chars
						idx += search1.length() + 3;
						// Check matching search2
						if (html.indexOf(search2, idx) == idx)
						{
							idx += search2.length();
							int endIdx = html.indexOf("\"", idx);
							if (endIdx == -1)
							{
								break;
							}
							eventIdSet.add(html.substring(idx, endIdx).trim());
							idx = endIdx;
						}
					}
				}
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				{
					String search1 = "<a href=\"/events/";
					int idx = 0;
					String eventItem = "";
					for (;;)
					{
						idx = html.indexOf(search1, idx);
						if (idx == -1)
						{
							break;
						}
						// We skip this the php or jsp, just knowing it is 3 chars
						idx += search1.length();
						int endIdx = html.indexOf("\"", idx);
						if (endIdx == -1)
						{
							break;
						}

						eventItem = html.substring(idx, endIdx).trim();
						if (!eventItem.contains("tagged") && !eventItem.contains("new") )
						{
							eventIdSet.add(html.substring(idx, endIdx).trim());
						}
						idx = endIdx;
					}
				}
				break;
		}

		int size = eventIdSet.size();
		if (size == 0)
		{
			return INVALID_EVENT_ID;
		}

		String[] eventIds = new String[size];
		eventIds = eventIdSet.toArray(eventIds);

		String str = eventIds[this.generateInt(0, size-1)];

		return Integer.parseInt(str);
	}

	/**
	 * Generates a random integer number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	private int generateInt(int x, int y)
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
	private long generateLong(long x, long y)
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
	private double generateDouble(double x, double y)
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
//	private int NURand( int A, int x, int y )
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
	private String generateAlphaNumString(int x, int y)
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
	private String generateAlphaString( int x, int y )
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
	private String generateNumString(int x, int y)
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
	private Date generateDateInInterval(Date refDate, int x, int y)
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
	private Calendar generateCalendarInInterval(Calendar refCal, int min, int max, int units)
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
	private Calendar generateCalendarInInterval(Calendar min, Calendar max)
	{
		long minMs = min.getTimeInMillis();
		long maxMs = max.getTimeInMillis();
		
		// We use cloning so Calendar type, timezone, locale, and stuff
		// stay the same as min.
		Calendar result = (Calendar) min.clone();
		result.setTimeInMillis(this.generateLong(minMs, maxMs));
		
		return result;
	}

	private String generateTimeZone()
	{
		return TIMEZONES[this.generateInt(0, TIMEZONES.length-1)];
	}

	// Phone is 0018889990000 or 0077669990000 for non-US 
	// 50% of the time, do US, 50% non-us.
	private String generatePhone()
	{
		StringBuilder buf = new StringBuilder();
		String v = this.generateNumString(1, 2);

		if (v.length() == 1)
		{
			buf.append("001"); // removed space
			v = this.generateNumString(3, 3);
			buf.append(v); // removed space
		}
		else
		{
			buf.append("00").append(v);
			v = this.generateNumString(2, 2);
			buf.append(v); //removed spaces
		}

		v = this.generateNumString(3, 3);
		buf.append(v); // removed space
		v = this.generateNumString(4, 4);
		buf.append(v);

		return buf.toString();
	}

	/**
	 * Generates a pseudorandom country.
	 * 
	 * @return Half the time, USA; otherwise, a random string.
	 */
    private String generateCountry()
	{
//		String country = "USA";
//
//		int toggle = this.generateInt(0, 1);
//		if (toggle == 0)
//		{
//			StringBuilder buffer = new StringBuilder(255);
//			country = this.generateName(buffer, 6, 16).toString();
//		}
//
//		return country;
        return COUNTRIES[this.generateInt(0, COUNTRIES.length-1)];
    }

	private String generateName(int minLength, int maxLength)
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
	private String generateText(int x, int y)
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

	private String[] generateAddressParts()
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
	private String generateStreet1()
	{
		StringBuilder buffer = new StringBuilder(255);
		buffer.append(this.generateNumString(1, 5)).append(' '); // Number
		buffer.append(this.generateName(1, 11)); // Street Name

		String[] STREETEXTS = {"Blvd", "Ave", "St", "Ln", ""};
		String streetExt = STREETEXTS[this.generateInt(0, STREETEXTS.length-1)];
		if (streetExt.length() > 0)
		{
			buffer.append(' ').append(streetExt);
		}

		return buffer.toString();
	}

	/**
	 * Generates the second line of a pseudorandom street address.
	 * 
	 * @return      Half the time, a second line of a random street address;
	 *              otherwise an empty string.
	 */
	private String generateStreet2()
	{
		String street = "";

		int toggle = this.generateInt(0, 1);
		if (toggle > 0)
		{
			street = this.generateAlphaString(5, 20);
		}

		return street;
	}

//	/**
//	 * Randomly selects an event from the events page.
//	 * @param r The random value generator
//	 * @param eventListPage The page from the response buffer
//	 * @return The selected event id, as a string
//	 */
//	private String generateEventId(StringBuilder eventListPage)
//	{
//		return this.generateEvent(eventListPage, null);
//	}
//
//	private String generateEventId(StringBuilder eventListPage, String pageType)
//	{
//		String search1 = null;
//		switch (this._cfg.getIncarnation())
//		{
//			case JAVA_INCARNATION:
//				search1 = "<a href=\"" + OlioGenerator.CONTEXT_ROOT + "/event/detail?socialEventID=";
//				break;
//			case RAILS_INCARNATION:
//				search1 = "<a href=\"" + OlioGenerator.CONTEXT_ROOT + "/events/";
//				break;
//		}
//		int idx = 0;
//		Set<String> eventIdSet = new HashSet<String>();
//		String eventItem = "";
//		for (;;)
//		{
//			idx = eventListPage.indexOf(search1, idx);
//			if (idx == -1)
//			{
//				break;
//			}
//			// We skip this the php or jsp, just knowing it is 3 chars
//			idx += search1.length();
//			int endIdx = eventListPage.indexOf("\"", idx);
//			if (endIdx == -1)
//			{
//				break;
//			}
//
//			eventItem = eventListPage.substring(idx, endIdx).trim();
//			if (!eventItem.contains("tagged") && !eventItem.contains("new"))
//			{
//				eventIdSet.add(eventListPage.substring(idx, endIdx).trim());
//			}
//			idx = endIdx;
//		}
//		int size = eventIdSet.size();
//		if (size == 0)
//		{
//			if (pageType != null && !pageType.equals("TagSearch"))
//			{
//				this._logger.severe(search1 + " not found in " + pageType + " response! Response was:\n\n" + eventListPage + "\n\n\n");
//			}
//			return null;
//		}
//
//		String[] eventIds = new String[size];
//		eventIds = eventIdSet.toArray(eventIds);
//		return eventIds[this.generateInt(0, size-1)];
//	}

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
			tagSet.add(this.generateTagId(0.1D));
		}

		List<String> tags = new ArrayList<String>();
		for (int tagId : tagSet)
		{
			tags.add(this.generateUserName(tagId));
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

		String strDate = this._dateFmt.format(this.generateDateInInterval(BASE_DATE, 0, 540));
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
	private int generateTagId(double meanRatio)
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
		return this.generateUserName(this.generateInt(1, ScaleFactors.tags));
		//return this.generateUserName(randomTagId(r, 0.1));
	}
}
