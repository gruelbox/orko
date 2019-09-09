const path = require("path")
const { removeModuleScopePlugin } = require("customize-cra")

module.exports = function override(config, env) {
  config = removeModuleScopePlugin()(config)
  return config
}
