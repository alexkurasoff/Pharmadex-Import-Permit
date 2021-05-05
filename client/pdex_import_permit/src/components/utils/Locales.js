import React , {Component} from 'react'
import alerts from './Alerts'
import Fetchers from './Fetchers'

/**
 * Works with i18N on the server
 * @example
 * Locales.createLabels(this) - create list of empty this.state.labels from name of properties in this.state.data
 * @example
 * Locales.resolveLabels(this) - call the server to resolve this.state.labels to cyrrent language values
 */
class Locales extends Component{
    constructor(props){
        super(props)
    }

    /**
     * add keys of component.state.data 
     * @param {object} component implementation of the component. Must implement "this.state.data"
     */
    static createLabels(component){
        if(component.state.labels==undefined){
            component.state.labels={}
        }
        if(component.state != undefined && Fetchers.isGoodArray(Object.keys(component.state.data))){
            Object.keys(component.state.data).forEach((key)=>{
                component.state.labels[key]=''
            })
        }
    }

    /**
     * Resolve labels with the current locale for a component, and then makes setstate for the component in case labels changed 
     * To store label values, use global window object
     * @param {object} implementation of the component. Must implement "this.state.labels"
     * @example  Locales.resolveLabels(this) 
     */
    static resolveLabels(component){
        //labels should be loaded
        let toLoad=[] 
        //labels that are already loaded
        if(typeof window.etbm_labels == 'undefined'){
            window.etbm_labels={}
        }
        let loaded = window.etbm_labels; 
        // is an object in the parameter correct? 
        if(typeof component != 'undefined' && typeof component.state != 'undefined' && typeof component.state.labels != 'undefined'){
            //current component's labels
            let componentLabels = component.state.labels
            //determine which keys should be loaded
            for(let key in componentLabels){
                //console.log(key)
                if(typeof loaded[key] == 'undefined' || loaded[key] != component.state.labels[key]){
                    toLoad.push(key)
                }
            }

            //ask for the keys that should be loaded
            if(toLoad.length>0){
                Fetchers.postJSONNoSpinner('/api/public/provideLabels',toLoad,(query,result)=>{
                    let refresh=false
                    for(let key in result){
                        loaded[key]=result[key]
                    }

                    //determine does something change
                    for(let key in componentLabels){
                        if((loaded[key] != undefined)){
                            componentLabels[key] = loaded[key]
                            refresh=true
                        }
                    }
                    componentLabels['locale'] = loaded['locale']
                    //refresh component's state if somethig changes
                    if(refresh){
                        let s = component.state
                        component.setState(s)
                    }
                })
            }
        }else{
            alerts.show("this.state.labels is undefined for some component")
        }

    }

    render(){
        return[]
    }
}

export default Locales
