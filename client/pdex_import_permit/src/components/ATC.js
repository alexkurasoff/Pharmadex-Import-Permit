import React , {Component} from 'react'
import {Row, Col, Container, Label, FormGroup} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import ViewEditOption from './form/ViewEditOption'
import FieldsComparator from './form/FieldsComparator'

class ATC extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{},
            labels:{
                atc:""
            }
        }
        this.isEmpty=this.isEmpty.bind(this)
        this.atc_1=this.atc_1.bind(this)
        this.atc_2=this.atc_2.bind(this)
        this.atc_3=this.atc_3.bind(this)
        this.atc_4=this.atc_4.bind(this)
        this.atc_5=this.atc_5.bind(this)
    }

    componentDidMount(){
        //Locales.createLabels(this)
       // this.state.data.labels[this.props.property]=''
        Locales.resolveLabels(this)
        this.state.data=this.props.data[this.props.property][0]
        //this.state.data.justloaded=false
        this.comparator = new FieldsComparator(this)
        this.setState(this.state)
    }

    componentDidUpdate(){
        //check property change
        //if(this.props.data[this.props.property][0].justloaded){
        this.state.data=this.props.data[this.props.property][0]
        // 06.08.2020 пока работаем только с первім значением из списка this.props.data[this.props.property][0]
        if(this.state.data.justloaded){
            this.state.data.justloaded=false
            this.comparator = new FieldsComparator(this) //check changes
            this.setState(this.state)
        }
        // reload lists when needed
        const fld = this.comparator.checkChanges()
        if(fld.length >0){
            this.tryReload(fld)
        }
    }

    /**
     * If reload will be needed - reload listst
     * @param {string} fldName - 'atc_1'...'atc_5'
     */
    tryReload(fldName){
        //does next field contains list
        let num = fldName.slice(-1)
        let nextFld = 'atc_'.concat(num)
        let optionFld = this.state.data[nextFld]
        if( optionFld!= undefined){
                if(!this.isEmpty(optionFld)){
                    Fetchers.postJSON("/api/public/atcs", this.state.data, (query,result)=>{
                        this.props.data[this.props.property][0]=result
                        this.state.data=this.props.data[this.props.property][0]
                        this.comparator = new FieldsComparator(this) //check changes
                        this.setState(this.state.data)
                    })
                }
        }


    }

    isEmpty(optionField){
        if(optionField.value == undefined){
            return true
        }
        return !(Fetchers.isGoodArray(optionField.value.options) || optionField.value.id > 0)
    }

    atc_1(){
        if(!this.isEmpty(this.state.data.atc_1)){
            return(
                <Col xs='12' sm='12' lg='4' xl='4'>
                    <ViewEditOption attribute="atc_1" component={this} edit={this.props.edit} />
                </Col>
            )
        }
    }

    atc_2(){
        if(!this.isEmpty(this.state.data.atc_2)){
            return(
                <Col xs='12' sm='12' lg='4' xl='4'>
                    <ViewEditOption attribute="atc_2" component={this} edit={this.props.edit} />
                </Col>
            )
        }
    }

    atc_3(){
        if(!this.isEmpty(this.state.data.atc_3)){
            return(
                <Col xs='12' sm='12' lg='4' xl='4'>
                    <ViewEditOption attribute="atc_3" component={this} edit={this.props.edit} />
                </Col>
            )
        }
    }

    atc_4(){
        if(!this.isEmpty(this.state.data.atc_4)){
            return(
                <Col xs='12' sm='12' lg='4' xl='4'>
                    <ViewEditOption attribute="atc_4" component={this} edit={this.props.edit} />
                </Col>
            )
        }
    }

    atc_5(){
        if(!this.isEmpty(this.state.data.atc_5)){
            return(
                <Col xs='12' sm='12' lg='4' xl='4'>
                    <ViewEditOption attribute="atc_5" component={this} edit={this.props.edit} />
                </Col>
            )
        }
    }

    

    
//|| this.labels.locale == undefined
    render(){
        if(this.state.data.atc_1 == undefined){
            return []
        }
        if(!this.props.edit && this.state.data.atc_2.value.code == ""){
            return []
        }
        else{
            return(
            <Container fluid>
                <FormGroup>
                    <Label for={this.props.property}> {this.state.labels[this.props.property]}</Label>
                    <Row>
                        {this.atc_1()}
                        {this.atc_2()}
                        {this.atc_3()}
                    </Row>
                    <Row>
                        {this.atc_4()}
                        {this.atc_5()}
                    </Row>
                </FormGroup>
            </Container>
            )
        }
    }
}
export default ATC
ATC.propTypes={
    data:PropTypes.object.isRequired,        //Data object that contains ATC property
    property:PropTypes.string.isRequired,    //Name of ATC property in data object
    edit:PropTypes.bool                     //View or edit
}