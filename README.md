# oco

How to deploy to Heroku
---

1. Create a Heroku account
1. Using the approach detailed in the getting started guide for Java at https://devcenter.heroku.com/articles/getting-started-with-java#set-up, add the application.
1. Add the PaperTrail and mLab MongoDB addons.
1. Set up the environment variables:

| Variable             | Set to                 | 
| -------------------- | ---------------------- |
| `LOOP_SECONDS`       | Between 3 and 10       |
| `LOCK_SECONDS`       | At least 2 times `loopSeconds`.  Longer just means cluster nodes will take longer to notice a job isn't running and take over.  Shorter is dangerous as jobs may lose locks while running, so err on the long side. |
| `USER_NAME`          | The username for HTTP authentication. |
| `PASSWORD`           | The password for HTTP authentication. |
| `MONGODB_URI`        | Should already have been set up for you by the add-on. |
| `MONGO_DATABASE`     | The bit at the end of `MONGODB_URI` after the last slash. |
| `TELEGRAM_BOT_TOKEN` | Create a telegram bot. This is the auth token you'll be given. |
| `TELEGRAM_CHAT_ID`   | Slightly tricky to get hold of. TBC |

1. Deploy and run.  Multiple instances will compete for the work and can be stopped/started freely. State is persistent.

How to start the application locally:
---

1. Install the Java JDK (`sudo apt-get install default-jdk`)
1. Install Maven (`sudo apt-get install maven`)
1. Run `mvn clean package` to build the application
1. Copy `example-config.yml` as `my-config.xml` and replace the wildcards with the appropriate settings.
1. Remove the entire telegram section if you don't have a telegram bot set up.
1. Start application with `java -jar target/oco-0.0.1-SNAPSHOT.jar server my-config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

(Not currently implemented)
To see your applications health enter url `http://localhost:8081/healthcheck`

API
---

| Verb   | URI                                                   | Action | Parameters | Payload | Example |
| ------ | ----------------------------------------------------- | ---------- |---------- | ------- | ------- |
| PUT    | /jobs                                                 | Adds a fully specified job | None       | JSON of job (see below) | |
| PUT    | /jobs/pumpchecker                                     | Shortcut to add a pump checker | exchange, counter, base | None | PUT /jobs/pumpchecker?exchange=binance&counter=BTC&base=ICX |
| PUT    | /jobs/softtrailingstop                                | Shortcut to add a soft trailing stop | exchange, counter, base, amount, stop, limit| None | PUT /jobs/softtrailingstop?exchange=binance&counter=BTC&base=VEN&amount=0.5&stop=0.000555&limit=0.0005 |
| DELETE | /jobs                                                 | Deletes all active jobs | None | None | DELETE /jobs |
| DELETE | /jobs/{id}                                            | Deletes the specified job | None | None | DELETE /jobs/512EDA231BFEA23 |
| GET    | /exchanges/{exchange}/markets/{base}-{counter}/ticker | Gets the current ticker | None | None | GET /exchanges/gdax/markets/BTC-EUR/ticker |
| GET    | /exchanges/{exchange}/orders                          | TBC | | | |
| GET    | /exchanges/{exchange}/orders/{id}                     | TBC | | | |

Example of payload for `PUT /jobs`:

```
{
    "@class": "com.grahamcrockford.oco.core.jobs.AutoValue_OneCancelsOther",
    "tickTrigger": {
        "exchange": "binance",
        "counter": "BTC",
        "base": "VEN"
    },
    "low": {
        "thresholdAsString": "0.00055",
        "job": {
		    "@class": "com.grahamcrockford.oco.core.jobs.AutoValue_LimitSell",
		    "tickTrigger": {
		        "exchange": "binance",
		        "counter": "BTC",
		        "base": "VEN"
		    },
		    "bigDecimals": {
		        "amount": "0.5",
		        "limitPrice": "0.00054"
		    }
		}
    },
    "high": {
        "thresholdAsString": "0.00057",
        "job": {
		    "@class": "com.grahamcrockford.oco.core.jobs.AutoValue_SoftTrailingStop",
		    "tickTrigger": {
		        "exchange": "binance",
		        "counter": "BTC",
		        "base": "VEN"
		    },
		    "bigDecimals": {
		        "amount": "0.5",
		        "startPrice": "0.00057",
		        "lastSyncPrice": "0.000576",
		        "stopPrice": "0.000565",
		        "limitPrice": "0.00055"
		    }
		}
    }
}
```

This adds an OCO order where:

* if the price drops below 0.00055, we will sell at the bid price
* If the price rises above 0.00057, we will start a trailing stop where the stop price trails the bid price by 0.000005.