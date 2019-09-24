export interface PartialServerCoin {
  base: string
  counter: string
}

export interface ServerCoin extends PartialServerCoin {
  exchange: string
}

export interface Coin extends ServerCoin {
  key: string
  name: string
  shortName: string
}

export interface Exchange {
  code: string
  name: string
  refLink: string
  authenticated: boolean
}
