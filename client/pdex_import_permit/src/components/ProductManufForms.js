import React , {Component} from 'react'
import {Card, CardBody,CardHeader,Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ManufacturerForm from './ManufacturerForm'
import ButtonUni from './form/ButtonUni'
import FieldsComparator from './form/FieldsComparator'
import ViewEditOption from "./form/ViewEditOption"
import FieldDisplay from "./form/FieldDisplay"
import FieldInput from "./form/FieldInput"
import ViewEditOracle from "./form/ViewEditOracle"

/**
 * Build list form by Manufs 
 */
class ProductManufForms extends Component{
    constructor(props){
        super(props)
        this.state={
            manufs:[],
            data:{
                id:0,
                applicant_country:"",
                manuf_name:{},
                notfound:false
            },
            prodId:0,
            labels:{
                manufacturer_detail:"",
                global_add:"",
                manuf_name:"",
                company_type:"",
                applicant_country:"",
                btn_not_found:"",
                othermanufname:""
            }
        }
        this.add=this.add.bind(this)
        this.deleteItem=this.deleteItem.bind(this)
        this.setData=this.setData.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.setData()
        this.state.prodId=this.props.productForm.state.data.id
        this.comparator = new FieldsComparator(this)
        this.setState(this.state)
    }

    componentDidUpdate(){
        if(this.state.prodId != this.props.productForm.state.data.id){
            this.setData()
            this.state.prodId=this.props.productForm.state.data.id
            this.state.manufs.justloaded=false
            this.setState(this.state)
        }else if(this.state.data.manuf_name.value.code.length >= 3){
            if (this.comparator != undefined){
                const fld = this.comparator.checkChanges();
                if(fld.length > 0){
                    if(fld == "manuf_name"){
                        if(this.state.data.manuf_name.value.id == -1){
                            this.state.data.notfound = true
                            this.setState(this.state);
                        }else{
                            Fetchers.postJSONNoSpinner("/api/common/load/company", this.state.data, (query, result)=>{
                                this.state.data = result
                                this.props.productForm.state.data.finProdManuf = this.state.data
                                this.comparator = new FieldsComparator(this)
                                this.setState(this.state.data)
                            })
                        }
                    }
                }
            }
        }
    }

    setData(){
        this.state.manufs = this.props.productForm.state.data.manufacturers
        this.state.data = this.props.productForm.state.data.finProdManuf
        this.comparator = new FieldsComparator(this)
    }

    add(){
        Fetchers.postJSONNoSpinner("/api/common/manufs/add", this.state.manufs, (query, result)=>{
            this.state.manufs = result
            this.props.productForm.state.data.manufacturers = this.state.manufs
            this.comparator = new FieldsComparator(this)
            this.props.productForm.setState(this.props.productForm.state)
        })
    }

    deleteItem(indexItem){
        if(indexItem < 0)
            return;
        if(Fetchers.isGoodArray(this.state.manufs)){
            let rows = []
            var i = 0
            this.state.manufs.forEach((inn)=>{
                if(i != indexItem){
                    rows.push(inn)
                }
                i = i + 1
            })
            this.state.manufs = rows
            this.props.productForm.state.data.manufacturers = this.state.manufs
            this.props.productForm.setState(this.props.productForm.state)
        }
    }

    paintFinishProdComp(){
        if(this.state.data.notfound){
            return(
                    <Row key="finishedproductmanuf">
                        <Col xs="12" sm="12" lg="12" xl="12">
                            <Card >
                                <CardBody>
                                    <Row >
                                        <Col xs='12' sm='12' lg='12' xl='12'>
                                            <ViewEditOracle attribute='manuf_name' component={this} 
                                                            api='/api/common/manufacturers' edit={this.props.edit}/>
                                        </Col>
                                    </Row>
                                    <Row >
                                        <Col xs='12' sm='12' lg='12' xl='12'>
                                            <FieldInput mode="text" attribute="othermanufname" component={this} />
                                        </Col>
                                    </Row>
                                    <Row >
                                        <Col xs='12' sm='12' lg='6' xl='6'>
                                            <ViewEditOption attribute='applicant_country' component={this} edit={true} />
                                        </Col>
                                        <Col xs='12' sm='12' lg='6' xl='6'>
                                            <FieldDisplay mode="text" attribute='company_type' component={this}/>
                                        </Col>
                                    </Row>
                                </CardBody>
                            </Card>
                        </Col>
                    </Row>
                )
        }else{
            return(
                <Row key="finishedproductmanuf">
                    <Col xs="12" sm="12" lg="12" xl="12">
                        <Card >
                            <CardBody>
                                <Row >
                                    <Col xs='12' sm='12' lg='12' xl='12'>
                                        <ViewEditOracle attribute='manuf_name' component={this} 
                                                        api='/api/common/manufacturers' 
                                                        edit={this.props.edit}/>
                                    </Col>
                                </Row>
                                <Row >
                                    <Col xs='12' sm='12' lg='6' xl='6'>
                                        <FieldDisplay mode="text" attribute='applicant_country' component={this}/>
                                    </Col>
                                    <Col xs='12' sm='12' lg='6' xl='6'>
                                        <FieldDisplay mode="text" attribute='company_type' component={this}/>
                                    </Col>
                                </Row>
                            </CardBody>
                        </Card>
                    </Col>
                </Row>
            )
        }
    }

    paintComponents(){
        let rows=[]
        rows.push(this.paintFinishProdComp())
        if(Fetchers.isGoodArray(this.state.manufs)){
            var i = 0
            this.state.manufs.forEach((mn)=>{
                rows.push(
                    <Row key={mn.uniqueKey+i} style={{marginTop:'10px'}}>
                        <Col xs="12" sm="12" lg="12" xl="12">
                            <ManufacturerForm item={mn} index={i} 
                                productForm={this.props.productForm} listForm={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                )
                i = i + 1
            })
        }
        return rows
    }

    render(){
        if(this.state.labels.locale != undefined){
            if(this.props.noadd || !this.props.edit){
                return this.paintComponents()
            }else{
            return(
                <Card>
                    <CardHeader>
                        <Row>
                            <Col xs='12' sm='9' lg='9' xl='9' className="d-flex justify-content-center">
                                <b>{this.state.labels.manufacturer_detail}</b>
                            </Col>
                            <Col xs='12' sm='3' lg='3' xl='3' className="d-flex justify-content-end" hidden={this.props.noadd}>
                               <ButtonUni label={this.state.labels.global_add} onClick={this.add} edit={this.props.edit}/>
                            </Col>
                        </Row>
                    </CardHeader>
                    <CardBody >
                        {this.paintComponents()}
                    </CardBody>
                </Card>
            )
            }
        }else{
            return []
        }
    }


}
export default ProductManufForms
ProductManufForms.propTypes={
    productForm:PropTypes.object.isRequired,       //Product object
    noadd:PropTypes.bool,                          //Only fnished product manufacturer is allowed
}