#!/bin/bash

echo  "BEAGLE_USERNAME : $BEAGLE_USERNAME"
USERNAME=$BEAGLE_USERNAME

if [[ -z $USERNAME ]] 
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{env.USER}/$USERNAME/" > tmp && mv tmp sites.xml
fi
