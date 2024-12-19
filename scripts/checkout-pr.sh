#!/usr/bin/env bash

if [ $# -eq 0 ]
  then
    echo "Expected one argument: Pull request Number"
    exit 1
fi


git fetch origin pull/$1/head:pr-$1
git checkout pr-$1
