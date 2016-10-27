#!/bin/sh
# ./process-staffs-bs.sh STAFF_BS_DATA LOCALITY_MAP PROUDUCT_MAP STAFF_MAP
staff_bs_file=$1
locality_map_file=$2
product_map_file=$3
staff_map_file=$4

declare -A locality_map
declare -A product_map
declare -A staff_map
declare -A staff_bs_map


#construct locality map
while read line;
do
    arr=($line)
    key=${arr[0]}
    value=${arr[1]}
    locality_map[$key]=$value
done < $locality_map_file
echo "#${locality_map[@]}"

#construct product_map
while read line;
do
    arr=($line)
    key=${arr[0]}
    value=${arr[1]}
    product_map[$key]=$value
done < $product_map_file
echo "#${product_map[@]}"


#construct staff_map
while read line;
do
    arr=($line)
    key=${arr[0]}
    value=${arr[1]}
    staff_map[$key]=$value
done < $staff_map_file
echo "#${staff_map[@]}"


#generate ldif
while read line;
do
    arr=($line)
    locality_key=${arr[0]}
    product_key=${arr[1]}
    staff_key=${arr[2]}
    locality=${locality_map[$locality_key]}
    product=${product_map[$product_key]}
    staff=${staff_map[$staff_key]}
    echo "#$locality $product $staff"
    staff_bs_map[$staff]+="Businesscategory: {\"locality\":\"l=$locality,ou=localities,dc=nkf\",
    \"product\":\"cn=$product,ou=products,dc=nkf\"}\n"
done < $staff_bs_file

for key in ${!staff_bs_map[@]}
do
    echo "
dn: cn=$key,ou=staffs,dc=nkf,dc=dev-prod,dc=jyx365,dc=top
changetype: modify
add: businesscategory"
    echo -e ${staff_bs_map[$key]}
done
