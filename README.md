# Orko

[![CircleCI](https://circleci.com/gh/badgerwithagun/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/badgerwithagun/orko/tree/master)

Orko is a web application which provides a unified UI and web service API to numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen, and without sharing your API keys with anyone.  [Read more...](https://github.com/badgerwithagun/orko/wiki/Why-Orko)

## Status

Early alpha. Please use with caution.  [What does this mean?](https://github.com/badgerwithagun/orko/wiki/Project-status)

## Quick start (local install)

```
sudo apt-get install maven
./build.sh
./start.sh
```

Navigate to http://localhost:8080 to view the application.

Note that this mode has numerous limitations.  You need to do some more setup to get the full experience.  [Read more...](https://github.com/badgerwithagun/orko/wiki/Local-installation)

## Deploy on Heroku

I personally run the application on Heroku. The Hobby account is cheap at $7/pm per server if running constantly, SSL is provided out of the box and the continuous deployment features are great. Best of all, the application is all preconfigured to work out of the box. 

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/badgerwithagun/orko)

If that doesn't work, or you don't trust it, you can [install the application manually](https://github.com/badgerwithagun/orko/wiki/Manual-installation-on-Heroku).
