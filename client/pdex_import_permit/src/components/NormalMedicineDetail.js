import React , {Component} from 'react'
import {Row, Col, Card, CardBody, CardHeader, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ButtonUni from './form/ButtonUni'
import FieldInput from './form/FieldInput'
import Alerts from './utils/Alerts'

/**
 * Component to edit detils for a product
 * @property applicationDetails - an application details form object
 * @example
 * 
 */
class NormalMedicineDetail extends Component{
    constructor(props){
        super(props)
        this.state={
            storedId:-1,
            storedProdId:-1,
            data:{},
            labels:{
                cancel:"Cancel",
                global_save:"",
                packs:"Packs",
                price:"Pack price:",
                amount:"Amount",
                global_delete:'',
                label_delete_record:'',
            }
        }
        this.loadData=this.loadData.bind(this)
        this.save=this.save.bind(this)
        this.delete=this.delete.bind(this)
        this.calcAmount=this.calcAmount.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData();
    }
    loadData(){
        this.state.data.id=this.props.id
        this.state.data.prodId=this.props.productId
        this.state.data.ipId=this.props.application.id
        Fetchers.postJSONNoSpinner("/api/applicant/application/detail", this.state.data, (query,result)=>{
            this.state.storedId=result.id
            this.state.storedProdId=result.prodId
            this.state.data=result
            this.setState(this.state)
        })
    }

    componentDidUpdate(){
        let idChanged = this.props.id!=0 && this.state.storedId != this.props.id
        let prodIdChanged = this.props.productId!=0 && this.state.storedProdId != this.props.productId
        if((idChanged || prodIdChanged) && this.state.data.packs != undefined){
            this.state.data.packs=undefined
            this.loadData()
        }
    }
    /**
     * fill a row from the data
     */
    save(){
       
        Fetchers.postJSONNoSpinner("/api/applicant/application/detail/save", this.state.data, (query,result)=>{
            if(result.valid){
                this.props.cancel()
                this.props.refresh()
            }else{
                this.state.data=result
                this.setState(this.state)
            }
        })
        
    }
    delete(){
        Alerts.warning(this.state.labels.label_delete_record, 
            ()=>{   //yes
                Fetchers.postJSONNoSpinner("/api/applicant/application/detail/delete", this.state.data, (query,result)=>{
                    if(result.valid){
                        this.props.cancel()
                        this.props.refresh()
                    }else{
                        this.state.data=result
                        this.setState(this.state)
                    }
                })
            },
            ()=>{   //no
                //nothing to do
            }
        )
    }
    /**
     * Recalc amount
     */
    calcAmount(){
        this.state.data.amount.value=this.state.data.packs.value*this.state.data.price.value 
    }

    render(){
        if(this.state.data.packs != undefined){
           this.calcAmount()
            return(
            <Card className="border border-primary shadow-lg m-3">
                <CardHeader>
                    <Row>
                    <Col xs='11' sm='11' lg='5' xl='5'>
                        <b>
                            {this.state.data.product}
                        </b>
                    </Col>
                    <Col xs='11' sm='11' lg='3' xl='3'>
                            <ButtonUni label={this.state.labels.global_save} onClick={this.save}/>
                    </Col>
                    <Col xs='11' sm='11' lg='3' xl='3'>
                            <ButtonUni label={this.state.labels.global_delete} onClick={this.delete}/>
                    </Col>
                    <Col xs='1' sm='1' lg='1' xl='1' className="p-0 m-0">
                        <Button close
                        onClick={this.props.cancel}/>
                    </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                    <Row>
                        <Col xs='12' sm='12' lg='9' xl='9'>
                            <Row>
                                    <Col xs="12" sm="12" lg="6" xl="6">
                                        <FieldInput mode='number' attribute='packs' component={this}/>
                                    </Col>
                                    <Col xs="12" sm="12" lg="6" xl="6">
                                        <FieldInput mode='number' attribute='price' component={this}/>
                                    </Col>
                            </Row>
                            <Row>
                                    <Col xs="12" sm="12" lg="6" xl="6">
                                        <FieldInput mode='number' attribute='amount' component={this} disabled/>
                                    </Col>
                            </Row>
                        </Col>
                        
                    </Row>
            </CardBody>
            </Card>
            )
        }else{
            return []
        }
    }


}
export default NormalMedicineDetail
NormalMedicineDetail.propTypes={
    appId:PropTypes.number.isRequired,                  //Import_permit ID to add new Import_permit_detail
    id:PropTypes.number.isRequired,                     //Import_permit_detail ID to edit 
    productId:PropTypes.number.isRequired,              //Product Id to add new Import_permit_detail
    cancel:PropTypes.func.isRequired,                   //cancel callback
    refresh:PropTypes.func.isRequired                   //after save callback
}