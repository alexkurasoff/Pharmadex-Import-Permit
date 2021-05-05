import React , {Component} from 'react'
import PropTypes from 'prop-types';
import {Row,Col, Button} from 'reactstrap'
import Locales from './utils/Locales'
import Languages from './Languages'
import Fetchers from './utils/Fetchers'
import Navigator from './Navigator'
import FieldOption from './form/FieldOption'
import FieldsComparator from './form/FieldsComparator'
//import { execFileSync } from 'child_process';

/**
 * Common header for all pages
 * @property title name of this page
 * @property user current user, object, class UserDetailsDto
 * @example <Head title={this.state.labels.application_title} user={this.state.user} />
 */
class Head extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{
              login:"",
              admin_logout:"",
              lostPassword:"",
              joinUs:"",
              pip:"",
              currentrole:"",
            },
            title:"",
            subTitle:"",
            flags:[],
            userOptions:[],
            data:{

            }
        }
        this.loginButtons = this.loginButtons.bind(this)
        this.createTitle=this.createTitle.bind(this)
        this.changeUserRole=this.changeUserRole.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.comparator = new FieldsComparator(this)
        Fetchers.postJSONNoSpinner("/api/public/header","",(query,result)=>{
            let s = this.state
            s.title=result.workspace.title
            s.subTitle=result.workspace.subTitle
            s.flags=result.flags
            this.setState(s)
          })
          Fetchers.postJSONNoSpinner("/api/public/userData","",(query,result)=>{
            this.state.user=result
            this.state.data=result
            this.comparator = new FieldsComparator(this)
            this.setState(this.state.user)
          })
    }

    componentDidUpdate(){
      if (this.comparator!=undefined){
        const fld = this.comparator.checkChanges();
        if(fld.length>0)
          this.changeUserRole()
      }
    }
    /**
     * Which buttons - login or logout should be
     */
    loginButtons(){
      if(this.state.user.username=="anonymous"){
        return ( //login prompt
          <Col xs={12} sm={12} lg={6} xl={6}>
            <Row>
              <Col xs={12} sm={12} lg={4} xl={4}>
              <Button color="link" style={{whiteSpace:'normal'}} size="sm"
              onClick={ ()=>{
                Navigator.navigate("term","Login", "void")
                }
              }>
                {this.state.labels.login}
              </Button>
            </Col>
            <Col xs={12} sm={12} lg={4} xl={4}>
              <Button color="link" style={{whiteSpace:'normal'}} size="sm">
                {this.state.labels.lostPassword}
              </Button>
            </Col> 
            <Col xs={12} sm={12} lg={4} xl={4}>
              <Button color="link" style={{whiteSpace:'normal'}} size="sm">
                  {this.state.labels.joinUs}
              </Button>
            </Col>
          </Row>
        </Col>
        )
      }else{
        return(
         <Col xs={12} sm={12} lg={6} xl={6}>
            <Row>
              <Col xs={12} sm={12} lg={4} xl={4}>
                <Button color="link" style={{whiteSpace:'normal'}} size="sm"
                onClick={ ()=>{
                  Fetchers.postForm("/logout","",(result)=>{
                    window.location.assign("/")
                  })
                  }
                }>
                  {this.state.labels.admin_logout}
                </Button>
              </Col>
              <Col xs={12} sm={12} lg={8} xl={8}>
                 <b>{this.state.user.fullName}</b>
              </Col>
            </Row>
            {this.buildRolesComponent()}
          </Col>
        )
      }
    }

    buildRolesComponent(){
      if(this.state.user.multirole){
        return(
          <Row>
            <Col className="m-1">
              <FieldOption attribute='currentrole' component={this} notEmpty/>
            </Col>
          </Row>
        )
      }else{
        return []
      }
    }

    changeUserRole(){
      Fetchers.postJSON("/api/common/changeuserrole", this.state.data.currentrole.value.id, (query, result)=>{
        this.state.user=result
        this.setState(this.state.user)
        this.comparator = new FieldsComparator(this)
        window.location.assign(Fetchers.contextPath()+"/")
      })
    }

    /**
     * Create a title
     */
    createTitle(){
      let ret=this.state.labels.pip
      if(typeof this.state.user.applicantName == 'string'){
        if(this.state.user.applicantName.length>3){
          ret=ret+", "+this.state.user.applicantName
        }
      }
      return ret
    }

    render() {  
        if(this.state.user != undefined){    
        return (
        <Row>
            <Col className="d-flex justify-content-center" xs={12} sm={12} lg={2} xl={2}>
              <img src="/api/public/emblem.svg" height="100"/>
            </Col>
            <Col xs={12} sm={12} lg={5} xl={5}>
              <Row>
                <Col className="d-flex justify-content-center">
                  <h3>{this.state.title}</h3>
                </Col>
              </Row>
              <Row>
                <Col className="d-flex justify-content-center">
                  <h5>{this.state.subTitle}</h5>
                </Col>
              </Row>
              <Row>
                <Col className="d-flex justify-content-center">
                  <h6>{this.createTitle()}</h6>
                </Col>
              </Row>
            </Col>
            <Col xs={12} sm={12} lg={5} xl={5}>
              <Row>
                    {this.loginButtons()}
                    <Col xs={12} sm={12} lg={6} xl={6}>
                       <Languages flags={this.state.flags} />
                    </Col>
              </Row>              
            </Col>
        </Row>
        )
        }else{
          return []
        }
    }

}

export default Head;