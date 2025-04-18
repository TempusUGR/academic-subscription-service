#!/bin/bash

# Variables
FTP_HOST=$1
FTP_PORT=$2
FTP_USER=$3
FTP_PASSWORD=$4
FTP_DIRECTORY=$5
LOCAL_FILE=$6

# Subir el archivo usando lftp
lftp -u "$FTP_USER","$FTP_PASSWORD" -p "$FTP_PORT" "$FTP_HOST" <<EOF
cd $FTP_DIRECTORY
put $LOCAL_FILE
bye
EOF