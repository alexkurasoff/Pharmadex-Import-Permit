import React , {Component,Suspense} from 'react'
import PropTypes from 'prop-types'
import { Container, Nav, NavItem, NavLink, TabContent, TabPane} from 'reactstrap';
import Locales from '../utils/Locales'
import Navigator from '../Navigator'
import Fetchers from '../utils/Fetchers';

const Notifications = React.lazy(()=>import('../Notifications'))
const ReviewApplication = React.lazy(()=>import('../ReviewApplication'))
const Applications = React.lazy(()=>import('../Applications'))

/**
 * Tabset for Review component
 * @example <Review />
 */
class Review extends Component{

    constructor(prop){
        super(prop)
        this.state={
            labels:{
                notifications:"",
                current_applications:"",
            }
        }
       this.activeComponent=this.activeComponent.bind(this)
       this.nav=new Navigator(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
    }

    /**
     * Create an active component with all necessary properties
    */
   activeComponent(){  
        switch(this.nav.state.activeComponent.toUpperCase()){
            case "NOTIFICATIONS":
                return <Notifications />
            case "REVIEWAPPLICATION":
                return <ReviewApplication appId={this.nav.state.activeParameter} />
            case "APPLICATIONS":
                return <Applications />
            default:
            if(window.location.hash.length==0){ //to avoid excess mount of the default component
                return <Notifications />
            }
        }
    }
    render(){
        return(
            <Container fluid>
            <Nav tabs>
                <NavItem>
                    <NavLink href={Fetchers.contextPath()+"/review#notifications,Notifications"}
                     onClick={()=>{Navigator.navigate("notifications","notifications")}}
                     active={Navigator.tabName().toLowerCase()=="notifications" || Navigator.tabName().length==0} >
                        {this.state.labels.notifications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/review#applications,applications"}
                        onClick={()=>{Navigator.navigate("applications", "applications")}}
                        active={Navigator.tabName()=="applications"}>
                        {this.state.labels.current_applications}
                    </NavLink>
                </NavItem>
            </Nav>
            <TabContent>
            <Suspense fallback={<div></div>}>
                {this.activeComponent()}
            </Suspense>
            </TabContent>
        </Container>
        )
    }

}

export default Review