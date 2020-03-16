if [ -z "${FILE_DIFF_HOME}" ]; then
  export FILE_DIFF_HOME="$(cd "`dirname "$0"`"/..; pwd)"
fi

if [ -n "${JAVA_HOME}" ]; then
  RUNNER="${JAVA_HOME}/bin/java"
elif [ `command -v java` ]; then
  RUNNER="java"
else
  echo "JAVA_HOME is not set" >&2
  exit 1
fi

# set home variable
if [ -z "$FILE_DIFF_HOME" ]; then
  echo "FILE_DIFF_HOME is not set~!"
  exit 1
fi

FILE_DIFF_JAR_DIR="${FILE_DIFF_HOME}/lib"

FILE_DIFF_MAIN_JAR=${FILE_DIFF_JAR_DIR}/"$(ls ${FILE_DIFF_JAR_DIR} |grep bigfile_diff- | grep .jar)"

for file in `ls ${FILE_DIFF_JAR_DIR}`;
do
CLASSPATH=${FILE_DIFF_JAR_DIR}/${file}:${CLASSPATH};
done

${RUNNER} -cp ${CLASSPATH} com.thl.filediff.Diff $@