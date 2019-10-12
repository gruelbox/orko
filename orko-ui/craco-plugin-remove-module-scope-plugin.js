module.exports = {
  overrideWebpackConfig: ({
    webpackConfig,
    cracoConfig,
    pluginOptions,
    context: { env, paths }
  }) => {
    webpackConfig.resolve.plugins = webpackConfig.resolve.plugins.filter(
      p => p.constructor.name !== "ModuleScopePlugin"
    )
    return webpackConfig
  }
}
