var webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
var path = require('path');


module.exports = {
  mode: 'development',
  entry: {
    head: path.resolve(__dirname, './src/js/head'),     
    anonymous: path.resolve(__dirname, './src/js/anonymous'),
    applicant: path.resolve(__dirname, './src/js/applicant'),
    secretary: path.resolve(__dirname, './src/js/secretary'),
    review: path.resolve(__dirname, './src/js/review'),
    moderator: path.resolve(__dirname, './src/js/moderator'),
  },  
  devServer: {
      inline: true,
      contentBase: './src',
      port: 3001,
      proxy: {
        "/": "http://localhost:8081",
        "/api": "http://localhost:8081",
        "/login": "http://localhost:8081",
        "/logout": "http://localhost:8081"
      }
  },
  module: {
      rules: [        
        {
          test: /\.(js|jsx)$/,
          exclude: /node_modules/,
          use: ['babel-loader']
        }, 
        {
          test: /\.css$/,
          use: [
            'style-loader',
            'css-loader'
          ]
        },
        {
          test: /\.scss$/,
          use: [
            'style-loader',
            'css-loader',
            'sass-loader'
          ]
        },
        {
          test: /\.(ttf|eot|png|svg|jpg|gif)$/,
           use: [
            'file-loader'
          ]
        },
        { 
          test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/, 
          loader: "url-loader?limit=10000&mimetype=application/font-woff" 
        },       
      ]
  }, 

  output: {
    publicPath: '/js/',
    filename: '[name]Bundle.js',
    chunkFilename: '[name].bundle.js',
  },
};