import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import FieldDate from './form/FieldDate'
import FieldOption from './form/FieldOption'
import FieldDisplay from './form/FieldDisplay'
import FieldsComparator from './form/FieldsComparator'
import ValidationMarkup from './ValidationMarkup'

/**
 * Who and when finalize an application. All chenges will be saved automatically
 * @property {string} appId - application id
 * @property readOnly - boolean - only for read or editable
 * @property strict - boolean - show validate mesages as strict 
 * @example <FinalizeMarkup appId={this.state.data.appId} readOnly={false} strict={this.state.data.valid}/>
 */
class FinalizeMarkup extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0,
            },
            labels:{
                locale:"",
                approval:"",
                approver:"",
                approvalDate:"",
                expiry_date:"",
                validation_date:"",
                auth_date:"",
                inspector:""
            }
        }
        this.loadData=this.loadData.bind(this)
 
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
    /**
     * save it "automatically"
     */
    save(){
        this.state.data.fields=Object.keys(this.state.labels)
        Fetchers.postJSONNoSpinner("/api/moderator/finalize/save",this.state.data,(query,result)=>{
            this.state.data=result
            this.state.data.editable=true
            this.comparator = new FieldsComparator(this)
            this.setState(this.state)
        })
    }

    componentDidUpdate(){
        if(!this.state.readOnly){
            //save if any change occured
            if (this.comparator!=undefined){
                const fld = this.comparator.checkChanges();
                if(fld.length>0){
                    this.save()
                }
            }
            //set strict/not strict field validations
            Object.keys(this.state.labels).forEach(key => {
                let field=this.state.data[key]
                if(field != undefined && field.error != undefined){
                    field.strict=this.props.strict
                }
            });
            //latch to avoid endless cycle and repaint
            if(this.state.strict!=this.props.strict){
                this.state.strict=this.props.strict
                this.setState(this.state.data)
            }
        }
    }
    /**
     * load markup data
     */
    loadData(){
        this.state.data.id=this.props.appId
        Fetchers.postJSONNoSpinner("/api/common/finalize/open",this.state.data, (query,result)=>{
            this.state.data=result
            this.comparator = new FieldsComparator(this)
            if(!this.props.readOnly){
                this.save() //turn on validation
            }else{
                this.setState(this.state)
            }
        })
    }


    render(){
        if(this.state.data.valid==undefined){
            return []
        }
        if(this.props.readOnly){
            if(this.state.data.approver.value.id==0){
                return []
            }
            return(
                    <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                        <Row>
                        <Col>
                            <h6>{this.state.labels.approval}</h6>
                        </Col>
                        </Row>
                    </CardHeader>
                    <CardBody>
                        <Row>
                            <Col xs='12' sm='12' lg='12' xl='6'>
                                <FieldDisplay mode='date' attribute="approvalDate" component={this}/>
                            </Col>
                            <Col xs='12' sm='12' lg='12' xl='6'>
                                <FieldDisplay mode='date' attribute="expiry_date" component={this}/>
                            </Col>
                        </Row>
                        <Row>
                            <Col  xs='12' sm='12' lg='12' xl='12'>
                                <FieldDisplay mode='text' attribute='approver' component={this} />
                        </Col>
                        </Row>
                    </CardBody>
                </Card>
            )
        }else{
        return(
            <Card className={"border-5 border-primary shadow-lg m-5"} style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col>
                        <h6>{this.state.labels.approval}</h6>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldDate attribute="approvalDate" component={this}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldDate attribute="expiry_date" component={this}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col  xs='12' sm='12' lg='12' xl='12'>
                            <FieldOption attribute='approver' component={this} />
                       </Col>
                    </Row>
                </CardBody>

            </Card>
        )
        }
    }


}
export default FinalizeMarkup
FinalizeMarkup.propTypes={
    appId:PropTypes.string.isRequired,
    readOnly:PropTypes.bool.isRequired,
    strict:PropTypes.bool.isRequired,
}