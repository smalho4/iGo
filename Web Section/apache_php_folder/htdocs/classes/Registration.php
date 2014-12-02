<?php

include 'Post.php';

/**
 * Class registration
 * handles the user registration
 */
class Registration
{
    private $registerurl = '/Register_Me' ;
    private $serviceurl = 'http://54.69.0.233:8080/iGoWeb/HappyPathClientService?Tester';
    private $serviceWSDLurl = 'http://54.69.0.233:8080/iGoWeb/HappyPathClientService?WSDL';

    /**
     * @var array $errors Collection of error messages
     */
    public $errors = array();
    /**
     * @var array $messages Collection of success / neutral messages
     */
    public $messages = array();

    /**
     * the function "__construct()" automatically starts whenever an object of this class is created,
     * you know, when you do "$registration = new Registration();"
     */
    public function __construct()
    {
        if (isset($_POST["register"])) {
            $this->registerNewUser();
        }
    }

    /**
     * handles the entire registration process. checks all error possibilities
     * and creates a new user in the database if everything is fine
     */
    private function registerNewUser()
    {
        $telephone = "0000000000"; // Not implemented.
        if (empty($_POST['user_name'])) {
            $this->errors[] = "Empty Username";
        } elseif (empty($_POST['user_password_new']) || empty($_POST['user_password_repeat'])) {
            $this->errors[] = "Empty Password";
        } elseif ($_POST['user_password_new'] !== $_POST['user_password_repeat']) {
            $this->errors[] = "Password and password repeat are not the same";
        } elseif (strlen($_POST['user_password_new']) < 6) {
            $this->errors[] = "Password has a minimum length of 6 characters";
        } elseif (strlen($_POST['user_name']) > 64 || strlen($_POST['user_name']) < 2) {
            $this->errors[] = "Username cannot be shorter than 2 or longer than 64 characters";
        } elseif (!preg_match('/^[a-z\d]{2,64}$/i', $_POST['user_name'])) {
            $this->errors[] = "Username does not fit the name scheme: only a-Z and numbers are allowed, 2 to 64 characters";
        } elseif (empty($_POST['user_email'])) {
            $this->errors[] = "Email cannot be empty";
        } elseif (strlen($_POST['user_email']) > 64) {
            $this->errors[] = "Email cannot be longer than 64 characters";
        } elseif (!filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)) {
            $this->errors[] = "Your email address is not in a valid email format";
        } elseif (!empty($_POST['user_name'])
            && strlen($_POST['user_name']) <= 64
            && strlen($_POST['user_name']) >= 2
            && preg_match('/^[a-z\d]{2,64}$/i', $_POST['user_name'])
            && !empty($_POST['user_email'])
            && strlen($_POST['user_email']) <= 64
            && filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)
            && !empty($_POST['user_password_new'])
            && !empty($_POST['user_password_repeat'])
            && ($_POST['user_password_new'] === $_POST['user_password_repeat'])
        ) {
            $truefalse = "0";
            $requestParams = array( 'arg0'=>'addAccount', 
                                    'arg1'=>$_POST['user_name'], 
                                    'arg2'=>$_POST['user_email'], 
                                    'arg3'=>$telephone, 
                                    'arg4'=>$_POST['user_password_new']);

            $client = new SoapClient('http://54.69.0.233:8080/iGoWeb/HappyPathClientService?wsdl', 
                array("trace" => 1, "exception" => 0, 'features' => SOAP_SINGLE_ELEMENT_ARRAYS) );

            $response = $client->getYelpData($requestParams); // Crashes the moment we do this.
            
            print_r($requestParams);
            $truefalse = $response->return;

            
            if( $truefalse == "" ) {
                $this->messages[] = "Your account has been created successfully. You can now log in.";
            } else {
                $this->errors[] = "yeeeeeeeees?";
            } 
        } else {
            $this->errors[] = "An unknown error occurred.";
        }
    }
}
