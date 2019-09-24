#!/usr/bin/env bash

# Get parameter

CSV_PATH=$1
NEW_CSV_PATH=$2
NEW_SIZE_MB=$3
TMP_FILE=${CSV_PATH}.tmp

# Copy files
 
cp ${CSV_PATH} ${NEW_CSV_PATH}

# Delete header for tmp

sed '1d' ${CSV_PATH} > ${TMP_FILE}

# Create file

NEW_SIZE_BYTE=$(echo ${NEW_SIZE_MB} \* 1048576 | bc -l)
NEW_SIZE_BYTE=${NEW_SIZE_BYTE%.*} # convert to int
SIZE_BYTE=$(($(wc -c < ${NEW_CSV_PATH})))

until [[ ${SIZE_BYTE} -ge ${NEW_SIZE_BYTE} ]]; do
	cat ${TMP_FILE} >> ${NEW_CSV_PATH}
	SIZE_BYTE=$(($(wc -c < ${NEW_CSV_PATH})))
done

rm ${TMP_FILE}
