package com.thl.filediff;

public class Diff {
	public static void main(String[] args) {
		CliUtils cliUtils = new CliUtils(args);
		SplitFile splitFileTools = new SplitFile(cliUtils.getFile1(), cliUtils.getFile2(),cliUtils.getFileSplitSize(), cliUtils.getFileCountPerThread());
		splitFileTools.SplitFiles().diffFiles().mergeDiffFile(cliUtils.getResFile());
	}
}
