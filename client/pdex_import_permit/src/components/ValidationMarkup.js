import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import FieldDate from './form/FieldDate'
import FieldOption from './form/FieldOption'
import FieldsComparator from './form/FieldsComparator'
import FieldDisplay from './form/FieldDisplay'

/**
 * Form to fill application validation data that will be present on stamps
 * @property appId - applicatins ID
 * @property readOnly - boolean - only for read or editable
 * @property strict - boolean - show validate mesages as strict 
 * @example
 * <ValidationData appId={this.state.data.appId} readOnly={false} strict={this.state.data.valid} /> 
 */
class ValidationMarkup extends Component{
    constructor(props){
        super(props)
        this.state={
            strict:false,
            labels:{
                locale:"",
                markup:"",
                validation_date:"",
                auth_date:"",
                expiry_date:"",
                custom:"",
                inspector:"",
 
            },
            data:{}
        }
        this.loadData=this.loadData.bind(this)
        this.save=this.save.bind(this)
     
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    componentDidUpdate(){
        if(!this.props.readOnly){
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

    save(){
        this.state.data.fields=Object.keys(this.state.labels)
        Fetchers.postJSONNoSpinner("/api/moderator/marking/save",this.state.data,(query,result)=>{
            this.state.data=result
            this.state.data.editable=true
            this.comparator = new FieldsComparator(this)
            this.setState(this.state)
        })
    }

 

    loadData(){
        this.state.data.id=this.props.appId
        this.state.data.fields=Object.keys(this.state.labels)
        Fetchers.postJSON("/api/common/marking/open",this.state.data,(query,result)=>{
            this.state.data=result
            this.state.data.editable=true
            this.comparator = new FieldsComparator(this)
            this.setState(this.state)
        })
    }
    /**
     * Is this application marked as valid
     * @param {this.state.data} data 
     * @return true if this application has all validate marking, i.e. is valid
     * @example ValidatuonMarkup.markedAsValid(this.state.data)
     */
    static markedAsValid(data){
        if(data == undefined){
            return false
        }
        if(data.auth_date == undefined){
            return false
        }
        if(data.auth_date.value==null){
            return false
        }
        return true
    }
    render(){
 
        if(this.props.readOnly){
            if (!ValidationMarkup.markedAsValid(this.state.data)){
                return([])
            }
            return(
            <Card style={{fontSize:"0.8rem"}}>
            <CardHeader>
                <Row>
                <Col  xs='12' sm='12' lg='12' xl='12'>
                    <h6>{this.state.labels.markup}</h6>
                </Col>

                </Row>
            </CardHeader>
            <CardBody>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='6'>
                        <FieldDisplay mode='date' attribute="validation_date" component={this}/>
                    </Col>
                    <Col xs='12' sm='12' lg='12' xl='6'>
                        <FieldDisplay  mode='date' className="ml-4" attribute="expiry_date" component={this}/>
                    </Col>
                </Row>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='6'>
                        <FieldDisplay  mode='date' attribute="auth_date" component={this}/>
                    </Col>
                    <Col xs='12' sm='12' lg='12' xl='6'>
                        <FieldDisplay  mode='text' className="ml-4" attribute='inspector' component={this} />
                   </Col>
                </Row>
                <Row>
                   <Col  xs='12' sm='12' lg='12' xl='12'>
                        <FieldDisplay  mode='text' attribute='custom' component={this} />
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
                    <Col  xs='12' sm='12' lg='12' xl='12'>
                        <h6>{this.state.labels.markup}</h6>
                    </Col>

                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldDate attribute="validation_date" component={this}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldDate className="ml-4" attribute="expiry_date" component={this}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldDate attribute="auth_date" component={this}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='6'>
                            <FieldOption className="ml-4" attribute='inspector' component={this} />
                       </Col>
                    </Row>
                    <Row>
                       <Col  xs='12' sm='12' lg='12' xl='12'>
                            <FieldOption attribute='custom' component={this} />
                       </Col>
                    </Row>
                </CardBody>

            </Card>
        )
        }
    }


}
export default ValidationMarkup
ValidationMarkup.propTypes={
    appId:PropTypes.string.isRequired, 
    readOnly:PropTypes.bool.isRequired,
    strict:PropTypes.bool.isRequired
}