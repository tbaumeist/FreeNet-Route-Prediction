package frp.fileConverters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import frp.utils.CmdLineTools;


public class GraphFileConverter {
	
	private final int INPUT_FLAG_I = 0;
	private final int HELP_FLAG_I = 1;

	private final String[][] PROG_ARGS = {
			{ "-i", "(required) input graph .dot file." },
			{ "-h", "help command. Prints available arguments." } };

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GraphFileConverter(args);

	}
	
	private GraphFileConverter(String[] args) {
		try {
			List<String> lwArgs = Arrays.asList(args);
			
			if(lwArgs.contains(CmdLineTools.getName(PROG_ARGS, HELP_FLAG_I))){
				System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
				return;
			}

			// // Arguments ////
			String inputFileName = CmdLineTools.getRequiredArg(CmdLineTools.getName(
					PROG_ARGS, INPUT_FLAG_I), lwArgs);
			
			String outputFile = inputFileName+".graphml";
			String tmpOutputFile = outputFile+".tmp";
			
			Runtime rt = Runtime.getRuntime();
			String[] cmds = { "graph-easy","--as=graphml", "--input="+inputFileName, "--output="+tmpOutputFile };
			String[] env = { };
			Process p1 = rt.exec(cmds, env);
			p1.waitFor();
			
			PrintStream writer = new PrintStream(new File(outputFile));
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(tmpOutputFile)));
			Pattern pattern = Pattern.compile("(.*)(<edge source=\")(0.[0-9]*)(.*target=\")(0.[0-9]*)(.*)");
			Pattern startPattern = Pattern.compile("</key>");
			String line = "";
			boolean addedDiff = false;
			while((line = reader.readLine()) != null){
				writer.println(line);
				
				if(!addedDiff && startPattern.matcher(line).find()){
					writer.println("  <key attr.name=\"difference\" attr.type=\"double\" for=\"edge\" id=\"difference\"/>");
					addedDiff = true;
				}
				
				Matcher matcher = pattern.matcher(line);
				if(matcher.find()){
					double d1 = Double.parseDouble(matcher.group(3));
					double d2 = Double.parseDouble(matcher.group(5));
					double diff = (Math.min(d1, d2) + 1) - Math.max(d1, d2);
					if(Math.max(d1, d2) - Math.min(d1, d2) <  diff)
						diff = Math.max(d1, d2) - Math.min(d1, d2);
					writer.println(matcher.group(1) + "  <data key=\"difference\">"+ diff +"</data>");
				}
			}
			File tmp = new File(tmpOutputFile);
			tmp.delete();
			

		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println(ex.getMessage());
			System.out.println("!!!Error closing program!!!");
		}
	}

}

