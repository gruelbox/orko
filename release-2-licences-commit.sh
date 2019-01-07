#!/bin/bash
set -e
git add -A
git diff-index --quiet HEAD || git commit -m "Update licence headers"
