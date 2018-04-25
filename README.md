# oco

Quick and easy run using Docker
---
You can run it locally with little to no setup, provided you have either a linux machine (pretty much just works) or a Windows/Mac machine with enough memory for the VMs the Mac/Win Docker implementations require (8GB or more).

1. Install Docker (https://docs.docker.com/install/#supported-platforms) and Docker Compose (https://docs.docker.com/compose/install/)
1. In the root directory, run `docker-compose up`.
1. The application should build and start.  It'll take a while.

By default there are no exchange account details, so trading isn't enabled, and Telegram notifications are also disabled, but it lets you get an idea.  To remedy this, copy this into `docker-config.xml`:

```
telegram:
  botToken: ${TELEGRAM_BOT_TOKEN}
  chatId: ${TELEGRAM_CHAT_ID}
exchanges:
  gdax-sandbox:
    apiKey: ${GDAX_SANDBOX_API_KEY}
    secretKey: ${GDAX_SANDBOX_SECRET}
    passphrase: ${GDAX_SANDBOX_PASSPHRASE}
  gdax:
    apiKey: ${GDAX_API_KEY}
    secretKey: ${GDAX_SECRET}
    passphrase: ${GDAX_PASSPHRASE}
  binance:
    apiKey: ${BINANCE_API_KEY}
    secretKey: ${BINANCE_SECRET}
  kucoin:
    apiKey: ${KUCOIN_API_KEY}
    secretKey: ${KUCOIN_SECRET}
```

And replace the variables with the telegram settings from "[Optional] Set up Telegram so you can get notifications." below and your exchange keys.  Then run `docker-compose build` to rebuild the docker images and `docker-compose up` to restart.

Local development environment
---
Backend setup:

1. Spin up a mongodb instance on Docker using this image: https://hub.docker.com/r/library/mongo/.  You'll need to follow the instructions for creating an admin user.
1. You also need a RabbitMQ instance. Again the standard Docker image is fine out of the box: https://hub.docker.com/_/rabbitmq/
1. Copy `example-developer-mode-worker-config.yml` as `my-config-worker.xml` and fill in the gaps.  The commented-out lines can be ignored for now
1. Copy `example-developer-mode-worker-web.yml` as `my-config-web.xml` and fill in the gaps.  The commented-out lines can be ignored for now
1. Install the Java JDK (`sudo apt-get install default-jdk`)
1. Either:
    1. Install Maven (`sudo apt-get install maven`)
    1. Run `mvn clean package` to build the application
    1. Start the worker application with `java -jar oco-worker/target/oco-worker.jar server ../my-config-worker.yml`.
    1. Start the web application with `java -jar oco-web/target/oco-web.jar server ../my-config-web.yml`.
1. Or:
    1. Install Eclipse
    1. Install the m2e-apt plugin from the marketplace
    1. In Preferences, under Maven > Annotation Processing, set to Automatic
    1. In Preferences, under Maven, enable Automatically Hide Nested Projects.
    1. Import the root directory as a Maven project
    1. Run the worker application by using Run As > Java Application and entering `server ../my-config-worker.yml` as command line parameters.
    1. Run the web application by using Run As > Java Application and entering `server ../my-config-web.yml` as command line parameters.

You should now be able to call the API entry points. Try just navigating to https://localhost:8080/api/exchanges.

The UI runs as a separate application.  It is an unejected [create-react-app](https://github.com/facebook/create-react-app) application.  Everything about it is basically the default:

1. Install NPM (`sudo apt-get install npm`) then run `HTTPS=true npm start` in the root folder to start a local dev server. You can access it at http://localhost:3000.  When running in Webpack Dev Server, it assumes the backend is at https://localhost:8080.

Alternatively:

1. Use `npm run build` to create a static deployable build you can drop on any static web server.
1. I deploy it to Heroku using this buildpack (full instructions there): https://github.com/mars/create-react-app-buildpack
1. When doing so, I ensure to set up API_URL to point to my API instance.

[Optional] Set up Telegram so you can get notifications.
---
1. Create a Telegram bot using the [BotFather](https://core.telegram.org/bots). Note down the API token.
1. Create a new channel from the Telegram app, and make it public (we'll make it private shortly).
1. Add your bot as a member of the channel, so it can post to it.
1. Use the following URL to get the ID of your channel: https://api.telegram.org/YOURBOTID:YOURTOKEN/getChat?chat_id=@YourChannelName
1. Once you've noted down the channel ID, make your channel private from the app so no-one else can access it (you can't use the above API to ge the IP of a private channel).

Once you have the connection details, you can set the appropriate section in your local config:

```
#telegram:
#  botToken: Generate this using the instructions in README.md if you want notifications 
#  chatId: And this
```

The application will now use this bot to send you notifications on your private channel.  You can add more people to the channel if you like.

How to deploy to Heroku
---

Once you've got it working locally, you probably want to deploy it somewhere it's not going to fall over. I like Heroku. The Hobby account is cheap at $7/pm per server if running constantly, SSL is provided out of the box and continuous deployment is sexy as fuck.

1. Create a Heroku account
1. Using the approach detailed in the getting started guide for Java at https://devcenter.heroku.com/articles/getting-started-with-java#set-up, and create two applications, one for the backend and one for the frontend (TODO expand this to actually be full instructions, preferably just an automated bash script)
1. You can leave the frontend as the Free Tier, but you'll need Hobby Tier for the backend, which means a credit card.  It's free until you pass a certain number of minutes running per month.  If you want to be a skinflint, just take it down when you're not using it.
1. On the back-end, add the mLab MongoDB addon (required)
1. On the back-end, add the CloudAMQP RabbitMQ addon (required)
1. On the front-end, set the buildpack to `https://github.com/badgerwithagun/create-react-app-buildpack.git`.
1. On both, add the Papertrail addon (optional, but by far the easiest way to handle logs)

On the backend, set up the environment variables in addition to those already configured by the add-ons you've provisioned:

| Variable                  | Set to                 | 
| ------------------------- | ---------------------- |
| `LOOP_SECONDS`            | 15 |
| `LOCK_SECONDS`            | 45 |
| `PROXIED`                | Set to `true` on Heroku so it uses the `X-Forwarded-For` header to determine the source IP.  This MUST be `false` if you're not hosted behind a trusted proxy where you can 100% believe the `X-Forwarded-For` header, or someone could easily spoof their IP and bypass your 2FA. |
| `MONGODB_URI`             | Should already have been set up for you by the add-on. |
| `MONGO_DATABASE`          | The bit at the end of `MONGODB_URI` after the last slash.  I should really just extract it from the URL. To do. |
| `TELEGRAM_BOT_TOKEN`      | The bot API token. Can be left blank, in which case Telegram notifications won't be used, but must be defined. Note that at the moment, there are no other notifications (even on-screen) so it's a bit of a nightmare to use without phone notifiations.  Turn them on. |
| `TELEGRAM_CHAT_ID`        | The chat ID. Must be defined but may be blank if `TELEGRAM_BOT_TOKEN` is. |
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
| `AUTH_TOKEN`                | Your 2FA secret key (generated with `java -cp target/oco-0.0.1-SNAPSHOT.jar com.grahamcrockford.oco.cli.GenerateSecretKey`) - more on this below.  Can be left blank (in which case 2FA whitelisting is disabled) but must be defined.  Strongly recommended to be enabled.|
| `OKTA_BASEURL`           | Will be provided during Okta setup (see below) |
| `OKTA_CLIENTID`           | Will be provided during Okta setup (see below) |
| `OKTA_ISSUER`           |  Will be provided during Okta setup (see below) |

On the front end, set up as follows:

| Variable                  | Set to                 | 
| ------------------------- | ---------------------- |
| `API_URL`           | The address of your backend server. |
| `NODE_ENV`           |  `production` |
| `NPM_CONFIG_PRODUCTION`           | `true` |
| `REACT_APP_WS_URL`           | The address of your backend server, but replace `https` with `wss`. |

We now need to worry about security.

On Heroku, we will deploy oco-worker as a worker dyno, which is never visible to the outside world. So far so good.  oco-ui is a static Javascript application, therefore security is meaningless - everything is in the open anyway.  We just have to make sure there are no secrets stored in either the JS or, to be super-safe, on the nginx server it's hosted on.  Therefore, all our security is focused on the Web API application, oco-web.

oco-web hosts REST endpoints and a single web socket.  Both are protected at the servlet container level; they will return HTTP status 401 if a suitable authorization header isn't included.  This takes one of two forms.

For the HTTP endpoints, the fairly standard:

```
authorization: Bearer MYJSONWEBTOKEN
```

For the Websockets, the entirely nonstandard:

```
Sec-WebSocket-Protocol: auth, MYJSONWEBTOKEBN
```

This latter is due to the fact that it's the only way to pass a JWT in the standard Javascript `WebSocket` constructor:

```
new WebSocket('wss://localhost:8080/ws', ['auth', 'MYJSONWEBTOKEBN'])
```

Call me paranoid, but I didn't like the idea of an attacker being able to open a websocket at all without authentication (which is a protocol I have complete control over and which means I need to trust my own state management code to be sure an attacker can't do stuff) - I preferred the idea of stopping them at the level of a much more restrictive protocol where I can just trust Java servlet filters to do their job.

Mostly you don't need to worry about this, because it's delegated to Okta - more on this in a moment.

The other element is that we will return 402 if the origin IP address isn't on a whitelist.  To avoid this being too restrictive, we provide a single REST entry point (`/auth`) which you can use to whitelist your IP address at any time, by passing a valid Google authenticator code.  Only a single IP address can be whitelisted at any one time and whitelisting expires.

So, first, let's create a 2FA key:

1. Generate a new 2FA secret using `java -cp target/oco-0.0.1-SNAPSHOT.jar com.grahamcrockford.oco.cli.GenerateSecretKey`
1. Store that somewhere safe and enter it into Google Authenticator on your phone.
1. If you don't want to set up a Java environment, give me a shout and I'll generate a keypair for you.
1. Set the `AUTH_TOKEN` environment variable accordingly.

Now you need an Okta account to handle the JWT authentication:

1. Create a basic (free) account at https://www.okta.com/
1. Create new application of type Single Page App (SPA), allowing both ID token and Access Token
1. Set your Login redirect URI and Initiate login URI to the address of your front-end server.
1. Note down the client ID and set it in your backend app's environment variables.
1. Go to the Sign On tab and note the Issuer. Set the `OKTA_BASEURL` and `OKTA_ISSUER` variables to this.  For the `OKTA_ISSUER` variable, append it with `/oauth2/default`.

Now you're ready to deploy.

1. You should have already installed Heroku CLI.
1. From a clean checkout of this code, add the two heroku remotes:
```
git remote add heroku-frontend git@heroku.com:your-frontend-app-name.git
git remote add heroku git@heroku.com:your-backend-app-name.git
```
1. Then simply:
```
./push-to-heroku.sh
```

That's it!


API Entry points
---
All are prefixed with `/api` and require JWT authentication.

IP whitelisting may also be needed if you've configured it (see Deploying To Heroku, below), in qhich case:

1. Call `PUT /auth?token=YourGoogleAuthenticatorCode` to authorise your originating IP.  Only a single IP address is allowed at any one time.  By default the whitelisting expires after 10 minutes and you need to authorise again.
2. Then call any other of the entry points (listed below) using basic authentication and the configured username/password.

Best way to work is to catch 401 any time you call an entry point, and if you get one, call `auth` then retry.


| Verb   | URI                                                   | Action | Parameters | Payload | Example |
| ------ | ----------------------------------------------------- | ---------- |---------- | ------- | ------- |
| PUT    | /auth                                                 | Whitelists your IP | `token` = the response from Google Authenticator. | None | PUT /auth?token=623432 |
| PUT    | /jobs                                                 | Adds a fully specified job | None       | JSON | See below |
| PUT    | /jobs/pumpchecker                                     | Shortcut to add a pump checker | exchange, counter, base | None | PUT /jobs/pumpchecker?exchange=binance&counter=BTC&base=ICX |
| PUT    | /jobs/softtrailingstop                                | Shortcut to add a soft trailing stop | exchange, counter, base, amount, stop, limit, direction| None | PUT /jobs/softtrailingstop?exchange=binance&counter=BTC&base=VEN&direction=SELL&amount=0.5&stop=0.000555&limit=0.0005 |
| PUT    | /jobs/limitsell                                       | Adds a limit order. | exchange, counter, base, amount, limit | None | PUT /jobs/limitsell?exchange=binance&counter=BTC&base=VEN&amount=0.5limit=0.0005 |
| PUT    | /jobs/limitbuy                                        | Adds a limit order. | exchange, counter, base, amount, limit | None | PUT /jobs/limitbuy?exchange=binance&counter=BTC&base=VEN&amount=0.5limit=0.0005 |
| DELETE | /jobs                                                 | Deletes all active jobs | None | None | DELETE /jobs |
| DELETE | /jobs/{id}                                            | Deletes the specified job | None | None | DELETE /jobs/512EDA231BFEA23 |
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
		    "jobType": "LimitOrderJob",
		    "direction": "SELL",
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
		    "direction": "SELL",
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
