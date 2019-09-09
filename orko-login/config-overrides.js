const {
  override,
  removeModuleScopePlugin,
  babelInclude,
  addWebpackAlias
} = require("customize-cra")
const path = require("path")

module.exports = override(
  removeModuleScopePlugin(),
  babelInclude([path.resolve("src"), path.resolve(__dirname, "../js-common")]),
  addWebpackAlias({
    "@orko-js-common": path.resolve(__dirname, "../js-common"),
    "@orko-semantic": path.resolve(__dirname, "../orko-semantic/dist")
  })
)
