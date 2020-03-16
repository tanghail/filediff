# BigFileDiff
**Interview**

this is a tool which can diff two big files. the idea is to divide the two files into the same count small files, and merge them at last.

**Get Start**

(1) enviroment prepare

jdk version 1.8

you should run this project in linux machine.

(2) packaging

`mvn clean package`

then you can find the tar.gz file in bigfile_diff-assemble/target directory

(3) how to start

`./bin/diff.sh -l1 /Users/haitang/version_6.html -l2 /Users/haitang/version_5.html -r /Users/haitang/res -f 10 -c 10`

tar the package file and run the command above. you can specify first file and second file by "l1" and "l2", and you can specify the result file which stores result
diff file by "-r", "c" and "s" options can specify the file split size and file count per thread

**result**

you will get the result file in you specify dirctory.