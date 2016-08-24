package com.bladecoder.ink.console;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.Story;

public class InkPlayer {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private final PrintStream out = System.out;
	private final PrintStream err = System.err;
	private boolean isAnsiCapable = false;

	private String filename;

	public static void main(String[] args) {
		InkPlayer player = new InkPlayer();
		player.parseParams(args);
		try {
			player.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-2);
		}
	}

	InkPlayer() {
		isAnsiCapable = detectIfIsAnsiCapable();
	}

	public void run() throws Exception {
		String json = getJsonString(filename).replace('\uFEFF', ' ');
		Story story = new Story(json);

		while (story.canContinue() || story.getCurrentChoices().size() > 0) {

			String text = story.continueMaximally();

			out.print(text);

			if (story.hasError()) {
				for (String errorMsg : story.getCurrentErrors()) {
					err.println(errorMsg);
				}
			}

			// Display story.currentChoices list, allow player to choose one
			if (story.getCurrentChoices().size() > 0) {

				out.println();

				int i = 1;
				for (Choice c : story.getCurrentChoices()) {

					if (isAnsiCapable) {
						out.println(ANSI_CYAN + i + ": " + c.getText() + ANSI_RESET);
					} else {
						out.println(i + ": " + c.getText());
					}

					i++;
				}

				story.chooseChoiceIndex(getChoiceIndex(story.getCurrentChoices()));

				out.println();
			}
		}

		if (isAnsiCapable)
			out.println(ANSI_CYAN + "\nTHE END." + ANSI_RESET);
		else
			out.println("\nTHE END.");
	}

	private int getChoiceIndex(List<Choice> currentChoices) throws IOException {

		int i = -1;

		while (i < 1 || i > currentChoices.size()) {
			
			if (isAnsiCapable)
				out.print(ANSI_CYAN + "\nEnter choice: " + ANSI_RESET);
			else
				out.print("\nEnter choice: ");

			try {
				i = Integer.parseInt(in.readLine());
			} catch (NumberFormatException nfe) {
			}

			if (i < 1 || i > currentChoices.size()) {
				out.println("Invalid choice!");
				i = -1;
			}
		}

		return i - 1;
	}

	private void parseParams(String[] args) {
		if (args.length != 1) {
			out.println("Json filename not specified.");
			usage();
		} else {
			filename = args[0];
		}
	}

	private void usage() {
		out.println("Usage:\n" + "\t InkPlayer <json_filename>\n");

		System.exit(-1);
	}

	private String getJsonString(String filename) throws IOException {

		InputStream is = new FileInputStream(filename);

		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	private static boolean detectIfIsAnsiCapable() {
		try {
			if (System.console() == null) {
				return false;
			}

			return !(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
		} catch (Throwable ex) {
			return false;
		}
	}

}