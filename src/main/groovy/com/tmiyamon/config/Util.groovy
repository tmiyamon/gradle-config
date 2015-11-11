package com.tmiyamon.config

class Util {
	public static String camelize(String cmdName, boolean firstUpper = false) {
		def words = cmdName.split("\\_");
		return (firstUpper ? capitalize(words[0]) : words[0]) + words[1..(words.size() -1)].collect({capitalize(it)}).join()
	}

	public static String capitalize(String a){
		String s = a.substring(1)
		return "${a[0].toUpperCase()}${s.toLowerCase()}"
	}
}

