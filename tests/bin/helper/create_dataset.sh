#!/usr/bin/env bash

# Get parameter

CSV_PATH=$1
NEW_CSV_PATH=$2
NEW_SIZE_MB=$3
TMP_FILE=${CSV_PATH}.tmp

#START_LINE=2
#END_LINE=$(wc -l < $NEW_CSV_PATH)

# Copy files
 
cp ${CSV_PATH} ${NEW_CSV_PATH}

# Delete header for tmp

sed '1d' ${CSV_PATH} > ${TMP_FILE}

# Create file

SIZE_MB=$(du -m ${NEW_CSV_PATH} | cut -f1)

until [[ ${SIZE_MB} -ge ${NEW_SIZE_MB} ]]; do
	#REC_ID=$(($START_LINE + RANDOM % $END_LINE));
	cat ${TMP_FILE} >> ${NEW_CSV_PATH}
	SIZE_MB=$(du -m ${NEW_CSV_PATH} | cut -f1);
	#echo "SIZE_MB: $SIZE_MB"
done

rm ${TMP_FILE}
