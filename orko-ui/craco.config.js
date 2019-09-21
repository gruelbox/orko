const path = require("path")
const removeModuleScopePlugin = require("./craco-plugin-remove-module-scope-plugin")

module.exports = {
  webpack: {
    alias: {
      "@orko-semantic": path.resolve(__dirname, "../orko-semantic/dist"),
      "@orko-ui-auth": path.resolve(__dirname, "./src/modules/auth"),
      "@orko-ui-common": path.resolve(__dirname, "./src/modules/common"),
      "@orko-ui-socket": path.resolve(__dirname, "./src/modules/socket")
    }
  },
  jest: {
    configure: {
      moduleNameMapper: {
        "^@orko-semantic(.*)$": "/../orko-semantic/dist$1",
        "^@orko-ui-auth(.*)$": "/src/modules/auths$1",
        "^@orko-ui-common(.*)$": "/src/modules/common$1",
        "^@orko-ui-socket(.*)$": "/src/modules/socket$1"
      }
    }
  },
  plugins: [
    {
      plugin: removeModuleScopePlugin
    }
  ]
}
