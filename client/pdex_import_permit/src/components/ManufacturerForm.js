import React , {Component} from 'react'
import {Row, Col, Card, CardBody, CardHeader} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ButtonUni from './form/ButtonUni'
import FieldDisplay from './form/FieldDisplay'
import ViewEditOption from "./form/ViewEditOption"
import ViewEditOracle from "./form/ViewEditOracle"
import FieldsComparator from './form/FieldsComparator'

/**
 * Component to edit detils for a product
 * @property productDetails - an product details form object
 * @example
 * <ManufacturerForm productDetails={this} productForm={this.props.productForm}/>
 */
class ManufacturerForm extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0,
                applicant_country:"",
                manuf_name:{},
                justloaded:false
            },
            index: -1,
            labels:{
                global_delete:"",
                global_add:"",
                manuf_name:"",
                company_type:"",
                applicant_country:""
            }
        }
        this.delete=this.delete.bind(this)
        this.createHeader=this.createHeader.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        
        this.state.data = this.props.item
        this.state.index = this.props.index
        this.comparator = new FieldsComparator(this)
    }

    componentDidUpdate(){
        if(this.props.item.justloaded){
            this.setData()
            this.state.data.justloaded=false
            this.setState(this.state)
        }else{
            if (this.comparator != undefined){
                const fld = this.comparator.checkChanges();
                if(fld.length > 0){
                    if(fld == "manuf_name"){
                        Fetchers.postJSONNoSpinner("/api/common/load/company", this.state.data, (query, result)=>{
                            this.state.data = result
                            this.props.productForm.state.data.manufacturers[this.state.index] = this.state.data
                            //this.props.productForm.state.data.finProdManuf = this.state.data
                            this.comparator = new FieldsComparator(this)
                            this.setState(this.state.data)
                        })
                    }
                }
            }
        }
    }

    setData(){
        this.state.data = this.props.item
        this.state.index = this.props.index

        this.comparator = new FieldsComparator(this)
    }

    delete(){
        this.props.listForm.deleteItem(this.state.index)
    }

    createHeader(){
        let header = this.state.labels.global_add + " " + this.state.labels.manuf_name
        if(this.state.data.id > 0){
            header = this.state.data.manuf_name.value.code
        }

        return header;
    }

    render(){
        if(this.state.data == undefined){
            return []
        }
        var paint = false
        if(this.state.data.id > 0){
            paint = true
        }else if(this.state.data.id == -1){
            paint = true
        }
        if(paint){
            return(
            <Card >
                <CardBody >
                    <Row key={this.state.data.uniqueKey}>
                        <Col xs='12' sm='12' lg='9' xl='9'>
                            <ViewEditOracle attribute='manuf_name' component={this} 
                                api='/api/common/manufacturers' edit={this.props.edit}/>
                        </Col>
                        <Col xs='12' sm='12' lg='3' xl='3' className="d-flex align-items-center">
                            <ButtonUni label={this.state.labels.global_delete} onClick={this.delete} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <FieldDisplay mode="text" attribute='applicant_country' component={this}/>
                        </Col>
                        <Col xs='12' sm='12' lg='6' xl='6'>
                            <ViewEditOption attribute="company_type" component={this} hideEmpty={false} edit={this.props.edit} />
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
export default ManufacturerForm
ManufacturerForm.propTypes={
}