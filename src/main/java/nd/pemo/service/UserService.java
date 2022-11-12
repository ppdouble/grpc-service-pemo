package nd.pemo.service;

import io.grpc.stub.StreamObserver;
import nd.pemo.grpcsample.User;
import nd.pemo.grpcsample.userGrpc;

/*
 * Implementation of interface e.g. apis
 * And implementation of the logic, that how to process the request
 */
public class UserService extends userGrpc.userImplBase {

    // the logic to process the request
    @Override
    public void login(User.LoginRequest loginRequest, StreamObserver<User.APIResponse> responseStreamObserver) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // mock data
        String usr = "nick";
        String pass = "npass";

        User.APIResponse.Builder responseBuilder = User.APIResponse.newBuilder();

        // business logic
        if (usr.equals(username) && pass.equals(password)) {
            responseBuilder.setResponseCode(1).setResponsemessage("Login Success");
        } else {
            responseBuilder.setResponseCode(0).setResponsemessage("Login Failure: In valid username or password");
        }

        responseStreamObserver.onNext(responseBuilder.build());
        responseStreamObserver.onCompleted();
    }

    @Override
    public void logout(User.Empty logoutRequest, StreamObserver<User.LogoutResponse> responseStreamObserver) {

        User.LogoutResponse.Builder responseBuilder = User.LogoutResponse.newBuilder();
        responseBuilder.setResponseCode(1).setLogoutmessage("Status: Logout");

        responseStreamObserver.onNext(responseBuilder.build());
        responseStreamObserver.onCompleted();
    }
}


