import React , {Component} from 'react'
import {Card, CardBody,CardHeader,Row, Col, Button, ButtonGroup, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import CollectorTable from './utils/CollectorTable'
import ApplicationFormDetail from './ApplicationFormDetail'
import SearchControl from './utils/SearchControl'

/**
 * Application details control. All operation with products and any single product
 * Suit only for Normal process and only medicines
 * @example
 * <ApplicationFormDetails application={this.state.data} data={this.state.data.details} loader={this.save}/>  //ApplicationFormDetailsDTO
 */
class ApplicationFormDetails extends Component{
    constructor(props){
        super(props)
        this.state={
            showComponent:0,            //which component to show: 0 - table only, 1 - table and detail, 2 - detail only
            detailId:0,                 //edit current
            productDetailId:0,          //add new one
            data:{},
            labels:{
                search:"",
                selectedonly:"",
                allfiltered:"",
                global_add:"",

            }
        }
        this.currentComponent=this.currentComponent.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.state.data=this.props.data
        this.setState(this.state)
    }
    componentDidUpdate(){
        if(this.props.data != undefined && this.props.data.justloaded){
            this.state.data=this.props.data
            this.state.data.justloaded=false
            this.setState(this.state)
        }
    }

    /**
     * Full table or short table with the form
     */
    currentComponent(){
        if(this.state.showComponent==0){
             //long table
             return(                   
                <Col>
                    <Row>
                        <Col>
                            <CollectorTable tableData={this.state.data.details} 
                                loader={this.props.loader}
                                headBackground={'#0099cc'}
                                linkProcessor={
                                    (rowNo, cell)=>{
                                        let table = this.state.data.details
                                        if(this.state.data.showDetails){
                                            //edit an existing record 
                                            this.state.detailId=table.rows[rowNo].dbID
                                            this.state.productDetailId=-1;
                                        }else{
                                            //add a new product detail record
                                            this.state.productDetailId=table.rows[rowNo].dbID
                                            this.state.detailId=-1
                                        }
                                        this.state.showComponent=1
                                        this.setState(this.state)
                                    }
                                }
                                styleCorrector={(headerKey)=>{
                                    if(['description','manufname'].indexOf(headerKey)>-1){
                                        return {width:'20%'}
                                    }
                                }}
                            />
                        </Col>
                    </Row>
                </Col>
                )
        }
        if(this.state.showComponent==1){
            //short table and form
            return(
                <Col>
                    <Row>           
                        <Col xs="12" sm="12" lg="8" xl="8">
                            <CollectorTable tableData={this.state.data.detailsShort} 
                                    loader={this.props.loader}
                                    headBackground={'#0099cc'}
                                    linkProcessor={
                                        (rowNo, cell)=>{
                                            let table = this.state.data.detailsShort
                                            if(this.state.data.showDetails){
                                                //edit an existing record 
                                                this.state.detailId=table.rows[rowNo].dbID
                                                this.state.productDetailId=-1;
                                            }else{
                                                //add a new product detail record
                                                this.state.productDetailId=table.rows[rowNo].dbID
                                                this.state.detailId=-1
                                            }
                                            this.setState(this.state)
                                        }
                                    }
                                    styleCorrector={(headerKey)=>{
                                            if(['description'].indexOf(headerKey)>-1){
                                                return {width:'60%'}
                                            }
                                    }}
                                    />
                        </Col>
                        <Col xs="12" sm="12" lg="4" xl="4">
                           <ApplicationFormDetail application={this.props.application} id={this.state.detailId} productId={this.state.productDetailId}
                            cancel={()=>{this.state.detailId=0; this.state.productDetailId=0; this.state.showComponent=0; this.setState(this.state)}}
                            refresh={()=>{
                                this.state.detailId=0;
                                this.state.productDetailId=0;
                                this.state.data.showDetails=true
                                this.state.showComponent=0
                                this.setState(this.state);
                                this.props.loader()}
                            }
                            />
                        </Col>
                    </Row>
                </Col>
                )
        }
        if(this.state.showComponent==2){
            //form only
            return(
                <Col>
                    <ApplicationFormDetail application={this.props.application} id={this.state.detailId} productId={this.state.productDetailId}
                                cancel={()=>{this.state.detailId=0; this.state.productDetailId=0; this.state.showComponent=0;this.setState(this.state)}}
                                refresh={()=>{
                                    this.state.detailId=0;
                                    this.state.productDetailId=0;
                                    this.state.data.showDetails=true
                                    this.state.showComponent=0
                                    this.setState(this.state);
                                    this.props.loader()}
                                }
                                />
                </Col>
            )
        }
    }

    render(){
        if(this.state.data != undefined && this.state.data.details != undefined && this.state.data.orderType != undefined){
            let showAddButton= 
                                (this.props.application.orderType.value.originalCode == "Special")
                                 || (this.props.application.itemType.value.originalCode=='medprod' &&  this.props.application.orderType.value.originalCode == "Normal")
                    
            return(
                <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                    <Row>
                        <Col xs="12" sm="12" lg="3" xl="3">
                            <SearchControl loader={this.props.loader} 
                            label={this.state.labels.search}
                            table={this.state.data.details} disabled={this.state.showComponent>0}/>
                        </Col>
                        <Col xs="12" sm="12" lg="3" xl="3" hidden={!showAddButton}>
                            <Button size="sm" outline color="primary" disabled={this.state.showComponent>0}
                                onClick={()=>{
                                    this.state.showComponent=2
                                    this.setState(this.state)
                                } 
                                }
                            >
                                {this.state.labels.global_add}
                            </Button>
                        </Col>
                        <Col xs="12" sm="12" lg="3" xl="3" hidden={showAddButton}>
                            <ButtonGroup>
                                <Button size="sm" outline color="primary" active={!this.state.data.showDetails}
                                    onClick={()=>{
                                        this.state.data.showDetails=false
                                        this.state.detailId=0;this.state.productDetailId=0;this.state.showComponent=0
                                        this.setState(this.state)
                                        this.props.loader()
                                        }
                                    }
                                >
                                    {this.state.labels.global_add}
                                </Button>
                                <Button size="sm" outline color="secondary" disabled={this.state.showComponent>0}
                                    onClick={()=>{
                                        this.state.data.showDetails=true
                                        this.state.detailId=0;this.state.productDetailId=0;this.state.showComponent=0
                                        this.setState(this.state)
                                        this.props.loader()
                                        }
                                    }
                                >
                                    {this.state.labels.selectedonly}
                                </Button>
                            </ButtonGroup>
                        </Col>
                        <Col xs="12" sm="12" lg="6" xl="6">
                            <Alert className="p-1" isOpen={this.state.data.detailsSuggest.length>0} color="danger">
                                {this.state.data.detailsSuggest}
                            </Alert>
                        </Col>
                    </Row>
                    </CardHeader>
                    <CardBody>
                    <Row>
                        {this.currentComponent()}
                    </Row>
                    </CardBody>
                </Card>
            )
        }else{
            return []
        }
    }


}
export default ApplicationFormDetails
ApplicationFormDetails.propTypes={
    application:PropTypes.object.isRequired,    //ApplicationDTO   
    data:PropTypes.object.isRequired,           //ApplicationDetailsDTO
    loader:PropTypes.func.isRequired            //loader-reloader
}