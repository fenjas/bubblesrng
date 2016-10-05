package bubblesRNG;

public class javaRNGs {

	public static void main(String[] args) throws Exception {

		int bytesToCapture=1048576;
		
		fileTools.genSecRnd("c:/bubbles/n_prng.bin",bytesToCapture);
		fileTools.genStdRnd("c:/bubbles/s_prng.bin",bytesToCapture);

	}
	}
