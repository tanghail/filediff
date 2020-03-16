package com.thl.filediff;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 将超大文件划分为若干个小文件，文件的命名格式是：文件名+分片序号
 *
 * 实现思路：通过文件迭代器读取文件若干行，将固定行数的文件写到文件中
 *
 */

public class SplitFile {

	private String path1;
	private String path2;
	private String dir1;
	private String dir2;
	private String fileName1;
	private String fileName2;
	private int fileSplitSize;
	private int fileCountPerThread;
	private CountDownLatch lock;




	public SplitFile(String path1, String path2) {
		this.path1 = path1;
		this.path2 = path2;
		this.dir1 = path1.substring(0, path1.lastIndexOf(File.separator)+1);
		this.dir2 = path2.substring(0, path2.lastIndexOf(File.separator)+1);
		this.fileName1 = path1.substring(path1.lastIndexOf(File.separator)+1);
		this.fileName2 = path2.substring(path2.lastIndexOf(File.separator)+1);
		this.fileSplitSize = 10;      //每一万行生成一个小文件
		this.fileCountPerThread = 10;  // 每个线程处理1千个小文件的diff
		this.lock = new CountDownLatch(2);
	}

	public SplitFile(String path1, String path2, int fileSplitSize, int fileCountPerThread) {
		this.path1 = path1;
		this.path2 = path2;
		this.dir1 = path1.substring(0, path1.lastIndexOf(File.separator)+1);
		this.dir2 = path2.substring(0, path2.lastIndexOf(File.separator)+1);
		this.fileName1 = path1.substring(path1.lastIndexOf(File.separator)+1);
		this.fileName2 = path2.substring(path2.lastIndexOf(File.separator)+1);
		this.fileSplitSize = fileSplitSize;
		this.fileCountPerThread = fileCountPerThread;
		this.lock = new CountDownLatch(2);
	}

	/**
	 * 将大文件split为小文件
	 *
	 * @param dir
	 * @param fileName
	 */
	public SplitFile splitFile(String dir, String fileName) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + File.separator + fileName)));
			String line;
			int linecount = 0;
			int offset = 0;
			int fileCount = 1;
			BufferedWriter bw = null;
			while ((line = br.readLine()) != null) {
				if (offset == 0) {
					//创建新的小文件
					File file = new File(dir + File.separator + "subFile" + fileName);
					if (!file.exists()) {
						file.mkdirs();
					}
					bw = new BufferedWriter(
							new OutputStreamWriter(
								new FileOutputStream(
										new File(file, fileName + "_" + fileCount++))));
				}

				bw.write(line);
				bw.newLine();
				linecount++;
				offset++;
				if (linecount % fileSplitSize == 0) {
					offset = 0;
				}

				bw.flush();
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	public SplitFile SplitFiles() {
		new Thread(new Runnable() {
			public void run() {
				splitFile(dir1, fileName1);
				lock.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				splitFile(dir2, fileName2);
				lock.countDown();
			}
		}).start();

		//此处存在一个同步的操作，等待文件切分操作结束
		try {
			lock.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return this;
	}

	/**
	 * 将对应的小文件进行diff操作
	 *
	 * 多线程的方式，将多个文件
	 */
	public SplitFile diffFiles() {
		File diffFile1 = new File(dir1 + File.separator + "subFile" + fileName1);
		File diffFile2 = new File(dir2 + File.separator + "subFile" + fileName2);
		File[] files1 = diffFile1.listFiles();
		File[] files2 = diffFile2.listFiles();
		if (files1 == null || files2 == null)
			throw new RuntimeException("not generate diff files");
		int file1Count = files1.length;
		int file2Count = files2.length;
		final int count = Math.max(file1Count, file2Count);
		int start = 1;

		ExecutorService service = Executors.newFixedThreadPool(100);
		while (start < count) {
			if (count - start < fileCountPerThread) {
				service.execute(new ThreadForDiff(this, start, count));
			}
			else {
				service.execute(new ThreadForDiff(this, start, start+fileCountPerThread-1));
			}

			start += fileCountPerThread;
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		service.shutdown();
		while (!service.isTerminated());

		return this;
	}

	/**
	 * 每一个线程负责一定数量小文件的diff，放到一个diff文件中
	 *
	 * @param start
	 * @param end
	 */
	public void diffSmallFiles(int start, int end) {
		File dir = new File(dir1 + File.separator + "diff");
		if (!dir.exists())
			dir.mkdirs();

		BufferedWriter bwDiff = null;
		try {
			bwDiff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, "diff_" + (start / fileCountPerThread + 1)))));
			for (int i = start; i <= end; i++) {
				String diff1 = dir1 + File.separator + "subFile" + fileName1 + File.separator + fileName1 + "_" + i;
				String diff2 = dir2 + File.separator + "subFile" + fileName2 + File.separator + fileName2 + "_" + i;
				String command = "diff " + diff1 + " " + diff2 + " -y -W 50";
				Process exec = null;    // generate the diff file
				BufferedReader reader = null;
				try {
					exec = Runtime.getRuntime().exec(command);
					if (exec != null) {
						String line = null;
						reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
						while ((line = reader.readLine()) != null) {
							bwDiff.append(line);
							bwDiff.newLine();
						}
						bwDiff.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					reader.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bwDiff.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 将所有的diff文件merge到一个文件中
	 *
	 */
	public void mergeDiffFile(String res) {
		//写入diff
		BufferedWriter bufferedWriter = null;
		try {
			File dir = new File(res);
			if (!dir.exists())
				dir.mkdirs();

			// 最终的结果保存在res/diffRes中
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, "diffRes"))));
			File diffDir = new File(dir1 + File.separator + "diff");
			if (!diffDir.exists())
				throw new Exception("no diff directory");

			// diff 小文件保存在 dir1/diff 目录中
			File[] files = diffDir.listFiles();
			for (int i = 1; i <= files.length; i++) {
				BufferedReader bufferedReader = null;
				try {
					bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(diffDir, "diff_" + i))));
					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						bufferedWriter.write(line);
						bufferedWriter.newLine();
						line = bufferedReader.readLine();
					}
					bufferedWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					bufferedReader.close();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
