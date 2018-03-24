#!/bin/bash
git push heroku-frontend `git subtree split --prefix oco-ui master`:master --force
git push heroku-backend master --force