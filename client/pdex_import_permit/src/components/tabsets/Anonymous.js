import React , {Component, Suspense} from 'react'
import PropTypes from 'prop-types'
import { Container, Nav, NavItem, NavLink, TabContent} from 'reactstrap';
const Enter = React.lazy(()=>import('../Enter'))
const Login = React.lazy(()=>import('../Login'))
import Locales from '../utils/Locales'
const Alerts = React.lazy(()=>import('../utils/Alerts'))
import Navigator from '../Navigator'

/**
 * Tabset for Anonymous component
 * @property {string} tab selected tab name
 * @property {string} component selected component name
 * @property {string} param - param for a component, if no params - string 'void'
 * @example <Anonymous tab='term' component='Login' param='void' />
 */
class Anonymous extends Component{

    constructor(prop){
        super(prop)
        this.state={
            labels:{
                termsAndConditions:"Terms and Conditions",
            }
        }
        this.nav = new Navigator(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
    }

    

    /**
     * Create an active component with all necessary properties
    */
    activeComponent(){  
        switch(this.nav.state.activeComponent){
            case "Enter":
              return(<Enter/>)
            case "Login":
                return(<Login/>)
            default:
              if(typeof this.props.component != 'undefined' && this.props.component.lenght>0){
                Alerts.show("Bad component " + this.props.component)
              }
              if(window.location.hash.length==0){ //to avoid excess mount of the default component
                return (<Enter/>)
              }
        }

    }


    render(){
        return(
            <Suspense fallback={<div></div>}>
                <Container fluid>
                <Nav tabs>
                    <NavItem>
                        <NavLink href="#" onClick={()=>{Navigator.navigate("term","Enter","void")}} active>
                            {this.state.labels.termsAndConditions}
                        </NavLink>
                    </NavItem>
                </Nav>
                <TabContent>
                    {this.activeComponent()}
                </TabContent>
            </Container>
        </Suspense>
        )
    }

}

export default Anonymous

Anonymous.propTypes ={
    tab:PropTypes.string.isRequired,
    component:PropTypes.string.isRequired,
    param:PropTypes.string.isRequired
}