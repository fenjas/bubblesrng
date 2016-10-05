package bubblesRNG;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class functions {

	final static String CIPHER_NAME = "AES/ECB/PKCS5Padding";
	final static String ALGORITHM_NAME = "AES"; // keySizes 128 only (http://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html)


	//Generate Key
	static SecretKey keyGen(String algorithm, int keySize) throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
		keygen.init(keySize);
		return keygen.generateKey();
	}

	//Encryption routine using AES
	static byte [] encrypt(SecretKey key, int S) throws Exception, NoSuchPaddingException {
		BigInteger bignum = BigInteger.valueOf(S);
		Cipher cipher = Cipher.getInstance(CIPHER_NAME);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		System.out.println(new BigInteger(cipher.doFinal(bignum.toByteArray())).intValue());
		return cipher.doFinal(bignum.toByteArray());
	}

	//Generates a randmom number to be used for as a Xor operand
	static int xorOp() throws NoSuchAlgorithmException{
		SecretKey key = keyGen(ALGORITHM_NAME,128);
		byte[] data = key.getEncoded();
		int r=0;
	
		for (int i=0; i<data.length; i++)  {
			r+=data[i];}

		return Math.abs(r%256);
	}

	//Random Number Generator method

	//static int generateRnd(Integer x, Integer y, boolean XOR, boolean Encrypt, boolean Hash) throws NoSuchPaddingException, NoSuchAlgorithmException, Exception{
	static int generateRnd(Integer rData, boolean XOR, boolean Encrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, Exception{
		int rndValue = 0, ct=0, S=0;	

		S = rData;

		if (!XOR && !Encrypt){  //No whitening
			rndValue=S;}
		else
			if (XOR && !Encrypt){ //XOR only
				rndValue=S  ^ xorOp();}
			else
				if (!XOR && Encrypt){ //Encrypt only
					rndValue=Math.abs(new BigInteger(encrypt(keyGen(ALGORITHM_NAME,128),S)).intValue());}
				else
					if (XOR && Encrypt){ //XOR and Encrypt
						ct=Math.abs(new BigInteger(encrypt(keyGen(ALGORITHM_NAME,128),S)).intValue());
						rndValue=(ct ^ xorOp());}

		return rndValue % 256;
	}



	//Return mean arithmetic value (127.5 -> implies randomness)
	static String meanValue(List<Integer> RandomData){

		double mean=0.0;
		Double r=0.0; Number am=0.0;
		int i, size=RandomData.size();
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);

		if (size>0) {
			for (i=0; i<size; i++){
				mean+=RandomData.get(i);}
			am = (mean / i);
			r = am.doubleValue();

		}

		return df.format(r).toString(); 
	}
}
