package authguidance.mobilesample.plumbing.utilities

import authguidance.mobilesample.plumbing.oauth.Authenticator

/*
 * Logic related to making HTTP calls
 */
class HttpClient(authenticator: Authenticator) {

    val _authenticator = authenticator;

    /*
     * The entry point for calling an API
     */
    fun callApi(url: String) {
        _authenticator.getAccessToken()
        println("*** DEBUG: CALLING API over " + url);
    }
}