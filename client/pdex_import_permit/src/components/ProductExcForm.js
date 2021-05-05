import React , {Component} from 'react'
import {Row, Col, Card, CardBody, Label} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import ButtonUni from './form/ButtonUni'
import ViewEditOption from "./form/ViewEditOption"
import ViewEdit from "./form/ViewEdit"
import ViewEditOracle from "./form/ViewEditOracle"

/**
 * Component to edit detils for a product
 * @property productDetails - an product details form object
 * @example
 * <ProductInnForm productDetails={this} productForm={this.props.productForm}/>
 */
class ProductExcForm extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0
            },
            index: -1,
            labels:{
                global_delete:"",
                product_active:"",
                global_add:"",
                dos_strength:"",
                dos_unit:"",
            }
        }
        this.delete=this.delete.bind(this)
        this.createHeader=this.createHeader.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        
        this.state.data = this.props.item
        this.state.index = this.props.index
    }

    componentDidUpdate(){
        this.state.data = this.props.item
        this.state.index = this.props.index

        if(this.state.data.justloaded){
            this.state.data.justloaded=false
            this.setState(this.state)
        }
    }

    delete(){
        this.props.listForm.deleteItem(this.state.index)
    }

    createHeader(){
        let header = this.state.labels.global_add + " " + this.state.labels.product_active
        if(this.state.data.id > 0){
            header = this.state.data.product_active.value.code
        }

        return header;
    }

    render(){
        if(this.state.data == undefined)
            return []
            var paint = false
        if(this.state.data.id > 0){
            paint = true
        }else if(this.state.data.id == -1){
            paint = true
        }
        if(paint){
           return(
                <Card >
                    <CardBody>
                        <Row key={this.state.data.uniqueKey}>
                            <Col xs='12' sm='12' lg='9' xl='9'>
                                <ViewEditOracle attribute='product_active' component={this} 
                                    api='/api/common/excipients' edit={this.props.edit}/>
                            </Col>
                            <Col xs='12' sm='12' lg='3' xl='3' className="d-flex align-items-center">
                                <ButtonUni label={this.state.labels.global_delete} onClick={this.delete} edit={this.props.edit}/>
                            </Col>
                        </Row>
                        <Row>
                            <Col xs='12' sm='12' lg='8' xl='8'>
                                <ViewEdit mode='text' attribute='dos_strength' component={this} edit={this.props.edit}/>
                            </Col>
                            <Col xs='12' sm='12' lg='4' xl='4'>
                                <ViewEditOption attribute="dos_unit" component={this} hideEmpty={false} edit={this.props.edit} />
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
export default ProductExcForm
ProductExcForm.propTypes={
}