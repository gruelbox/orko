# oco

1: Set up Telegram so you can get notifications.
---
1. Create a Telegram bot using the BotFather: https://core.telegram.org/bots. Note down the API token.
1. Create a new channel from the Telegram app, and make it public (we'll make it private shortly).
1. Add your bot as a member of the channel, so it can post to it.
1. Use the following URL to get the ID of your channel: https://api.telegram.org/YOURBOTID:YOURTOKEN/getChat?chat_id=@YourChannelName
1. Once you've noted down the channel ID, make your channel private from the app so no-one else can access it (you can't use the above API to ge the IP of a private channel).

The application will now use this bot to send you notifications on your private channel.  You can add more people to the channel if you like.

2: Local setup and build
---
1. Install the Java JDK (`sudo apt-get install default-jdk`)
1. Install Maven (`sudo apt-get install maven`)
1. Run `mvn clean package` to build the application
1. Generate a new 2FA secret using `java -cp target/oco-0.0.1-SNAPSHOT.jar com.grahamcrockford.oco.cli.GenerateSecretKey`
1. Store that somewhere safe and enter it into Google Authenticator on your phone.
1. Spin up a mongodb instance on Docker using this image: https://hub.docker.com/r/library/mongo/.

How to start the application locally:
---
1. Copy `example-config.yml` as `my-config.xml` and replace the wildcards with the appropriate settings.
1. Note that you can omit the `secretKey` for running locally - the 2FA is a PITA if you don't need it.
1. You can also omit the entire telegram section if you don't want your phone beeping.
1. Start application with `java -jar target/oco-0.0.1-SNAPSHOT.jar server my-config.yml`

The 2FA is a bit clunky.  I bodged something together quickly to make things a bit more secure but it's not great.  It works as follows:

1. Call `PUT /auth?token=YourGoogleAuthenticatorCode` to authorise your originating IP.  Only a single IP address is allowed at any one time.  By default the whitelisting expires after 10 minutes and you need to authorise again.
2. Then call any other of the entry points (listed below) using basic authentication and the configured username/password.

Best way to work is to catch 401 any time you call an entry point, and if you get one, call `auth` then retry.

The plan is to change the `auth` entry point to return back a session token which expires after a certain period.  Dunno.  Welcome to hear your thoughts.  I know 2FA is a pain with something like this, but this thing is deployed publicly and can do unspeakable things.

All this should secure enough over SSL or on a private box.

How to deploy to Heroku
---

Once you've got it working locally, you probably want to deploy it somewhere it's not going to fall over. I like Heroku. The Hobby account is cheap at $7/pm if running constantly, SSL is provided out of the box and continuous deployment is sexy as fuck.

1. Create a Heroku account
1. Using the approach detailed in the getting started guide for Java at https://devcenter.heroku.com/articles/getting-started-with-java#set-up, add the application.
1. Add the mLab MongoDB addon (required)
1. Add the Papertrail addon (optional, but by far the easiest way to handle logs)
1. Upgrade to a Hobby Dyno or the application will shut down when you're not sending web requests to it.  It's free until you pass a certain number of minutes running per month.
1. Set up the environment variables:

| Variable                  | Set to                 | 
| ------------------------- | ---------------------- |
| `LOOP_SECONDS`            | Between 3 and 10. 5 is good. Note that at the moment, a game loop is used, but I'll be phasing this out in favour of a continuous event stream from exchanges' web socket APIs where they are supported. Beware if you set this low and have a lot of running jobs on a single exchange, you may end up spamming the exchange API and get banned. You'll get Telegram alerts when this happens but you'll need to shut down the app quick-smart or the ban could be lengthly.  |
| `LOCK_SECONDS`            | At least 2 times `loopSeconds`.  Longer just means cluster nodes will take longer to notice a job isn't running and take over.  Shorter is dangerous as jobs may lose locks while running, so err on the long side.  I normally go for 30. This isn't perfect and I'm looking at alternatives like ZooKeeper in the longer term. |
| `USER_NAME`               | The username for HTTP authentication.  You must deploy the application over SSL (HTTPS) to make this secure (his is the default on Heroku).|
| `PASSWORD`                | The password for HTTP authentication. You must deploy the application over SSL (HTTPS) to make this secure (this is the default on Heroku).|
| `AUTH_TOKEN`                | Your 2FA secret key (generated with `java -cp target/oco-0.0.1-SNAPSHOT.jar com.grahamcrockford.oco.cli.GenerateSecretKey`).  Can be left blank (in which case 2FA is disabled) but must be defined.|
| `PROXIED`                | Set to `true` on Heroku so it uses the `X-Forwarded-For` header to determine the source IP.  This MUST be `false` if you're not hosted behind a trusted proxy where you can 100% believe the `X-Forwarded-For` header, or someone could easily spoof their IP and bypass your 2FA. |
| `MONGODB_URI`             | Should already have been set up for you by the add-on. |
| `MONGO_DATABASE`          | The bit at the end of `MONGODB_URI` after the last slash.  I should really just extract it from the URL. To do. |
| `TELEGRAM_BOT_TOKEN`      | The bot API token. Can be left blank, in which case Telegram notifications won't be used, but must be defined. |
| `TELEGRAM_CHAT_ID`        | The chat ID. Must be defined but may be blank if `TELEGRAM_BOT_TOKEN`  is. |
| `GDAX_SANDBOX_API_KEY`    | Your API key from the GDAX sandbox (https://public.sandbox.gdax.com). If left blank, paper trading will be used. |
| `GDAX_SANDBOX_SECRET`     | Your secret from the GDAX sandbox (https://public.sandbox.gdax.com). May be left blank for paper trading. |
| `GDAX_SANDBOX_PASSPHRASE` | Your passphrase from the GDAX sandbox (https://public.sandbox.gdax.com). May be left blank for paper trading. |
| `GDAX_API_KEY`            | Your GDAX API key. May be left blank for paper trading. |
| `GDAX_SECRET`             | Your GDAX secret. May be left blank for paper trading.. |
| `GDAX_PASSPHRASE`         | Your GDAX passphrase. May be left blank for paper trading. |
| `BINANCE_API_KEY`         | Your Binance API key. May be left blank for paper trading. |
| `BINANCE_SECRET`          | Your Binance secret. May be left blank for paper trading. |
| `KUCOIN_API_KEY`          | Your Kucoin API key. May be left blank for paper trading. | 
| `KUCOIN_SECRET`           | Your Kucoin secret. May be left blank for paper trading. |

1. Deploy and scale.  Multiple instances will compete for the work and can be stopped/started freely. State is persistent.

Health Check
---

No health checks are currently implemented.

Job submission API
---

| Verb   | URI                                                   | Action | Parameters | Payload | Example |
| ------ | ----------------------------------------------------- | ---------- |---------- | ------- | ------- |
| PUT    | /auth                                                 | Whitelists your IP | `token` = the response from Google Authenticator. | None | PUT /auth?token=623432 |
| PUT    | /jobs                                                 | Adds a fully specified job | None       | JSON | See below |
| PUT    | /jobs/pumpchecker                                     | Shortcut to add a pump checker | exchange, counter, base | None | PUT /jobs/pumpchecker?exchange=binance&counter=BTC&base=ICX |
| PUT    | /jobs/softtrailingstop                                | Shortcut to add a soft trailing stop | exchange, counter, base, amount, stop, limit| None | PUT /jobs/softtrailingstop?exchange=binance&counter=BTC&base=VEN&amount=0.5&stop=0.000555&limit=0.0005 |
| PUT    | /jobs/limitsell                                      | Adds a limit order. | exchange, counter, base, amount, limit | None | PUT /jobs/limitsell?exchange=binance&counter=BTC&base=VEN&amount=0.5limit=0.0005 |
| DELETE | /jobs                                                 | Deletes all active jobs | None | None | DELETE /jobs |
| DELETE | /jobs/{id}                                            | Deletes the specified job | None | None | DELETE /jobs/512EDA231BFEA23 |

Exchange API
---

| Verb   | URI                                                   | Action | Parameters | Payload | Example |
| ------ | ----------------------------------------------------- | ---------- |---------- | ------- | ------- |
| GET    | /exchanges                                            | Gets the list of supported exchanges | None | None | GET /exchanges |
| GET    | /exchanges/{exchange}/counters                        | Gets the list of supported countercurrencies on the exchange (e.g. USDT, BTC) | None | None | GET /exchanges/binance/counters |
| GET    | /exchanges/{exchange}/counters/{counter}/bases        | Gets the list of currencies which can be traded against the specified countercurrency. | None | None | GET /exchanges/binance/counters/BTC/bases |
| GET    | /exchanges/{exchange}/markets/{base}-{counter}/ticker | Gets the current ticker | None | None | GET /exchanges/gdax/markets/BTC-EUR/ticker |
| GET    | /exchanges/{exchange}/markets/{base}-{counter}/orders | Gets your open orders on the specified ticker. | None | None | GET /exchanges/kucoin/markets/DRGN-BTC/orders |
| GET    | /exchanges/{exchange}/orders                          | Gets your open orders on the specified exchange. Not supported on many exchanges. | None | None | GET /exchanges/gdax/orders |
| GET    | /exchanges/{exchange}/orders/{id}                     | Gets a specific order. | None | None | GET /exchanges/binance/orders/DRGN-BTC/orders/5a9098f1d038110f1c4b7b0e |


Advanced examples
---

To perform an OCO trade, call `PUT /jobs` with a payload like this:

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