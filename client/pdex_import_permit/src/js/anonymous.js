import React from 'react'
import ReactDom from 'react-dom'
import Application from '../components/Application'
import '../sass/custom.scss'
import 'font-awesome/scss/font-awesome.scss'
import Anonymous from '../components/tabsets/Anonymous'

const component = <Anonymous tab={'anonymous'} component={'enter'} param={'void'} />
ReactDom.render(<div>                                 
                    <Application tabset={component}/>         
                </div>, document.getElementById('app'));