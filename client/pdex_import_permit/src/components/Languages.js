import React , {Component} from 'react'
import PropTypes from 'prop-types';
import {ButtonGroup, Button} from 'reactstrap'
import Fetchers from './utils/Fetchers'

/**
 * Languages switch
 * @property flags - array of objects FlagDTO ( selected:false,localeStr:"en_US",displayName:"English")
 */
class Languages extends Component{
    constructor(props){
        super(props)
        this.createFlags=this.createFlags.bind(this)
        this.switchLanguage=this.switchLanguage.bind(this)
    }
     /**
       * Switch a language for UI
       * @param {i.e. en_us} localeStr 
       */
      switchLanguage(localeStr){
        let path = ""
        if(window.location.pathname){
          path=window.location.pathname
        }
        let params="?lang="+localeStr
        if(window.location.search){
          let search=window.location.search
          let params = search.split("&");
          params.forEach((value)=>{
            if(value.indexOf("lang=")==-1){
              params=params+"&"+value
            }
          })

        }
        let newURL = window.location.protocol +"//" 
                     + window.location.host
                     +path
                     +params
                     +window.location.hash
        window.location.replace(newURL);
      }

      /**
       * create language switch control
       */
      createFlags(){
        let flags=this.props.flags
        let ret = [];
        let selected=false
        if(Fetchers.isGoodArray(flags)){
          flags.forEach((value)=>{
            selected=value.selected || selected //wrong lang cookie in very rare cases
            ret.push(
              <Button size="sm" outline color="secondary"key={value.displayName} 
              active={value.selected}
              onClick={()=>{
                this.switchLanguage(value.localeStr)
              }}
              >
                  <img src={"/api/public/flag?"+"localeStr="+value.localeStr} width="20" />
                  {" " + value.displayName}
              </Button>)
          })
        }
        return ret;
      }
    render(){
        return(
          <ButtonGroup size="sm">
            {this.createFlags()}
          </ButtonGroup>
        )
    }
}

export default Languages;

Languages.propTypes={
    flags: PropTypes.arrayOf(PropTypes.shape({
        selected:PropTypes.bool.isRequired,
        localeStr:PropTypes.string.isRequired,
        displayName:PropTypes.string.isRequired
    })).isRequired
}