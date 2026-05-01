#!/bin/bash

VERSION=$1
awk '/^## \[Unreleased\]/{flag=1; next} /^## \[/{flag=0} flag' CHANGELOG.md | sed '/^$/N;/^\n$/d'