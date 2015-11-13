package com.tmiyamon.config

class Util {
	public static String camelize(String cmdName, boolean firstUpper = false) {

		def words = cmdName.split("\\_");
		return (firstUpper ? capitalize(words[0]) : words[0]) + words[1..(words.size() -1)].collect({capitalize(it)}).join()
	}

	public static String className(String str) {
		return str.split('_').collect { it[0].toUpperCase() + it[1..-1] }.join()
	}

	public static String capitalize(String a){
		String s = a.substring(1)
		return "${a[0].toUpperCase()}${s.toLowerCase()}"
	}

	//http://blog.mathieu.carbou.me/post/103163278870/deep-merge-map-in-groovy
	public static Map deepMerge(Map onto, Map... overrides) {
		if (!overrides) {
			return onto
		} else if (overrides.length == 1) {
			overrides[0]?.each { k, v ->
				if (v instanceof Map && onto[k] instanceof Map) {
					deepMerge((Map) onto[k], (Map) v)
				} else {
					onto[k] = v
				}
			}
			return onto
		}
		return overrides.inject(onto, { acc, override -> deepMerge(acc, override ?: [:]) })
	}
}

