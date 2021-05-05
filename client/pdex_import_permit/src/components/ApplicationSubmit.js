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
import Navigator from './Navigator'
import Downloader from './utils/Downloader'
import { isUndefined } from 'util'
import { relativeTimeThreshold } from 'moment'




/**
 * Check and submit an application. Fill out the checklist
 * @property {number} applId
 * @example <ApplicationSubmit appId={this.props.applId} />
 */
class ApplicationSubmit extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            data:{},
            labels:{
                locale:"",
                submittoregister:"",
                askforpermit:"",
                willsubmit:"",
                checkbeforesubmit:"",
                downloadbief:"",
            }
        }
        this.loadData=this.loadData.bind(this)
        this.submit=this.submit.bind(this)
        this.downloadBief=this.downloadBief.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/applicant/application/open",this.state.data,(query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    /**
     * Submit an application
     */
    submit(){
        Alerts.warning(this.state.labels.willsubmit, ()=>{
            this.state.data.id=this.props.appId
            Fetchers.postJSON("/api/applicant/application/submit", this.state.data, (query,result)=>{
                this.state.data=result
                if(!this.state.data.valid){
                    this.state.alertColor="danger"
                    this.setState(this.state)
                }else{
                        Navigator.navigate(result.navigator.tab, result.navigator.component, result.navigator.params)
                }
                
            })
        }, ()=>{
            
        })
 
    }

    /**
     * Generate from the template and upload BIEF
     */
    downloadBief(){
        let downloader = new Downloader();
        downloader.postDownload("/api/applicant/upload/bief",
             this.state.data, "bief.docx");
    }

    render(){
        if(this.state.data.alertMessage != undefined){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='4' xl='3'>
                        <h2>{this.state.labels.checkbeforesubmit}</h2>
                    </Col>
                    <Col xs='12' sm='12' lg='4' xl='4'>
                            <Alert className="p-1" isOpen={this.state.data.alertMessage.length>0} color={this.state.alertColor}>{this.state.data.alertMessage}</Alert>
                    </Col>
                    <Col xs='12' sm='12' lg='3' xl='2' className="d-flex justify-content-end">
                        <ButtonUni onClick={this.downloadBief} outline={true} label={this.state.labels.downloadbief} />
                    </Col>
                    <Col xs='12' sm='12' lg='3' xl='2' className="d-flex justify-content-end">
                        <ButtonUni onClick={this.submit} outline={false} label={this.state.labels.submittoregister} />
                    </Col>
                    <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>{
                                    window.history.back()
                            }
                        }/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <ApplicationDisplay application={this.state.data} title={this.state.data.pipNumber.value} 
                            loader={this.loadData}/>
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <Row>
                                <Col xs='12' sm='12' lg='12' xl='12'>
                                    <Attachments appId={this.props.appId} />
                                </Col>
                            </Row>
                            <Row>
                                <Col xs='12' sm='12' lg='12' xl='12'>
                                    <ApplicationCheckList appId={this.props.appId} colorize={!this.state.data.valid} readOnly={false}/>
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
        }else{
            return []
        }
    }


}
export default ApplicationSubmit
ApplicationSubmit.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}