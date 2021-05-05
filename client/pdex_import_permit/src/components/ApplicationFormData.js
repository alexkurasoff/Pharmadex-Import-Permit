import React , {Component} from 'react'
import {Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import ViewEdit from './form/ViewEdit'
import ViewEditDate from './form/ViewEditDate'
import ViewEditOption from './form/ViewEditOption'
import Locales from './utils/Locales'

/**
 * Display/edit application data
 * @property application - object ApplicationDTO
 * @example
 * <AApplicationFormData application={this.state.data} edit/>
 */
class ApplicationFormData extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{},
            data:{}
        }
    }

    componentDidMount(){
        this.state.data=this.props.application
        Locales.createLabels(this)
        Locales.resolveLabels(this)
    }

    componentDidUpdate(){
        if(this.props.application.justloaded){
            this.state.data=this.props.application
            this.state.data.justloaded=false
        }
    }

    render(){
        if(this.state.data.id != undefined && this.state.labels.requested_date != undefined){
            return (
                <Row>
                    <Col>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='5'>
                            <ViewEditDate attribute='requested_date' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='5'>
                            <ViewEdit mode='text' attribute='proformaNumber' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='2'>
                            <ViewEditOption attribute='currency' component={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs='12' sm='12' lg='12' xl='3'>
                            <ViewEditOption attribute='transport' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='4'>
                            <ViewEditOption attribute='port' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='3'>
                            <ViewEditOption attribute='custom' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='12' xl='2'>
                            <ViewEditOption attribute='incoterms' component={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                <Row>
                    <Col xs='12' sm='12' lg='12' xl='12'>
                        <ViewEdit mode='textarea' rows='5' attribute='remark' component={this} edit={this.props.edit}/>
                    </Col>
                </Row>
                </Col>
            </Row>
            )
        }else{
            return []
        }
    }
    
}
export default ApplicationFormData
ApplicationFormData.propTypes={
    application: PropTypes.object.isRequired,           //ApplicationDTO
    edit:   PropTypes.bool                              //edit or display
}
