import React , {Component} from 'react'
import {Card, CardBody,CardHeader,Row, Col,Alert, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ApplicationFormDetails from './ApplicationFormDetails'
import ButtonUni from './form/ButtonUni'
import Navigator from './Navigator'
import ApplicationFormData from './ApplicationFormData'

/**
 * Create, update, delete Import Permit Application
 * @property {string} appId - application id, zero - create a new application
 * @example <ApplicatipnForm appId={this.props.id} />
 */
class ApplicationForm extends Component{
    constructor(props){
        super(props)
        this.state={
            status:"",
            alertColor:"info",
            labels:{
                locale:"",
                askforpermit:"",
                editpip:"",
                orderType:"",
                cancel:"",
                saveasdraft:"",
                saving:"",
                saved:"",
                checkbeforesubmit:"",
                pleasefix:"",
                quotas:"",
                global_delete:'',
            },
            data:{
                id:0,
                valid:true,
                detailsSuggest:"",

            }
        }
        this.loadData=this.loadData.bind(this)
        this.cancel=this.cancel.bind(this)
        this.save=this.save.bind(this)
        this.title=this.title.bind(this)
        this.submit=this.submit.bind(this)
    }
    componentDidMount(){
            Locales.resolveLabels(this)
            this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSON("/api/applicant/application/open",this.state.data,(query,result)=>{
            if(result.navigator.component.toUpperCase() == "APPLICATIONFORM"){
                this.state.data=result
                this.setState(this.state)
            }else{
                Navigator.navigate(result.navigator.tab, result.navigator.component, result.navigator.params)
            }
        })
    }

    /**
     * Cancel edit go back
     */
    cancel(){
       Navigator.navigate("applications","applications")
    }
    /**
     * Save. but leave on the page
     */
    save(){
        this.state.status=this.state.labels.saving
        this.setState(this.state)
        Fetchers.postJSON("/api/applicant/application/save", this.state.data, (query,result)=>{
            this.state.data=result
            let lastSaved = new Date(this.state.data.lastSaved)
            this.state.alertColor="info"
            this.state.status=this.state.labels.saved +" "+ lastSaved.toLocaleString(this.state.labels.locale)
            this.setState(this.state.data)
        })
    }

    title(){
        if(this.state.data.pipNumber.value==null){
            return this.state.labels.askforpermit
        }else{
            return this.state.labels.askforpermit+" " + this.state.data.pipNumber.value
        }
    }
    /**
     * Strict verify an application and then provide the review form or ask to fix errors
     */
    submit(){
        this.state.status=this.state.labels.saving
        this.setState(this.state)
        Fetchers.postJSON("/api/applicant/application/verify", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
            let lastSaved = new Date(this.state.data.lastSaved)
            if(this.state.data.valid){
                this.state.status=""
                this.state.alertColor="info"
                Navigator.navigate("applications", "ApplicationSubmit", ""+this.state.data.id)
            }else{
                this.state.status=this.state.labels.pleasefix + " " + lastSaved.toLocaleString(this.state.labels.locale)
                this.state.alertColor="danger"
            }
            this.setState(this.state)
        })
    }
    /**
     * Details depends on ordertype and itemtype, e.g. Normal, mediicnes
     */
    detailsForm(){
        if(this.state.data.orderType.value="Normal"){
            return <ApplicationFormDetails applicationForm={this} loader={this.loadData} />
        }
        return <h2>Unrecognized order type</h2>
    }
    //

    render(){
        if((this.state.data.details != undefined) && (this.state.labels.locale != undefined)){
            return(
                <Card className="border border-primary shadow-lg m-3" style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                    <Row>
                        <Col xs='12' sm='12' lg='3' xl='3' className="text-center">
                            <h6>{this.title()+"("+this.state.data.orderType.value.code+")"}</h6>
                        </Col>
                        <Col xs='12' sm='12' lg='2' xl='2'>
                            <Alert className="p-1" isOpen={this.state.status.length>0} color={this.state.alertColor}>{this.state.status}</Alert>
                        </Col>
                        <Col xs='12' sm='12' lg='2' xl='2'>
                            <ButtonUni onClick={this.submit} label={this.state.labels.checkbeforesubmit} outline={false} />
                        </Col>
                        <Col xs='12' sm='12' lg='2' xl='2'>
                            <ButtonUni onClick={this.save} label={this.state.labels.saveasdraft} />
                        </Col>
                        <Col xs='12' sm='12' lg='2' xl='2'>
                            <ButtonUni onClick={()=>{
                                 Fetchers.postJSON("/api/applicant/application/delete", this.state.data, (query,result)=>{
                                    if(result.valid){
                                        Navigator.navigate("applications", "applicationTrack", this.state.data.id + "")
                                    }else{
                                        this.state.data=result
                                        this.state.alertColor="danger"
                                        this.setState(this.state)
                                    }
                                 })
                            }} label={this.state.labels.global_delete} />
                        </Col>
                        <Col xs='12' sm='12' lg='1' xl='1' className="text-right">
                            <Button close onClick={this.cancel}/>
                        </Col>
                    </Row>
                    </CardHeader>
                    <CardBody>
                        <ApplicationFormData application={this.state.data} edit/>
                        <Row>
                            <Col className="d-flex justify-content-end">
                            <h6>
                                {this.state.labels.quotas + ": "+this.state.data.details.totalAmount.value+" "
                                +this.state.data.currency.value.code}
                            </h6>
                            </Col>
                        </Row>
                        <Row className="pl-3 pr-3">
                            <Col>
                                <ApplicationFormDetails application={this.state.data} data={this.state.data.details} loader={this.save}/>
                            </Col>
                        </Row>
                    </CardBody>
                </Card>
            )
        }else{
            return[]
        }
    }


}
export default ApplicationForm
ApplicationForm.propTypes={
    appId:PropTypes.string.isRequired,              //Appication's ID
}