///****************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2017 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// *
// ***************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created by dobrin on 6/30/17.
// */
//class AnalyticsContextDataHelper {
//
//	static Map<String, Object> getContextData(String source) {
//		return getContextData(source, "c");
//	}
//
//
//	static Map<String, Object> getContextData(String source, String tag) {
//		Map<String, Object> contextData = new HashMap<String, Object>(64);
//		Pattern pattern = Pattern.compile(".*(&" + tag + "\\.(.*)&\\." + tag + ").*");
//		Matcher matcher = pattern.matcher(source);
//
//		if (!matcher.matches()) {
//			return contextData;
//		}
//
//		String contextDataString = matcher.group(2);
//
//		if (contextDataString == null) {
//			return contextData;
//		}
//
//		Map<String, Object> additionalData = new HashMap<String, Object>(64);
//		String additionalDataString = source.replace(contextDataString, "");
//
//		for (String param : additionalDataString.split("&")) {
//			String[] kvpair = param.split("=");
//
//			if (kvpair.length != 2) {
//				additionalData.put(param, "");
//			} else {
//				additionalData.put(kvpair[0], kvpair[1]);
//			}
//		}
//
//		List<String> keyPath = new ArrayList<String>(16);
//
//		for (String param : contextDataString.split("&")) {
//			if (param.endsWith(".") && !param.contains("=")) {
//				keyPath.add(param);
//			} else if (param.startsWith(".") && keyPath.size() > 0) {
//				keyPath.remove(keyPath.size() - 1);
//			} else {
//				String[] kvpair = param.split("=");
//
//				if (kvpair.length != 2) {
//					continue;
//				}
//
//				String contextDataKey = contextDataStringPath(keyPath, kvpair[0]);
//
//				try {
//					contextData.put(contextDataKey, java.net.URLDecoder.decode(kvpair[1], "UTF-8"));
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return contextData;
//	}
//
//	static Map<String, Object> getCidData(String source) {
//		return getContextData(source, "cid");
//	}
//
//	static Map<String, String> getAdditionalData(String source) {
//		Pattern pattern = Pattern.compile(".*(&c\\.(.*)&\\.c).*");
//		Matcher matcher = pattern.matcher(source);
//		String addtionalDataString = source;
//
//		if (matcher.matches() && matcher.group(2) != null) {
//			addtionalDataString = source.replace(matcher.group(2), "");
//		}
//
//		pattern = Pattern.compile(".*(&cid\\.(.*)&\\.cid).*");
//		matcher = pattern.matcher(source);
//
//		if (matcher.matches() && matcher.group(2) != null) {
//			addtionalDataString = addtionalDataString.replace(matcher.group(2), "");
//		}
//
//		Map<String, String> additionalData = new HashMap<String, String>();
//
//		for (String param : addtionalDataString.split("&")) {
//			String[] kvpair = param.split("=");
//
//			if (kvpair.length != 2) {
//				additionalData.put(param, "");
//			} else {
//				additionalData.put(kvpair[0], kvpair[1]);
//			}
//		}
//
//		return additionalData;
//	}
//
//	static String contextDataStringPath(List<String> keyPath, String lastComponent) {
//		StringBuilder sb = new StringBuilder();
//
//		for (String pathComponent : keyPath) {
//			sb.append(pathComponent);
//		}
//
//		sb.append(lastComponent);
//		return sb.toString();
//	}
//
//	static boolean containsAll(final Map<String, String> expectedValues, final Map<String, String> actualValues) {
//		for (Map.Entry<String, String> elem : expectedValues.entrySet()) {
//			if (!elem.getValue().equals(String.valueOf(actualValues.get(elem.getKey())))) {
//				return false;
//			}
//		}
//
//		return true;
//	}
//}
