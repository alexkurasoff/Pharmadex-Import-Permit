import React , {Component} from 'react'
import PropTypes from 'prop-types';
import { Card, CardHeader,CardBody, Label, Form, FormGroup,Input, Button, Row, Col, FormFeedback, Alert} from 'reactstrap';
import Locales from './utils/Locales';
import Fetchers from './utils/Fetchers';

/**
 * Login form, self - sufficient, except success and cancel handlers
 * @property success - function on success login, parameter is UserDetailsDto
 * @property cancel -function on cancel button
 * @example <Login success={(result)=>{window.location.assign(Fetchers.contextPath()+"/secure")}} cancel={Navigator.navigate("term","Enter","void")} />
 */
class LoginForm extends Component{
    constructor (props, context){
        super( props, context)
        this.state={
            formData:{
                username:"",
                usernameValid:true,
                password:"",
                passwordValid:true,
                remember_me:true,
                errorLogin:false,
            },
            labels:{
                login:"login",
                User_username:"Login Name:",
                password:"Password:",
                remember_me:"remember me for a week",
                global_submit:"Submit",
                form_cancel:"Cancel",
                errorEmpty3:"Should be at least three chars",
                password_error:"",
            }
        }
        this.submit=this.submit.bind(this)
    }
    componentDidMount(){
        Locales.resolveLabels(this)
    }
    /**
     * Submit a login form
     */
    submit(){
        if(this.validate()){
            let formData = new FormData()
            formData.append("username", this.state.formData.username)
            formData.append("password", this.state.formData.password)
            formData.append("remember-me", this.state.formData.remember_me)
            Fetchers.postForm("/login", formData, (formData,res)=>{
                Fetchers.postJSONNoSpinner("/api/public/userData","",(query,result)=>{
                   this.state.formData.errorLogin = result.username=='anonymous'
                   if(this.state.formData.errorLogin){
                        this.setState(this.state.formData)
                   }else{
                       this.props.success(result)
                   }
                })
            })
        }
    }

    validate(){
        let f = this.state.formData
        f.usernameValid=f.username.length>2
        f.passwordValid=f.password.length>2
        this.setState(f)
        return f.usernameValid && f.passwordValid
    }

    render(){
        return(
            <Card className="border-5 border-primary shadow-lg m-1" style={{fontSize:"0.8rem"}}>
                <CardHeader>
                    {this.state.labels.login}
                </CardHeader>
                <CardBody>
                <Alert color="danger" hidden={!this.state.formData.errorLogin}>
                    {this.state.labels.password_error}
                </Alert>
                    <Form>
                        <FormGroup>
                            <Label for='loFld'>{this.state.labels.User_username}</Label>
                            <Input id='loFld'
                            invalid={!this.state.formData.usernameValid} 
                            value={this.state.formData.username}
                            onChange={(e)=>{
                                this.state.formData.username=e.target.value
                                this.state.formData.usernameValid = e.target.value.length>2 || this.state.formData.usernameValid || e.target.value.length==0
                                this.setState(this.state.formData)
                            }}
                            />
                            <FormFeedback>{this.state.labels.errorEmpty3}</FormFeedback>
                        </FormGroup>
                        <FormGroup>
                            <Label for='paFld'>{this.state.labels.password}</Label>
                            <Input id='paFld' type="password"
                            invalid={!this.state.formData.passwordValid}
                            value={this.state.formData.password}
                            onChange={(e)=>{
                                this.state.formData.password=e.target.value
                                this.state.formData.passwordValid = e.target.value.length>2 || this.state.formData.passwordValid || e.target.value.length==0
                                this.setState(this.state.formData)
                            }}
                            />
                            <FormFeedback>{this.state.labels.errorEmpty3}</FormFeedback>
                        </FormGroup>
                      
                        <Row className="mt-3">
                        <Col className="d-flex justify-content-end">
                                <Button color='primary' autoFocus active
                                    onClick={this.submit}>
                                    {this.state.labels.global_submit}
                                </Button>
                            </Col>
                            <Col>
                                <Button color='secondary'
                                outline
                                onClick={this.props.cancel}>
                                    {this.state.labels.form_cancel}
                                </Button>
                            </Col>
                        </Row>
                        <Row>
                        <FormGroup check>
                            <Label check>
                            <Input type="checkbox"
                                checked={this.state.formData.remember_me}
                                value={this.state.formData.remember_me}
                                onChange={(e)=>{
                                    this.state.formData.remember_me= !this.state.formData.remember_me
                                    this.setState(this.state.formData)
                                }} />{' '}
                                {this.state.labels.remember_me}
                            </Label>
                        </FormGroup>
                        </Row>
                    </Form>
                </CardBody>
            </Card>
        )
    }
}
export default LoginForm
LoginForm.propTypes = {
    success: PropTypes.func.isRequired,
    cancel: PropTypes.func.isRequired
  };