package org.example.tools;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base32;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/************************
 * Made by [MR Ferry™]  *
 * on November 2025     *
 ************************/

public class Tzt{
	@SneakyThrows
	static void main() {
		String instanceId = generateInstanceId();
		System.out.println("instanceId = " + instanceId);
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//		MessageDigest messageDigest2 = MessageDigest.getInstance("SHA256");
		Set<Object> strings = HashSet.newHashSet(1000000);
		SecureRandom instanceStrong = SecureRandom.getInstanceStrong();
		System.out.println("instanceStrong.getAlgorithm() = " + instanceStrong.getAlgorithm());
		for(int i = 0; i <= 100000000; i++){
			byte[] bytes = new byte[6];
			instanceStrong.nextBytes(bytes);
//			byte[] digest = messageDigest.digest(bytes);
//			Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
			Base32 base32 = new Base32();
			String x = base32.encodeToString(bytes).replace("=", "");
//			BigInteger x = new BigInteger(bytes).abs();
			if(!strings.add(x)){
				System.out.println("x = " + x);
				System.out.println("i = " + i);
//				System.out.println("strings = " + strings);
				break;
			}
			if(i == 100000000){
				System.out.println("lastx = " + x);
			}
		}
		System.out.println("strings = " + strings.size());
	}

	private static String generateInstanceId() {
		try {
			// Use machine-specific identifier
			String hostname = InetAddress.getLocalHost().getHostName();
			System.out.println("hostname = " + hostname);
			return Integer.toHexString(hostname.hashCode());
		} catch (Exception e) {
			return UUID.randomUUID().toString().substring(0, 4);
		}
	}
}
