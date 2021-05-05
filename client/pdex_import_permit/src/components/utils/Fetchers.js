import React , {Component} from 'react'
import Spinner from './Spinner'
import Alerts from './Alerts'

/**
 * Fetch API utilities as well as any useful utilities related to arrays, window urls etc. All are static
 */
class Fetchers{

    /**
     * Post a JSON Query to server With a spinner
     * set justloaded=true for any object loaded - deep
     * @param {String} api API URL, like "/api/countryDetails"
     * @param {Object} bodyDTO DTO that will be converted to JSON and placed to Body
     * @param {function} parser function with params api and result of query
     * @example  Fetchers.postJSON('/api/countryDetails', parseInt(countryId),(query, result)=>{ ... process result ...})
     */
    static postJSON(api,bodyDTO, parser){
        Spinner.show()
        Fetchers.postJSONNoSpinner(api,bodyDTO, parser)
    }
   /**
     * Post a JSON Query to server Do not use a Spinner!
     * set justloaded=true for any object inside the response - deep
     * @param {String} api API URL, like "/api/countryDetails"
     * @param {Object} bodyDTO DTO that will be converted to JSON and placed to Body
     * @param {function} parser function with params api and result of query
     * @example  Fetchers.postJSON('/api/countryDetails', parseInt(countryId),(query, result)=>{ ... process result ...})
     */
    static postJSONNoSpinner(api,bodyDTO, parser){
        fetch(Fetchers.contextPath()+api , {
            credentials: 'include',
            method: 'POST',          
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'              
            },           
            body: JSON.stringify(bodyDTO)
          })      
          .then(response => {            
            if (response.ok) { 
                let resp = response.json()
                
                return resp;              
            }else{
                Spinner.hide();
                Alerts.show(api +" " + response.status + " " + response.statusText,3)
            }
          }) 
          .then(res=> {
              Spinner.hide()
              if(typeof res != "undefined"){
                Fetchers.setJustLoaded(res)
                parser(api,res)
              }
          })
          .catch(res=> {
              Spinner.hide()
              Alerts.show(api +" " + res,3,3)
            }
           ) 
    }

    /**
     * For this and all objects downto sets justloaded property
     * @param {object} resp 
     */
    static setJustLoaded(resp){
        if(Fetchers.isTrueObject(resp)){
            resp.justloaded=true
            Object.keys(resp).forEach(element=>{
                if(typeof resp[element] == 'object'){
                    if(Fetchers.isGoodArray(resp[element])){
                        let arr = resp[element]
                        arr.forEach(e=>Fetchers.setJustLoaded(e))
                    }else{
                        Fetchers.setJustLoaded(resp[element])
                    }
                }
            })
        }
    }
    /**
     * is obj is true obj
     * @param {object or come else} obj 
     */
    static isTrueObject(obj){
        return (typeof obj == 'object') && (obj != null) && (!Array.isArray(obj))
    }

    /**
     * POST a form JSON reply
     * return JSON as well
     * set justloaded=true for any object inside the response - deep
     * @param {String} api API URL, like "/api/save/file"
     * @param {FormData} formData object FormData that should be initiated and filled by form fields, files and other data that will POST to the api
     * @param {function} processor parser function with params api and result of POST, may contain any JSON object that will be processed by the processor
     * @example   
     *       formData.append('dto', JSON.stringify(this.state.data))    //whole DTO will packed to JSON and placed to the field
     *       formData.append('file', this.state.file.file);
     *       Fetchers.postForm("/api/common/upload/save/file",formData, (api,result)=>{ ... process result ...})
     */
    static postFormJson(api, formData, processor){
        Spinner.show()
        fetch(Fetchers.contextPath()+api, {
            credentials: 'include',
            method: 'POST',
            body: formData
        })
        .then(res => { 
            Spinner.hide()            
            if (res.ok) { 
                return res.json();             
            }else{
                Spinner.hide()
                Alerts.show(api +" " + res.status + " " + res.statusText,3)
            }
        }) 
        .then(res=> {
            Spinner.hide()
            if(typeof res != "undefined"){
                Fetchers.setJustLoaded(res)
                processor(api,res)
            }
        })
        .catch(res=>{ 
            Spinner.hide()
            Alerts.show(api +" " + res.message,3)
            }
        )
   }    

   /**
     * POST a form NON-JSON reply
     * @param {String} api API URL, like "/api/save/file"
     * @param {FormData} formData object FormData that should be initiated and filled by form fields, files and other data that will POST to the api
     * @param {function} parser function with params api and result of POST, may contain any JSON object that will be processed by the processor
     * @example   
     *       formData.append('dto', JSON.stringify(this.state.data))    //whole DTO will packed to JSON and placed to the field
     *       formData.append('file', this.state.file.file);
     *       Fetchers.postForm("/api/common/upload/save/file",formData, (api,result)=>{ ... process result ...})
     */
    static postForm(api, formData, processor){
        Spinner.show()
        fetch(Fetchers.contextPath()+api, {
            credentials: 'include',
            method: 'POST',
            body: formData
        })
        .then(res => { 
            Spinner.hide()            
            if (res.ok) { 
                return res;             
            }else{
                Spinner.hide()
                Alerts.show(api +" " + response.status + " " + response.statusText,3)
            }
        }) 
        .then(res=> {
            Spinner.hide()
            if(typeof res != "undefined"){
                processor(api,res)
            }
        })
        .catch(res=>{ 
            Spinner.hide()
            Alerts.show(api +" " + res.message,3)
            }
        )
   }    

    /**
     * Is result not empty array?
     * @param {[]} result 
     */
    static isGoodArray(result){
        return typeof result != 'undefined' && result != null && Array.isArray(result) && result.length>0
    }
    
    /**
     * Determine context path properly - full path with protocol
     */
    static contextPath(){
        let ret = window.location.protocol + "//" + window.location.host
        if (Fetchers.contextName.length>0){
            return ret + window.contextPathCollector
        }else{
            return ret
        }
    }

    /**
     * pure name of context or empty
     */
    static contextName(){
        let ret = window.contextPathApplication
        if( typeof ret == 'undefined'){
            ret=''
        }
        return ret
    }

}
export default Fetchers