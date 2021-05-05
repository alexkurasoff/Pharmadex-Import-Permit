import React , {Component} from 'react'
import PropTypes from 'prop-types'
import {Container,Row, Col} from 'reactstrap'
import Locales from './utils/Locales'
import ViewEdit from './form/ViewEdit'
import ProductManufForms from './ProductManufForms'

/**
 * Allows add/edit medical product data for both normal and special import
 */
class ProductMedical extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{},
            labels:{}
        }
    }

    componentDidMount(){
        this.state.data=this.props.data
        this.state.data.justloaded=false
        this.setState(this.state)
        Locales.createLabels(this)
        Locales.resolveLabels(this)
    }

    componentDidUpdate(){
        if(this.props.data.justloaded){
            this.state.data=this.props.data
            this.state.data.justloaded=false
            this.setState(this.state)
        }
    }

    render(){
        if(this.state.labels.locale != undefined && this.state.data.prod_name != undefined){
            return(
                <Container fluid>
                    <Row>
                        <Col xs='12' sm='12'lg='12' xl='12'>
                            <ViewEdit mode='text' attribute='prod_name' component={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <ViewEdit mode='textarea' rows={"4"} attribute='medical_product' component={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                    <Row xs={12} sm={12} lg={12} xl={12} style={{marginTop:'10px'}}>
                        <Col>
                            <ProductManufForms productForm={this} edit={this.props.edit} noadd/>
                        </Col>
                    </Row>
                    <Row>
                        <Col xs={12} sm={12} lg={12} xl={8}>
                            <ViewEdit mode='text' attribute='product_conttype' component={this} edit={this.props.edit}/>
                        </Col>
                        <Col xs={12} sm={12} lg={12} xl={4}>
                            <ViewEdit mode='text' attribute='product_packsize' component={this} edit={this.props.edit}/>
                        </Col>
                    </Row>
                </Container>
            )
        }else{
            return []
        }
    }
}
export default ProductMedical
ProductMedical.propTypes={
    data:PropTypes.object.isRequired,    //ProductDTO
    edit:PropTypes.bool
}