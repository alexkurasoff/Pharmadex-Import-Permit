import React , {Component} from 'react'
import PropTypes from 'prop-types'
import {Row,Col, Button} from 'reactstrap';
/**
 * Control that should be at a bottom of each filter
 * @property runFilter - apply a filter function
 * @property cancelFilter - cancel a filter/default filter function
 * @property offFilter 
 */
class FilterControl extends Component{

    render(){
        return(
            <Row className="m-0 p-0">
            <Col className="m-0 p-0" xs="3" style={{textAlign:'right'}}>
                <Button color='link' className="fa fa-check"
                onClick={this.props.runFilter} ></Button>
            </Col>
            <Col className="m-0 p-0" xs="3" style={{textAlign:'right'}}>
                <Button color='link' className="fa fa-undo"
                onClick={this.props.cancelFilter} ></Button>
            </Col> 
            <Col className="m-0 p-0" xs="3" style={{textAlign:'right'}}>
                <Button color='link'style={{color:'red'}} className="fa fa-power-off"
                onClick={this.props.offFilter} ></Button>
            </Col> 
        </Row>
        )
    }
}

FilterControl.propTypes={
    runFilter:PropTypes.func.isRequired,
    cancelFilter:PropTypes.func.isRequired,
    offFilter:PropTypes.func.isRequired
}
export default FilterControl
