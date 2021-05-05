import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Alert, Col, Row, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import ApplicationFormData from './ApplicationFormData'
import ApplicationDisplayDetails from './ApplicationDisplayDetails'
import { isUndefined } from 'util'


/**
 *Present an applicatiuon in read-only mode
 * @property {ApplicationDTO} application
 * @property {string} title
 * @property func loader
 * @example <ApplicationDisplay application={this.state.data} title={this.state.data.pipNumber.value} loader={this.loadData}/>
 */
class ApplicationDisplay extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0
            },
            labels:{
                locale:"",
                submittoregister:"",
                askforpermit:"",
            }
        }
    }
    componentDidMount(){
        Locales.resolveLabels(this)
    }

    render(){
        if(this.state.labels.locale != '' && !isUndefined(this.props.application.proformaNumber)){
            this.state.data=this.props.application
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='6' xl='6' className="text-center">
                        <h6>{this.props.title}</h6>
                    </Col>
                    <Col xs='12' sm='12' lg='6' xl='6' className="text-center">
                        <h6>{this.state.data.orderType.value.description}</h6>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <ApplicationFormData application={this.props.application} />
                    <ApplicationDisplayDetails application={this.props.application} loader={this.props.loader}/>
                </CardBody>
            </Card>
        )
        }else{
            return []
        }
    }


}
export default ApplicationDisplay
ApplicationDisplay.propTypes={
    application:PropTypes.object.isRequired,
    title:PropTypes.string.isRequired,
    loader: PropTypes.func.isRequired,          //ApplicationDTO loader callback
}