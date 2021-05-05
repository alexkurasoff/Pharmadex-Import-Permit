import React , {Component} from 'react'
import {Row, Col, Button, ButtonGroup} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Navigator from './Navigator'
import Fetchers from './utils/Fetchers'
import Alerts from './utils/Alerts'

/**
 * Manage button groups rigth above the applications table - application type and medicine or medical product
 * Data are in the proprty buttons ApplicationButtonsDTO
 */
class ApplicationsButtons extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                visible:false
            },
            labels:{
                global_add:'',
                allfiltered:'',
                descr_normal:'',
                descr_special:'',
                descr_contest:'',
                descr_donat:'',
                medicines:'',
                medprod:'',
            },
        }
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.state.data=this.props.buttons
    }
    componentDidUpdate(){
        if(this.props.buttons.justloaded){
            this.state.data=this.props.buttons
            this.state.data=this.props.buttons
        }
    }
    render(){
        if(this.state.data.visible){
            return(
            <Row>
                <Col xs='12' sm='12' lg='6' xl='6'>
                    <ButtonGroup>
                        <Button size="sm" outline color="primary" active={this.state.data.appType==0}
                            onClick={()=>{
                                this.state.data.appType=0
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.allfiltered}
                        </Button>
                        <Button size="sm" outline color="secondary" active={this.state.data.appType==1}
                            onClick={()=>{
                                this.state.data.appType=1
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.descr_normal}
                        </Button>
                        <Button size="sm" outline color="primary" active={this.state.data.appType==2}
                            onClick={()=>{
                                this.state.data.appType=2
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.descr_special}
                        </Button>
                    </ButtonGroup>
                </Col>
                <Col xs='12' sm='12' lg='5' xl='5'>
                    <ButtonGroup>
                        <Button size="sm" outline color="primary" active={this.state.data.productType==0}
                            onClick={()=>{
                                this.state.data.productType=0
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.allfiltered}
                        </Button>
                        <Button size="sm" outline color="secondary" active={this.state.data.productType==1}
                            onClick={()=>{
                                this.state.data.productType=1
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.medicines}
                        </Button>
                        <Button size="sm" outline color="primary" active={this.state.data.productType==2}
                            onClick={()=>{
                                this.state.data.productType=2
                                this.setState(this.state)
                                this.props.reload()
                                }
                            }
                        >
                            {this.state.labels.medprod}
                        </Button>
                    </ButtonGroup>
                </Col>
                <Col xs='12' sm='12' lg='2' xl='1'>
                    <Button size="sm" color="primary" disabled={this.state.data.productType==0 || this.state.data.appType==0} outline={this.state.data.productType==0 || this.state.data.appType==0}
                        onClick={()=>{
                                Fetchers.postJSONNoSpinner("/api/applicant/application/create", this.state.data, (query,result)=>{
                                    if(result.valid){
                                        Navigator.navigate("applications", "ApplicationForm",result.newApplicationId)
                                    }else{
                                        Alerts.show(result.alertMessage,0)
                                    }
                                })
                            }
                        }
                    >
                        {this.state.labels.global_add}
                    </Button>
                </Col>
            </Row>
        )
                    }else{
                        return []
                    }
    }
}

export default ApplicationsButtons
ApplicationsButtons.propTypes={
    buttons:PropTypes.object.isRequired,    //ApplicationsButtonsDTO
    reload:PropTypes.func.isRequired        //reload the parent
}