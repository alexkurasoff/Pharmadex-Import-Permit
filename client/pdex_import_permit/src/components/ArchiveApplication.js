import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Col, Row, Button,Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import Alerts from './utils/Alerts'
import ApplicationEvents from './ApplicationEvents'
import ApplicationDisplay from './ApplicationDisplay'
import Attachments from './Attachments'
import QuestionAnswers from './QuestionAnswers'
import Applicant from './Applicant'
import ButtonUni from './form/ButtonUni'
import ApplicationCheckList from './ApplicationCheckList'
import VerificationResults from './VerificationResults'
import FinalizeMarkup from './FinalizeMarkup'
import ValidationMarkup from './ValidationMarkup'
import Navigator from './Navigator'

/**
 * Archive an application after an applicant will receive it
 * @property {number} applId
 * @example <ArchiveApplication applId={this.props.applId} />
 */
class ArchiveApplication extends Component{
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
                global_archive:"",
                archiveapplication:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.cancel=this.cancel.bind(this)
        this.archive=this.archive.bind(this)
        this.submitAction=this.submitAction.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/secretary/application/open",this.state.data,(query,result)=>{
            if(result.navigator.component.toUpperCase() == "ARCHIVEAPPLICATION"){
                this.state.data=result
                this.state.data.editable=true
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
               this.submitAction(result)
            })
        }, ()=>{
            
        })
 
    }

    /**
     * Archive it
     */
    archive(){
        Alerts.warning(this.state.labels.archiveapplication, ()=>{
            this.state.data.fields=[]
        Fetchers.postJSON("/api/secretary/application/archive", this.state.data, (query,result)=>{
           this.submitAction(result)
        })}, ()=>{
            
        })
    }
    /**
     * Common action after submit
     */
    submitAction(result){
        this.state.data=result
        if(this.state.data.valid){
            window.history.back()
        }else{
            this.state.alertColor="danger"
        }
        this.state.data.editable=true
        this.setState(this.state)
    }

    buildTitle(){
        var title = this.state.labels.global_archive + " (" + this.state.data.orderType.value.description + ", " + this.state.data.itemType.value.code + ")"
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
                    <Col xs='12' sm='12' lg='3' xl='3'>
                    <h6>{this.buildTitle()}</h6>
                    </Col>
                    <Col xs='12' sm='12' lg='4' xl='4'>
                            <Alert className="p-1" isOpen={this.state.data.alertMessage.length>0} color={this.state.alertColor}>{this.state.data.alertMessage}</Alert>
                    </Col>
                    <Col xs='12' sm='12' lg='2' xl='2'>
                           <ButtonUni onClick={this.archive} label={this.state.labels.archiveapplication} outline={false} />
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
                                    <ApplicationEvents appId={this.props.appId} /> 
                                </Col>
                            </Row>
                            <Row>
                                <Col>
                                    <VerificationResults appId={this.props.appId} />
                                </Col>
                            </Row>
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
                            <Row hidden={!ValidationMarkup.markedAsValid(this.state.data)}>
                                <Col>
                                    <FinalizeMarkup appId={this.props.appId} readOnly={true} strict={!this.state.data.valid}/>
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

                        </Col>
                    </Row>
                </CardBody>
            </Card>
        )
        }
    }
}
export default ArchiveApplication
ArchiveApplication.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}