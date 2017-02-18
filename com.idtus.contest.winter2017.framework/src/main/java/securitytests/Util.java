package securitytests;

import java.util.List;
import java.util.Random;

public class Util {
	static <T> T pickRandomValue(T[] list, Random random) {
		int index = random.nextInt(list.length);
		return list[index];
	}

	static <T> T pickRandomValue(List<T> list, Random random) {
		int index = random.nextInt(list.size());
		return list.get(index);
	}

	static char generateRandomChar(Random random) {
		return (char) (random.nextInt(256 - 0x20) + 0x20);
	}

	/** Generates reasonably well-formed random strings */
	static String generateRandomString(int length, Random random) {
		char[] arr = new char[length];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = generateRandomChar(random);
		}
		return String.valueOf(arr);
	}

	/** Generates less well-formed random UTF-16 strings */
	static String generateBadRandomString(int length, Random random) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.appendCodePoint(random.nextInt(0x20000 - 1) + 1);
		}
		return sb.toString();
	}

}
