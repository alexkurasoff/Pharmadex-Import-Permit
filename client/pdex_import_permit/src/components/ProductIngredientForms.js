import React , {Component} from 'react'
import {Card, CardBody,CardHeader,Row, Col} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ProductInnForm from './ProductInnForm'
import ProductExcForm from './ProductExcForm'
import ButtonUni from './form/ButtonUni'

/**
 * Build list form by Inns or Excipients
 */
class ProductIngredientForms extends Component{
    constructor(props){
        super(props)
        this.state={
            data:[],
            prodId:0,
            labels:{
                active_substance:"",
                product_active:"",
                global_add:""
            }
        }
        this.add=this.add.bind(this)
        this.deleteItem=this.deleteItem.bind(this)
        this.buildHeader=this.buildHeader.bind(this)
        this.setData=this.setData.bind(this)
    }

    componentDidMount(){
        Locales.resolveLabels(this)
        this.setData()
        this.state.prodId=this.props.productForm.state.data.id
        this.setState(this.state)
    }

    componentDidUpdate(){
        this.setData()

        if(this.state.prodId != this.props.productForm.state.data.id){
            this.state.prodId=this.props.productForm.state.data.id
            this.state.data.justloaded=false
            this.setState(this.state)
        }
    }

    setData(){
        if(this.props.isInn){
            this.state.data=this.props.productForm.state.data.inns
        }else{
            this.state.data=this.props.productForm.state.data.excipients
        }
    }

    add(){
        if(this.props.isInn){
            Fetchers.postJSONNoSpinner("/api/common/inns/add", this.state.data, (query, result)=>{
                this.state.data = result
                this.props.productForm.state.data.inns = this.state.data
                this.props.productForm.setState(this.props.productForm.state)
            })
        }else{
            Fetchers.postJSONNoSpinner("/api/common/excs/add", this.state.data, (query, result)=>{
                this.state.data = result
                this.props.productForm.state.data.excipients = this.state.data
                this.props.productForm.setState(this.props.productForm.state)
            })
        }
    }

    deleteItem(indexItem){
        if(indexItem < 0)
            return;
        if(Fetchers.isGoodArray(this.state.data)){
            let rows = []
            var i = 0
            this.state.data.forEach((inn)=>{
                if(i != indexItem){
                    rows.push(inn)
                }
                i = i + 1
            })
            this.state.data = rows
            if(this.props.isInn){
                this.props.productForm.state.data.inns = this.state.data
            }else{
                this.props.productForm.state.data.excipients = this.state.data
            }
            this.props.productForm.setState(this.props.productForm.state)
        }
    }

    paintComponents(){
        if(this.props.isInn){
            return this.paintInnsList()
        }else{
            return this.paintExcsList()
        }
    }

    paintInnsList(){
        if(Fetchers.isGoodArray(this.state.data)){
            let rows = []
            var i = 0

            this.state.data.forEach((inn)=>{
                if(i == 0){
                    rows.push(
                        <Row key={inn.uniqueKey}>
                            <Col xs="12" sm="12" lg="12" xl="12">
                                <ProductInnForm item={inn} index={i} 
                                        productForm={this.props.productForm} listForm={this} edit={this.props.edit}/>
                            </Col>
                        </Row>
                    )
                }else{
                    rows.push(
                        <Row key={inn.uniqueKey} style={{marginTop:'10px'}}>
                            <Col xs="12" sm="12" lg="12" xl="12">
                                <ProductInnForm item={inn} index={i} 
                                        productForm={this.props.productForm} listForm={this} edit={this.props.edit}/>
                            </Col>
                        </Row>
                    )
                }
                i = i + 1
            })
            return rows
        }else{
            return []
        }
    }

    paintExcsList(){
        if(Fetchers.isGoodArray(this.state.data)){
            let rows = []
            var i = 0

            this.state.data.forEach((exc)=>{
                if(i == 0){
                    rows.push(
                        <Row key={exc.uniqueKey}>
                            <Col xs="12" sm="12" lg="12" xl="12">
                                <ProductExcForm item={exc} index={i} 
                                        productForm={this.props.productForm} listForm={this} edit={this.props.edit}/>
                            </Col>
                        </Row>
                    )
                }else{
                    rows.push(
                        <Row key={exc.uniqueKey} style={{marginTop:'10px'}}>
                            <Col xs="12" sm="12" lg="12" xl="12">
                                <ProductExcForm item={exc} index={i} 
                                        productForm={this.props.productForm} listForm={this} edit={this.props.edit}/>
                            </Col>
                        </Row>
                    )
                }
                i = i + 1
            })
            return rows
        }else{
            return []
        }
    }

    buildHeader(){
       var lbl = this.state.labels.product_active
        if(this.props.isInn){
            lbl = this.state.labels.active_substance
        }

        return(
                <Row>
                    <Col xs='12' sm='9' lg='9' xl='9' className="d-flex justify-content-center">
                        <b>{lbl}</b>
                    </Col>
                    <Col xs='12' sm='3' lg='3' xl='3' className="d-flex justify-content-end">
                        <ButtonUni label={this.state.labels.global_add} onClick={this.add} edit={this.props.edit}/>
                    </Col>
                </Row>
            )
    }

    render(){
        if(Fetchers.isGoodArray(this.state.data)){
            return(
                <Card >
                    <CardHeader>
                        {this.buildHeader()}
                    </CardHeader>
                    <CardBody >
                        {this.paintComponents()}
                    </CardBody>
                </Card>
            )
        }else{
            return []
        }
    }


}
export default ProductIngredientForms
ProductIngredientForms.propTypes={
    isInn:PropTypes.bool.isRequired,
    productForm:PropTypes.object       //Product object
}