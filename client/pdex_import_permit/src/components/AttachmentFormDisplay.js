import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import FieldInput from './form/FieldInput'
import FieldDate from './form/FieldDate'
import FieldOption from './form/FieldOption'
import ButtonUni from './form/ButtonUni'
import Downloader from './utils/Downloader'
import FieldDisplay from './form/FieldDisplay'

/**
 * Form to add/edit an attachment for application or applicant
 * @property {object} atatchment - must contain an id to which this document shpuld be attached
 * @property {function} close - callback to close this form without save or after save with a message
 * @example <AttachmentForm attachment=this.state.data.selected close={this.closeAttachment} />
 */
class AttachmentFormDisplay extends Component{
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
                signNo:"",
                signDate:"",
                annotation:"",
                docType:"",
                global_download:"",
                attachedby:"",
                modifiedwhen:"",
            }

        }
        this.loadData=this.loadData.bind(this)
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

    render(){
       
        if(this.state.labels.locale.length==0 || this.state.data.signNo=== undefined){
            return([])
        }else{
            this.state.file.prompt=this.state.labels.newdocument
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
                        <Col xs='12' sm='12' lg='12' xl='12' className="d-flex justify-content-end">
                            <FieldDisplay mode='time' attribute='modifiedwhen' component={this} />
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='12' className="d-flex justify-content-end">
                            <FieldDisplay mode='text' attribute='attachedby' component={this} />
                        </Col>
                    </Row>
                </CardBody>

            </Card>
        )
        }
    }


}
export default AttachmentFormDisplay
AttachmentFormDisplay.propTypes={
    attachment:PropTypes.object.isRequired,
    close:PropTypes.func.isRequired
}