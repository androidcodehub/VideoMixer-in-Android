package com.androidcodehub.demo.utils;

import java.util.regex.Pattern;

public class IsUtils {

	private static final String PATTERN_ALPHABETIC_OR_NUMBERIC = "[A-Za-z0-9]*";
	private static final String PATTERN_NUMBERIC = "\\d*\\.{0,1}\\d*";


	public static boolean isAlphabeticOrNumberic(String str) {
		return Pattern.compile(PATTERN_ALPHABETIC_OR_NUMBERIC).matcher(str).matches();
	}


	public static boolean isNumeric(String str) {
		return Pattern.compile(PATTERN_NUMBERIC).matcher(str).matches();
	}


	public static boolean isNullOrEmpty(String str) {
		return (str == null || str.length() == 0);
	}


	public static boolean isNullOrEmpty(final Object str) {
		return (str == null || str.toString().length() == 0);
	}


	public static boolean isNullOrEmpty(final String... strs) {
		if (strs == null || strs.length == 0) {
			return true;
		}
		for (String str : strs) {
			if (str == null || str.length() == 0) {
				return true;
			}
		}
		return false;
	}


	public static boolean find(String str, String c) {
		if (isNullOrEmpty(str)) {
			return false;
		}
		return str.indexOf(c) > -1;
	}

	public static boolean findIgnoreCase(String str, String c) {
		if (isNullOrEmpty(str)) {
			return false;
		}
		return str.toLowerCase().indexOf(c.toLowerCase()) > -1;
	}
	

	public static boolean equals(String str1, String str2) {
		if (str1 == str2)
			return true;

		if (str1 == null)
			str1 = "";
		return str1.equals(str2);
	}
}
