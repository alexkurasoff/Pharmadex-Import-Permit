import React , {Component} from 'react'
import {Card, CardHeader, CardBody,Row, Col, Button, Alert} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import ButtonUni from './form/ButtonUni'
import ViewEdit from './form/ViewEdit'
import ViewEditDate from './form/ViewEditDate'
import ViewEditOption from './form/ViewEditOption'
import ATC from './ATC'
import ProductIngredientForms from "./ProductIngredientForms"
import ProductManufForms from "./ProductManufForms"
import Navigator from './Navigator'

/**
 * Display data for a product
 * @property {string} prodId - id of a product
 * @property {func} close - close this component
 * @property {boolean} edit - edit or view this component
 * @property {boolean} noschedule - do not display the schedule
 * @example
 * <Product prodId={this.state.data.table.rows[selected].dbId} 
 *          close={()=>{this.state.productDetails=false this.setState(this.state)}}
 * />
 */
class Product extends Component{
    constructor(props){
        super(props)
        this.state={
            status:"",
            alertColor:"",
            data:{
                id:0,
                prod_name:{
                    value:""
                },
                inns:[],
                excipients:[],
                finProdManuf:{},
                manufacturers:[],
                validError:""
                },
            labels:{
                narc:"",
                prescr:"",
                hospital:"",
                otc:"",
                prod_name:"",
                prodgenname:"",
                dos_form:"",
                dos_unit:"",
                admin_route:"",
                prod_desc:"",
                product_shelflife:"",
                product_storcndtn:"",
                product_packsize:"",
                product_conttype:"",
                prod_indications:"",
                prod_posology:"",
                license_holder:"",
                registration_date:"",
                valid_to:"",
                prod_fnm:"",
                reg_number:"",
                address:"",
                global_save:"",
                saving:"",
                pleasefix:"",
                prod_cat:"",
                age_group:"",
                dos_strength:""
            }
        }
        this.cancel=this.cancel.bind(this)
        this.loadData=this.loadData.bind(this)
        this.save=this.save.bind(this)
    }
    componentDidMount(){
        this.state.data.id=this.props.prodId
        Locales.resolveLabels(this)
        this.loadData()
    }

    componentDidUpdate(){
        if(this.props.prodId != this.state.data.id){
            this.state.data.id=this.props.prodId
            this.loadData()
        }
    }

    loadData(){
        Fetchers.postJSONNoSpinner("/api/common/product/load", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    cancel(){
        this.props.close()
    }

    save(){
        this.state.status=this.state.labels.saving
        this.setState(this.state)
        Fetchers.postJSON("/api/common/product/save", this.state.data, (query,result)=>{
            this.state.data=result
            let lastSaved = new Date(this.state.data.lastSaved)
            if(this.state.data.valid){
                this.state.status=""
                this.state.alertColor="info" 
                Navigator.navigate("quotas","quotas","void")
            }else{
                this.state.status=this.state.labels.pleasefix + " " + lastSaved.toLocaleString(this.state.labels.locale)
                this.state.alertColor="danger"
            }
            this.setState(this.state.data)
        })
    }

    render(){
        if(this.state.data.address== undefined || this.state.labels.locale==undefined){
            return []
        }
        let emptySchedule = !this.props.edit
                             && this.state.data.narc.value.originalCode=="global_no"
                             && this.state.data.prescr.value.originalCode=="global_no"
                             && this.state.data.hospital.value.originalCode=="global_no"
                             && this.state.data.otc.value.originalCode=="global_no"
        return(
            <Card style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    <Row>
                        <Col xs='12' sm='12' lg='8' xl='8' className="d-flex justify-content-center">
                            <h6>{this.state.data.prod_name.value}</h6>
                        </Col>
                        <Col xs='12' sm='12' lg='3' xl='3' className="d-flex justify-content-end">
                            <ButtonUni onClick={this.save} label={this.state.labels.global_save} edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='1' xl='1' className="d-flex justify-content-end">
                            <Button close onClick={this.cancel}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs="12" sm="12" lg="6" xl="6">
                            <Alert className="p-1" isOpen={this.state.data.validError.length>0} color={this.state.data.alertColor}>
                                {this.state.data.validError}</Alert>
                        </Col>
                    </Row>
                </CardHeader>
                <CardBody>
                   <Row>
                        <Col  xs={12} sm={12} lg={4} xl={4}>
                            <ViewEdit mode='text' attribute='license_holder' component={this} edit={false} hideEmpty/>
                        </Col>
                        <Col  xs={12} sm={12} lg={8} xl={8}>
                            <ViewEdit mode='text' attribute='address' component={this} edit={false} hideEmpty/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEdit mode='text' attribute='prod_name' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEdit mode='text' attribute='prodgenname' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEditOption attribute='dos_form' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEditOption attribute='dos_unit' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEdit mode='text' attribute='dos_strength' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEditOption attribute='prod_cat' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEditOption attribute='age_group' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                        <Col xs={12} sm={12} lg={6} xl={6}>
                            <ViewEditOption attribute='admin_route' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                    </Row>
                    <Row>{ /* Only read fields*/}
                        <Col  xs={12} sm={12} lg={3} xl={3}>
                            <ViewEdit mode='text' attribute='reg_number' component={this} edit={false} hideEmpty />
                        </Col>
                        <Col  xs={12} sm={12} lg={3} xl={3} hidden={this.state.data.reg_number.value==""}>
                            <ViewEditDate attribute='registration_date' component={this} edit={false} hideEmpty />
                        </Col>
                        <Col  xs={12} sm={12} lg={3} xl={3} hidden={this.state.data.reg_number.value==""}>
                            <ViewEditDate attribute='valid_to' component={this} edit={false} hideEmpty />
                        </Col>
                        <Col  xs={12} sm={12} lg={3} xl={3}>
                            <ViewEdit mode='text' attribute='prod_fnm' component={this} edit={false} hideEmpty />
                        </Col>
                   </Row>
                    <Row>
                        <Col>
                            <ATC data={this.state.data} property="atc" edit={this.props.edit}></ATC>
                        </Col>
                    </Row>
                    <Row xs={12} sm={12} lg={12} xl={12}>
                        <Col>
                            <ProductIngredientForms isInn={true} productForm={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row xs={12} sm={12} lg={12} xl={12} style={{marginTop:'10px'}}>
                        <Col>
                            <ProductIngredientForms isInn={false} productForm={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row xs={12} sm={12} lg={12} xl={12} style={{marginTop:'10px'}}>
                        <Col>
                            <ProductManufForms productForm={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={4} xl={4}>
                            <ViewEdit mode='text' attribute='product_packsize' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={12} lg={8} xl={8}>
                            <ViewEdit mode='text' attribute='product_conttype' component={this} edit={this.props.edit} hideEmpty />
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={4} xl={4}>
                            <ViewEdit mode='text' attribute='product_shelflife' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={12} lg={8} xl={8}>
                            <ViewEdit mode='text' attribute='product_storcndtn' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <ViewEdit mode='textarea' attribute='prod_indications' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <ViewEdit mode='textarea' attribute='prod_posology' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                    </Row>
                    <Row hidden={this.props.noschedule || emptySchedule}>
                        <Col xs={12} sm={6} lg={3} xl={3}>
                            <ViewEditOption attribute='narc' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={6} lg={3} xl={3}>
                            <ViewEditOption attribute='prescr' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={6} lg={3} xl={3}>
                            <ViewEditOption attribute='hospital' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                        <Col xs={12} sm={6} lg={3} xl={3}>
                            <ViewEditOption attribute='otc' component={this} edit={this.props.edit} hideEmpty/>
                        </Col>
                    </Row>
                </CardBody>

            </Card>
        )
    }


}
export default Product
Product.propTypes={
    prodId:PropTypes.oneOfType([PropTypes.string,PropTypes.number]).isRequired,
    close:PropTypes.func.isRequired,
    edit:PropTypes.bool,
    noschudule :PropTypes.bool,             //do not display the product schedule
}