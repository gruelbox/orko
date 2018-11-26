import crypto from "crypto"
import authenticator from "otplib/authenticator"

authenticator.options = {
  crypto
}

export function tokenForSecret(secret) {
  return authenticator.generate(secret)
}
