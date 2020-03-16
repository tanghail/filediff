package com.thl.filediff;

public class ThreadForDiff extends Thread {
	private SplitFile tool;
	private int start;
	private int end;

	public ThreadForDiff(SplitFile tool, int start, int end) {
		this.tool = tool;
		this.start = start;
		this.end = end;

	}

	@Override
	public void run() {
		tool.diffSmallFiles(start, end);
	}
}
