import React , { Component } from 'react'
import ReactDom from 'react-dom'
import '../sass/custom.scss'
import Head from '../components/Head'

ReactDom.render(<div>                                 
                    <Head/>         
                </div>, document.getElementById('head'));