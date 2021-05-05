import React , { Component } from 'react'
import ReactDom from 'react-dom'
import Application from '../components/Application'
import 'font-awesome/scss/font-awesome.scss'
import '../sass/custom.scss'
import Secretary from '../components/tabsets/Secretary'

const component = <Secretary />
ReactDom.render(<div>                                 
                    <Application tabset={component}/>         
                </div>, document.getElementById('app'));