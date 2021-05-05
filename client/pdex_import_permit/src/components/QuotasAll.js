import React , {Component} from 'react'
import { Card, CardHeader,CardBody,Row, Col, Button, ButtonGroup} from 'reactstrap';
import PropTypes from 'prop-types'
import Locales from './utils/Locales'
import Fetchers from './utils/Fetchers';
import SearchControl from './utils/SearchControl'
import CollectorTable from './utils/CollectorTable'
import Downloader from './utils/Downloader'

/**
 * Display the full Quotas table
 * @property quotas - caller component
 * @example <QuotasAll quotas={this} />
 */
class QuotasAll extends Component{
    constructor(props){
        super(props)
        this.state={
            data:{
                table:{
                    headers:{
                        selectedOnly:false
                    },
                    rows:[]
                }
            },
            labels:{
                search:"Search",
                exportExcel:"Export to Excel",
                allfiltered:"All filtered",
                selectedonly:"Selected only",
                askforpermit: "Ask for permit",
                registershipment: "Register a shipment"
            }
        }
        this.loadData=this.loadData.bind(this)
        this.excelTable=this.excelTable.bind(this)

    }
    componentDidMount(){
        Locales.resolveLabels(this)
        this.loadData()
    }
       /**
     * load data to the table
     */
    loadData(){
        Fetchers.postJSON("/api/applicant/quotas", this.state.data, (query, result)=>{
            this.state.data=result
            this.setState(this.state.data)
        })
    }
    /**
     * Upload an Excel table
     */
    excelTable(){
        let downloader = new Downloader();
        downloader.postDownload("/api/applicant/quotas/excel", this.state.data, "quotas.xlsx")
    }


    render(){
        return(
                <Card style={{fontSize:"0.8rem"}}>
                    <CardHeader>
                        <Row>
                            <Col xs="12" sm="12" lg="4" xl="4">
                                <SearchControl label={this.state.labels.search} table={this.state.data.table} loader={this.loadData} />
                            </Col>
                            <Col xs="12" sm="12" lg="7" xl="7">
                                
                            </Col>
                            <Col xs="12" sm="12" lg="1" xl="1" className="d-flex justify-content-right">
                                <Button className="btn-block" size="sm" outline color="primary" 
                                        onClick={this.excelTable}>{this.state.labels.exportExcel}
                                </Button>
                            </Col>
                        </Row>
                    </CardHeader>
                    <CardBody>
                        <CollectorTable tableData={this.state.data.table} 
                                    loader={this.loadData}
                                    headBackground={'#0099cc'}
                                    linkProcessor={
                                        (rowNo, cell)=>{
                                            this.props.quotas.state.expandRow = this.state.data.table.rows[rowNo].dbID;
                                            this.props.quotas.setState(this.props.quotas.state)
                                        }
                                    }
                                    styleCorrector={(headerKey)=>{
                                        switch(headerKey){
                                            case 'countryName':
                                                return {width:'10%'}
                                            case 'prod.prod_name':
                                                return {width:'20%'}
                                            case 'description':
                                                return {width:'40%'}
                                        }
                                    }}

                                    />
                        </CardBody>
            </Card>
        )
    }


}
export default QuotasAll
QuotasAll.propTypes={
    quotas:PropTypes.object.isRequired  //shout expand Component
}