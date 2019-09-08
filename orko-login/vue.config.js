const TsconfigPathsPlugin = require("tsconfig-paths-webpack-plugin");

module.exports = {
  configureWebpack: {
    resolve: {
      plugins: [new TsconfigPathsPlugin()]
    }
  }
};
