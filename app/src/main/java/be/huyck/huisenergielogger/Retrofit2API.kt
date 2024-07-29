package be.huyck.huisenergielogger

import retrofit2.Call
import retrofit2.http.GET

import be.huyck.huisenergielogger.modellen.JsonGegevens

interface Retrofit2API {
    // as we are making get request
    // so we are displaying GET as annotation.
    // and inside we are passing
    // last parameter for our url.
    @GET("huidigedata.json")
    fun getJsonDATA(): Call<JsonGegevens?>?
    // as we are calling data from array
    // so we are calling it with json object
    // and naming that method as getCourse();


}