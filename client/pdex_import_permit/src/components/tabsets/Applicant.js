import React , {Component, Suspense} from 'react'
import { Container, Nav, NavItem, NavLink, TabContent} from 'reactstrap';
import Locales from '../utils/Locales'
import Navigator from '../Navigator'
import Fetchers from '../utils/Fetchers';

const Notifications = React.lazy(()=>import('../Notifications'))
const Quotas = React.lazy(()=>import('../Quotas'))
const ApplicationForm = React.lazy(()=>import('../ApplicationForm'))
const Applications = React.lazy(()=>import('../Applications'))
const ApplicationSubmit = React.lazy(()=>import('../ApplicationSubmit'))
const ApplicationTrack = React.lazy(()=>import('../ApplicationTrack'))
const Product = React.lazy(()=>import('../Product'))
const Applications1 = React.lazy(()=>import('../Applications1'))
const Applications2 = React.lazy(()=>import('../Applications2'))
const Applications3 = React.lazy(()=>import('../Applications3'))
const ApplicantInvoiced = React.lazy(()=>import('../ApplicantInvoiced'))

/**
 * Tabset for Applicant component
 * @example <Applicant />
 */
class Applicant extends Component{

    constructor(prop){
        super(prop)
        this.state={
            labels:{
                notifications:"",
                product_list:"",
                current_applications:"",
                global_archive:"",
                paymentneeded:"",
                permits:"",
            }
        }
       this.activeComponent=this.activeComponent.bind(this)
       this.nav = new Navigator(this)
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
            case "QUOTAS":
                return <Quotas />
            case "APPLICATIONFORM":
                return <ApplicationForm appId={this.nav.state.activeParameter} edit/>
            case "APPLICATIONSUBMIT":
                return <ApplicationSubmit appId={this.nav.state.activeParameter} />
            case "APPLICATIONTRACK":
                    return <ApplicationTrack appId={this.nav.state.activeParameter} />
            case "APPLICANTINVOICED":
                    return <ApplicantInvoiced appId={this.nav.state.activeParameter} />
            case "APPLICATIONS":
                return <Applications />
            case "APPLICATIONS1":
                return <Applications1 />
            case "APPLICATIONS2":
                return <Applications2 />
            case "APPLICATIONS3":
                return <Applications3 />
            case "PRODUCT":
                return <Product prodId={this.nav.state.activeParameter} close={()=>{
                    window.history.back()
                }} />
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
                    <NavLink href={Fetchers.contextPath()+"/applicant#notifications,notifications"} 
                    onClick={()=>{Navigator.navigate("notifications","notifications","void")}} 
                    active={Navigator.tabName().toLowerCase()=="notifications" || Navigator.tabName().length==0}>
                        {this.state.labels.notifications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink href={Fetchers.contextPath()+"/applicant#quotas,quotas"} 
                    onClick={()=>{Navigator.navigate("quotas","quotas","void")}}
                    active={Navigator.tabName().toLowerCase()=="quotas"}>
                        {this.state.labels.product_list}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/applicant#applications,applications"}
                        onClick={()=>{Navigator.navigate("applications", "applications")}}
                        active={Navigator.tabName()=="applications"}>
                        {this.state.labels.current_applications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/applicant#invoiced,applications1"}
                        onClick={()=>{Navigator.navigate("invoiced", "applications1")}}
                        active={Navigator.tabName()=="invoiced"}>
                        {this.state.labels.paymentneeded}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/applicant#permits,applications2"}
                        onClick={()=>{Navigator.navigate("permits", "applications1")}}
                        active={Navigator.tabName()=="permits"}>
                        {this.state.labels.permits}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/applicant#archive,applications3"}
                        onClick={()=>{Navigator.navigate("archive", "applications1")}}
                        active={Navigator.tabName()=="archive"}>
                        {this.state.labels.global_archive}
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

export default Applicant