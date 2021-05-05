import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Alert,Container} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import ButtonUni from './form/ButtonUni'
import AttachmentForm from './AttachmentForm'

/**
 * List of applicants attachments and form to add them
 * Only for Applicant and their applications
 * @property {string} appId
 * @example <Attachments appId={this.state.data.id} />
 */
class Attachments extends Component{
    constructor(props){
        super(props)
        this.state={
            alert:"",
            editTitle:"",
            data:{
                appId:0,
                editor:false,
                selected:{
                    id:0
                },
                table:{
                    headers:{},
                    rows:[]
                }
            },
            labels:{
                attachments:"",
                newdocument:"",
            }
        }
        this.activeComponent=this.activeComponent.bind(this)
        this.loadData=this.loadData.bind(this)
        this.listComponent=this.listComponent.bind(this)
        this.formComponent=this.formComponent.bind(this)
        this.openForm=this.openForm.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    /**
     * load the table and/or selected element
     */
    loadData(){
        this.state.data.appId=this.props.appId
        Fetchers.postJSONNoSpinner("/api/common/application/attachments", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }
    /**
     * Open form on a click
     * @param {number} rowNo 
     */
    openForm(rowNo){
        this.state.data.selected.id=this.state.data.table.rows[rowNo].dbID
        this.state.data.selected.pipId=this.props.appId
        this.state.data.selected.applicantId=this.state.data.applicantId
        this.state.data.editor=true
    }
    /**
     * List of attachments
     */
    listComponent(){
        return(
        <Container fluid>
        <Row className="mb-2">
            <Col xs="12" sm="12" lg="8" xl="8">
                <Alert className="p-1" isOpen={this.state.alert!== undefined && this.state.alert.length>0} color="info">{this.state.alert}</Alert>
            </Col>
            <Col xs="12" sm="12" lg="4" xl="4" className="d-flex justify-content-end">
                <ButtonUni onClick={()=>{
                    this.state.data.selected.id=0
                    this.state.data.selected.pipId=this.props.appId
                    this.state.data.selected.applicantId=this.state.data.applicantId
                    this.state.data.editor=true;
                    this.state.editTitle=this.state.labels.newdocument
                    this.loadData();
                }}
                 label={this.state.labels.newdocument}
                 outline={this.state.data.editor} disabled={this.state.data.editor} />
            </Col>
        </Row>
        <Row>
            <Col>
                <CollectorTable
                    tableData={this.state.data.table}
                    loader={this.loadData}
                    headBackground={'#0099cc'}
                    linkProcessor={ (rowNo,col)=>{
                               this.openForm(rowNo)
                               this.loadData()
                            }
                        }
                />
            </Col>
        </Row>
        </Container>
        )
    }
    /**
     * Add or edit an attachment
     */
    formComponent(){
        let title = this.state.labels.newdocument
        if(this.state.data.selected.id>0){
            title=this.state.data.selected.signNo
        }
        return (
            <Row>
                <Col xs={12} sm={12} lg={4} xl={4}>
                <CollectorTable
                    tableData={this.state.data.table}
                    loader={this.loadData}
                    headBackground={'#0099cc'}
                    linkProcessor={ (rowNo,col)=>{
                        this.openForm(rowNo)
                        this.setState(this.state)
                    }
                }
                />
                </Col>
                <Col xs={12} sm={12} lg={8} xl={8}>
                   <AttachmentForm attachment={this.state.data.selected} close={(alert)=>{
                       this.state.data.editor=false
                       this.state.alert=alert
                       this.state.data.selected.id=0
                       this.loadData();
                   }}/>
                </Col>
            </Row>
        )
    }
    /**
     * Select an active component
     */
    activeComponent(){
        if(this.state.data.editor){
            return this.formComponent()
        }else{
            return(this.listComponent())
        }
    }
    render(){
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col  xs='12' sm='12' lg='12' xl='12' className="d-flex justify-content-left">
                        <h6>{this.state.labels.attachments}</h6>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    {this.activeComponent()}
                </CardBody>
            </Card>
        )
    }
}
export default Attachments
Attachments.propTypes={
    appId: PropTypes.string.isRequired  //current application ID
}