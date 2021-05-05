import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Col, Row, Button,Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ButtonUni from './form/ButtonUni'
import Alerts from './utils/Alerts'
import ApplicationDisplay from './ApplicationDisplay'
import ApplicationCheckList from './ApplicationCheckList'
import ApplicationEvents from './ApplicationEvents'
import Attachments from './Attachments'
import QuestionAnswers from './QuestionAnswers'
import Applicant from './Applicant'
import { isUndefined } from 'util'
import Navigator from './Navigator' 



/**
 * Review am application by import responsible. Fill out the checklist
 * @property {number} applId
 * @example <ReviewApplication appId={this.props.applId} />
 */
class ReviewApplication extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            data:{
                valid:true,
                id:0,
                alertMessage:"",
            },
            labels:{
                locale:"",
                review:"",
                submitforapproval:"",
                submitforreject:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.approve=this.approve.bind(this)
        this.reject=this.reject.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/review/application/open",this.state.data,(query,result)=>{
            if(result.navigator.component.toUpperCase() == "REVIEWAPPLICATION"){
                this.state.data=result
                this.state.data.editable=true
                this.setState(this.state)
            }else{
                Navigator.navigate(result.navigator.tab, result.navigator.component, result.navigator.params)
            }
        })
    }

    /**
     * Submit an application - approve
     */
    approve(){
        Alerts.warning(this.state.labels.submitforapproval, ()=>{
            this.state.data.id=this.props.appId
            Fetchers.postJSON("/api/review/approve", this.state.data, (query,result)=>{
                this.state.data=result
                if(this.state.data.valid){
                    this.state.alertColor="info"
                    window.history.back()
                }else{
                    this.state.alertColor="danger"
                    this.setState(this.state)
                }
            })
        }, ()=>{
            //nothing to do
        })
 
    }

    /**
     * Submit an application - reject
     */
    reject(){
        Alerts.warning(this.state.labels.submitforreject, ()=>{
            this.state.data.id=this.props.appId
            Fetchers.postJSON("/api/review/reject", this.state.data, (query,result)=>{
                this.state.data=result
                if(this.state.data.valid){
                    this.state.alertColor="info"
                    window.history.back()
                }else{
                    this.state.alertColor="danger"
                    this.setState(this.state)
                }
            })
        }, ()=>{
            //nothing to do
        })
 
    }

    buildTitle(){
        var title = this.state.labels.review + " (" + this.state.data.orderType.value.description + ", " + this.state.data.itemType.value.code + ")"
        return title
    }

    render(){
        if(isUndefined(this.state.data.pipStatus)){
            return []
        }else{
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='4' xl='2'>
                    <h6>{this.buildTitle()}</h6>
                    </Col>
                    <Col xs='12' sm='12' lg='4' xl='3'>
                            <Alert className="p-1" isOpen={this.state.data.alertMessage.length>0} color={this.state.alertColor}>{this.state.data.alertMessage}</Alert>
                    </Col>
                    <Col xs='12' sm='12' lg='3' xl='3' className="d-flex justify-content-end">
                        <ButtonUni onClick={this.approve} outline={false} label={this.state.labels.submitforapproval} />
                    </Col>
                    <Col xs='12' sm='12' lg='3' xl='3' className="d-flex justify-content-end">
                        <ButtonUni onClick={this.reject} outline={true} label={this.state.labels.submitforreject} />
                    </Col>
                    <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>{
                               //Navigator.openApplication(this.props.appId)
                               window.history.back()
                            }
                        }/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <Row>
                                <Col>
                                    <Applicant appId={this.props.appId} />
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    <ApplicationDisplay application={this.state.data} title={this.state.data.pipNumber.value} 
                                    loader={this.loadData}/>
                                </Col>
                            </Row>
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <Row>
                                <Col>
                                    <QuestionAnswers appId={this.props.appId} />
                                </Col>
                            </Row>

                            <Row>
                            <Col xs='12' sm='12' lg='12' xl='12'>
                                <ApplicationCheckList appId={this.props.appId} colorize={!this.state.data.valid} readOnly={!this.state.data.editable}/>
                            </Col>
                            </Row>
                            <Row>
                                <Col xs='12' sm='12' lg='12' xl='12'>
                                    <Attachments appId={this.props.appId} />
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    <ApplicationEvents appId={this.props.appId} /> 
                                </Col>
                            </Row>
                        </Col>
                    </Row>
                </CardBody>
            </Card>
        )
        }
    }


}
export default ReviewApplication
ReviewApplication.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}