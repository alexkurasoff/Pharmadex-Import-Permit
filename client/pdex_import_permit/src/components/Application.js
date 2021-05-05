import React , {Component,Suspense} from 'react'
const Alerts = React.lazy(()=>import('./utils/Alerts'))
import Spinner from './utils/Spinner'
import {Container, Row, Col} from 'reactstrap' 
import 'whatwg-fetch'
import 'custom-event-polyfill'
import 'url-search-params-polyfill'
import PropTypes from 'prop-types'

/**
 * This component places common things to a page
 * @property {component} tabset
 * @example
 * const component = <Anonymous tab={'anonymous'} component={'enter'} param={'void'} /> 
 * <Application tabset={component}/>
 */
class Application extends Component {   
    
      constructor(props) {
        super(props);
        this.state={}
      }
      
     /**
      * Display a tabset depends on context name
      */
     render() {    
        return (
          <Container fluid>
             <Suspense fallback={<div></div>}>
            <Spinner />
            <Alerts  />
            </Suspense>
            <Row>
              <Col>
               {this.props.tabset}
              </Col>
            </Row>
          </Container>     
        )
     }
   }
   
   export default Application;
   Application.propTypes={
    tabset:PropTypes.object.isRequired //Implementation of a tabset. Component
  }