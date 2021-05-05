import React , { Component } from 'react'
import ReactDom from 'react-dom'
import Application from '../components/Application'
import 'font-awesome/scss/font-awesome.scss'
import '../sass/custom.scss'
import Applicant from '../components/tabsets/Applicant'

const component = <Applicant />
ReactDom.render(<div>                                 
                    <Application tabset={component}/>         
                </div>, document.getElementById('app'));