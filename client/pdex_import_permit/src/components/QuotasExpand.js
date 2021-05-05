import React , {Component} from 'react'
import {Row, Col, Card, CardBody, CardHeader, Button} from 'reactstrap'
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers'
import CollectorTable from './utils/CollectorTable'
import Product from './Product'

/**
 * Detail information regarding a quota
 * @property quotas - the caller component
 * @example <QuotasExpand quotas={this} /> 
 */
class QuotasExpand extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                id:0,
                productName:"",
                manufacturer:"",
                table:{
                    headers:{},
                    rows:[]
                },
                tableExpand:{
                    headers:{},
                    rows:[] 
                }

            },
            labels:{}
        }
        this.close=this.close.bind(this)
        this.loadData=this.loadData.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }

    loadData(){
        this.state.data.id=this.props.quotas.state.expandRow;
        Fetchers.postJSON("/api/applicant/quotas/expand", this.state.data, (query,result)=>{
            this.state.data=result
            this.setState(this.state)
        })
    }

    close(){
        this.props.quotas.state.expandRow=-1
        this.props.quotas.setState(this.props.quotas.state)
    }

    render(){
        if(this.state.data.id==0){
            return []
        }else{
        return(
        <Card>
            <CardHeader >
                <Row>
                    <Col xs='11' sm='11' lg='11' xl='11' className="d-flex justify-content-end">
                        <b>{this.state.data.productName+", "+this.state.data.manufacturer}</b>
                    </Col>
                    <Col xs='1' sm='1' lg='1' xl='1' className="d-flex justify-content-end">
                        <Button
                            onClick={this.close} 
                            close
                        />
                    </Col>
                </Row>
            </CardHeader>
            <CardBody>
                <Row>
                <Col xs="12" sm="12" lg="4" xl="4">
                <CollectorTable
                    headBackground={'#0099cc'}
                    tableData={this.state.data.table}
                    loader={this.loadData}
                    linkProcessor={
                        (rowNo,cell)=>{
                            let id=this.state.data.table.rows[rowNo].dbID
                            if(this.props.quotas.state.expandRow != id){
                            this.props.quotas.state.expandRow=id
                            this.loadData()
                            }else{
                                this.close()
                            }
                        }
                    }
                    styleCorrector={
                        (key)=>{

                        }
                    }
                />
                </Col>
                <Col  xs="12" sm="12" lg="8" xl="8">
                    <Row>
                        <Col>
                            <CollectorTable
                            headBackground={'#0099cc'}
                            tableData={this.state.data.tableExpand}
                            loader={this.loadData}
                            />
                        </Col>
                    </Row>
                    <Row>
                        <Col>
                            <Product
                                prodId={this.state.data.id}
                                close={this.close}
                                edit={false}
                            />
                        </Col>
                    </Row>
                </Col>
                </Row>
            </CardBody>
        </Card>
        )
        }
    }


}
export default QuotasExpand
QuotasExpand.propTypes={
    quotas:PropTypes.object.isRequired
}