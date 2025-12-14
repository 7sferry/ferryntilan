package org.example.tools;

/************************
 * Made by [MR Ferry™]  *
 * on November 2025     *
 ************************/

import de.huxhorn.sulky.ulid.ULID;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CrockfordBase32 {
	private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
	private static final Map<Character, Integer> DECODE_MAP;

	static {
		Map<Character, Integer> map = new HashMap<>();
		for (int i = 0; i < ALPHABET.length(); i++) {
			map.put(ALPHABET.charAt(i), i);
		}

		map.put('O', 0);
		map.put('o', 0);
		map.put('I', 1);
		map.put('i', 1);
		map.put('L', 1);
		map.put('l', 1);
		DECODE_MAP = Collections.unmodifiableMap(map);
	}

	public static String encodeWithPadding(long timestamp) {
		char[] out = new char[10];

		// 48-bit timestamp → 10 chars
		long value = timestamp;
		for (int i = 9; i >= 0; i--) {
			out[i] = ALPHABET.charAt((int) (value & 0x1F));
			value >>= 5;
		}
		return new String(out);
	}

	public static String encode(byte[] data) {
		if (data == null || data.length == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		int buffer = 0;
		int bitsLeft = 0;

		for (byte b : data) {
			buffer = (buffer << 8) | (b & 0xFF);
			bitsLeft += 8;

			while (bitsLeft >= 5) {
				int index = (buffer >>> (bitsLeft - 5)) & 0x1F;
				result.append(ALPHABET.charAt(index));
				bitsLeft -= 5;
			}
		}

		// Handle remaining bits
		if (bitsLeft > 0) {
			int index = (buffer << (5 - bitsLeft)) & 0x1F;
			result.append(ALPHABET.charAt(index));
		}

		return result.toString();
	}

	public static byte[] decode(String encoded) {
		if (encoded == null || encoded.isEmpty()) {
			return new byte[0];
		}

		// Remove hyphens and convert to uppercase
		String cleaned = encoded.replace("-", "").toUpperCase();

		int buffer = 0;
		int bitsLeft = 0;
		int count = 0;

		// Calculate output length
		byte[] temp = new byte[cleaned.length() * 5 / 8];

		for (char c : cleaned.toCharArray()) {
			Integer value = DECODE_MAP.get(c);
			if (value == null) {
				throw new IllegalArgumentException("Invalid character in encoded string: " + c);
			}

			buffer = (buffer << 5) | value;
			bitsLeft += 5;

			if (bitsLeft >= 8) {
				temp[count++] = (byte) ((buffer >>> (bitsLeft - 8)) & 0xFF);
				bitsLeft -= 8;
			}
		}

		// Validate no trailing bits that would cause ambiguous padding
		if (bitsLeft >= 5) {
			throw new IllegalArgumentException("Invalid padding in encoded string");
		}

		if ((buffer & ((1 << bitsLeft) - 1)) != 0) {
			throw new IllegalArgumentException("Non-zero trailing bits in encoded string");
		}

		byte[] result = new byte[count];
		System.arraycopy(temp, 0, result, 0, count);
		return result;
	}

	public static String encodeNumber(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("Value must be non-negative");
		}
		if (value == 0) {
			return "0";
		}

		char[] buffer = new char[13]; // max chars for 64-bit value (ceil(64/5)=13)
		int pos = buffer.length;

		while (value > 0) {
			int index = (int)(value & 0x1F); // take 5 bits
			buffer[--pos] = ALPHABET.charAt(index);
			value >>>= 5;
		}

		return new String(buffer, pos, buffer.length - pos);
	}

	public static long decodeNumber(String encoded) {
		if (encoded == null || encoded.isEmpty()) {
			throw new IllegalArgumentException("Encoded string cannot be null or empty.");
		}

		long result = 0;

		for (int i = 0; i < encoded.length(); i++) {
			char c = encoded.charAt(i);

			// 1. Check if the character is in our lookup map
			Integer value = DECODE_MAP.get(c);

			if (value == null) {
				// To handle case-insensitivity, you could try DECODE_MAP.get(Character.toUpperCase(c))
				// here, but for strictness, we'll throw an error.
				throw new IllegalArgumentException("Invalid character in encoded string: " + c);
			}

			// 2. Shift the running result by 5 bits to the left
			// This prepares space for the new 5-bit value.
			result <<= 5;

			// 3. Add the character's 5-bit value to the result using bitwise OR
			result |= value;

			// Optional Safety Check (highly recommended for production code):
			// Check for overflow before the next shift/add. A full 64-bit
			// long can only hold 13 Base32 characters. If the length exceeds 13
			// and the value is large, an overflow can occur.
			// We skip the explicit overflow check here for simplicity,
			// trusting the input was created by the corresponding encode function.
		}

		return result;
	}

	// Example usage
	static void main(String[] args) throws UnknownHostException{
		System.out.println(Integer.toHexString(10));
		ULID ulid = new ULID();
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[6];
		System.out.println("ulid.nextULID() = " + ulid.nextULID());
//		ByteBuffer allocate = ByteBuffer.allocate(16);
//		allocate.putLong(System.currentTimeMillis());
		random.nextBytes(bytes);
//		allocate.put(bytes);
		System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
		System.out.println("base32.encodeToString(allocate.array()) = " + encode(bytes));
		System.out.println("base32.decode(base32.encodeToString(allocate.array())) = " + encodeNumber(Inet6Address.getLocalHost().getHostName().hashCode()));
		System.out.println(Integer.toHexString(Inet6Address.getLocalHost().getHostAddress().hashCode()));
	}
}
