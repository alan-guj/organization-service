#!/bin/sh
# ./process-locality.sh LOCALITY_DATA
FILE=$1
while read line;
do
    arr=($line)
    name=${arr[0]}
    id=${arr[1]}

    echo "#$name $id"
    echo "
dn: l=$id,ou=localities,dc=nkf,dc=dev-prod,dc=jyx365,dc=top
description: $name
l: $id
objectclass: locality
"
done < $FILE
