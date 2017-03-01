package model;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Enock on 9/29/2016.
 */
public interface EmployeeAPI {

    @FormUrlEncoded
    @POST("/Home/Login")
    public void Login(
            @Field("txtUsername") String username,
            @Field("txtPassWord") String password,
            Callback<Response> callback);
    @FormUrlEncoded
    @POST("/Home/ReloadPayslip")
    public void ReloadPayslip(
            @Field("StaffIDNO") String StaffId,
            @Field("Period") String Period,
            Callback<Response> callback);
    @FormUrlEncoded
    @POST("/Home/validatePassword")
    public void validatePassword(
            @Field("txtUsername") String Username,
            @Field("txtPassWord") String Password,
            Callback<Response> callback);

    @FormUrlEncoded
    @POST("/Home/ChangePassword")
    public void ChangePassword(
            @Field("Confirm") String Password,
            @Field("StaffIDNO") String StaffId,
            Callback<Response> callback);


    @FormUrlEncoded
    @POST("/Home/ReloadP9Form")
    public void ReloadP9Form(
            @Field("StaffIDNO") String StaffId,
            @Field("Period") String Period,
            Callback<Response> callback);
}
