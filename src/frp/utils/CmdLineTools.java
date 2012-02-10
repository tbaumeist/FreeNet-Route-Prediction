package frp.utils;

import java.util.List;

public class CmdLineTools {

	/*
	 * Prints out the parameters for a given program.
	 */
	public static String toStringProgArgs(String[][] progArgs) {
		String s = "HELP: Program arguments\n";
		for (String[] arg : progArgs) {
			s += arg[0] + "\t" + arg[1] + "\n";
		}
		return s;
	}

	/*
	 * Return the text name of a program parameter.
	 */
	public static String getName(String[][] args, int index) {
		return args[index][0];
	}

	/*
	 * Gets the value associated with the argument from args. If no value found,
	 * throws an error.
	 */
	public static String getRequiredArg(String argName, List<String> args)
			throws Exception {

		String value = getArg(argName, args, "");
		if (value.isEmpty())
			throw new Exception("Required argument " + argName
					+ " was not found.");
		return value;
	}

	/*
	 * If args contains the given argument then return the next value. Otherwise
	 * return the default value given.
	 */
	public static String getArg(String argName, List<String> args,
			String defaultValue) throws Exception {

		String value = defaultValue;
		if (!args.contains(argName))
			return value;
		try {
			return args.get(args.indexOf(argName) + 1);
		} catch (Exception e) {
			throw new Exception("Error reading argument " + argName
					+ ", please validate its properly formatted.");
		}
	}
}
