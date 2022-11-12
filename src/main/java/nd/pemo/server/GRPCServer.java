package nd.pemo.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import nd.pemo.service.UserService;

import java.io.IOException;

public class GRPCServer {

    public static void main(String args[]) {
        Server server = ServerBuilder.forPort(9901).addService(
                new UserService()
        ).build();

        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Failed to start server");
            System.out.println(e.getMessage());
        }

        System.out.println("Server started on " + server.getPort());

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.out.println("server termination failure");
            System.out.println(e.getMessage());
        }
    }
}
