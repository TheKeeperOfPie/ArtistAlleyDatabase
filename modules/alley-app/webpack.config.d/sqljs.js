config.resolve = {
    fallback: {
        fs: false,
        path: false,
        crypto: false,
    }
};

config.devServer = Object.assign(
    {},
    config.devServer || {},
    {
        headers: {
            "Cross-Origin-Opener-Policy": "same-origin",
            "Cross-Origin-Embedder-Policy": "require-corp"
        }
    }
);

const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/sql.js/dist/sql-wasm.wasm'
        ]
    })
);

// https://github.com/sqldelight/sqldelight/issues/2057#issuecomment-1087799619
const webpack = require("webpack");

module.exports = function override(config) {
    const fallback = config.resolve.fallback || {};
    Object.assign(fallback, {
        path: require.resolve("path-browserify"),
        fs: require.resolve("browserify-fs"),
        "util": require.resolve("util/"),
        "buffer": require.resolve("buffer/"),
        "stream": require.resolve("stream-browserify"),
        "crypto": require.resolve("crypto-browserify")
    });
    config.resolve.fallback = fallback;
  return config;
};
