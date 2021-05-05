var webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
var path = require('path');


module.exports = {
  mode: 'production',
  entry: {
    head: path.resolve(__dirname, './src/js/head'),   
    anonymous: path.resolve(__dirname, './src/js/anonymous'),
    applicant: path.resolve(__dirname, './src/js/applicant'),
    secretary: path.resolve(__dirname, './src/js/secretary'),
    review: path.resolve(__dirname, './src/js/review'),
    moderator: path.resolve(__dirname, './src/js/moderator'),
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
  plugins:[
    //new BundleAnalyzerPlugin(),
    new  CleanWebpackPlugin(['C:/eclipse/workspace/pdex_import_permit/src/main/resources/static/js'], {allowExternal: true})
  ],

  output: {
    path: path.resolve('C:/eclipse/workspace/pdex_import_permit/src/main/resources/static/', 'js'), 
    publicPath: '/js/',
    filename: '[name].[hash].js',
    chunkFilename: '[id][name].[chunkhash].js',
  },
};