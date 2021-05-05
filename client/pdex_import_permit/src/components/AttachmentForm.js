import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import FieldInput from './form/FieldInput'
import FieldDate from './form/FieldDate'
import FieldOption from './form/FieldOption'
import FieldUpload from './form/FieldUpload'
import FieldDisplay from './form/FieldDisplay'
import ButtonUni from './form/ButtonUni'
import Downloader from './utils/Downloader'
import Alerts from './utils/Alerts'

/**
 * Form to add/edit an attachment for application or applicant
 * @property {object} atatchment - must contain an id to which this document shpuld be attached
 * @property {function} close - callback to close this form without save or after save with a message
 * @example <AttachmentForm attachment=this.state.data.selected close={this.closeAttachment} />
 */
class AttachmentForm extends Component{
    constructor(props){
        super(props)
        this.state={
            status:"",
            alertColor:"info",
            file:{},
            data:{
                id:0
            },
            labels:{
                locale:"",
                newdocument:"",
                uploaddocument:"",
                signNo:"",
                signDate:"",
                annotation:"",
                docType:"",
                pleasefix:"",
                success:"",
                save:"",
                global_download:"",
                global_delete:"",
                label_delete_record:"",
                attachedby:"",
                modifiedwhen:"",
            }

        }
        this.loadData=this.loadData.bind(this)
        this.fileError=this.fileError.bind(this)
        this.postForm=this.postForm.bind(this)
        this.serveAlert=this.serveAlert.bind(this)
        this.delete=this.delete.bind(this)
        this.formComponent=this.formComponent.bind(this)
        this.formForDisplay=this.formForDisplay.bind(this)
        this.formForEdit=this.formForEdit.bind(this)
    }
    componentDidMount(){
        this.state.data=this.props.attachment
        this.setState(this.state)
        Locales.resolveLabels(this)
        this.loadData()
    }

    componentDidUpdate(){
        if(this.props.attachment.id != this.state.data.id){
            this.state.data=this.props.attachment
            this.loadData()
        }
    }

    /**
     * load data for an existing document or init new one
     */
    loadData(){
        Fetchers.postJSONNoSpinner("/api/common/attachment/open",this.state.data,(query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }



    /**
     * Error message for a file
     */
    fileError(){
        let fileName = this.state.data.fileName
        let ret=""
        if(fileName !== undefined){
            if(fileName.error){
                ret = fileName.suggest
            }
        }
        return ret;
    }
    /**
     * Post form data with or without a file
     */
    postForm(){
            let formData = new FormData()
            this.state.data.fields=Object.keys(this.state.labels)
            formData.append('dto', JSON.stringify(this.state.data))
            formData.append('file', this.state.file);
            Fetchers.postFormJson("/api/common/upload/save/file",formData, (formData,result)=>{
                this.state.data=result;
                this.serveAlert()
                if(result.valid){
                    this.props.close(this.state.labels.success)
                }else{
                    this.setState(this.state)
                }
            })
        }
    /**
     * Show/hide/colorize an alert message
     */
    serveAlert(){
        if(this.state.data.valid){
            this.state.alertColor="info"
            this.state.status=this.state.labels.success
        }else{
            this.state.status=this.state.labels.pleasefix
            this.state.alertColor="danger"
        }
    }
    /**
     * Delete a document
     */
    delete(){
        Alerts.warning(this.state.labels.label_delete_record,()=>{
            Fetchers.postJSON("/api/applicant/attachment/delete",this.state.data, (query,result)=>{
                this.props.close(this.state.labels.success)
            })
        }, ()=>{})
    }
    /**
     * Return form for display an attachemnt
     */
    formForDisplay(){
        return(
        <Card style={{fontSize:"0.8rem"}}>
        <CardHeader>
            <Row>
                <Col  xs='12' sm='12' lg='11' xl='11' className="d-flex justify-content-end">
                    <ButtonUni
                        onClick={()=>{
                            let downloader=new Downloader()
                            downloader.pureGetDownload("/api/common/download/id="+this.props.attachment.id)
                        }}
                        label={this.state.labels.global_download}
                        outline={false}
                        disabled={this.state.data.id==0}
                    />
                </Col>  
                <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                    <Button close onClick={()=>
                        {
                            this.props.close()
                        }
                    }/>
                </Col>
            </Row>
        </CardHeader>
        <CardBody>
            <Row>
                <Col xs='12' sm='12' lg='12' xl='12'>
                    <FieldDisplay mode='text' attribute='docType' component={this} />
                </Col>
            </Row>
            <Row>
                <Col xs='12' sm='12' lg='6' xl='6'>
                    <FieldDisplay mode='date' attribute='signDate' component={this} />
                </Col>
                <Col xs='12' sm='12' lg='6' xl='6'>
                    <FieldInput mode='text' attribute='signNo' component={this} />
                </Col>
            </Row>
            <Row>
                <Col xs='12' sm='12' lg='12' xl='12'>
                    <FieldDisplay mode='text' attribute='annotation' component={this} />
                </Col>
            </Row>
            <Row>
                <Col xs='12' sm='12' lg='6' xl='6' className="d-flex justify-content-end">
                    <FieldDisplay mode='time' attribute='modifiedwhen' component={this} />
                </Col>
                <Col xs='12' sm='12' lg='6' xl='6' className="d-flex justify-content-end">
                    <FieldDisplay mode='text' attribute='attachedby' component={this} />
                </Col>
            </Row>
        </CardBody>

    </Card>
        )
    }
    /**
     * Return form for edit an attachment
     */
    formForEdit(){
        return(
            <Card style={{fontSize:"0.8rem"}} className="border-5 border-primary shadow m-1">
            <CardHeader>
                <Row>
                    <Col  xs='12' sm='12' lg='12' xl='4' className="d-flex justify-content-end">
                        <ButtonUni
                            onClick={()=>{
                                let downloader=new Downloader()
                                downloader.pureGetDownload("/api/common/download/id="+this.props.attachment.id)
                            }}
                            label={this.state.labels.global_download}
                            outline
                            disabled={this.state.data.id==0}
                        />
                    </Col>
                    <Col  xs='12' sm='12' lg='12' xl='4' className="d-flex justify-content-end">
                        <ButtonUni
                            onClick={()=>{
                                this.delete()
                            }}
                            label={this.state.labels.global_delete}
                            outline
                            disabled={this.state.data.id==0}
                        />
                    </Col>
                    <Col xs='12' sm='12' lg='12' xl='3' className="d-flex justify-content-end">
                        <ButtonUni
                            onClick={()=>{
                                this.state.data.fields=Object.keys(this.state.labels)
                                Fetchers.postJSON("/api/common/upload/verify", this.state.data, (query,result)=>{
                                    this.state.data=result
                                    this.serveAlert()
                                    if(result.valid){
                                        this.postForm()
                                    }else{
                                        this.setState(this.state.data)
                                    }
                                })
                            }} 
                            label={this.state.labels.save}
                            outline={false}
                        />
                    </Col>   
                    <Col xs='12' sm='12' lg='12' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>
                            {
                                this.props.close()
                            }
                        }/>
                    </Col>
                </Row>
                <Row className="pt-1" >
                    <Col>
                            <Alert className="p-0 m-0" isOpen={this.state.status.length>0} color={this.state.alertColor}>{this.state.status}</Alert>
                    </Col>
                </Row>
            </CardHeader>
            <CardBody>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='12'>
                        <FieldOption attribute='docType' component={this} />
                    </Col>
                </Row>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='12'>
                        <FieldDate attribute='signDate' component={this} />
                    </Col>
                </Row>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='12'>
                        <FieldInput mode='text' attribute='signNo' component={this} />
                    </Col>
                </Row>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='12'>
                        <FieldInput mode='textarea' attribute='annotation' component={this} />
                    </Col>
                    
                </Row>
                <Row>
                <Col xs='12' sm='12' lg='12' xl='12'>
                   <FieldUpload onChange={(file)=>{
                            this.state.file=file
                            if(this.state.data.fileSize !== undefined && this.state.data.fileName !== undefined){
                                this.state.data.fileSize.value=file.size/1024   //KBytes
                                this.state.data.fileName.value=file.name
                                this.setState(this.state)
                            }
                        }}
                        prompt={this.state.labels.newdocument}
                        error={this.fileError()}                            
                   />
                </Col>
                </Row>
                <Row>
                    <Col xs='12' sm='12' lg='6' xl='6' className="d-flex justify-content-end">
                        <FieldDisplay mode='time' attribute='modifiedwhen' component={this} />
                    </Col>
                    <Col xs='12' sm='12' lg='6' xl='6' className="d-flex justify-content-end">
                        <FieldDisplay mode='text' attribute='attachedby' component={this} />
                    </Col>
                </Row>

            </CardBody>

        </Card>
        )
    }
    /**
     * Edit or display?
     */
    formComponent(){
        if(this.state.data.displayOnly){
            return this.formForDisplay()
        }else{
            return this.formForEdit();
        }
    }
    render(){
        if(this.state.labels.locale.length==0 || this.state.data.signNo=== undefined){
            return([])
        }else{
            return(this.formComponent())
        }
    }


}
export default AttachmentForm
AttachmentForm.propTypes={
    attachment:PropTypes.object.isRequired,
    close:PropTypes.func.isRequired
}