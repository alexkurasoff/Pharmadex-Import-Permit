import React , {Component,Suspense} from 'react'
import { Container, Nav, NavItem, NavLink, TabContent} from 'reactstrap';
import Locales from '../utils/Locales'
import Navigator from '../Navigator'
import Fetchers from '../utils/Fetchers'

const Notifications = React.lazy(()=>import('../Notifications'))
const Applications = React.lazy(()=>import('../Applications'))
const Applications1 = React.lazy(()=>import('../Applications1'))
const QuestionAnswers = React.lazy(()=>import('../QuestionAnswers'))
const SecretaryApplicationReceipt = React.lazy(()=>import('../SecretaryApplicationReceipt'))
const ApplicationTrack = React.lazy(()=>import('../ApplicationTrack'))
const SecretaryApplicationValidation = React.lazy(()=>import('../SecretaryApplicationValidation'))
const ArchiveApplication = React.lazy(()=>import('../ArchiveApplication'))
/**
 * Tabset for Secretary workspace
 * @example <Secretary />
 */
class Secretary extends Component{

    constructor(prop){
        super(prop)
        this.state={
            labels:{
                notifications:"",
                profile:"",
                global_archive:"",
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
            case "APPLICATIONS":
                return <Applications />
                case "APPLICATIONS1":
                    return <Applications1 />
            case "QUESTIONANSWERS":
                return <QuestionAnswers appId={this.nav.state.activeParameter}/>
            case "SECRETARYAPPLICATIONRECEIPT":
                return <SecretaryApplicationReceipt appId={this.nav.state.activeParameter}/>
            case "SECRETARYAPPLICATIONVALIDATION":
                return <SecretaryApplicationValidation appId={this.nav.state.activeParameter} />
            case "ARCHIVEAPPLICATION":
                return <ArchiveApplication appId={this.nav.state.activeParameter} />
            case "APPLICATIONTRACK":
                return <ApplicationTrack appId={this.nav.state.activeParameter} />
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
                    <NavLink href={Fetchers.contextPath()+"/secretary#notifications,notifications"} 
                    onClick={()=>{Navigator.navigate("notifications","notifications","void")}} 
                    active={Navigator.tabName().toLowerCase()=="notifications" || Navigator.tabName().length==0}>
                        {this.state.labels.notifications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/secretary#applications,applications"}
                        onClick={()=>{Navigator.navigate("applications", "applications")}}
                        active={Navigator.tabName()=="applications"}>
                        {this.state.labels.current_applications}
                    </NavLink>
                </NavItem>
                <NavItem>
                    <NavLink 
                        href={Fetchers.contextPath()+"/secretary#archive,applications1"}
                        onClick={()=>{Navigator.navigate("archive", "applications")}}
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

export default Secretary