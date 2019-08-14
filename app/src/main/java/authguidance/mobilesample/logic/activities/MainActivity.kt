package authguidance.mobilesample.logic.activities

import android.app.PendingIntent
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import authguidance.mobilesample.Application
import authguidance.mobilesample.R
import authguidance.mobilesample.configuration.Configuration
import authguidance.mobilesample.databinding.ActivityMainBinding
import authguidance.mobilesample.plumbing.api.HttpClient
import authguidance.mobilesample.plumbing.errors.ErrorHandler
import authguidance.mobilesample.plumbing.oauth.Authenticator
import authguidance.mobilesample.plumbing.utilities.ConfigurationLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

/*
 * Our single activity application's only activity
 */
class MainActivity : AppCompatActivity() {

    // Navigation related fields
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    // The configuration is loaded once and a global authenticator object is created
    private lateinit var configuration: Configuration
    private lateinit var authenticator: Authenticator

    // Constants
    private val EXTRA_LOGIN_RESULT = "login_result"
    @Volatile private var loginInProgress = false

    /*
     * Activity creation
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Set up data binding and navigation
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Give the application class a reference to ourself for exception handling purposes
        (this.application as Application).setActivity(this)

        // Run the application startup logic
        this.initialiseApp()
    }

    /*
     * Receive the login response as a new intent for the existing single activity
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        println("GJA: new intent received")

        // See if we are processing a login response from the Chrome Custom Tab
        val loginResult = intent?.getIntExtra(EXTRA_LOGIN_RESULT, 0)
        if(loginResult != null) {

            println("GJA: login result is $loginResult")
            when {
                loginResult < 0 -> {

                    // Handle completion after Chrome Custom Tab cancellation
                    println("GJA: Custom tab cancelled")
                }
                loginResult > 0 -> {

                    // Handle completion after login completion, which could be a success or failure response
                    println("GJA: Handling login response")
                    this.handleLoginResponse()
                }
            }
        }
    }

    /*
     * Return an HTTP client to enable fragments to get data
     */
    fun getHttpClient(): HttpClient {
        return HttpClient(this.configuration.app, this.authenticator)
    }

    /*
     * Receive unhandled exceptions and navigate to the error fragment
     */
    fun handleUnhandledException(exception: Throwable) {

        // Get the error as a known object
        val handler = ErrorHandler()
        val error = handler.fromException(exception)

        if (error.errorCode == "login_required") {

            // Start the AppAuth redirect to get a new token
            startLogin()
        } else {

            // Navigate to the error fragment to render it
            val args = Bundle()
            args.putSerializable("EXCEPTION_DATA", error as Serializable)
            navController.navigate(R.id.errorFragment, args)
        }
    }

    /*
     * Load the configuration and create the authenticator object, used to get tokens during API calls
     */
    private fun initialiseApp() {
        this.configuration = ConfigurationLoader().loadConfiguration(this.applicationContext)
        this.authenticator = Authenticator(this.configuration.oauth, this)
    }

    /*
     * Initialise AppAuth processing
     */
    private fun startLogin() {

        if(this.loginInProgress) {
            println("GJA: loginInProgress - do nothing")
        }

        if(!this.loginInProgress) {

            println("GJA: startLogin")
            this.loginInProgress = true
            CoroutineScope(Dispatchers.IO).launch {

                // The activity's this reference
                val that = this@MainActivity

                // Return here with a positive response if authorization succeeds
                val successIntent = Intent(that, MainActivity::class.java)
                successIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                successIntent.putExtra(EXTRA_LOGIN_RESULT, 1)

                // Return here with a negative response if authorization fails
                val failureIntent = Intent(that, MainActivity::class.java)
                failureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                failureIntent.putExtra(EXTRA_LOGIN_RESULT, -1)

                // Start the redirect
                that.authenticator.startAuthorization(
                    that,
                    that.getTabColor(),
                    PendingIntent.getActivity(that, 0, successIntent, 0),
                    PendingIntent.getActivity(that, 0, failureIntent, 0)
                )
            }
        }
    }

    /*
     * Receive the authorization code and trigger the authorization code grant message
     */
    private fun handleLoginResponse() {

        println("GJA: Login response being handled")

        CoroutineScope(Dispatchers.IO).launch {

            // The activity's this reference
            val that = this@MainActivity
            that.loginInProgress = false

            println("GJA: Doing finish authorization")

            // Handle completion after login success
            val success = that.authenticator.finishAuthorization(that)
            if (success) {

                println("GJA: Auth code grant succeeded")
            } else {
                println("GJA: Auth code grant failed")
            }
        }
    }

    /*
     * Use the App Bar's colour for the Chrome Custom Tab header
     */
    private fun getTabColor(): Int {

        val color = R.color.colorPrimary
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getColor(color)
        } else {
            resources.getColor(color)
        }
    }
}