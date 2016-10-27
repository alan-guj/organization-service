#!/bin/sh
# ./process-locality.sh STAFF_DATA
FILE=$1
while read line;
do
    arr=($line)
    name=${arr[0]}
    id=${arr[1]}

    echo "#$name $id"
    echo "
dn: cn=$id,ou=staffs,dc=nkf,dc=dev-prod,dc=jyx365,dc=top
cn: $id
o: dc=nkf
objectclass: inetOrgPerson
objectclass: organizationalPerson
objectclass: person
objectclass: uidObject
ou: ou=$3,ou=departments,dc=nkf
sn: $name
uid: null
"
done < $FILE
