import React , { Component } from 'react'
import ReactDom from 'react-dom'
import Application from '../components/Application'
import 'font-awesome/scss/font-awesome.scss'
import '../sass/custom.scss'
import Moderator from '../components/tabsets/Moderator'

const component = <Moderator />
ReactDom.render(<div>                                 
                    <Application tabset={component}/>         
                </div>, document.getElementById('app'));