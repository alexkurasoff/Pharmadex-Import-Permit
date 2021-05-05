import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Col, Row, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ButtonUni from './form/ButtonUni'
import Alerts from './utils/Alerts'
import ApplicationDisplay from './ApplicationDisplay'




/**
 * Unified method to open any application for any user
 * @property {number} applId
 * @example <ApplicationOpen applId={this.props.applId} />
 */
class ApplicationOpen extends Component{
    constructor(props){
        super(props)
        this.state={
            alertColor:"info",
            data:{
                valid:true,
                id:0,
                alertMessage:"",
            },
            labels:{
                locale:"",
                submittoregister:"",
                askforpermit:"",
                willsubmit:"",
            }
        }
        this.submit=this.submit.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
    }

    /**
     * Submit an application
     */
    submit(){
        Alerts.warning(this.state.labels.willsubmit, ()=>{
            this.state.data.id=this.props.appId
            Fetchers.postJSON("/api/applicant/application/submit", this.state.data, (query,result)=>{
                this.state.data=result
                if(this.state.data.valid){
                    this.state.alertColor="info"
                }else{
                    this.state.alertColor="danger"
                }
                this.setState(this.state)
            })
        }, ()=>{
            
        })
 
    }

    render(){
        if(this.props.applId==0){
            return []
        }else{
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                    <Col xs='12' sm='12' lg='11' xl='11' className="d-flex justify-content-end">
                        <ButtonUni onClick={this.submit} outline={false} label={this.state.labels.submittoregister} />
                    </Col>
                    <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button close onClick={()=>{window.history.back()}}/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <ApplicationDisplay appId={this.props.appId} />
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <ApplicationCheckList appId={this.props.appId} />
                        </Col>
                    </Row>
                </CardBody>
            </Card>
        )
        }
    }
}
export default ApplicationOpen
ApplicationOpen.propTypes={
    appId:PropTypes.string.isRequired          //PIP id
}