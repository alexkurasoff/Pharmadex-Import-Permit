import React , {Component} from 'react'
import PropTypes from 'prop-types'
import {Container, Row, Col} from 'reactstrap'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import Product from './Product'


/**
 *Present the applicatiuon details in read-only mode
 *@property application - object AppplicationDTO
 * @example <ApplicationDisplayDetails application={this.state.data} />
 */
class ApplicationDisplayDetails extends Component{
    constructor(props){
        super(props)
        this.state={
            labels:{},
            productId:0,
        }
        this.loadProductDetail=this.loadProductDetail.bind(this)
    }


    loadProductDetail(){
        if(this.state.productId>0){
            return(
                <Product
                    prodId={this.state.productId}
                    close={()=>{
                        this.state.productId=0
                        this.setState(this.state)
                    }}
                    edit={false}
                    noschedule={this.props.application.itemType.value.originalCode=="medprod"}
                />
            )
        }else{
            return[]
        }
    }

    render(){
       if(this.props.application.details != undefined){
        return(
            <Container fluid>
                <Row>
                    <Col>
                    <CollectorTable tableData={this.props.application.details.details} 
                            loader={this.props.loader}
                            headBackground={'#0099cc'}
                            styleCorrector={(headerKey)=>{
                                switch(headerKey){
                                    case 'prodname':
                                        return {width:'18%'}
                                    case 'manufname':
                                        return {width:'18%'}
                                }
                            }}
                            linkProcessor={(rowNo,col)=>{
                                let appDetail ={id: this.props.application.details.details.rows[rowNo].dbID}  //ApplicationDetailDTO
                                Fetchers.postJSONNoSpinner("/api/common/application/detail", appDetail, (query,result)=>{
                                    this.state.productId=result.product.id
                                    this.setState(this.state)
                                })
                            }}
                    />
                    </Col>
                </Row>
                <Row>
                    <Col>
                        {this.loadProductDetail()}
                    </Col>
                </Row>
            </Container>

            )
        }else{
            return []
        }
    }


}
export default ApplicationDisplayDetails
ApplicationDisplayDetails.propTypes={
    application:PropTypes.object.isRequired,
    loader :PropTypes.func.isRequired,          //callback ApplictionDTO loader
}