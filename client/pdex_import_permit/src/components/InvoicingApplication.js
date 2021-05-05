import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Col, Row, Button,Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import Alerts from './utils/Alerts'
import ApplicationEvents from './ApplicationEvents'
import ApplicationDisplay from './ApplicationDisplay'
import Navigator from './Navigator'
import Attachments from './Attachments'
import QuestionAnswers from './QuestionAnswers'
import Applicant from './Applicant'
import ButtonUni from './form/ButtonUni'
import ValidationMarkup from './ValidationMarkup'
import ApplicationCheckList from './ApplicationCheckList'

/**
 * Invoicing an application
 * @property {number} applId
 * @example <InvoicingApplication applId={this.props.applId} />
 */
class InvoicingApplication extends Component{
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
                cancelapplication:"",
                willcancel:"",
                descr_FACT:"",
                submitforapproval:""
            }
        }
        this.loadData=this.loadData.bind(this)
        this.cancel=this.cancel.bind(this)
        this.toApproval=this.toApproval.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/moderator/application/open",this.state.data,(query,result)=>{
            if(result.navigator.component.toUpperCase() == "INVOICINGAPPLICATION"){
                this.state.data=result
                this.setState(this.state)
            }else{
                Navigator.navigate(result.navigator.tab, result.navigator.component, result.navigator.params)
            }
        })
    }

    /**
     * Cancel an application
     */
    cancel(){
        Alerts.warning(this.state.labels.willcancel, ()=>{
            this.state.data.id=this.props.appId
            Fetchers.postJSON("/api/common/application/cancel", this.state.data, (query,result)=>{
                this.state.data=result
                if(this.state.data.valid){
                    window.history.back()
                }else{
                    this.state.alertColor="danger"
                }
                this.setState(this.state)
            })
        }, ()=>{
            
        })
 
    }
    /**
     * Pass to approval
     */
    toApproval(){
        Alerts.warning(this.state.labels.submitforapproval, ()=>{
        Fetchers.postJSON("/api/moderator/submit/approval", this.state.data, (query,result)=>{
            this.state.data=result
                if(this.state.data.valid){
                    window.history.back()
                }else{
                    this.state.alertColor="danger"
                }
                this.setState(this.state)
        })
        }, ()=>{
            
        })
    }

    buildTitle(){
        var title = this.state.labels.descr_FACT + " (" + this.state.data.orderType.value.description + ", " + this.state.data.itemType.value.code + ")"
        return title
    }

    render(){
        if(this.state.data.pipNumber==undefined){
            return []
        }else{
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='2' xl='2'>
                    <h6>{this.buildTitle()}</h6>
                    </Col>
                    <Col xs='12' sm='12' lg='5' xl='5'>
                            <Alert className="p-1" isOpen={this.state.data.alertMessage.length>0} color={this.state.alertColor}>{this.state.data.alertMessage}</Alert>
                    </Col>
                    <Col xs='12' sm='12' lg='2' xl='2'>
                           <ButtonUni onClick={this.toApproval} label={this.state.labels.submitforapproval} outline={false} />
                    </Col>
                    <Col xs='12' sm='12' lg='2' xl='2'>
                           <ButtonUni onClick={this.cancel} label={this.state.labels.cancelapplication} outline={true} disabled={!this.state.data.showCancel}/>
                    </Col>
                    <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>{window.history.back()}}/>
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
                                    <ApplicationDisplay application={this.state.data} 
                                    title={this.state.data.applicant.value.code+' / '+this.state.data.pipNumber.value}
                                    loader={this.loadData}/>
                                </Col>
                            </Row>
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <Row>
                                <Col>
                                    <ApplicationCheckList appId={this.props.appId} colorize={!this.state.data.valid} readOnly={false}/>
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    <ValidationMarkup appId={this.props.appId} readOnly={true} strict={false} />
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                <QuestionAnswers appId={this.props.appId} />
                                </Col>
                            </Row>
                            <Row>
                                <Col>
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
export default InvoicingApplication
InvoicingApplication.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}