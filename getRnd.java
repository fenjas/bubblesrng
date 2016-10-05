import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class getRnd {

	static Options options = new Options();
	static CommandLineParser parser = new DefaultParser();
	static HelpFormatter formatter = new HelpFormatter();


	static void defineArguments(){
		options.addOption("f", "file", true,"Path to the binary sequence.");  								
		options.addOption("t", "type", true,"The format random numbers are extracted in; 8|16|32-bit. Default t=32.");  	
		options.addOption("n", "total",true,"Total random numbers to extract. Default: n=1.");
		options.addOption("d", "delimeter", true,"Delimiter type; c|s|t. Default: d='c'.");									   
		options.addOption("c", "columns", true,"Group numbers by columns (max. 100). Default: c=1.");			
		options.addOption("s", "seed", false,"Seeds java.util.Random(seed). Default: No seeding.");									
		options.addOption("i", "interval", true,"Re-seeding interval. Default: i=1.");							
		options.addOption("h", "help", false,"Displays this help screen.");
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

		String fName="", total="", type="", cols="", delim="",seedInt="";
		int t=32, c=1, n=1, i=1, bit8=0, bit16=0, bit32=0;
		char delimit=',';
		long fsize; 
		boolean seed=false;

		defineArguments();

		try {
			CommandLine cmd = parser.parse(options, args);

			//Display Help
			if (cmd.hasOption("h") || args.length==0) {
			
				formatter.printHelp("getRnd -f BubblesRNG.bin -t 32 -n 100 -seed", options );
				System.exit(0);
			}
			
			//No dice if filename is not specified
			if (cmd.hasOption("f")) 
			{
				fName = cmd.getOptionValue("f");

				//Check if file exists first
				if (!(new File(fName).isFile())) {
					System.out.println("File " + fName + " not found!");
					System.exit(0); }
				else 
				{
					fsize = new File(fName).length();

					//Calculate how many random numbers can be extracted by type
					bit8 =(int) (fsize/8);
					bit16=(int) (fsize/16);
					bit32=(int) (fsize/32);

					//Print to screen
					System.out.println();
					System.out.println("File size       : " + fsize);
					System.out.println("------------------------------");
					System.out.println(" 8-bit integers : " + bit8);
					System.out.println("16-bit integers : " + bit16);
					System.out.println("32-bit integers : " + bit32);
					System.out.println("------------------------------\n");

					//Random number output type
					if(cmd.hasOption("t")){
						type=cmd.getOptionValue("t");
						t=Integer.valueOf(type);
						if (t!=8 && t!=16 && t!=32){
							t=32; 
							System.out.println("Defaulting to generating 32-bit numbers");}
					}

					//Total random numbers to extract
					if(cmd.hasOption("n")){
						total=cmd.getOptionValue("n");
						n=Integer.valueOf(total);

						//Default to the max. number of extractable numbers if user exceeds limit
						if (t==8 && n>bit8) {n=bit8;}
						if (t==16 && n>bit16) {n=bit16;}
						if (t==32 && n>bit32) {n=bit32;}
						if (n<1) n=1;
					}

					//No. of formatting columns
					if(cmd.hasOption("c")){
						cols=cmd.getOptionValue("c");
						c=Integer.valueOf(cols);
						if (c>100 || c<1) c=1;
					}

					//Delimiter type
					if(cmd.hasOption("d")){
						delim=cmd.getOptionValue("d");
						if (delim.equals("c")) delimit = ','; else
							if (delim.equals("s")) delimit = ' '; else
								if (delim.equals("t")) delimit = '	'; else delimit='\t';
					}

					//Seed?
					if(cmd.hasOption("s")){
						seed=true;

						//Seeding interval
						if(cmd.hasOption("i")){
							seedInt=cmd.getOptionValue("i");
							i=Integer.valueOf(seedInt);
							if (i<1 || i>n) n=1;
						}
					}

					//Call main method
					readBinaryFile(fName,t,n,c,delimit,seed,i);
				}
			}
		} 
		catch (ParseException exp) {
			System.err.println( "Argument parsing failed due to : " + exp.getMessage() );
			System.out.println();
			formatter.printHelp("getRnd -f BubblesRNG.bin -t 32 -n 100 -seed", options );
			System.exit(0);
		}
	}

	@SuppressWarnings("unchecked")
	private static void readBinaryFile(String fName, int type, int total, int cols, char delimit, boolean seed, int seedInt) throws IOException, NoSuchAlgorithmException
	{
		long number=0;
		boolean endOfFile = false;
		byte[] bit16 = new byte[2]; 
		byte[] bit32 = new byte[4];
		@SuppressWarnings("rawtypes")
		ArrayList rndNums = new ArrayList();

		FileInputStream fstream =   new FileInputStream(fName);
		DataInputStream inputFile = new DataInputStream(fstream);
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(total + "_" + type + "bit_BubblesRNG_numbers.txt"));

		int i=0;

		while (!endOfFile && i<total)
		{
			try
			{
				if (type==8) {number = inputFile.readUnsignedByte();
				writer.write(Long.toString(number));
				}

				else if (type==16) {
					inputFile.read(bit16);
					number=new BigInteger(1,bit16).longValue();
					writer.write(String.valueOf(number));
				}

				else if (type==32) {
					inputFile.read(bit32);
					number=new BigInteger(1,bit32).longValue();
					writer.write(String.valueOf(number));
				}

				i++;
				
				//Save current random number to a list
				rndNums.add(number);

				//Write 2 tabs in case of 32 bit numbers < 10,000,000 to preserve formatting
				if (i % cols==0) writer.newLine(); else if (i<total && cols!=1) writer.write(delimit);

				//Rem add extra tab to preserve column formatting when 32-bit numbers are written to file
				if (number>9999999 && delimit=='\t' && type==32 && !(i % cols==0)) writer.write(delimit); 
				if (number<10000000 && delimit=='\t' && type==32 && !(i % cols==0)) {writer.write(delimit);writer.write(delimit);}

			}
			catch (EOFException e)
			{
				endOfFile = true;
			}
		}

		inputFile.close();
		writer.close();

		System.out.println("Extracted " + total + " " + type + "-bit random numbers from " + fName);
		System.out.println();


		//If seeding has been specified ...
		if (seed)
		{
			System.out.print("Seeding PRNG ...");

			File file = new File(rndNums.size()*seedInt+"_"+"long_random("+seedInt+ ")_numbers.txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			Random r=new Random();
			long rn=0;
			int x=0,j=0;

			for (j=0; j < rndNums.size(); j++)
			{
				//Seed PRNG using rnd nums stored in list
				r.setSeed((long) rndNums.get(j));

				//Generate rnd nums and replace seed every <seedInt> runs
				for (x=0; x<seedInt; x++){
				
					//Next random number
					rn = r.nextLong()>>>1; //Logical shift to eliminate -ve numbers
					bw.write((String.valueOf(rn)));
					bw.newLine();
					
					//Print a dot for every 100 nums generated
					if (x  % 20 == 0) System.out.print(".");
				}
										
			}

			System.out.println();
			System.out.println(seedInt*j + " random integers generated");
			bw.close();
		}
	}
}
