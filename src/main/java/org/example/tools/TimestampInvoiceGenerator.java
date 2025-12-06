package org.example.tools;

import org.apache.commons.codec.binary.Base32;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/************************
 * Made by [MR Ferry™]  *
 * on November 2025     *
 ************************/

public class TimestampInvoiceGenerator {
	private final String instanceId;
	private final AtomicInteger counter = new AtomicInteger(0);
	private final DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public TimestampInvoiceGenerator() {
		// Use machine identifier for distributed systems
		this.instanceId = getInstanceIdentifier();
	}

	public String generateInvoiceId() {
		String timestamp = LocalDateTime.now().format(formatter);
		int seq = counter.incrementAndGet() % 1000; // Reset after 999
		return String.format("INV-%s-%s-%03d", timestamp, instanceId, seq);
		// Example: INV-20240520143015-APP1-001
	}

	private String getInstanceIdentifier() {
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			return hostname.substring(Math.max(0, hostname.length() - 4));
		} catch (Exception e) {
			return "0001"; // Fallback
		}
	}

	public interface Generator{
		Generator withPrefix(String prefix);

		Generator withByteSize(int size);

		Generator withDelimiter(String delimiter);

		Generator withDateFormat(String dateFormat);

		Generator withMachineId(boolean usingMachineId);

		String generate();

		Generator copy();
	}

	public static class SerialNumberGenerator implements Generator{
		private static final AtomicLong COUNTER = new AtomicLong();
		private static final String MACHINE_ID = getMachineId();
		private static final SecureRandom RANDOM = initRandom();

		private static SecureRandom initRandom(){
			try{
				return SecureRandom.getInstanceStrong();
			} catch(NoSuchAlgorithmException e){
				return new SecureRandom();
			}
		}

		private String prefix;
		private int byteSize = 8;
		private String delimiter;
		private DateTimeFormatter dateFormat;
		private boolean usingMachineId;

		public SerialNumberGenerator(){}

		private SerialNumberGenerator(SerialNumberGenerator origin){
			this.prefix = origin.prefix;
			this.byteSize = origin.byteSize;
			this.delimiter = origin.delimiter;
			this.dateFormat = origin.dateFormat;
			this.usingMachineId = origin.usingMachineId;
		}

		private static String getMachineId(){
			try{
				return Integer.toHexString(InetAddress.getLocalHost().getHostName().hashCode());
			} catch(UnknownHostException e){
				return UUID.randomUUID().toString();
			}
		}

		@Override
		public Generator withPrefix(String prefix){
			this.prefix = prefix;
			return this;
		}

		@Override
		public Generator withByteSize(int size){
			if(size <= 0){
				throw new IllegalArgumentException("size must be positive");
			}
			this.byteSize = size;
			return this;
		}

		@Override
		public Generator withDelimiter(String delimiter){
			this.delimiter = delimiter;
			return this;
		}

		@Override
		public Generator withDateFormat(String dateFormat){
			this.dateFormat = DateTimeFormatter.ofPattern(dateFormat);
			return this;
		}

		@Override
		public Generator withMachineId(boolean usingMachineId){
			this.usingMachineId = usingMachineId;
			return this;
		}

		@Override
		public String generate(){
			StringJoiner joiner = new StringJoiner(this.delimiter != null ? this.delimiter : "");
			if(this.prefix != null && !this.prefix.isEmpty()){
				joiner.add(this.prefix);
			}
			if(this.dateFormat != null){
				joiner.add(dateFormat.format(LocalDateTime.now()));
			}
			if(this.usingMachineId){
				joiner.add(MACHINE_ID.substring(0, Math.min(4, MACHINE_ID.length())));
			}
			byte[] bytes = new byte[this.byteSize];
			RANDOM.nextBytes(bytes);
			Base32 base32 = new Base32();
			joiner.add(base32.encodeToString(bytes).replace("=", ""));
//			joiner.add(HexFormat.of().withUpperCase().formatHex(bytes));
//			joiner.add(HexFormat.of().withUpperCase().formatHex(bytes));
			return joiner.toString();
		}

		private static final Generator COMMON = new SerialNumberGenerator()
				.withDelimiter("-")
				.withMachineId(true)
				.withByteSize(6);

		public static Generator getCommonGenerator(){
			return COMMON.copy();
		}
		@Override
		public Generator copy(){
			return new SerialNumberGenerator(this);
		}
	}

	static void main(){
		Generator prdGenerator = SerialNumberGenerator.getCommonGenerator()
				.withPrefix("PRD");
		Generator ordGenerator = SerialNumberGenerator.getCommonGenerator()
				.withPrefix("ORD")
				.withDateFormat("yyyyMMdd");
		Generator invGenerator = SerialNumberGenerator.getCommonGenerator()
				.withPrefix("INV")
				.withDelimiter("-")
				.withMachineId(false)
				.withByteSize(10)
				.withDateFormat("yyyyMMddHHmm");

		String prdGenerated = prdGenerator.generate();
		System.out.println("result = " + prdGenerated);

		String ordGenerated = ordGenerator.generate();
		System.out.println("result = " + ordGenerated);

		String invGenerated = invGenerator.generate();
		System.out.println("result = " + invGenerated);

		String prdGenerated2 = prdGenerator.generate();
		System.out.println("result = " + prdGenerated2);

		String ordGenerated2 = ordGenerator.generate();
		System.out.println("result = " + ordGenerated2);

		String invGenerated2 = invGenerator.generate();
		System.out.println("result = " + invGenerated2);
	}
}
