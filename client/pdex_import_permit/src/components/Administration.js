import React , {Component} from 'react'
import {TabContent, Col, Row, Label, Nav, NavItem, NavLink} from 'reactstrap'
import Locales from './utils/Locales'
import PropTypes from 'prop-types'
import Navigator from './Navigator'
import CheckLists from "./CheckLists"
import Documents from "./Documents"

/**
 * Tab Administration by Moderator
 */
class Administration extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{
                locale:"",
                checklist:"",
                documents:""
            }
        }
        this.nav=this.props.nav
        this.activeComponent=this.activeComponent.bind(this)
    }

    componentDidMount() {
        Locales.resolveLabels(this)
        this.nav=this.props.nav
    }

    activeComponent(){
        switch(this.nav.state.activeComponent.toUpperCase()){
            case "CHECKLISTS":
                return <CheckLists />
            case "DOCUMENTS":
                return <Documents />
            default:
                return(<Label>{"404 Not found " + this.nav.state.activeComponent}</Label>)
        }
    }

    render(){
        return(
                <TabContent >
                        <Row className="m-3">
                            <Col xs="12" sm="12" lg="2" xl="1" className="border">
                                <Nav  className={"justify-content-center nav-pills nav-justified mt-2"}>
                                    <NavItem>
                                        <NavLink href="#" active={this.props.tab==1}
                                            onClick={(e)=>{Navigator.navigate("administration", "checklists")}}>
                                            {this.state.labels.checklist}
                                        </NavLink>
                                    </NavItem>
                                    <NavItem>
                                        <NavLink href="#" active={this.props.tab==2}
                                            onClick={(e)=>{Navigator.navigate("administration", "documents")}}>
                                            {this.state.labels.documents}
                                        </NavLink>
                                    </NavItem>
                                </Nav>
                            </Col>
                            <Col className="border">
                                {this.activeComponent()}
                            </Col>
                        </Row>
                </TabContent>
        )
    }
}
export default Administration
Administration.propTypes={
    nav:PropTypes.object.isRequired,
    tab:PropTypes.number.isRequired
}