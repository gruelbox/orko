#!/bin/bash
git push heroku-frontend `git subtree split --prefix orko-ui master`:master --force