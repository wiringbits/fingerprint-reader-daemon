package com.neurotec.tutorials.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.neurotec.lang.NThrowable;
import com.neurotec.util.concurrent.AggregateExecutionException;

public final class Utils {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String VERSION = "11.1.0.0";
	public static final String COPYRIGHT = "Copyright © 2011-2019 Neurotechnology";

	private static final boolean DEFAULT_TRIAL_MODE = false;

	public static boolean getTrialModeFlag() throws IOException {
		Path p = Paths.get(".." + FILE_SEPARATOR + "Licenses" + FILE_SEPARATOR, "TrialFlag.txt");
		if (p.toFile().exists()) {
			String str = Files.readAllLines(p).get(0);
			return str.toLowerCase().contentEquals("true");
		} else {
			System.out.println();
			System.out.println("Failed to locate file: " + p.toString());
			System.out.println();
			return DEFAULT_TRIAL_MODE;
		}
	}

	private static int handleNThrowable(NThrowable th) {
		int errorCode = -1;
		if (th instanceof AggregateExecutionException) {
			List<Throwable> causes = ((AggregateExecutionException) th).getCauses();
			for (Throwable cause : causes) {
				if (cause instanceof NThrowable) {
					if (cause.getCause() instanceof NThrowable) {
						errorCode = handleNThrowable((NThrowable) cause.getCause());
					} else {
						errorCode = ((NThrowable) cause).getCode();
					}
					break;
				}
			}
		} else {
			errorCode = ((NThrowable) th).getCode();
		}
		return errorCode;
	}

	public static void printTutorialHeader(String description, String name, String[] args) {
		printTutorialHeader(description, name, VERSION, COPYRIGHT, args);
	}

	public static void printTutorialHeader(String description, String name, String version, String[] args) {
		printTutorialHeader(description, name, version, COPYRIGHT, args);
	}

	public static void printTutorialHeader(String description, String name, String version, String copyright, String[] args) {
		System.out.println(name);
		System.out.println();
		System.out.format("%s (Version: %s)%n", description, version);
		System.out.println(copyright.replace("©", "(C)"));
		System.out.println();
		if (args != null && args.length > 0) {
			System.out.println("Arguments:");
			for (int i = 0; i < args.length; i++) {
				System.out.format("\t%s%n", args[i]);
			}
			System.out.println();
		}
	}

	public static void writeText(String pathname, String text) throws IOException {
		if (text == null)
			throw new NullPointerException("text");
		File file = new File(pathname);
		if (file.isAbsolute() && (file.getParentFile() != null)) {
			file.getParentFile().mkdirs();
		} else if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("No such file: " + file.getAbsolutePath());
		}
		Writer writer = new FileWriter(file);
		Closeable resource = writer;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(writer);
			resource = bw;
			bw.write(text);
		} finally {
			if (bw != null) {
				bw.close();
			}
			resource.close();
		}
	}

	public static String readText(String file) throws IOException {
		Reader reader = new FileReader(file);
		Closeable resource = reader;
		BufferedReader br = null;
		try {
			br = new BufferedReader(reader);
			resource = br;
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			if (line == null) {
				return "";
			} else {
				for (;;) {
					sb.append(line);
					line = br.readLine();
					if (line == null) {
						return sb.toString();
					}
					sb.append(System.getProperty("line.separator"));
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
			resource.close();
		}
	}

	public static void handleError(Throwable th) {
		if (th == null)
			throw new NullPointerException("th");
		int errorCode = -1;
		if (th instanceof NThrowable) {
			errorCode = handleNThrowable((NThrowable) th);
		} else if (th.getCause() instanceof NThrowable) {
			errorCode = handleNThrowable((NThrowable) th.getCause());
		}
		th.printStackTrace();
		System.exit(errorCode);
	}

	/**
	 * Gets user working directory.
	 */
	public static String getWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	/**
	 * Gets user home directory.
	 */
	public static String getHomeDirectory() {
		return System.getProperty("user.home");
	}

	public static String combinePath(String part1, String part2) {
		return String.format("%s%s%s", part1, FILE_SEPARATOR, part2);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || "".equals(value);
	}

	public static String[] getDirectoryFilesList(String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});

		String[] string = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			string[i] = files[i].getAbsolutePath();
		}
		return string;
	}

	private Utils() {
		// Suppress default constructor for noninstantiability.
	}

}
