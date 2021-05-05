import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ButtonUni from './form/ButtonUni'
import Navigator from './Navigator'
import ViewEditOption from './form/ViewEditOption'
import FieldInput from './form/FieldInput'
import ViewEdit from './form/ViewEdit'

/**
 * Edit Checklist
 * @property {number} pipStatusId - id of the PipStatus 
 */
class Document extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            status:"",
            data:{
                valid:true,
                id:0,
                sra_code:{},
                name_port:{},
                name_us:{},
                validError:""
            },
            labels:{
                locale:"",
                global_save:"",
                lbl_deactive:"",
                name_port:"",
                name_us:"",
                applicant:"",
                process:"",
                active:"",
                sra_code:"",
                newDocType:"",
            }
        }
        this.save=this.save.bind(this)
        //this.deactive=this.deactive.bind(this)
        this.cancel=this.cancel.bind(this)
       // this.isActiveDoc=this.isActiveDoc.bind(this)
        //this.isNewDoc=this.isNewDoc.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.docid
        Fetchers.postJSON("/api/moderator/document/open/edit", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    save(){
        Fetchers.postJSONNoSpinner("/api/moderator/document/save", this.state.data, (query,result)=>{
            this.state.data=result
            if(this.state.data.valid){
                this.state.status=""
                this.state.alertColor="info" 
                this.cancel()
            }else{
                this.state.status=this.state.labels.pleasefix // + " " + lastSaved.toLocaleString(this.state.labels.locale)
                this.state.alertColor="danger"
            }
            this.setState(this.state.data)
        })
    }

    cancel(){
        Navigator.navigate("administration", "documents")
    }

    isNewDoc(){
        if(this.state.data.id > 0){
            return false;
        }
        if(this.state.data.id == 0){
            return true;
        }
    }

    buildHeader(){
        if(this.isNewDoc()){
            return this.state.labels.newDocType
        }else{
            return this.state.data.sra_code.value + ", " + this.state.data.name_us.value;
        } 
    }

    render(){
        if(this.state.labels.locale==undefined){
            return []
        }
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                        <Col xs='12' sm='12' lg='9' xl='9' className="d-flex justify-content-center">
                            <h6>{this.buildHeader()}</h6>
                        </Col>
                        <Col xs='12' sm='12' lg='2' xl='2' className="d-flex justify-content-end">
                            <ButtonUni onClick={this.save} label={this.state.labels.global_save}/>
                        </Col>
                        <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                            <Button close onClick={this.cancel}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs="12" sm="12" lg="6" xl="6">
                            <Alert className="p-1" isOpen={this.state.data.validError.length>0} color={this.state.data.alertColor}>
                                {this.state.data.validError}</Alert>
                        </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs={12} sm={6} lg={2} xl={2}>
                            <ViewEdit mode='text' attribute='sra_code' component={this} edit={this.isNewDoc()}/>
                        </Col>
                        <Col xs={12} sm={6} lg={2} xl={2}>
                            <ViewEditOption attribute='active' component={this} edit={true}/>
                        </Col>
                        <Col xs={1} sm={1} lg={1} xl={1}>
                        </Col>
                        <Col xs={12} sm={12} lg={7} xl={7}>
                            <FieldInput mode='text' attribute='name_port' component={this}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={6} lg={2} xl={2}>
                            <ViewEditOption attribute='applicant' component={this} edit={true}/>
                        </Col>
                        <Col xs={12} sm={6} lg={2} xl={2}>
                            <ViewEditOption attribute='process' component={this} edit={true}/>
                        </Col>
                        <Col xs={1} sm={1} lg={1} xl={1}>
                        </Col>
                        <Col xs={12} sm={12} lg={7} xl={7}>
                            <FieldInput mode='text' attribute='name_us' component={this}/>
                        </Col>
                    </Row>
                </CardBody>
            </Card>
        )
    }
}
export default Document
Document.propTypes={
}