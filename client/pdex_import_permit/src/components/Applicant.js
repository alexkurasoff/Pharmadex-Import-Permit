import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import FieldDisplay from './form/FieldDisplay'

/**
 * Applicant data for an application or applicant given
 * @property {appId} application Id
 * @example <Applicant appId={this.props.appId}
 */
class Applicant extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{
                locale:"",
                companydata:"",
                applicant_name:"",
                applicant_contactname:"",
                applicant_country:"",
                applicant_addr1:"",
                applicant_elink:"",
            },
            data:{
               appId:0
            }
        }
        this.loadData=this.loadData.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.appId = this.props.appId
        Fetchers.postJSONNoSpinner("/api/common/applicant", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }

    render(){
        if(this.state.labels.locale != '' && this.state.data.appId > 0 && this.state.data.applicant_name != undefined){
            return(
                <Card className="border-5 border-primary" style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                    <h6>{this.state.labels.companydata}</h6>
                    </CardHeader>
                    <CardBody>
                        <Row>
                            <Col xs="12" sm="12" lg="8" xl="8">
                                <FieldDisplay mode='text' attribute='applicant_name' component={this} />
                            </Col>
                            <Col xs="12" sm="12" lg="4" xl="4">
                                <FieldDisplay mode='text' attribute='applicant_country' component={this} />
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <FieldDisplay mode='text' attribute='applicant_addr1' component={this} />
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <FieldDisplay mode='text' attribute='applicant_elink' component={this} />
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <FieldDisplay mode='text' attribute='applicant_contactname' component={this} />
                            </Col>
                        </Row>
                    </CardBody>
    
                </Card>
            )
        }else{
            return [];
        }
    }


}
export default Applicant
Applicant.propTypes={
    appId:PropTypes.string.isRequired
}