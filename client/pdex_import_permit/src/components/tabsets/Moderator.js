import React , {Component,Suspense} from 'react'
import { Container, Nav, NavItem, NavLink, TabContent} from 'reactstrap';
import Locales from '../utils/Locales'
import Fetchers from '../utils/Fetchers';
import Navigator from '../Navigator'


const Notifications = React.lazy(()=>import('../Notifications'))
const ApplicationTrack = React.lazy(()=>import('../ApplicationTrack'))
const Applications = React.lazy(()=>import('../Applications'))
const Applications1 = React.lazy(()=>import('../Applications1'))
const Applications2 = React.lazy(()=>import('../Applications2'))
const Applications3 = React.lazy(()=>import('../Applications3'))
const Applications4 = React.lazy(()=>import('../Applications4'))
const ValidateApplication = React.lazy(()=>import('../ValidateApplication'))
const InvoicingApplication = React.lazy(()=>import('../InvoicingApplication'))
const FinalizeApplication = React.lazy(()=>import('../FinalizeApplication'))
const Document = React.lazy(()=>import('../Document'))
const CheckList = React.lazy(()=>import('../CheckList'))
const Administration = React.lazy(()=>import('../Administration'))

/**
 * Tabset for Moderator component
 * @example <Anonymous tab='term' component='Login' param='void' />
 */
class Moderator extends Component{

    constructor(prop){
        super(prop)
        this.state={
            labels:{
                notifications:"",
                inprocess:"",
                tovalidate:"",
                descr_FACT:"",
                global_archive:"",
                finalize:"",
                administration:""
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
            case "APPLICATIONS":
                    return <Applications />
            case "APPLICATIONS1":
                    return <Applications1 />
            case "APPLICATIONS2":
                    return <Applications2 />
            case "APPLICATIONS3":
                    return <Applications3 />
            case "APPLICATIONS4":
                        return <Applications4 />
            case "APPLICATIONTRACK":
                    return <ApplicationTrack appId={this.nav.state.activeParameter}/>
            case "VALIDATEAPPLICATION":
                    return <ValidateApplication appId={this.nav.state.activeParameter}/>
            case "INVOICINGAPPLICATION":
                    return <InvoicingApplication appId={this.nav.state.activeParameter}/>
            case "FINALIZEAPPLICATION":
                    return <FinalizeApplication appId={this.nav.state.activeParameter}/>
            case "CHECKLISTS":
                    return <Administration tab={1} nav={this.nav}/>
            case "CHECKLIST":
                    return <CheckList pipStatusId={this.nav.state.activeParameter}/>
            case "DOCUMENTS":
                    return <Administration tab={2} nav={this.nav}/>
            case "DOCUMENT":
                return <Document docid={this.nav.state.activeParameter}/>
           /* case "CHECKLISTS":
                    return <CheckLists />
            case "CHECKLIST":
                    return <CheckList pipStatusId={this.nav.state.activeParameter}/>*/
            default:
            if(window.location.hash.length==0){ //to avoid excess mount of the default component
                return <Notifications />
            }
        }
    }
    //className="nav flex-column"
    render(){
        return(
            <Container fluid>
            <Nav tabs >
                <NavItem>
                    <NavLink href={Fetchers.contextPath()+"/moderator#notifications,Notifications"}
                     onClick={()=>{Navigator.navigate("notifications","notifications")}}
                     active={Navigator.tabName().toLowerCase()=="notifications" || Navigator.tabName().length==0} >
                        {this.state.labels.notifications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#validation,applications"}
                        onClick={()=>{Navigator.navigate("validation", "applications")}}
                        active={Navigator.tabName()=="validation"}>
                        {this.state.labels.tovalidate}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#invoicing,applications1"}
                        onClick={()=>{Navigator.navigate("invoicing", "applications1")}}
                        active={Navigator.tabName()=="invoicing"}>
                        {this.state.labels.descr_FACT}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#finalize,applications2"}
                        onClick={()=>{Navigator.navigate("finalize", "applications2")}}
                        active={Navigator.tabName()=="finalize"}>
                        {this.state.labels.finalize}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#inprocess,applications3"}
                        onClick={()=>{Navigator.navigate("inprocess", "applications3")}}
                        active={Navigator.tabName()=="inprocess"}>
                        {this.state.labels.inprocess}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#archive,applications4"}
                        onClick={()=>{Navigator.navigate("archive", "applications3")}}
                        active={Navigator.tabName()=="archive"}>
                        {this.state.labels.global_archive}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/moderator#administration,checklists"}
                        onClick={()=>{Navigator.navigate("administration", "checklists")}}
                        active={Navigator.tabName()=="administration"}>
                        {this.state.labels.administration}
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

export default Moderator