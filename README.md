# oco

How to deploy to Heroku
---

1. Create a Heroku account
1. Using the approach detailed in the getting started guide for Java at https://devcenter.heroku.com/articles/getting-started-with-java#set-up, add the application.
1. Add the PaperTrail and mLab MongoDB addons to your application.
1. Upgrade to a Hobby Dyno or the application wil shut down when you're not sending web requests to it.
1. Create a Telegram bit using the BotFather: https://core.telegram.org/bots. Note down the API token.
1. Create a Telegram channel from the app and make it public (we'll make it private shortly)
1. Add the bot as a member of the channel.
1. Use the following URL to get the ID of your channel: https://api.telegram.org/YOURBOTID:YOURTOKEN/getChat?chat_id=@YourChannelName
1. Once you've noted down the channel ID, make your channel private from the app so no-one else can access it.
1. Set up the environment variables:

| Variable             | Set to                 | 
| -------------------- | ---------------------- |
| `LOOP_SECONDS`       | Between 3 and 10. 5 is good.       |
| `LOCK_SECONDS`       | At least 2 times `loopSeconds`.  Longer just means cluster nodes will take longer to notice a job isn't running and take over.  Shorter is dangerous as jobs may lose locks while running, so err on the long side.  I normally go for 30. |
| `USER_NAME`          | The username for HTTP authentication.  You must deploy the application over SSL (HTTPS) to make this secure. This is the default on Heroku.|
| `PASSWORD`           | The password for HTTP authentication. You must deploy the application over SSL (HTTPS) to make this secure. This is the default on Heroku.|
| `MONGODB_URI`        | Should already have been set up for you by the add-on. |
| `MONGO_DATABASE`     | The bit at the end of `MONGODB_URI` after the last slash. |
| `TELEGRAM_BOT_TOKEN` | The bot API token. |
| `TELEGRAM_CHAT_ID`   | The chat ID. |

1. Deploy and run.  Multiple instances will compete for the work and can be stopped/started freely. State is persistent.

How to start the application locally:
---

1. Install the Java JDK (`sudo apt-get install default-jdk`)
1. Install Maven (`sudo apt-get install maven`)
1. Run `mvn clean package` to build the application
1. Copy `example-config.yml` as `my-config.xml` and replace the wildcards with the appropriate settings.
1. Remove the entire telegram section if you don't have a telegram bot set up, otherwise follow steps 5-9 above to get the auth token and ID.
1. Start application with `java -jar target/oco-0.0.1-SNAPSHOT.jar server my-config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

No health checks are currently implemented.

API
---

| Verb   | URI                                                   | Action | Parameters | Payload | Example |
| ------ | ----------------------------------------------------- | ---------- |---------- | ------- | ------- |
| PUT    | /jobs                                                 | Adds a fully specified job | None       | JSON | See below |
| PUT    | /jobs/pumpchecker                                     | Shortcut to add a pump checker | exchange, counter, base | None | PUT /jobs/pumpchecker?exchange=binance&counter=BTC&base=ICX |
| PUT    | /jobs/softtrailingstop                                | Shortcut to add a soft trailing stop | exchange, counter, base, amount, stop, limit| None | PUT /jobs/softtrailingstop?exchange=binance&counter=BTC&base=VEN&amount=0.5&stop=0.000555&limit=0.0005 |
| DELETE | /jobs                                                 | Deletes all active jobs | None | None | DELETE /jobs |
| DELETE | /jobs/{id}                                            | Deletes the specified job | None | None | DELETE /jobs/512EDA231BFEA23 |
| GET    | /exchanges/{exchange}/markets/{base}-{counter}/ticker | Gets the current ticker | None | None | GET /exchanges/gdax/markets/BTC-EUR/ticker |
| GET    | /exchanges/{exchange}/markets/{base}-{counter}/orders | Gets your open orders on the specified ticker. | None | None | GET /exchanges/kucoin/markets/DRGN-BTC/orders |
| GET    | /exchanges/{exchange}/orders                          | Gets your open orders on the specified exchange. Not supported on many exchanges. | None | None | GET /exchanges/gdax/orders |
| GET    | /exchanges/{exchange}/orders/{id}                     | Gets a specific order. | None | None | GET /exchanges/binance/orders/DRGN-BTC/orders/5a9098f1d038110f1c4b7b0e |


Example of payload for `PUT /jobs`:

```
{
    "jobType": "OneCancelsOther",
    "tickTrigger": {
        "exchange": "binance",
        "counter": "BTC",
        "base": "VEN"
    },
    "low": {
        "thresholdAsString": "0.00055",
        "job": {
		    "jobType": "LimitSell",
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
		    "jobType": "SoftTrailingStop",
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