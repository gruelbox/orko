const path = require("path")
const removeModuleScopePlugin = require("./craco-plugin-remove-module-scope-plugin")

module.exports = {
  webpack: {
    alias: {
      "@orko-semantic": path.resolve(__dirname, "../orko-semantic/dist")
    }
  },
  jest: {
    configure: {
      moduleNameMapper: {
        "^@orko-semantic(.*)$": "/../orko-semantic/dist$1"
      }
    }
  },
  plugins: [
    {
      plugin: removeModuleScopePlugin
    }
  ]
}
