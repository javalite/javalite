#!/usr/bin/env bash

git fetch origin pull/$1/head:pr-$1
git checkout pr-$1
