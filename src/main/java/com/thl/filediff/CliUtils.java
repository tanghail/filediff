package com.thl.filediff;

import org.apache.commons.cli.*;

public  class CliUtils {

		private String[] args;
		private Options opts = new Options();

		private String file1;
		private String file2;
		private String resFile;
		private int fileSplitSize;
		private int fileCountPerThread;

		public int getFileSplitSize() {
			return fileSplitSize;
		}

		public void setFileSplitSize(int fileSplitSize) {
			this.fileSplitSize = fileSplitSize;
		}

		public int getFileCountPerThread() {
			return fileCountPerThread;
		}

		public void setFileCountPerThread(int fileCountPerThread) {
			this.fileCountPerThread = fileCountPerThread;
		}

		public String[] getArgs() {
				return args;
			}

		public void setArgs(String[] args) {
			this.args = args;
		}

		public Options getOpts() {
			return opts;
		}

		public void setOpts(Options opts) {
			this.opts = opts;
		}

		public String getFile1() {
			return file1;
		}

		public void setFile1(String file1) {
			this.file1 = file1;
		}

		public String getFile2() {
			return file2;
		}

		public void setFile2(String file2) {
			this.file2 = file2;
		}

		public String getResFile() {
			return resFile;
		}

		public void setResFile(String resFile) {
			this.resFile = resFile;
		}

		public CliUtils(String[] args) {
				this.args = args;
				definedOptions();
				parseOptions();
			}

		private void definedOptions() {
			Option opt_h = new Option("h", "help.");

			Option opt_l1 = Option.builder("l1").hasArg().argName("file 1").desc("first file").build();
			Option opt_l2 = Option.builder("l2").hasArg().argName("file 2").desc("second file").build();
			Option opt_r = Option.builder("r").hasArg().argName("result directory").desc("result directory").build();
			Option opt_s = Option.builder("s").hasArg().argName("fileSplitSize").desc("how many line you split per file").build();
			Option opt_c = Option.builder("c").hasArg().argName("fileCountPerThread").desc("how many files a thread handle").build();

			opts.addOption(opt_h);
			opts.addOption(opt_l1);
			opts.addOption(opt_l2);
			opts.addOption(opt_r);
			opts.addOption(opt_s);
			opts.addOption(opt_c);
		}

		private void parseOptions() {
			CommandLineParser parser = new DefaultParser();
			CommandLine line = null;

			//parse
			try {
				line = parser.parse(opts, args);
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

			if (args == null || args.length == 0 || line.hasOption("h")) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("encrypt", opts);
				System.exit(-1);
			}

			if (line.hasOption("l1")) {
				file1 = line.getOptionValue("l1");
			}

			if (line.hasOption("l2")) {
				file2 = line.getOptionValue("l2");
			}

			if (line.hasOption("r")) {
				resFile = line.getOptionValue("r");
			}

			if (line.hasOption("s")) {
				fileSplitSize = Integer.parseInt(line.getOptionValue("s"));
			}

			if (line.hasOption("c")) {
				fileCountPerThread = Integer.parseInt(line.getOptionValue("c"));;
			}

		}
}
