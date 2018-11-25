# ExponentCore
This is the core library of Exponent, a set of Android utilities for accessing private Google APIs. Standalone, it provides comprehensive support for authentication via reimplementation of Play Services methods, including support for multi-factor/2-factor authentication. It offers both high-level and low-level APIs for interacting directly with Play Services at any skill level. 

Exponent is written in Kotlin and has library dependencies on the Kotlin stdlib, AppCompat v7, and OkHttp.

Exponent has *no dependency* on Play Services (or microG etc.) being installed on a target device, so it's also great for open-source apps intended for custom ROMs. However, support for public Google APIs is currently nonexistent (feel free to open an issue).

### Initialization
Before using any of the below methods, make sure to call `Exponent.init(context)`. A good place to put it is in your Application subclass, but it can go anywhere as long as it is called before any other Exponent method.

## AuthFlow Authentication
Exponent provides a high-level API, AuthFlow, that creates and manages the entire authentication process, including UI and multi-factor authentication, with minimal effort. To use AuthFlow, call the `ExAuthFlow.start` method from within your Activity or Fragment:
```kotlin
class MyActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
  
    super.onCreate(savedInstanceState)
    
    loginButton.setOnClickListener {
      ExAuthFlow.start(this@MyActivity, ExAuthFlow.Format.WEB_AUTH, { loginState ->
       if(loginState == ExAuthFlow.State.COMPLETE) {
        // Auth complete. Do something!
        Log.d("My First Name", ExponentAccounts.currentAccount!!.firstName)
       }
      }, { throwable ->
        Toast.makeText(this@MyActivity, "Error logging in", Toast.LENGTH_LONG).show();
      })
    }
  }
}
```

## One Level Down: ExponentAccounts
If you want to implement a custom authentication UI, ExponentAccounts is still a fairly high level API that manages login, persistence, and service tokens. It's highly encouraged to use it in concert with `AutoMFA` to enable multi-factor authentication. Here's the example from above, modified to use ExponentAccounts:
```kotlin
class MyActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
  
    super.onCreate(savedInstanceState)
    
    loginButton.setOnClickListener {
    
      // You'll have to prompt the user for these on your own
      val email = emailTextField.text
      val password = passwordTextField.text
      
      ExponentAccounts.login(email, password, { accountInfo -> 
        // Auth complete. Do something!
        Log.d("My First Name", accountInfo.firstName)
      }, { throwable ->
      
        // Did this error happen because the user has MFA? If so, we can use AutoMFA to recover!
        if(throwable is NeedsBrowserException) {
        
          // Use AutoMFA to recover
          AutoMFA.handleBrowserRecover(this@MyActivity, email, throwable, { accountInfo ->
            // Auth complete. Do something!
            Log.d("My First Name", accountInfo.firstName)
          }, { err ->
            Toast.makeText(this@MyActivity, "Error logging in", Toast.LENGTH_LONG).show();
          });
          
        } else {
          Toast.makeText(this@MyActivity, "Error logging in", Toast.LENGTH_LONG).show();
        }
        
      }
    }
  }
}
```
